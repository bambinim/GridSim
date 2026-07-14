# Kanban — Nucleo della simulazione

## Collocazione nel flusso

Questa prima finestra descrittiva del periodo Kanban raccoglie le attività dedicate alla definizione e all'esecuzione del nucleo della simulazione, sviluppate dopo la prima iterazione Scrum. La suddivisione segue le responsabilità funzionali e le dipendenze tra i work package.

I work package già inclusi nello Sprint 1 non vengono ripetuti. Il documento parte quindi dal lavoro successivo sulle componenti necessarie a rendere eseguibile il dominio precedentemente modellato.

## Work package completati

| ID   | Tipo    | Work package                                     | Assegnatario    |
|------|---------|--------------------------------------------------|-----------------|
| #438 | Task    | DSL                                              | Matteo Bambini  |
| #439 | Feature | Simulation Runner                                | Michele Nardini |
| #450 | Task    | Simulation State                                 | Michele Nardini |
| #451 | Task    | Simulation Model                                 | Michele Nardini |
| #452 | Task    | Simulation execution trait and implementation(s) | Michele Nardini |

Tutti i work package elencati risultano chiusi e avevano priorità normale. La feature `Simulation Runner` aggregava attività più specifiche gestite attraverso i relativi task.

## Evoluzione del lavoro

Il modello costruito durante la fase iniziale è stato trasformato in una simulazione eseguibile distinguendo la configurazione strutturale (`Simulation Model`, `#451`) dallo stato dinamico (`Simulation State`, `#450`). Su queste basi sono stati definiti il contratto di esecuzione e le sue implementazioni (`#452`), confluiti nella feature complessiva del `Simulation Runner` (`#439`).

In parallelo, il DSL (`#438`) ha reso possibile descrivere gli scenari attraverso costrutti dedicati, collegando la configurazione dichiarativa degli scenari al modello eseguibile.

## Risultato della fase

Al termine della fase il sistema poteva costruire gli scenari tramite DSL, rappresentarli attraverso model e state ed eseguirne l'evoluzione con il runner. Queste capacità hanno costituito la base tecnica per le successive attività di osservabilità, configurazione, visualizzazione e analisi.

[Processo di sviluppo](../02-development_process.md) · [Sprint 1](sprint-01.md) · [Fase Kanban successiva](kanban-02-user-interface.md)
