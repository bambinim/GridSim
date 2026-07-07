# Design Architetturale

## 1. Obiettivi e criteri di progettazione

GridSim è un simulatore a tempo discreto per micro-grid energetiche. Il sistema modella una rete come un grafo di entità energetiche e collegamenti fisici, configura scenari tramite una DSL embedded in Scala ed espone una GUI desktop per avviare, controllare e osservare l’esecuzione.

L’architettura è stata definita per soddisfare quattro qualità principali:

- **correttezza e testabilità del calcolo simulativo**, ottenute separando le regole energetiche dagli effetti collaterali;
- **manutenibilità**, tramite responsabilità coese e contratti espliciti tra i sottosistemi;
- **estendibilità**, in particolare verso nuovi tipi di entità, strategie di evoluzione e algoritmi di power flow;
- **reattività della GUI**, evitando che la pianificazione dei tick o la distribuzione degli aggiornamenti blocchino il thread di presentazione.

Il progetto è distribuito come una singola applicazione Gradle, ma la struttura dei package identifica confini architetturali distinti: dominio e simulazione, DSL, runtime, osservabilità e interfaccia grafica. Il capitolo descrive tali confini ad alto livello; i tipi concreti, le formule energetiche e i dettagli algoritmici sono trattati nel design di dettaglio.

---

## 2. Stile architetturale adottato

L’architettura di GridSim non coincide con un unico pattern. È più accurato descriverla come una combinazione di quattro scelte complementari:

1. **architettura domain-centred con Functional Core, Imperative Shell** per il motore di simulazione;
2. **Publish-Subscribe Event-Driven** per distribuire gli aggiornamenti della simulazione;
3. **MVVM** per la GUI ScalaFX.

Questa combinazione è particolarmente adatta a un simulatore: il calcolo energetico deve restare deterministico e verificabile, mentre timer, concorrenza, DSL e GUI devono poter evolvere senza alterare le regole del dominio.

### 2.1 Functional Core, Imperative Shell

Il nucleo del sistema è una transizione di stato discreta:

$$S_{t+1} = step(S_t)$$

Lo stato della simulazione è immutabile: un tick non modifica lo snapshot ricevuto, ma costruisce il successivo. La parte funzionale del sistema comprende il modello della rete, l’ambiente, gli stati dinamici delle entità, le regole di evoluzione e il calcolo dei flussi. Essa è quindi indipendente da GUI, thread, timer e I/O.

La shell imperativa gestisce invece gli aspetti necessariamente effectful: avvio, pausa e arresto della simulazione; pianificazione periodica dei tick; conservazione dello snapshot corrente; pubblicazione degli aggiornamenti e trasferimento di tali aggiornamenti nel thread JavaFX.

Questa separazione è ideale per GridSim perché consente di verificare il comportamento della simulazione come una normale funzione: dato uno scenario iniziale, il risultato di ogni tick è riproducibile e testabile senza avviare la GUI o risorse concorrenti.

### 2.2 Event-driven publish-subscribe

Al termine di ogni tick, il runtime pubblica uno snapshot aggiornato tramite un dispatcher di osservabilità. Gli osservatori possono richiedere dati granulari — ambiente, stati delle entità, flussi o carichi dei cavi — oppure uno snapshot sincronizzato completo.

Il meccanismo implementa un pattern **Observer / Publish-Subscribe**: il produttore dello stato non conosce i consumatori specifici. La GUI è quindi solo uno dei possibili destinatari; lo stesso canale può supportare in futuro analytics, persistenza della storia, esportazione o monitoraggio esterno.

Questo non costituisce Event Sourcing: gli eventi sono notifiche dello stato corrente e non un log persistito usato per ricostruire la simulazione.

### 2.3 MVVM

La GUI non adotta MVC classico, ma il pattern **Model-View-ViewModel (MVVM)**. Le View ScalaFX dichiarano layout e binding; i ViewModel espongono proprietà osservabili e comandi legati alla presentazione; il Model è rappresentato dagli oggetti di dominio e dalla sessione di simulazione attiva.

Un coordinator collega lo stream degli aggiornamenti proveniente dal runtime ai diversi ViewModel della dashboard. Esso centralizza il passaggio dal contesto asincrono al thread JavaFX e impedisce che ciascuna View debba conoscere dettagli di concorrenza o osservabilità.

---

## 3. Vista architetturale di contesto

Il diagramma seguente mostra i principali sottosistemi e il flusso delle responsabilità.

```mermaid
flowchart LR
    User[Utente]

    subgraph Presentation[Presentazione]
        GUI[GUI ScalaFX<br/>View, ViewModel e Coordinator]
        Navigation[Composizione e navigazione<br/>delle schermate]
    end

    subgraph Configuration[Configurazione degli scenari]
        ScenarioPorts[Porte per catalogo e caricamento<br/>degli scenari]
        DSL[DSL embedded e catalogo<br/>di scenari]
    end

    subgraph Runtime[Runtime della simulazione]
        Controller[Controllo del ciclo di vita<br/>start, pause, step e stop]
        Scheduler[Scheduler temporale]
        Dispatcher[Dispatcher di osservabilità<br/>publish-subscribe]
    end

    subgraph Domain[Core di dominio e simulazione]
        Model[Modello e stato immutabili<br/>della simulazione]
        Engine[Motore di simulazione<br/>transizione di un tick]
        Evolution[Regole di evoluzione<br/>delle entità]
        Solver[Strategia di power flow]
    end

    User -->|seleziona scenario e invia comandi| GUI
    GUI --> Navigation
    Navigation --> ScenarioPorts
    ScenarioPorts --> DSL
    DSL -->|modello statico e stato iniziale| Navigation

    GUI -->|comandi di ciclo di vita| Controller
    Controller --> Scheduler
    Controller -->|snapshot corrente| Engine
    Engine --> Model
    Engine --> Evolution
    Engine --> Solver
    Engine -->|nuovo snapshot| Controller

    Controller -->|aggiornamenti| Dispatcher
    Dispatcher -->|snapshot sincronizzato| GUI
```

Il diagramma evidenzia una distinzione fondamentale. La GUI, la DSL, lo scheduler e il dispatcher appartengono ai bordi dell’applicazione; il motore e il dominio energetico occupano il centro. Il flusso dei comandi procede dall’utente verso il core, mentre il flusso dei dati aggiornati procede dal core verso la GUI attraverso il canale di osservabilità.

---

## 4. Componenti principali e scambio dei dati

| Componente | Responsabilità architetturale | Dati in ingresso | Dati prodotti / collaborazioni |
|---|---|---|---|
| **Presentazione e navigazione** | Avvia l’applicazione, mantiene la schermata corrente e compone le dipendenze della GUI. | Eventi di navigazione e sessioni di simulazione create. | Selezione dello scenario o dashboard della simulazione. |
| **Configurazione degli scenari** | Espone gli scenari disponibili, valida la richiesta dell’utente e costruisce una simulazione iniziale. | Identificativo del preset e durata simulata del tick. | `SimulationModel` e `SimulationState` iniziali, oppure errori di configurazione. |
| **DSL** | Offre una sintassi specifica del dominio per descrivere entità, componenti, topologia e tempo simulato. | Dichiarazioni degli scenari. | Configurazione validata della micro-grid. |
| **Runtime e controller** | Gestisce il ciclo di vita effectful della sessione: avvio, pausa, ripresa, avanzamento singolo e arresto. | Comandi della GUI e stato corrente. | Invocazione periodica del motore e pubblicazione dello stato aggiornato. |
| **Motore di simulazione** | Esegue una transizione completa da uno snapshot discreto al successivo. | Stato corrente, modello statico e strategie configurate. | Nuovo stato immutabile della simulazione. |
| **Evoluzione delle entità** | Risolve il comportamento locale di case, produttori e sistemi di accumulo. | Stato dinamico, entità statica, ambiente e durata del tick. | Stato locale aggiornato e flusso energetico netto dell’entità. |
| **Power-flow solver** | Determina il carico trasferito sui cavi a partire dai flussi netti dei nodi e dalla topologia. | Flussi delle entità e grafo della micro-grid. | Mappa dei carichi sui cavi. |
| **Osservabilità** | Disaccoppia l’esecuzione della simulazione dai suoi consumatori. | Nuovo snapshot della simulazione. | Eventi tipizzati o snapshot sincronizzati per GUI e futuri observer. |
| **GUI** | Traduce i dati di dominio in stato di presentazione e rende disponibili i comandi dell’utente. | Snapshot e stato del controller. | Proprietà osservabili, comandi e aggiornamento delle View. |

### 4.1 Contratti informativi fondamentali

La separazione tra dati statici e dinamici è il principale contratto interno del sistema:

- **`SimulationModel`** descrive gli elementi invarianti durante una sessione: grafo della micro-grid, entità, cavi e durata simulata di un tick.
- **`SimulationState`** descrive uno snapshot dinamico: ambiente e tempo simulato, stati delle entità, flussi netti e carichi dei cavi.
- **`SimulationSnapshot`** è la vista coerente dei dati emessa verso gli observer alla fine di un tick.

Il grafo della micro-grid è formato da nodi energetici e cavi. I cavi rappresentano connessioni non orientate con una capacità massima, mentre la rete esterna funge da nodo di bilanciamento. Questo modello consente di rappresentare sia reti radiali sia, mediante un solver appropriato, topologie magliate.

La distinzione tra modello e stato evita che la configurazione della rete venga modificata durante l’esecuzione. Inoltre rende naturale conservare o confrontare snapshot diversi, poiché ciascun tick produce un nuovo valore senza distruggere quello precedente.

---

## 5. Ciclo di vita della simulazione

La sequenza seguente descrive il flusso principale, dal caricamento di uno scenario all’aggiornamento della dashboard.

```mermaid
sequenceDiagram
    participant U as Utente
    participant V as GUI MVVM
    participant C as Configurazione / DSL
    participant R as Controller runtime
    participant E as Motore di simulazione
    participant O as Osservabilità

    U->>V: seleziona preset e durata del tick
    V->>C: richiesta di caricamento scenario
    C-->>V: modello statico e stato iniziale
    V->>R: avvio, pausa, step o stop

    loop per ogni tick attivo
        R->>E: esegui transizione sullo stato corrente
        E->>E: avanza ambiente ed evolve entità
        E->>E: calcola i flussi e i carichi dei cavi
        E-->>R: nuovo stato immutabile
        R->>O: pubblica aggiornamento
        O-->>V: snapshot sincronizzato
        V->>V: aggiorna proprietà osservabili e vista
    end
```

Nel caricamento dello scenario, la DSL crea un modello statico e lo stato iniziale coerente con esso. La GUI non costruisce direttamente le entità di dominio: si limita a richiedere il caricamento attraverso le porte dedicate e a gestire eventuali errori di validazione.

Durante l’esecuzione, il controller mantiene lo stato della sessione e decide quando richiedere un nuovo tick. Il motore rimane invece focalizzato sulla transizione deterministica: aggiorna l’ambiente, evolve le entità e calcola il power flow. Terminato il calcolo, il runtime pubblica i dati aggiornati senza introdurre una dipendenza diretta tra motore e GUI.

---

## 6. Core di dominio e strategie di calcolo

Il core rappresenta una micro-grid come un grafo di entità energetiche. Le abitazioni possono incapsulare componenti locali, quali produzione fotovoltaica e accumulo; i produttori e gli accumulatori possono inoltre partecipare al bilancio della rete secondo il modello configurato.

L’evoluzione delle entità è separata dalla topologia della rete. Ogni entità determina innanzitutto il proprio flusso energetico residuo in funzione dello stato, dell’ambiente e della durata del tick. Il motore raccoglie tali flussi e li passa al solver, che è responsabile della distribuzione sulle connessioni fisiche.

La separazione tra evoluzione locale e power flow rappresenta una scelta importante per la manutenibilità:

- le regole di consumo, produzione e accumulo evolvono senza modificare l’algoritmo di rete;
- gli algoritmi di rete possono essere sostituiti senza riscrivere le entità;
- gli scenari DSL possono cambiare topologia senza alterare la logica della GUI.

Il sistema prevede infatti più strategie di power flow. Una strategia semplice è orientata a reti radiali e aggrega i flussi lungo un albero radicato nella rete esterna. Una seconda strategia applica un modello DC basato sulle leggi di Kirchhoff, permettendo di distribuire i flussi anche su topologie magliate. La scelta dell’algoritmo è quindi un punto di estensione architetturale, non una decisione incorporata nella GUI o nel ciclo di vita della simulazione.

---

## 7. Architettura della GUI

La GUI ScalaFX è organizzata secondo MVVM, scelto perché separa in modo naturale il rendering grafico dallo stato di presentazione e sfrutta il meccanismo di binding delle proprietà osservabili.

```mermaid
classDiagram                                                                       
        %% Stili e classificazione dei moduli                                          
        class GuiApp {                                                                 
          <<Entry Point>>                                                              
          +start()                                                                     
        }                                                                              
                                                                                       
        class AppRouter {                                                              
          <<Router>>                                                                   
          -state: AppState                                                             
          -rootPane: BorderPane                                                        
          +dispatch(event: AppEvent)                                                   
        }                                                                              
                                                                                       
        class SceneBuilder {                                                           
          <<Factory>>                                                                  
          +render(route: Route, dispatch: AppEvent => Unit) Parent                     
        }                                                                              
                                                                                       
        class ScenarioSelectionView {                                                  
          <<View>>                                                                     
          -scenariosList: ListView                                                     
          -tickField: TextField                                                        
          -startButton: Button                                                         
        }                                                                              
                                                                                       
        class ScenarioSelectionViewModel {                                             
          <<ViewModel>>                                                                
          +scenariosNames: ObservableBuffer                                            
          +tickDurationText: StringProperty                                            
          +isStartDisabled: BooleanProperty                                            
          +startScenario() Option[RunningSimulation]                                   
        }                                                                              
                                                                                       
        class SimulationView {                                                         
          <<View>>                                                                     
          -summaryView: SimulationSummaryView                                          
          -entityDetailsView: EntityDetailsView                                        
          -controlView: SimulationControlView                                          
          -graphPlaceholder: BorderPane                                                
        }                                                                              
                                                                                       
        class SimulationCoordinator {                                                  
          <<Coordinator / Presenter>>                                                  
          +selectedEntity: ObjectProperty[Selection]                                   
          +summaryViewModel: SimulationSummaryViewModel                                
          +entityDetailsViewModel: EntityDetailsViewModel                              
          +controlViewModel: SimulationControlViewModel                                
          +renderCurrent()                                                             
          -updateWith(env, states, flows)                                              
        }                                                                              
                                                                                       
        class SimulationSummaryView {                                                  
          <<View>>                                                                     
          -netFlowLabel: Label                                                         
          -simHours: Label                                                             
        }                                                                              
                                                                                       
        class SimulationSummaryViewModel {                                             
          <<ViewModel>>                                                                
          +netFlowText: StringProperty                                                 
          +timeText: StringProperty                                                    
          +update(flows, env, ctrlState)                                               
        }                                                                              
                                                                                       
        class EntityDetailsView {                                                      
          <<View>>                                                                     
          -contentContainer: VBox                                                      
          -render(state: DetailsEntity)                                                
        }                                                                              
                                                                                       
        class EntityDetailsViewModel {                                                 
          <<ViewModel>>                                                                
          +detailsEntityProperty: ReadOnlyObjectProperty                               
          +update(states, flows, env)                                                  
        }                                                                              
                                                                                       
        class SimulationControlView {                                                  
          <<View>>                                                                     
          -statusLabel: Label                                                          
          -playPauseButton: Button                                                     
          -stepButton: Button                                                          
        }                                                                              
                                                                                       
        class SimulationControlViewModel {                                             
          <<ViewModel>>                                                                
          +playPauseText: StringProperty                                               
          +statusText: StringProperty                                                  
          +togglePlayPause()                                                           
          +stepOnce()                                                                  
        }                                                                              
                                                                                       
        class DetailDispatcher {                                                       
          <<Extractor Adapter>>                                                        
          +resolve(selection, states, flows, env) ExtractedSelectionDetails            
          +resolveEntity(entity, state, env) ExtractedEntityDetails                    
        }                                                                              
                                                                                       
        class RunningSimulation {                                                      
          <<Core Bridge>>                                                              
          +model: SimulationModel                                                      
          +controller: SimulationController                                            
          +snapshotEvents: Stream[IO, SimulationState]                                 
        }                                                                              
                                                                                       
        %% Relazioni strutturali                                                       
        GuiApp --> AppRouter : inizializza                                             
        AppRouter --> SceneBuilder : delega rendering                                  
        SceneBuilder ..> ScenarioSelectionView : istanzia                              
        SceneBuilder ..> SimulationView : istanzia                                     
                                                                                       
        %% MVVM Scenario Selection                                                     
        ScenarioSelectionView --> ScenarioSelectionViewModel : si lega a (Binding)     
        ScenarioSelectionViewModel --> ScenarioSelectionView : notifica modifiche      
                                                                                       
        %% MVVM Active Simulation                                                      
        SimulationView --> SimulationCoordinator : referenzia                          
        SimulationView --> SimulationSummaryView : contiene                            
        SimulationView --> EntityDetailsView : contiene                                
        SimulationView --> SimulationControlView : contiene                            
                                                                                       
        SimulationCoordinator --> SimulationSummaryViewModel : aggiorna                
        SimulationCoordinator --> EntityDetailsViewModel : aggiorna                    
        SimulationCoordinator --> SimulationControlViewModel : aggiorna                
                                                                                       
        SimulationSummaryView --> SimulationSummaryViewModel : data binding            
        EntityDetailsView --> EntityDetailsViewModel : data binding                    
        SimulationControlView --> SimulationControlViewModel : data binding            
  
        %% Collegamenti con il Core e Threading
        SimulationCoordinator --> RunningSimulation : consuma Stream (Background)      
        SimulationCoordinator ..> DetailDispatcher : usa per estrarre dettagli         
        EntityDetailsViewModel ..> DetailDispatcher : usa per mappare dati
  
        %% Azioni e Callbacks
        ScenarioSelectionView --> AppRouter : dispatch AppEvent (ScenarioLoaded)       
        SimulationControlViewModel --> RunningSimulation : comanda Controller          
  (Play/Pause/Step)

```

Le responsabilità sono ripartite come segue:

- le **View** dichiarano layout, controlli e binding; non conoscono formule energetiche, scheduling o stream;
- i **ViewModel** mantengono lo stato necessario alla presentazione, validano gli input dell’utente e offrono comandi adatti alla View;
- il **Model** è costituito dai dati del dominio e dalla sessione di simulazione;
- il **Coordinator** riceve gli snapshot dal runtime e aggiorna i ViewModel sul thread della GUI.

Il flusso è prevalentemente unidirezionale. L’utente genera un’intenzione attraverso la View; il ViewModel la inoltra al runtime; il runtime pubblica il nuovo snapshot; il coordinator aggiorna le proprietà osservabili; i binding ScalaFX riflettono automaticamente il nuovo stato nella View.

Questa struttura evita due forme di accoppiamento indesiderato. In primo luogo, le View non invocano direttamente il motore di simulazione. In secondo luogo, i dettagli asincroni non vengono duplicati in ogni pannello grafico. La conseguenza è una GUI più testabile, più semplice da estendere e meno esposta a errori di concorrenza.

Nello stato corrente dell’applicazione, la dashboard espone riepilogo, dettagli delle entità e controlli della simulazione. L’architettura mette già a disposizione flussi e carichi necessari per un renderer dinamico del grafo; l’implementazione completa di tale vista costituisce un’estensione della presentazione senza richiedere modifiche al core.


---

[Sommario](index.md) |
[Capitolo precedente](03-requirements.md) |
[Capitolo successivo](05-detailed_design/05-detailed_design.md)
