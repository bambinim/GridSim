# Relazione sull'Implementazione - Michele Nardini

Il presente capitolo descrive in dettaglio le scelte progettuali, i pattern funzionali e le componenti chiave del simulatore **GridSim** sviluppate da Michele Nardini. L'esposizione adotta un rigore accademico e formale, supportato da frammenti di codice esplicativi che documentano l'adozione dell'immutabilità dello stato, del determinismo simulativo e della robustezza formale dei modelli di dominio.

---

## 1. Quadro Generale dei Contributi Sviluppati

Nel contesto del progetto **GridSim**, Michele Nardini ha progettato e sviluppato i seguenti componenti e moduli logici del core e del simulatore:

* **Modellazione del Dominio e Unità di Misura (Core Models)**:
  * Definizione delle entità statiche e dinamiche della rete: [House](file:///home/michelenardini/GridSim/app/src/main/scala/org/gridsim/core/model/house/House.scala), [HouseState](file:///home/michelenardini/GridSim/app/src/main/scala/org/gridsim/core/model/house/HouseState.scala), [Battery](file:///home/michelenardini/GridSim/app/src/main/scala/org/gridsim/core/model/storage/battery/Battery.scala) e [BatteryState](file:///home/michelenardini/GridSim/app/src/main/scala/org/gridsim/core/model/storage/battery/BatteryState.scala).
  * Modellazione algebrica delle unità di misura fisiche e dei flussi: [Power](file:///home/michelenardini/GridSim/app/src/main/scala/org/gridsim/core/common/Power.scala), [Energy](file:///home/michelenardini/GridSim/app/src/main/scala/org/gridsim/core/common/Energy.scala) e [Flow](file:///home/michelenardini/GridSim/app/src/main/scala/org/gridsim/core/common/Flow.scala) nel package `org.gridsim.core.common`.
* **Motore di Simulazione e Concorrenza (Simulator Engine)**:
  * Sviluppo del ciclo di evoluzione discreto basato su pipeline monadiche: [DefaultSimulationEngine](file:///home/michelenardini/GridSim/app/src/main/scala/org/gridsim/core/simulation/DefaultSimulationEngine.scala).
  * Controllo concorrente e thread-safe del ciclo di vita della simulazione: [DefaultSimulationController](file:///home/michelenardini/GridSim/app/src/main/scala/org/gridsim/core/simulation/DefaultSimulationController.scala).
* **Evoluzione Temporale e Scambio Energetico (Grid Evolution)**:
  * Sviluppo delle type class per l'avanzamento dei nodi: [GridEvolution](file:///home/michelenardini/GridSim/app/src/main/scala/org/gridsim/core/behaviour/GridEvolution.scala), [HouseEvolution](file:///home/michelenardini/GridSim/app/src/main/scala/org/gridsim/core/behaviour/house/HouseEvolution.scala) e [StorageEnergyExchanger](file:///home/michelenardini/GridSim/app/src/main/scala/org/gridsim/core/behaviour/storage/StorageEnergyExchanger.scala).
  * Logiche e strategie di carica/scarica delle batterie: [BatteryEnergyExchange](file:///home/michelenardini/GridSim/app/src/main/scala/org/gridsim/core/behaviour/storage/battery/BatteryEnergyExchange.scala), `BatteryStrategy` e `StandardBatteryStrategy`.
* **Infrastruttura di Validazione Semantica (Validator)**:
  * Progettazione dell'algebra di validazione e accumulo errori: [Validator](file:///home/michelenardini/GridSim/app/src/main/scala/org/gridsim/core/validation/Validator.scala).
  * Validatori specifici e di coerenza: [BatteryValidator](file:///home/michelenardini/GridSim/app/src/main/scala/org/gridsim/core/validation/BatteryValidator.scala), [HouseValidator](file:///home/michelenardini/GridSim/app/src/main/scala/org/gridsim/core/validation/HouseValidator.scala), [HouseComponentValidator](file:///home/michelenardini/GridSim/app/src/main/scala/org/gridsim/core/validation/HouseComponentValidator.scala) e [SimulationValidator](file:///home/michelenardini/GridSim/app/src/main/scala/org/gridsim/core/validation/SimulationValidator.scala).

---

## 2. Modellazione delle Unità di Misura e Flussi (Opaque Types & ADT)

La simulazione numerica di un dominio fisico richiede una rigorosa modellazione dei vincoli dimensionali per prevenire errori semantici (es. sommare grandezze non omogenee). A tal fine, si è fatto ricorso alle potenzialità del type system di Scala 3, implementando **Opaque Types** e **Algebraic Data Types (ADT)**.

### 2.1 Astrazione a Costo Zero (Opaque Types)
Per modellare le grandezze fisiche di potenza (kW) ed energia (kWh) prevenendo accoppiamenti errati a tempo di compilazione, sono stati definiti i tipi opachi `Power` ed `Energy`:

```scala
// In org.gridsim.core.common.Power
opaque type Power = Double

object Power:
  def apply(v: Double): Power = v
  
  extension (p: Power)
    def toDouble: Double = p
    @targetName("powerPlus") def +(o: Power): Power = p + o
    // ... metodi di estensione algebrici

// In org.gridsim.core.common.Energy
opaque type Energy = Double

object Energy:
  def apply(v: Double): Energy = v
```
* **Analisi dei Vantaggi**:
  * **Type Safety a tempo di compilazione**: Il compilatore tratta `Power` ed `Energy` come tipi disgiunti. Qualsiasi tentativo di sommare potenza ed energia o di passare un valore di potenza dove è atteso un valore di energia genera un errore di compilazione statico.
  * **Zero Runtime Overhead**: A tempo di esecuzione, il compilatore cancella l'astrazione e compila i tipi opachi direttamente come tipi primitivi `Double`. Ciò evita l'allocazione di oggetti heap e l'indirezione tipica dei tradizionali wrapper, garantendo performance ottimali.
  * **Sintassi Espressiva (DSL)**: Tramite appositi metodi di estensione, è possibile definire le grandezze in modo leggibile ed elegante:
    ```scala
    extension (d: Double) infix def kwh: Energy = Energy(d)
    extension (d: Double) def kw: Power = Power(d)
    
    val cap: Energy = 10.kwh
    val load: Power = 5.kw
    ```

### 2.2 Rappresentazione Semantica dei Flussi (Flow ADT)
Lo scambio bidirezionale di energia nella micro-grid (surplus, deficit o bilanciamento) è modellato come un tipo algebrico di dati somma (ADT) tramite un `enum` parametrizzato:

```scala
enum Flow[+A]:
  case Surplus(amount: A)
  case Deficit(amount: A)
  case Balanced
```
Il coordinamento tra la grandezza fisica `Energy` e la semantica del flusso è gestito mediante estensioni che implementano regole algebriche interne:

```scala
// Conversione da Energy a Flow[Energy]
def toFlow: Flow[Energy] =
  if e > 0.0 then Flow.Surplus(e)
  else if e < 0.0 then Flow.Deficit(e.abs)
  else Flow.Balanced

// Somma algebrica tra flussi
extension (f: Flow[Energy])
  def value: Double = f match
    case Flow.Surplus(e) => e.toDouble
    case Flow.Deficit(e) => -e.toDouble
    case Flow.Balanced   => 0.0

  def +(o: Flow[Energy]): Flow[Energy] =
    (f.value + o.value).kwh.toFlow
```
* **Analisi**:
  * La proiezione sul segno algebrico è centralizzata nel metodo `value`.
  * La somma di due flussi delegata a `+` risolve l'operazione sui valori numerici reali per poi riconvertire il risultato nell'ADT appropriato. Questo incapsula la logica dei segni matematici all'interno del sistema di tipi, aumentando la robustezza semantica del dominio.

---

## 3. Gestione Funzionale dello Stato (State Monad)

La transizione di stato tra i singoli passi discreti della simulazione deve avvenire in modo referenzialmente trasparente e deterministico. Per coordinare questa pipeline senza ricorrere a mutabilità in-place o concatenazioni manuali, in [DefaultSimulationEngine.scala](file:///home/michelenardini/GridSim/app/src/main/scala/org/gridsim/core/simulation/DefaultSimulationEngine.scala) è stata adottata la monade **State** (`cats.data.State`).

### 3.1 Sequenziamento del Tick Simulativo
Il ciclo di evoluzione temporale ad ogni passo (tick) è strutturato come una *for-comprehension* che compone sequenzialmente le transizioni di stato:

```scala
override def step(state: SimulationState): SimulationState =
  simulationPipeline.run(state).value._1

private def simulationPipeline: State[SimulationState, Unit] =
  for {
    _ <- advanceEnvironment
    _ <- evolveEntities
    _ <- calculateCableLoads
  } yield()
```
* **Spiegazione Ingegneristica**:
  - `step` accetta uno snapshot immutabile `SimulationState` e avvia la computazione monadica mediante `.run(state)`. Il valore ritornato corrisponde al nuovo stato immutabile consolidato.
  - La *for-comprehension* incapsula tre transizioni di stato distinte:
    1. **`advanceEnvironment`**: Incrementa il contatore temporale e aggiorna le condizioni meteorologiche.
    2. **`evolveEntities`**: Calcola l'evoluzione interna di ciascun nodo ricavando i flussi residui.
    3. **`calculateCableLoads`**: Esegue l'algoritmo di risoluzione dei flussi elettrici sui cavi di collegamento.

### 3.2 Isolamento delle Modifiche
Ogni fase aggiorna lo stato globale mediante la primitiva `State.modify`:

```scala
private def advanceEnvironment: State[SimulationState, Unit] = State.modify {
  s => s.copy(environment = s.environment.advance(model.delta))
}

private def calculateCableLoads: State[SimulationState, Unit] = State.modify {
  s => s.copy(cableLoads = flowSolver.solve(s.entityFlows).toMap)
}
```
* **Valutazione Architetturale**: La monade `State` funge da barriera contro gli effetti collaterali, assicurando che lo stato aggiornato in una fase venga passato implicitamente e in modo thread-safe alla successiva. Questo previene bug derivanti dall'accesso a snapshot obsoleti o parzialmente modificati.

---

## 4. Infrastruttura di Validazione Funzionale (Accumulo degli Errori)

La corretta inizializzazione del sistema richiede la validazione dei vincoli fisici e topologici delle componenti. Per massimizzare la diagnostica e prevenire l'approccio *fail-fast* (interruzione al primo errore riscontrato), è stata implementata un'infrastruttura di validazione basata su functori applicativi e sul tipo `ValidatedNec` di **Cats**.

### 4.1 La Type Class `Validator` e la Composizione Applicativa
L'astrazione di validazione è definita tramite la type class `Validator`:

```scala
trait Validator[E]:
  def validate(a: E): ValidatedNec[DomainError, E]
```
Utilizzando `ValidatedNec` (dove `Nec` rappresenta una `NonEmptyChain`), le violazioni non interrompono il flusso, ma vengono accumulate in una struttura dati non vuota ad alte performance. Le estensioni su tipi numerici consentono di definire regole formali atomiche e riutilizzabili:

```scala
extension (value: Double)
  def mustBePositive(field: String): ValidatedNec[DomainError, Double] =
    Validated.condNec(value > 0, value, ValueMustBePositive(field, value))

  def mustBeInRange(field: String, min: Double, max: Double): ValidatedNec[DomainError, Double] =
    Validated.condNec(value >= min && value <= max, value, OutOfRange(field, value, min, max))
```

### 4.2 Validazione della Batteria (BatteryValidator)
Il validatore della batteria verifica contemporaneamente i parametri statici dell'entità e la coerenza dello stato di carica dinamico:

```scala
given Validator[(Battery, BatteryState)] with
  def validate(pair: (Battery, BatteryState)): ValidatedNec[DomainError, (Battery, BatteryState)] =
    val (entity, state) = pair
    (
      validateCoherence(entity, state),
      validateBatteryEntity(entity),
      validateBatteryState(entity, state)
    ).mapN((_, _, _) => pair)

  private def validateBatteryEntity(b: Battery): ValidatedNec[DomainError, Battery] =
    (
      b.maxCapacity.toDouble.mustBePositive("Capacity"),
      b.maxPowerCharge.toDouble.mustBePositive("Max Power Charge"),
      b.maxPowerDischarge.toDouble.mustBePositive("Max Power Discharge"),
      b.minSoC.mustBeInRange("Min SoC", 0.0, 1.0)
    ).mapN((_, _, _, _) => b)
```
* **Spiegazione**:
  - `validate` compone tre funzioni di verifica. Grazie al metodo `.mapN` del funtore applicativo, se una o più verifiche falliscono, i relativi `DomainError` vengono combinati ed accumulati nella catena risultante.
  - `validateBatteryEntity` garantisce che le potenze massime e la capacità siano positive, e che la percentuale del SoC minimo sia inclusa nell'intervallo chiuso $[0, 1]$.

---

## 5. Evoluzione Temporale del Nodo Casa (GridEvolution)

L'avanzamento discreto dello stato di un'abitazione e delle sue componenti annidate implementa la type class polimorfa [GridEvolution](file:///home/michelenardini/GridSim/app/src/main/scala/org/gridsim/core/behaviour/GridEvolution.scala).

### 5.1 Risoluzione del Bilancio Energetico Domestico
Il comportamento di evoluzione dell'abitazione in [HouseEvolution.scala](file:///home/michelenardini/GridSim/app/src/main/scala/org/gridsim/core/behaviour/house/HouseEvolution.scala) coordina la logica di consumo e l'interazione con i dispositivi ad essa collegati:

```scala
object HouseEvolution extends GridEvolution[HouseState, House, EvolutionContext[HouseEvolutionDependencies]]:

  extension (state: HouseState)
    def evolve(house: House, environment: Environment)(
      using context: EvolutionContext[HouseEvolutionDependencies]
    ): (HouseState, Flow[Energy]) =
      val initialFlow = resolveConsumption(house, environment)
      val componentsById = house.components.map(c => c.id -> c).toMap

      val result =
        HouseComponentEvolution.evolveAll(
          states = state.componentStates,
          componentsById = componentsById,
          initialFlow = initialFlow,
          environment = environment
        )(using context.delta)

      (state.copy(componentStates = result.states), result.flow)
```
* **Analisi del Flusso**:
  1. Il consumo basale viene calcolato in base al profilo giornaliero configurato per la casa (`initialFlow`).
  2. Gli elementi fisici interni all'abitazione vengono indicizzati in base all'identificativo statico (`componentsById`).
  3. Il metodo `HouseComponentEvolution.evolveAll` esegue la computazione sequenziale dei componenti, calcolando l'evoluzione dei produttori e dei dispositivi di accumulo.
  4. Viene generata una nuova istanza di `HouseState` contenente la lista degli stati aggiornati dei componenti insieme al flusso residuo finale.

### 5.2 Evoluzione Sequenziale in Due Fasi (`evolveAll`)
Per riprodurre in modo realistico il bilancio elettrico domestico, i componenti interni non vengono evoluti in parallelo, ma secondo una rigida precedenza fisica: prima i generatori (fotovoltaico) per soddisfare il fabbisogno locale, e successivamente gli accumulatori (batterie) per caricarsi con il surplus o sopperire al deficit.

```scala
def evolveAll(
  states: Iterable[GridEntityState],
  componentsById: Map[String, GridEntity],
  initialFlow: Flow[Energy],
  environment: Environment
)(using delta: FiniteDuration): ComponentEvolutionResult =
  val producerResult =
    evolvePhase(
      states,
      componentsById,
      initialFlow,
      environment
    )(evolveProducer)

  evolvePhase(
    producerResult.states,
    componentsById,
    producerResult.flow,
    environment
  )(evolveStorage)
```
* **Spiegazione**:
  * **Fase 1 (Produttori)**: Il consumo iniziale (`initialFlow`) viene modificato da `evolveProducer` aggiungendo l'energia prodotta dai pannelli solari.
  * **Fase 2 (Accumulatori)**: Il flusso risultante (`producerResult.flow`) viene passato a `evolveStorage` per determinare la carica o scarica delle batterie.
  * **Purezza e Determismo**: L'ordine è preservato funzionalmente tramite l'algoritmo di piegatura (`foldLeft`) all'interno di `evolvePhase`, garantendo che lo stato dinamico venga aggiornato in modo referenzialmente trasparente.

---

## 6. Reattività dei Sistemi di Accumulo (StorageEnergyExchanger)

A differenza dei produttori o dei carichi attivi, i sistemi di accumulo (es. batterie) non possiedono un'evoluzione temporale autonoma, ma operano in modo **reattivo** rispetto a flussi preesistenti nella rete. Per formalizzare questa distinzione semantica ed evitare accoppiamenti con l'avanzamento temporale standard (`GridEvolution`), è stata introdotta la type class [StorageEnergyExchanger](file:///home/michelenardini/GridSim/app/src/main/scala/org/gridsim/core/behaviour/storage/StorageEnergyExchanger.scala):

```scala
trait StorageEnergyExchanger[S <: StorageState, E <: Storage]:
  def exchange(
    state: S,
    entity: E,
    flow: Flow[Energy],
    env: Environment
  )(using delta: FiniteDuration): (S, Flow[Energy])
```
Questa astrazione consente di definire come un dispositivo di accumulo reagisce ad uno squilibrio energetico (surplus o deficit) calcolando lo stato finale e restituendo l'energia residua non scambiata.

### 6.1 Instradamento Polimorfo dello Scambio (BatteryEnergyExchange)
L'adattatore concreto per le batterie implementa la type class eseguendo il dispatching in base al tipo di flusso energetico rilevato:

```scala
object BatteryEnergyExchange:
  given StorageEnergyExchanger[BatteryState, Battery] with
    def exchange(
      state: BatteryState,
      b: Battery,
      flow: Flow[Energy],
      env: Environment
    )(using delta: FiniteDuration): (BatteryState, Flow[Energy]) =
      val strategy = BatteryStrategy.forModel(b.model)

      flow match
        case Surplus(e) => strategy.charge(state)(e, b)
        case Deficit(e) => strategy.discharge(state)(e, b)
        case _          => (state, Balanced)
```
* **Spiegazione del Codice**:
  - `exchange` seleziona la strategia fisica specifica in base al modello costruttivo della batteria (`BatteryStrategy.forModel`).
  - Utilizzando il pattern matching sull'ADT del flusso energetico residuo `flow`:
    - In caso di **`Surplus`** (energia in eccesso post-consumo domestico), invoca l'operazione di carica (`charge`).
    - In caso di **`Deficit`** (energia mancante per soddisfare i carichi locali), invoca l'operazione di scarica (`discharge`).
    - In assenza di flussi attivi, lo stato della batteria non viene modificato.

---

[Implementazione](../06-implementation.md)
