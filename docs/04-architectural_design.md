# Design Architetturale

## 1. Divisione in Moduli / Macro-Componenti
La struttura del sistema si articola in quattro macro-componenti principali indipendenti per garantire coesione e basso accoppiamento:

- **Engine (Motore di Simulazione):** Il nucleo del sistema, implementato in modo puramente funzionale. Definisce i modelli delle entità, le regole di transizione di stato ad ogni tick e la monade di stato per gestire l'evoluzione temporale. È un motore totalmente puro: non contiene riferimenti a interfacce grafiche o ad effetti collaterali. Riceve uno stato e restituisce lo stato successivo.
- **GUI (Interfaccia Utente):** Componente basato su JavaFX/ScalaFX per il rendering dello stato della griglia e dei grafici in tempo reale. Gestisce i comandi di controllo (start/pause).
- **DSL (Domain Specific Language):** Un layer sintattico embedded in Scala che espone le primitive per configurare la simulazione. Traduce le dichiarazioni dell'utente nei **modelli delle entità** (case class) e nello `SimulationState` iniziale.

- **Analytics (Statistiche):** Modulo dedicato all'elaborazione e all'interrogazione dei dati estratti dagli stati di simulazione per calcolare le metriche di business (autosufficienza, blackout, ecc.).

## 2. Architettura del Motore (Separazione Dati, Logica e Orchestrazione)
Il design del motore segue rigidamente il principio di separazione tra dati, logica di calcolo e orchestrazione degli effetti.

- **Modelli Dati (Pure Data) e Abbellimenti di Dominio:**
  - *Modelli / Entità:* Rappresentano la struttura statica della rete definita tramite DSL. La topologia della rete è definita come un **Grafo**, in cui i vertici sono le entità della rete (es. case, generatori) e gli archi sono i cavi (con i riferimenti ai nodi connessi e la portata massima).
  - *Stato a runtime:* Rappresenta lo stato dinamico di nodi e cavi in un dato istante (es. livello di carica corrente della batteria, flusso di potenza effettivo su un cavo).
  - *Astrazione Unificata (GridEntity):* Tutti gli elementi di primo livello connessi direttamente alla micro-grid (case, produttori standalone, ecc.) implementano o condividono una stessa astrazione o interfaccia (es. `GridEntity`). Questo permette al ciclo di simulazione di trattarli uniformemente per risolverne il bilancio energetico con un'unica chiamata di funzione.
- **Logica di Dominio (Type Classes & Strategie di Calcolo):**
  Le operazioni matematiche e le formule fisiche sono separate in due livelli:
  - `trait EnergyResolver[A]` (Type Class): per calcolare polimorficamente lo scambio energetico netto di ciascuna tipologia di nodo (es. casa, produttore). È una Type Class stateless e indipendente dal grafo.
  - `trait PowerFlowSolver` (Strategy / Algebra): un'interfaccia pura per il calcolo della distribuzione dei flussi di potenza (load flow) sugli archi del grafo. Accetta la mappa dei bilanci energetici dei nodi e la lista dei cavi, restituendo il carico calcolato per ogni cavo. Questa astrazione permette di scambiare facilmente diversi algoritmi di risoluzione (es. calcolo lineare su albero vs leggi di Kirchhoff su rete magliata).
  Tutte le logiche di calcolo sono pure e indipendenti dalla monade di stato.
- **Orchestration (Tagless Final & State Monad):**
    Il flusso e il sequenziamento delle operazioni ad ogni tick sono astratti tramite algebre in stile **Tagless Final** (trait parametrici rispetto a un costruttore di tipo `F[_]`). L'interprete di queste algebre usa la monade `State[SimulationState, A]` fornita da **Cats** per applicare in modo puramente funzionale le logiche di dominio (risoluzione dei nodi e dei flussi sui cavi) e produrre lo stato successivo.

## 3. Gestione dello Stato e Ciclo Temporale (Simulation Loop)
La simulazione viene modellata come una serie di transizioni di stato pure guidate da un runner asincrono esterno.

- **La Singola Transizione di Stato (Il Tick):**
  Un singolo passo temporale rappresenta una transizione pura da uno stato $S_t$ a uno stato $S_{t+1}$. La transizione viene eseguita dall'Engine tramite una funzione pura `SimulationState => SimulationState` (sotto forma di transizione monadica di `State`), implementando i seguenti passi:
  1. *Aggiornamento dell'Ambiente:* Modifica dell'ora solare e delle condizioni meteorologiche.
  2. *Risoluzione Unificata dei Nodi:* Applicazione della funzione di risoluzione energetica su tutte le `GridEntity` connesse alla rete per ottenere il surplus/deficit di ciascuna tramite la stessa chiamata.
  3. *Risoluzione dei Flussi sui Cavi:* Calcolo dei carichi di potenza transitati su ciascun cavo in base al bilancio dei nodi, delegato all'istanza dell'algebra `PowerFlowSolver` configurata.
  4. *Bilancio Globale:* Somma algebrica dei flussi per calcolare lo scambio netto con la Rete Esterna.

- **Gestione dell'Osservabilità e del Tempo Reale (Simulation Runner):**
  Il motore di simulazione è tenuto puro e privo di effetti collaterali. L'osservabilità e il loop temporale sono gestiti dal **Simulation Runner** (collocato nel modulo asincrono/GUI):
  - Il Runner gestisce lo stato mutabile del tempo reale (avviato, in pausa).
  - In esecuzione, ad ogni intervallo di tempo (es. 1 secondo), il Runner calcola lo stato successivo chiamando la funzione pura dell'Engine: `currentState = Engine.step(currentState)`.
  - Subito dopo il calcolo, il Runner notifica gli **Observer** registrati (es. il controller della GUI per aggiornare i grafici). In questo modo, l'Engine rimane 100% puro e isolato da logiche di I/O o di notifica.

## 4. Architettura dell'Interfaccia Utente (Unidirectional Flow)
L'interfaccia grafica (GUI) adotta un pattern a **Flusso di Dati Unidirezionale (Unidirectional Data Flow)** per interfacciarsi in modo pulito con la natura funzionale dell'Engine:

- **View (Vista):** Sviluppata in JavaFX/ScalaFX, descrive il layout della dashboard (controlli di avvio/pausa, visualizzazione della griglia e grafici di andamento temporale). La vista è passiva: visualizza i dati dello stato corrente e delega le interazioni dell'utente al Controller.
- **Controller:** Gestisce le interazioni (pressione del tasto Play, Pause) e controlla l'istanza del *Simulation Runner*.
- **Simulation Runner (Lo "Stato Mutabile" dell'applicazione):** È l'unico punto in cui risiede lo stato mutabile a runtime (il riferimento alla simulazione corrente e lo stato di pausa/play). 
- **Flusso dei Dati:**
  1.  L'utente interagisce con la View (es. preme "Start").
  2.  Il Controller comanda al Runner di avviare il timer asincrono.
  3.  Ad ogni tick del timer, il Runner invia il vecchio `SimulationState` all'Engine, che calcola il nuovo `SimulationState` in modo puro.
  4.  Il Runner riceve il nuovo stato e notifica il Controller (Observer).
  5.  Il Controller aggiorna i nodi della View con le nuove metriche (aggiornando i grafici e lo stato delle batterie in tempo reale).

[Sommario](index.md) |
[Capitolo precedente](03-requirements.md) |
[Capitolo successivo](05-detailed_design/05-detailed_design.md)
