# Roadmap GUI - Schermata Di Configurazione

## Scopo

Questa roadmap riguarda solo la prima parte della GUI: una schermata di
configurazione minimale in cui l'utente sceglie uno scenario predefinito da
avviare.

La GUI non e' la parte core del progetto. Deve rimanere semplice, leggibile e
facile da integrare quando saranno completati:

- DSL di scenario;
- runner della simulazione;
- meccanismo Observable/Observer.

## Obiettivo Della Prima Versione

La prima versione della GUI deve permettere di:

1. visualizzare una lista di scenari disponibili;
2. selezionare uno scenario;
3. leggere una breve descrizione dello scenario;
4. vedere una preview testuale minimale;
5. scegliere solo i parametri di esecuzione indispensabili;
6. premere `Start`;
7. mostrare eventuali errori di configurazione o caricamento.

Non deve permettere di creare o modificare scenari.

## Cosa Non Deve Fare

La schermata iniziale non deve includere:

- editor di nodi;
- editor di cavi;
- editor di batterie;
- editor di pannelli solari;
- drag and drop;
- wizard multi-step;
- modifica della topologia;
- scrittura o parsing del DSL da parte dell'utente;
- dashboard real-time;
- grafici;
- metriche runtime.

Queste parti vanno rimandate o gestite da altri componenti.

## Stato Attuale Del Progetto

### DSL Disponibile

Il DSL attuale contiene builder per singole entita':

- `BatteryBuilder`;
- `SolarArrayBuilder`;
- `HouseBuilder`.

Il contratto base e':

```scala
trait Builder[E <: GridEntity, S <: GridEntityState]:
  def build(): ValidatedNec[DSLError, (E, S)]
```

Quindi oggi il DSL costruisce coppie del tipo:

```text
entita' statica + stato iniziale
```

Esempi:

- `(Battery, BatteryState)`;
- `(SolarPanel, SolarPanelState)`;
- `(House, HouseState)`.

### DSL Mancante Per La GUI

Per collegare davvero la schermata di configurazione alla simulazione serviranno
ancora:

- definizione di scenario completo;
- catalogo di scenari predefiniti;
- builder della topologia;
- builder dei cavi;
- builder dell'ambiente;
- builder o factory dello stato iniziale della simulazione;
- conversione degli errori DSL in messaggi mostrabili dalla GUI.

Finche' questi pezzi non sono pronti, la GUI deve usare un repository statico di
scenari finti o descrittivi.

### Observable Mancante

L'Observable non serve per la prima schermata di configurazione.

Servira' solo dopo lo start, quando si passera' alla schermata di simulazione.
Per questo motivo nella prima fase non bisogna implementare una dashboard
real-time.

## Flusso Della Prima Schermata

Flusso desiderato:

```text
Avvio GUI
-> carica lista preset
-> utente seleziona scenario
-> view mostra descrizione e preview
-> utente imposta parametri minimi
-> utente preme Start
-> controller crea ScenarioRunConfig
-> loader prova a caricare lo scenario
-> se ok: passa la configurazione al passo successivo
-> se errore: mostra messaggio
```

Per ora il passo successivo puo' essere:

- un placeholder;
- un mock;
- una chiamata vuota;
- una futura navigazione verso dashboard.

## Layout Minimo

Layout consigliato:

```text
+--------------------------------------------------+
| GridSim                                          |
+----------------------+---------------------------+
| Scenari              | Dettagli scenario          |
|                      |                           |
| [ ] Base             | Nome                      |
| [ ] Batterie         | Descrizione breve         |
| [ ] Sovraccarico     |                           |
|                      | Nodi: 4                   |
|                      | Cavi: 3                   |
|                      | Note: ...                 |
+----------------------+---------------------------+
| Tick duration: [ 15 ] minuti                     |
+--------------------------------------------------+
|                                      [ Start ]   |
+--------------------------------------------------+
| Messaggi / errori                                |
+--------------------------------------------------+
```

Questo e' sufficiente per una demo iniziale.

## Package Consigliati

Per questa GUI conviene usare una struttura MVC semplice. Evitare per ora
sottopackage come `configuration` e `simulation`: sarebbero piu' ordinati in un
progetto grande, ma qui rischiano di appesantire una GUI che deve restare
secondaria rispetto al core.

Struttura consigliata:

```text
org.gridsim.gui
├── app
│   └── GuiApp
├── model
│   ├── ScenarioPreset
│   ├── ScenarioPresetId
│   ├── ScenarioPreview
│   └── ScenarioRunConfig
├── view
│   ├── ScenarioSelectionView
│   ├── ScenarioPresetListView
│   ├── ScenarioDetailsView
│   └── RunConfigurationView
├── controller
│   └── ScenarioSelectionController
├── ports
│   ├── ScenarioPresetRepository
│   ├── ScenarioPresetLoader
│   └── ScenarioStartHandler
└── mock
    ├── InMemoryScenarioPresetRepository
    └── MockScenarioPresetLoader
```

In futuro, se verra' aggiunta una schermata di simulazione, si potranno
aggiungere nuovi file negli stessi package:

```text
model/GuiState.scala
view/SimulationView.scala
controller/SimulationController.scala
ports/GuiStateSource.scala
```

Non serve creare ora package dedicati a dashboard, grafici o observable.

## Ruolo Dei Package MVC

### `app`

Contiene l'entry point grafico e compone le dipendenze.

Responsabilita':

- creare repository;
- creare loader;
- creare controller;
- creare view;
- collegare view e controller.

### `model`

Contiene solo dati letti o prodotti dalla GUI.

Per la prima schermata:

- `ScenarioPresetId`;
- `ScenarioPreset`;
- `ScenarioPreview`;
- `ScenarioRunConfig`;

Questi modelli non devono dipendere dal DSL.

### `view`

Contiene componenti JavaFX/ScalaFX.

Responsabilita':

- mostrare lista scenari;
- mostrare dettagli scenario;
- mostrare input per tick duration;
- mostrare pulsante `Start`;
- mostrare errori.

La view deve essere passiva: inoltra eventi al controller e renderizza dati.

### `controller`

Contiene la logica della schermata.

Responsabilita':

- caricare preset;
- gestire scenario selezionato;
- validare input minimi;
- creare `ScenarioRunConfig`;
- chiamare il loader;
- notificare errori o successo alla view.

### `ports`

Contiene le interfacce verso codice non ancora stabile o non appartenente alla
GUI.

Per ora:

- `ScenarioPresetRepository`;
- `ScenarioPresetLoader`;
- `ScenarioStartHandler`.

Queste porte permettono di iniziare con mock e collegare il DSL reale dopo.

### `mock`

Contiene implementazioni temporanee.

Per ora:

- `InMemoryScenarioPresetRepository`;
- `MockScenarioPresetLoader`.

Queste classi servono a rendere usabile la schermata prima che DSL e runner
siano pronti.

## Modelli Necessari

### `ScenarioPresetId`

Identificatore stabile dello scenario.

```scala
case class ScenarioPresetId(value: String)
```

Esempi di id:

```text
base-neighborhood
solar-production
battery-storage
cable-overload
```

### `ScenarioPreset`

Modello letto dalla schermata di configurazione.

```scala
case class ScenarioPreset(
    id: ScenarioPresetId,
    name: String,
    description: String,
    preview: ScenarioPreview
)
```

Questo modello non deve contenere direttamente builder DSL.

### `ScenarioPreview`

Preview statica e leggera.

```scala
case class ScenarioPreview(
    nodeCount: Int,
    cableCount: Int,
    highlights: Seq[String]
)
```

Esempio:

```scala
ScenarioPreview(
  nodeCount = 4,
  cableCount = 3,
  highlights = Seq(
    "2 houses",
    "1 solar array",
    "1 external grid"
  )
)
```

### `ScenarioRunConfig`

Configurazione prodotta dalla schermata quando l'utente preme `Start`.

```scala
case class ScenarioRunConfig(
    presetId: ScenarioPresetId,
    tickDurationMinutes: Int
)
```

Per ora evitare campi aggiuntivi se non sono davvero supportati dal runner.

## Porte Necessarie

### `ScenarioPresetRepository`

Serve a ottenere gli scenari mostrati nella lista.

```scala
trait ScenarioPresetRepository:
  def allPresets(): Seq[ScenarioPreset]
  def findById(id: ScenarioPresetId): Option[ScenarioPreset]
```

Prima implementazione:

```text
InMemoryScenarioPresetRepository
```

Questa implementazione puo' contenere scenari statici, senza usare il DSL.

### `ScenarioPresetLoader`

Serve a caricare lo scenario quando l'utente preme `Start`.

```scala
trait ScenarioPresetLoader[A]:
  def load(config: ScenarioRunConfig): Either[String, A]
```

Nella prima versione `A` puo' essere un placeholder.

Quando il DSL sara' completo, `A` potra' diventare:

- stato iniziale della simulazione;
- configurazione accettata dal runner;
- tipo intermedio definito dal modulo scenario.

## Controller

Il controller della schermata deve essere sottile.

Responsabilita':

- caricare i preset dal repository;
- mantenere lo scenario selezionato;
- validare `tickDurationMinutes`;
- creare `ScenarioRunConfig`;
- chiamare `ScenarioPresetLoader`;
- comunicare alla view errori o successo.

Interfaccia concettuale:

```scala
trait ScenarioSelectionController:
  def loadPresets(): Unit
  def selectScenario(id: ScenarioPresetId): Unit
  def updateTickDuration(minutes: Int): Unit
  def startSelectedScenario(): Unit
```

Il controller non deve:

- costruire manualmente case, batterie o pannelli;
- conoscere i dettagli del DSL;
- conoscere il futuro Observable;
- avviare direttamente il loop della simulazione, se questo sara' responsabilita'
  del runner.

## View

La view deve essere passiva.

Responsabilita':

- mostrare lista scenari;
- mostrare dettagli scenario selezionato;
- mostrare input per tick duration;
- abilitare/disabilitare `Start`;
- mostrare errori;
- inoltrare eventi al controller.

Interfaccia concettuale:

```scala
trait ScenarioSelectionView:
  def renderPresets(presets: Seq[ScenarioPreset]): Unit
  def renderSelectedPreset(preset: ScenarioPreset): Unit
  def renderValidationError(message: String): Unit
  def clearValidationError(): Unit
```

La view non deve leggere direttamente dal repository.

## Dipendenze Da Rispettare

Regola principale:

```text
view -> controller -> ports
```

I model possono essere usati da tutti i livelli GUI.

Da evitare:

```text
view -> DSL
view -> runner
view -> observable
view -> repository concreto
```

Il package `app` e' l'unico punto in cui e' normale istanziare implementazioni
concrete e collegarle tra loro.

## Preset Iniziali Consigliati

Per iniziare bastano 3 scenari statici:

### Base

Scenario semplice per verificare che il sistema parta.

```text
id: base-neighborhood
name: Quartiere base
preview:
  nodes: 3-4
  cables: 2-3
  highlights:
    - 2 houses
    - 1 external grid
    - balanced demand
```

### Produzione Solare

Scenario utile per mostrare produzione locale.

```text
id: solar-production
name: Produzione solare
preview:
  nodes: 4-5
  cables: 3-4
  highlights:
    - houses with solar panels
    - daytime production
    - external grid exchange
```

### Sovraccarico

Scenario utile per mostrare criticita' sui cavi.

```text
id: cable-overload
name: Sovraccarico cavi
preview:
  nodes: 4-5
  cables: 3-4
  highlights:
    - constrained cable capacity
    - high demand
    - overload expected
```

Questi preset possono essere solo descrittivi finche' il DSL di scenario non e'
pronto.

## Validazioni Minime

La schermata deve validare solo cio' che controlla direttamente:

- deve essere selezionato uno scenario;
- `tickDurationMinutes` deve essere maggiore di zero;
- `tickDurationMinutes` deve stare in un range ragionevole, ad esempio 1-1440.

Non deve validare:

- correttezza della topologia;
- capacita' dei cavi;
- parametri fisici;
- consistenza di batterie o pannelli.

Queste validazioni appartengono al DSL/core.

## Integrazione Futura Con DSL

Quando il DSL sara' completo, la GUI non dovra' cambiare molto.

Il collegamento corretto sara':

```text
ScenarioRunConfig
-> ScenarioPresetLoader
-> ScenarioCatalog
-> DSL scenario
-> build/validate
-> initial simulation state
```

Contratto utile lato DSL:

```scala
trait ScenarioDefinition[A]:
  def id: ScenarioPresetId
  def name: String
  def description: String
  def preview: ScenarioPreview
  def build(config: ScenarioRunConfig): ValidatedNec[DSLError, A]
```

La GUI deve continuare a conoscere solo:

- `ScenarioPreset`;
- `ScenarioRunConfig`;
- messaggi di errore gia' formattati come `String`.

## Integrazione Futura Con Observable

L'Observable non serve alla schermata di configurazione.

Servira' dopo lo start, quando esistera' una schermata di simulazione.

Per prepararsi senza implementarla ora:

- non far dipendere `ScenarioSelectionView` dall'Observable;
- non far dipendere `ScenarioSelectionController` dagli observer;
- fare in modo che `ScenarioPresetLoader` restituisca un valore che potra' essere
  passato al futuro runner;
- lasciare la navigazione post-start dietro una piccola astrazione.

Esempio:

```scala
trait ScenarioStartHandler[A]:
  def onScenarioLoaded(loaded: A): Unit
```

In futuro questa astrazione potra':

- aprire la dashboard;
- creare il runner;
- iscrivere la GUI all'Observable;
- avviare la simulazione.

## Roadmap Operativa

### Fase 1 - Modelli

Creare:

- `ScenarioPresetId`;
- `ScenarioPreset`;
- `ScenarioPreview`;
- `ScenarioRunConfig`.

Output:

- modelli semplici;
- nessuna dipendenza da JavaFX;
- nessuna dipendenza dal DSL.

### Fase 2 - Repository Statico

Creare:

- `ScenarioPresetRepository`;
- `InMemoryScenarioPresetRepository`.

Output:

- lista di 3 preset statici;
- testabile senza GUI;
- modificabile facilmente quando arrivera' il catalogo DSL.

### Fase 3 - Loader Placeholder

Creare:

- `ScenarioPresetLoader[A]`;
- `MockScenarioPresetLoader`.

Output:

- il pulsante `Start` puo' gia' chiamare qualcosa;
- gli errori possono gia' essere mostrati;
- nessuna simulazione reale richiesta.

### Fase 4 - Controller

Creare:

- `ScenarioSelectionController`;
- una implementazione concreta minima.

Output:

- gestione selezione;
- gestione tick duration;
- creazione `ScenarioRunConfig`;
- chiamata al loader;
- gestione errori.

### Fase 5 - View

Creare:

- `ScenarioSelectionView`;
- lista preset;
- pannello dettagli;
- input tick duration;
- pulsante `Start`;
- area messaggi.

Output:

- prima schermata usabile;
- nessun editor scenario;
- nessuna dashboard.

### Fase 6 - Collegamento A DSL Reale

Da fare solo quando il DSL di scenario sara' pronto.

Sostituire:

```text
MockScenarioPresetLoader
```

con:

```text
DslScenarioPresetLoader
```

Output:

- selezione scenario realmente collegata al DSL;
- errori DSL mostrati nella schermata.

## Definition Of Done Della Prima Schermata

La prima schermata e' completa quando:

- mostra almeno 3 scenari;
- permette di selezionare uno scenario;
- mostra descrizione e preview;
- permette di impostare `tickDurationMinutes`;
- disabilita o segnala errore se la configurazione non e' valida;
- al click su `Start` produce un `ScenarioRunConfig`;
- usa un loader dietro interfaccia;
- non dipende direttamente da DSL incompleto;
- non dipende da Observable;
- non permette editing custom dello scenario.

## Regola Finale

Per ora la GUI deve rispondere a una sola domanda:

```text
Quale scenario predefinito vuoi avviare?
```

Tutto cio' che non serve a rispondere a questa domanda va rimandato.
