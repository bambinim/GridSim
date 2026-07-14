# Kanban — Osservabilità e accesso alla simulazione

## Collocazione nel flusso

Questa seconda finestra descrittiva riunisce i work package che collegano il nucleo della simulazione ai suoi utilizzatori. Il lavoro comprende l'osservabilità, il primo accesso grafico agli scenari, la visualizzazione della topologia e la configurazione temporale dell'esecuzione. Gli aggiornamenti sono distribuiti tra il **30 giugno e l'11 luglio 2026**, ma il raggruppamento è determinato soprattutto dalla loro funzione comune.

Il periodo non costituisce uno sprint. Le attività sono state prese in carico progressivamente, quando il nucleo della simulazione ha reso disponibili le dipendenze necessarie.

## Work package completati

| ID | Tipo | Work package | Assegnatario | Ultimo aggiornamento |
|---|---|---|---|---|
| #445 | Task | Observability simulazione | Matteo Bambini | 30 giugno 2026, 09:45 |
| #457 | Task | Interfaccia di configurazione e avvio della simulazione | Michele Nardini | 30 giugno 2026, 09:53 |
| #460 | Task | Pannello grafo simulazione | Matteo Bambini | 10 luglio 2026, 18:24 |
| #463 | Task | Rendere configurabile il delta time tra gli step | Michele Nardini | 11 luglio 2026, 12:07 |
| #464 | Task | Aumentare la granularità del tick delta almeno ai secondi | Michele Nardini | 11 luglio 2026, 13:13 |

Tutti i work package elencati risultano chiusi e avevano priorità normale.

## Evoluzione del lavoro

L'osservabilità (`#445`) ha introdotto il meccanismo necessario a distribuire gli aggiornamenti prodotti dal runner verso componenti esterni. Su questo collegamento è stata costruita l'interfaccia di configurazione e avvio (`#457`), che ha costituito il primo punto di accesso grafico agli scenari definiti dal sistema.

Il flusso è quindi proseguito con il pannello del grafo (`#460`), che ha reso visibile la topologia della rete e ha posto le basi per mostrare lo stato delle entità e dei collegamenti durante l'esecuzione. La configurazione del delta tra gli step (`#463`) e l'aumento della sua granularità almeno ai secondi (`#464`) hanno completato le possibilità iniziali di configurazione della simulazione.

La distanza temporale tra gli aggiornamenti mostra come il processo Kanban non imponesse una cadenza uniforme: i work package avanzavano in base alle dipendenze, alla complessità e alla disponibilità dell'assegnatario, mantenendo continuità sulla lavagna fino alla review e alla chiusura.

## Risultato della fase

Al termine della fase gli aggiornamenti della simulazione potevano essere osservati da altri componenti e l'utente poteva configurare il tick, avviare uno scenario e visualizzarne la rete attraverso un grafo. Questo incremento ha preparato l'aggiunta dei dettagli interattivi, dei controlli del ciclo di vita e delle statistiche.

[Fase Kanban precedente](kanban-01-simulation-core.md) · [Processo di sviluppo](../02-development_process.md) · [Fase Kanban successiva](kanban-03-interface-statistics.md)
