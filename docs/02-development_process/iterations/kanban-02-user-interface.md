# Kanban — Osservabilità e controllo della simulazione

## Collocazione nel flusso

Questa seconda finestra descrittiva riunisce i work package che rendono accessibile e controllabile il nucleo della simulazione. Il lavoro comprende la distribuzione degli aggiornamenti, la configurazione e l'avvio degli scenari e i comandi per governarne l'esecuzione.

Il periodo non costituisce uno sprint. Le attività sono state prese in carico progressivamente, quando il nucleo della simulazione ha reso disponibili le dipendenze necessarie.

## Work package completati

| ID | Tipo | Work package | Assegnatario |
|---|---|---|---|
| #445 | Task | Observability simulazione | Matteo Bambini |
| #457 | Task | Interfaccia di configurazione e avvio della simulazione | Michele Nardini |
| #462 | Task | Toolbar controllo simulazione | Michele Nardini |

Tutti i work package elencati risultano chiusi e avevano priorità normale.

## Evoluzione del lavoro

L'osservabilità (`#445`) ha introdotto il meccanismo necessario a distribuire gli aggiornamenti prodotti dal runner verso componenti esterni. Su questo collegamento è stata costruita l'interfaccia di configurazione e avvio (`#457`), che ha costituito il primo punto di accesso grafico agli scenari definiti dal sistema.

Il flusso è quindi proseguito con la toolbar di controllo (`#462`), che ha raccolto i comandi necessari a governare il ciclo di vita della simulazione.

## Risultato della fase

Al termine della fase gli aggiornamenti della simulazione potevano essere osservati da altri componenti e l'utente poteva configurare e avviare uno scenario, quindi controllarne l'esecuzione dalla toolbar. Questo incremento ha posto le basi architetturali per la successiva visualizzazione della rete e per la comunicazione con il modulo delle statistiche.

[Fase Kanban precedente](kanban-01-simulation-core.md) · [Processo di sviluppo](../02-development_process.md) · [Fase Kanban successiva](kanban-03-interface-statistics.md)
