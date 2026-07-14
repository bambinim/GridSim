# Enrico Marchionni

Il presente capitolo descrive in dettaglio le scelte strategiche, i pattern funzionali e le componenti chiave del
codice Scala sviluppato da me, Enrico Marchionni.
L'esposizione è supportata da frammenti di codice esplicativi.

## Statistiche

Ho sviluppato il sottosistema di **raccolta ed elaborazione delle statistiche** (`org.gridsim.statistics`):

- **Infrastruttura di Accumulo Funzionale (Fold)**:
  - Astrazione generica di accumulatore a passo singolo: [Fold](/app/src/main/scala/org/gridsim/statistics/Fold.scala).
- **Motore e Registro delle Statistiche (Engine)**:
  - Composizione di più statistiche in un'unica pipeline a passata singola: [StatisticsEngine](/app/src/main/scala/org/gridsim/statistics/StatisticsEngine.scala).
  - Registro tipizzato e pluggable delle statistiche attive: [StatisticsRegistry](/app/src/main/scala/org/gridsim/statistics/StatisticsRegistry.scala).
- **Statistiche Concrete**:
  - Bilancio energetico import/export della rete: [FlowStatistic](/app/src/main/scala/org/gridsim/statistics/FlowStatistic.scala).
  - Cronologia a finestra mobile del flusso netto: [NetFlowHistoryStatistic](/app/src/main/scala/org/gridsim/statistics/NetFlowHistoryStatistic.scala).
  - Carica aggregata degli accumulatori, incluse le batterie annidate nelle abitazioni: [BatteriesChargeStatistic](/app/src/main/scala/org/gridsim/statistics/BatteriesChargeStatistic.scala).
  - Rilevamento del sovraccarico dei cavi della rete: [CablesOverloadStatistic](/app/src/main/scala/org/gridsim/statistics/CablesOverloadStatistic.scala).
  - Avanzamento temporale e calendario della simulazione: [SimulationTimeStatistic](/app/src/main/scala/org/gridsim/statistics/SimulationTimeStatistic.scala).

### Accumulo funzionale a passo singolo (Fold)

La raccolta delle statistiche deve avvenire senza mantenere in memoria l'intera storia della simulazione e senza
introdurre stato mutabile condiviso tra i vari osservatori. A tale scopo è stata definita l'astrazione `Fold[In, Out]`
in [Fold.scala](/app/src/main/scala/org/gridsim/statistics/Fold.scala), concettualmente
una macchina di Moore: uno stato interno opaco, una funzione di transizione pura e una funzione di proiezione finale.

```scala
trait Fold[-In, +Out]:
  type State
  def initial: State
  def step(state: State, in: In): State
  def extract(state: State): Out
```

**Analisi**:
  - Il tipo `State` è astratto e privato all'implementazione: chi consuma un `Fold` non conosce né manipola
    direttamente la rappresentazione interna dell'accumulo, sapendo solo comporlo (`map`, `contramap`) o eseguirlo.
  - La varianza (`-In`, `+Out`) consente a un `Fold` più generale di essere utilizzato ovunque ne sia richiesto uno
    più specifico, favorendo il riuso.

#### Due strategie di costruzione

Il companion object espone due *smart constructor* per coprire i due casi ricorrenti nel dominio delle statistiche:

```scala
object Fold:
  def monoidal[In, A](sample: In => A)(using monoid: cats.kernel.Monoid[A]): Fold[In, A] =
    new Fold[In, A]:
      type State = A
      def initial: A = monoid.empty
      def step(s: A, in: In): A = monoid.combine(s, sample(in))
      def extract(s: A): A = s

  def unfold[In, S](init: S)(stepFunction: (S, In) => S): Fold[In, S] =
    new Fold[In, S]:
      type State = S
      def initial: S = init
      def step(s: S, in: In): S = stepFunction(s, in)
      def extract(s: S): S = s
```

`Fold.monoidal`: sfrutta la type class `Monoid[A]` di Cats per ottenere "gratuitamente" un intero `Fold` a
partire dalla sola funzione di campionamento `sample`. È la scelta adottata per tutte le statistiche il cui
accumulo è associativo e dotato di elemento neutro (somme, conteggi, massimi): `FlowStatistic`,
`BatteriesChargeStatistic`, `CablesOverloadStatistic`.

`Fold.unfold`: copre il caso generale in cui l'accumulo non è esprimibile come monoide, ad esempio una
cronologia FIFO a capacità limitata (`NetFlowHistoryStatistic`) o un contatore che dipende dall'ordine di arrivo
degli eventi (`SimulationTimeStatistic`).

### Composizione a passata singola (StatisticsEngine e StatsBoard)

Con più statistiche attive contemporaneamente, la soluzione naïve richiederebbe di attraversare lo stream degli
`SimulationSnapshot` una volta per ciascuna. `StatisticsEngine.build`, in
[StatisticsEngine.scala](/app/src/main/scala/org/gridsim/statistics/StatisticsEngine.scala),
compone N registrazioni indipendenti in un unico `Fold[In, StatsBoard]`, così che ogni snapshot venga elaborato
una sola volta da tutte le statistiche in blocco:

```scala
object StatisticsEngine:
  def build[In](registrations: List[Registration[In, ?]]): Fold[In, StatsBoard] =
    new Fold[In, StatsBoard]:
      type State = Map[StatKey[?], Any]
      def initial: Map[StatKey[?], Any] = registrations.map(r => r.key -> r.fold.initial).toMap
      def step(s: State, in: In): Map[StatKey[?], Any] = registrations.map { r =>
          r.key -> r.fold.step(s(r.key).asInstanceOf[r.fold.State], in)
        }.toMap
      def extract(s: State): StatsBoard = StatsBoard.fromMap(registrations.map { r =>
          r.key -> r.fold.extract(s(r.key).asInstanceOf[r.fold.State])
        }.toMap)
```

Per rappresentare in modo sicuro una collezione eterogenea di risultati (tipi diversi per ciascuna statistica) senza
esporre cast al chiamante, il risultato è incapsulato in `StatsBoard`, un *opaque type* su
`Map[StatKey[?], Any]`, accoppiato a un enum tipizzato `StatKey[A]` che lega ogni chiave al proprio tipo di
risultato:

```scala
enum StatKey[A](val name: String):
  case FlowStatKey extends StatKey[FlowStatistic]("flowStat")
  case BatteryChargeStatKey extends StatKey[BatteriesChargeStatistic]("batteryCharge")
  // ...

opaque type StatsBoard = Map[StatKey[?], Any]
object StatsBoard:
  extension (board: StatsBoard)
    def get[A](key: StatKey[A]): A = board(key).asInstanceOf[A] // safe: only the engine builds this map
```

L'unico punto del sistema in cui avviene un cast non verificato dal compilatore è interno a `get`, ed è reso sicuro dal
fatto che `StatsBoard` viene costruito esclusivamente da `StatisticsEngine.build` a partire dalle stesse coppie
`(StatKey[A], Fold[In, A])` fornite in `registrations`: per costruzione, il valore associato a `FlowStatKey` è sempre un
`FlowStatistic`.

**Estendibilità (Registry Pattern)**: [StatisticsRegistry.scala](/app/src/main/scala/org/gridsim/statistics/StatisticsRegistry.scala)
centralizza la composizione concreta, esponendo un unico `Fold[SimulationSnapshot, StatsBoard]` (`engine`).
Aggiungere una nuova statistica richiede solo un nuovo caso `StatKey`, un nuovo `Fold` e una voce in
`allStatistics`: né `StatisticsEngine`, né i consumatori di `StatsBoard` (GUI, observability) devono essere
modificati.

### Statistiche Concrete: Pattern e Motivazioni

#### Statistiche monoidali (FlowStatistic, BatteriesChargeStatistic, CablesOverloadStatistic)

Queste statistiche condividono lo stesso schema: un `sample: In => A` puro che estrae un singolo campione dallo
snapshot corrente, combinato da un'istanza `given Monoid[A]` che definisce come due campioni si aggregano.

```scala
given Monoid[BatteriesChargeStatistic] with
  def empty: BatteriesChargeStatistic = BatteriesChargeStatistic.empty
  def combine(a: BatteriesChargeStatistic, b: BatteriesChargeStatistic): BatteriesChargeStatistic =
    if a.samples == 0 then b
    else if b.samples == 0 then a
    else BatteriesChargeStatistic(
      samples = a.samples + b.samples,
      totalCharge = a.totalCharge + b.totalCharge,
      maxCharge = a.maxCharge max b.maxCharge
    )
```

Un caso rilevante è il campionamento della carica delle batterie, che deve attraversare ricorsivamente le
abitazioni (le cui componenti annidate possono a loro volta includere altre abitazioni):

```scala
private def totalBatteryCharge(state: GridEntityState): Energy = state match
  case b: BatteryState => b.currentCharge
  case h: HouseState    => h.componentStates.foldLeft[Energy](0.kwh)(_ + totalBatteryCharge(_))
  case _                => 0.kwh
```

**Motivazione della Ricorsione**: un attraversamento a un solo livello (`componentStates.collect { case b: BatteryState => ... }`)
ignorerebbe silenziosamente le batterie annidate in un'abitazione contenuta a sua volta in un'altra abitazione.
`totalBatteryCharge` risolve il problema per qualunque profondità di nesting, mantenendo però la semantica per cui
ogni abitazione di primo livello contribuisce come un unico campione aggregato (e non come N campioni separati) a
`maxCharge`/`totalCharge`.

#### Statistiche a stato non-monoidale (NetFlowHistoryStatistic, SimulationTimeStatistic)

`NetFlowHistoryStatistic` mantiene una finestra FIFO limitata (`capacity`) di campioni recenti: l'operazione di
"scarto del campione più vecchio" non è associativa nel senso richiesto da un monoide, per cui la statistica è
costruita con `Fold.unfold`, aggiornando esplicitamente lo stato ad ogni passo tramite `record`.

`SimulationTimeStatistic` traccia tick trascorsi, tempo simulato accumulato e le date di calendario di
inizio/fine, anch'esse non componibili in modo associativo puro (dipendono dall'ordine di arrivo degli snapshot),
e per lo stesso motivo utilizza `Fold.unfold` anziché `Fold.monoidal`.

---

## Ambiente e Modello Fisico Solare

Ho progettato e sviluppato il modello ambientale e fisico che governa la produzione fotovoltaica:

- **Grandezze Fisiche e Geografia**:
  - Posizione geografica di un componente: [GeographicPoint](/app/src/main/scala/org/gridsim/core/common/GeographicPoint.scala).
  - Irraggiamento solare (W/m²): [Irradiance](/app/src/main/scala/org/gridsim/core/common/Irradiance.scala).
  - Temperatura multi-unità con sicurezza a tempo di compilazione: [Temperatures](/app/src/main/scala/org/gridsim/core/common/Temperatures.scala).
- **Ambiente e Formule Astronomiche**:
  - Stato meteorologico e temporale della simulazione: [Environment](/app/src/main/scala/org/gridsim/core/model/Environment.scala).
  - Formule astronomiche pure (declinazione solare, alba/tramonto, irraggiamento a cielo sereno): [SolarModel](/app/src/main/scala/org/gridsim/core/model/SolarModel.scala).
- **Pannello Fotovoltaico**:
  - Entità statica e stato dinamico del pannello: [SolarPanel](/app/src/main/scala/org/gridsim/core/model/SolarPanel.scala).
  - Strategia di calcolo della produzione elettrica: [SolarPanelStrategy](/app/src/main/scala/org/gridsim/core/behaviour/producer/SolarPanelStrategy.scala).
  - Evoluzione temporale del pannello: [SolarPanelEvolution](/app/src/main/scala/org/gridsim/core/behaviour/producer/SolarPanelEvolution.scala).
  - Adattatore verso il dispatcher generico di evoluzione: [SolarPanelEvolutionHandler](/app/src/main/scala/org/gridsim/core/behaviour/producer/SolarPanelEvolutionHandler.scala).

### Grandezze Fisiche a Prova di Errore

Coerentemente con l'approccio adottato nel resto del dominio (opaque type per `Power`/`Energy`), `Irradiance` è
modellata come opaque type con smart constructor validante:

```scala
opaque type Irradiance = Double

object Irradiance:
  def apply(v: Double): Irradiance =
    require(v >= 0.0, s"Irradiance cannot be negative: $v")
    v
```

`Temperatures` estende questo approccio a un caso più complesso: temperature espresse in unità diverse (Celsius,
Kelvin, Fahrenheit) non devono poter essere confuse tra loro né sommate direttamente, ma devono comunque poter
essere convertite in modo sicuro:

```scala
opaque type Temperature[U <: TemperatureUnit] = Double

trait TempValidator[U <: TemperatureUnit]:
  def validate(value: Double): Temperature[U]

given TempValidator[Celsius] with
  def validate(v: Double): Temperature[Celsius] = celsius(v)
```

Il parametro fantasma `U <: TemperatureUnit` rende `Temperature[Celsius]` e `Temperature[Kelvin]` tipi distinti
per il compilatore, pur condividendo la medesima rappresentazione a runtime (`Double`), evitando qualunque
overhead di boxing.

La type class `TempValidator[U]` disaccoppia la validazione (ogni unità ha uno zero assoluto diverso: -273.15°C,
0 K, -459.67°F) dagli operatori aritmetici generici (`+`, `-`), che possono così restare polimorfi rispetto
all'unità pur richiamando la validazione corretta tramite `using`.

Le conversioni (`toKelvin`, `toFahrenheit`, ...) sono funzioni pure esposte come extension method, e
`AnyTemperature` (alias di `Temperature[Celsius]`) permette di normalizzare un valore di provenienza eterogenea
senza perdere la sicurezza di tipo altrove nel sistema.

### Modello Astronomico e Ambiente

`SolarModel` isola in un oggetto `private[model]` un insieme di formule astronomiche pure e indipendenti tra loro
(declinazione solare, lunghezza del giorno, elevazione a mezzogiorno solare, irraggiamento a cielo sereno), ciascuna
corredata di riferimento al modello fisico/astronomico approssimato che implementa (es. l'equazione di Cooper per la
declinazione). Questa separazione tiene `Environment` libero da dettagli trigonometrici e rende ogni formula testabile
in isolamento.

```scala
private[model] object SolarModel:
  def solarDeclinationDeg(dayOfYear: Int): Double =
    23.45 * math.sin(math.toRadians(360.0 / 365.0 * (284 + dayOfYear)))

  def dayLengthHours(latitudeDeg: Double, declinationDeg: Double): Double =
    val arcosHourAngle = -math.tan(math.toRadians(latitudeDeg)) * math.tan(math.toRadians(declinationDeg))
    if arcosHourAngle <= -1.0 then 24.0 // giorno polare
    else if arcosHourAngle >= 1.0 then 0.0 // notte polare
    else /* ... */
```

`Environment.weather(point: GeographicPoint)` compone queste formule per derivare irraggiamento e temperatura
localizzati, in funzione della posizione geografica del componente richiedente e dell'istante corrente:

```scala
trait Environment:
  def startDateTime: LocalDateTime
  def time: FiniteDuration
  final def currentDateTime: LocalDateTime = startDateTime.plusNanos(time.toNanos)
  def weather(point: GeographicPoint): WeatherConditions
  def advance(delta: FiniteDuration): Environment
```

`Environment` è immutabile: `advance` restituisce una nuova istanza anziché mutare `time`, coerentemente con il
pattern "Functional Core" descritto nel design di dettaglio.

La dipendenza dalla posizione geografica (anziché un unico valore di irraggiamento globale) permette a più
pannelli collocati in punti diversi della rete di ricevere condizioni meteorologiche differenti nello stesso
tick, requisito necessario per una simulazione realistica.

`currentDateTime` è derivato (non memorizzato) da `startDateTime` e `time`, evitando la possibilità che le due
grandezze divergano.

### Produzione Fotovoltaica: Strategy + GridEvolution + Handler

La produzione elettrica del pannello segue la stessa composizione a tre livelli già utilizzata per le batterie
(Strategy Pattern, type class `GridEvolution`, adattatore verso il dispatcher generico), applicata nel package
`org.gridsim.core.behaviour.producer`:

```scala
trait SolarPanelStrategy extends ProducerStrategy[SolarPanelState, SolarPanel, Irradiance]:
  def produce(state: SolarPanelState, panel: SolarPanel, irradiance: Irradiance)
             (using delta: FiniteDuration): (SolarPanelState, Flow[Energy])

object StandardSolarPanelStrategy extends SolarPanelStrategy:
  def produce(state: SolarPanelState, panel: SolarPanel, irradiance: Irradiance)
             (using delta: FiniteDuration): (SolarPanelState, Flow[Energy]) =
    val rawKw = (irradiance.toDouble * panel.areaSqm * panel.efficiency) / 1000.0
    val production = panel.maxProduction.min(rawKw.kw)
    // ...
```

La formula STC lineare (potenza = irraggiamento × area × efficienza) è incapsulata in `StandardSolarPanelStrategy`,
isolata dietro `SolarPanelStrategy.forPhysics`.
L'enum `SolarPanelPhysics` la rende selezionabile a runtime, predisponendo l'estensione a modelli fisici più accurati
(es. temperature coefficient) senza toccare il chiamante.

`SolarPanelEvolution` applica la type class `GridEvolution` (stesso contratto usato per `HouseEvolution`),
interrogando l'ambiente nella posizione del pannello e delegando alla strategia:

```scala
object SolarPanelEvolution extends GridEvolution[SolarPanelState, SolarPanel, EvolutionContext[Unit]]:
  extension (state: SolarPanelState)
    def evolve(panel: SolarPanel, environment: Environment)(using context: EvolutionContext[Unit]) =
      given FiniteDuration = context.delta
      given SolarPanelStrategy = SolarPanelStrategy.forPhysics(panel.physics)
      val weather = environment.weather(panel.location)
      state.produce(panel, weather.irradiance)
```

Infine, `SolarPanelEvolutionHandler` adatta l'evoluzione standalone del pannello al dispatcher generico
`EntityEvolutionDispatcher`, tramite pattern matching su `EvolutionRequest`:

```scala
final case class SolarPanelEvolutionHandler() extends EntityEvolutionHandler:
  override def supports(request: EvolutionRequest): Boolean =
    (request.state, request.entity) match
      case (_: SolarPanelState, _: SolarPanel) => true
      case _ => false

  override def evolve(request: EvolutionRequest): (SolarPanelState, Flow[Energy]) = /* ... */
```

Il pannello non richiede dipendenze esterne (a differenza della casa, che necessita di un `ConsumptionResolver`), per
cui il suo `EvolutionContext[Unit]` porta solo il `delta` del tick. `supports` verifica la coppia stato/entità senza
eseguire alcuna evoluzione, rispettando il contratto imposto da `EntityEvolutionHandler`; l'handler viene poi registrato
in `EntityEvolutionDispatcher.default` insieme a quello delle abitazioni, senza che il dispatcher generico debba
conoscere l'esistenza dei pannelli fotovoltaici.

---

## Interfaccia Grafica delle Statistiche e Registrazione degli Observer

Ho sviluppato la presentazione a schermo delle statistiche e il collegamento del modulo `statistics` al ciclo di vita
reattivo della simulazione:

- **Vista delle Statistiche (MVVM)**:
  - ViewModel dedicati a ciascuna statistica: [FlowStatisticViewModel](/app/src/main/scala/org/gridsim/gui/viewmodel/FlowStatisticViewModel.scala),
    [BatteriesChargeStatisticViewModel](/app/src/main/scala/org/gridsim/gui/viewmodel/BatteriesChargeStatisticViewModel.scala),
    [CableOverloadStatisticViewModel](/app/src/main/scala/org/gridsim/gui/viewmodel/CableOverloadStatisticViewModel.scala),
    [SimulationTimeStatisticViewModel](/app/src/main/scala/org/gridsim/gui/viewmodel/SimulationTimeStatisticViewModel.scala),
    [NetFlowChartStatisticViewModel](/app/src/main/scala/org/gridsim/gui/viewmodel/NetFlowChartStatisticViewModel.scala).
  - Viste ScalaFX corrispondenti: [FlowStatisticView](/app/src/main/scala/org/gridsim/gui/view/FlowStatisticView.scala),
    [BatteriesChargeStatisticView](/app/src/main/scala/org/gridsim/gui/view/BatteriesChargeStatisticView.scala),
    [CableOverloadStatisticView](/app/src/main/scala/org/gridsim/gui/view/CableOverloadStatisticView.scala),
    [SimulationTimeStatisticView](/app/src/main/scala/org/gridsim/gui/view/SimulationTimeStatisticView.scala),
    [NetFlowChartStatisticView](/app/src/main/scala/org/gridsim/gui/view/NetFlowChartStatisticView.scala).
- **Registrazione degli Observer**:
  - Wiring dell'observer grafico e dell'observer statistico nel runtime: [RunningSimulationFactory](/app/src/main/scala/org/gridsim/gui/runtime/RunningSimulationFactory.scala).

### Presentazione delle Statistiche (MVVM)

Ogni statistica prodotta dal motore delle statistiche è resa a schermo tramite una coppia ViewModel/View indipendente,
seguendo il medesimo pattern MVVM adottato nel resto della GUI.
Il ViewModel espone esclusivamente proprietà reattive testuali già formattate, mentre la View si limita a legarle
dichiarativamente attraverso il binding si scalafx:

```scala
class BatteriesChargeStatisticViewModel:
  val averageText = StringProperty(s"Average: ${Energy.Zero.show}")
  val totalText = StringProperty(s"Total: ${Energy.Zero.show}")
  val maxText = StringProperty(s"Max: ${Energy.Zero.show}")

  def update(stats: BatteriesChargeStatistic): Unit =
    averageText.value = s"Average: ${stats.averageCharge.show}"
    totalText.value = s"Total: ${stats.totalCharge.show}"
    maxText.value = s"Max: ${stats.maxCharge.show}"
```

```scala
class BatteriesChargeStatisticView(viewModel: BatteriesChargeStatisticViewModel) extends VBox with ViewFX:
  private val averageLabel = new Label(): text <== viewModel.averageText
  // ...
  children = Seq(averageLabel, minLabel, maxLabel)
```

La View non contiene logica di formattazione né accede direttamente a `BatteriesChargeStatistic`;
riceve dal ViewModel solo `String` già pronti per la visualizzazione (tramite l'istanza `Show` definita sui tipi
del dominio, es. `Energy`), mantenendo la separazione MVVM anche per un componente di sola lettura come un pannello
statistico.

### Registrazione degli Observer

Il collegamento tra il motore di simulazione e i consumatori (GUI e statistiche) avviene tramite il pattern
Observer/Publish-Subscribe descritto a livello architetturale.
Ho solo dovuto registrare i due observer applicativi nel runtime GUI, in
[RunningSimulationFactory.createSimpleSimulation](/app/src/main/scala/org/gridsim/gui/runtime/RunningSimulationFactory.scala):

```scala
val guiObserver = Observer[IO, SimulationData.SimulationSnapshot](snapshotSignal.set)

val statisticsObserver = Observer[IO, SimulationData.SimulationSnapshot] { snapshot =>
  statisticsStateSignal.update(s => StatisticsRegistry.engine.step(s, snapshot))
}

val controller = SimulationControllerFactory.create(
  model, state,
  observers = List(guiObserver, statisticsObserver),
  conf = SimulationConf(delta)
)
```

`guiObserver` inoltra ogni nuovo `SimulationSnapshot` a una `SignallingRef` fs2, da cui la GUI legge lo stato
corrente della rete per il rendering.

`statisticsObserver` applica lo stesso snapshot come singolo passo (`step`) del `Fold` composito esposto da
`StatisticsRegistry.engine`, aggiornando una seconda `SignallingRef` dedicata allo stato aggregato delle
statistiche.

I due observer sono registrati fianco a fianco nella stessa lista passata al controller: entrambi ricevono lo
stesso stream di eventi in modo del tutto disaccoppiato l'uno dall'altro, coerentemente con il pattern
Event-driven Publish-Subscribe adottato dal `Dispatcher` (descritto nel design di dettaglio) — il motore di
simulazione non conosce né la GUI né il modulo statistiche, e i due osservatori non conoscono l'esistenza
l'uno dell'altro.

---

## Testing

### Statistiche

I test del modulo `statistics` definiscono le proprietà che l'accumulatore doveva rispettare — leggi del monoide
(identità, combinazione), casi limite del campionamento (snapshot vuoto, entità non pertinenti, valori esattamente
al limite) — per poi far collassare l'implementazione su quelle proprietà.

- **`FlowStatisticSpec` / `CablesOverloadStatisticSpec` / `BatteriesChargeStatisticSpec`**: verificano le leggi di
  monoide (`empty` come identità, associatività della `combine`) e il comportamento del relativo `Sampler` su
  snapshot vuoti, entità non pertinenti, casi limite (es. carico di un cavo esattamente pari alla capacità massima,
  che non deve essere considerato sovraccarico) e casi di nesting multi-livello per le batterie annidate nelle
  abitazioni.
- **`SimulationTimeStatisticSpec`**: esercita direttamente `initial`/`step`/`extract` del `Fold` (non essendo
  presente un `Sampler` separato), verificando l'incremento dei tick, l'accumulo del tempo simulato a partire dal
  `delta` di ciascuno snapshot e la corretta lettura delle date di calendario dall'ambiente della simulazione.
- **`NetFlowHistoryStatisticSpec`**: verifica il comportamento a finestra mobile (FIFO), inclusa la rimozione dei
  campioni più vecchi al superamento della capacità.
- **`StatisticsRegistrySpec` / `StatisticsEngineSpec`**: test di integrazione a livello di motore, che verificano
  che ciascuna statistica registrata in `StatisticsRegistry.allStatistics` sia correttamente raggiungibile tramite
  la propria `StatKey` nello `StatsBoard` prodotto da `engine`, e che uno stesso stream di snapshot venga elaborato
  correttamente e in modo indipendente da tutte le statistiche registrate contemporaneamente (guardia di
  regressione contro bug di wiring, ad esempio un fold registrato due volte o una chiave associata al fold
  sbagliato).

### Ambiente e Modello Solare

I test di questa area combinano proprietà astronomiche note (equinozi, solstizi, casi limite polari) a verifiche
di validazione e di composizione, per statistiche identiche a quelle già adottate nel modulo `statistics`: leggi di
identità/associatività dove applicabile, e casi limite espliciti su ogni smart constructor.

- **`GeographicPointSpec` / `IrradianceSpec` / `TemperaturesSpec`**: verificano gli smart constructor dei rispettivi
  opaque/phantom type — accettazione dei valori validi, rifiuto dei valori fuori dominio (latitudine/longitudine
  fuori range, irraggiamento negativo, temperature sotto lo zero assoluto per ciascuna delle tre unità), e la
  correttezza delle conversioni e degli operatori di confronto/aritmetica esposti come extension method.
- **`SolarModelSpec`**: esercita in isolamento ciascuna formula astronomica pura, verificandone il comportamento nei
  punti notevoli del dominio fisico — declinazione solare ai solstizi/equinozio, lunghezza del giorno all'equatore e
  ai poli (giorno/notte polare), simmetria di alba/tramonto rispetto al mezzogiorno solare, irraggiamento nullo
  prima dell'alba e dopo il tramonto.
- **`EnvironmentSpec`**: verifica l'immutabilità di `Environment` (`advance` restituisce una nuova istanza senza
  mutare l'originale), la coerenza tra `time` e `currentDateTime`, la normalizzazione di `hourOfDay` oltre le 24 ore
  e la determinismo di `weather` a parità di input.
- **`SolarPanelSpec` / `SolarPanelValidatorSpec`**: verificano l'accumulo degli errori di validazione (potenza di
  picco, area ed efficienza fuori range) e i casi limite dello stato dinamico del pannello (produzione a zero,
  produzione pari alla potenza di picco, produzione superiore al limite fisico del pannello).
- **`SolarPanelStrategySpec` / `SolarPanelEvolutionSpec`**: verificano rispettivamente la formula di produzione STC
  lineare (proporzionalità a irraggiamento/area/efficienza, saturazione alla potenza di picco, produzione nulla a
  irraggiamento zero) e l'integrazione tra `Environment.weather` e la strategia di produzione durante un tick di
  evoluzione.

---

[Implementazione](06-implementation.md)
