# Kanban — Interfaccia, controllo e statistiche

## Collocazione nel flusso

Questa terza finestra descrittiva raccoglie le attività di completamento dell'interfaccia e di presentazione dei risultati. I work package sono stati aggiornati principalmente il **13 luglio 2026**, ma sono raggruppati per il loro contributo all'esperienza finale dell'utente, non per costruire una scadenza artificiale.

Il periodo termina alla data dell'esportazione dei work package e non rappresenta una deadline Kanban. Le attività sono state completate e revisionate progressivamente, mentre la lavagna veniva aggiornata in funzione dei risultati ottenuti e delle nuove necessità emerse durante l'integrazione.

## Work package completati

| ID | Tipo | Work package | Assegnatario      | Ultimo aggiornamento |
|---|---|---|-------------------|---|
| #461 | Task | Pannello informazioni dettagliate su nodi e archi | Michele Nardini   | 13 luglio 2026, 17:13 |
| #462 | Task | Toolbar controllo simulazione | Michele Nardini   | 13 luglio 2026, 17:13 |
| #456 | Task | Interfaccia visualizzazione simulazione | Enrico Marchionni | 13 luglio 2026, 19:33 |
| #446 | Task | Raccolta statistiche | Enrico Marchionni | 13 luglio 2026, 19:34 |

L'esportazione segnala inoltre il work package `#465`, **Pannello visualizzazione statistiche**, ma i dati forniti non ne riportano stato, assegnatario e data di aggiornamento. Il task viene quindi considerato parte funzionale di questa fase, senza attribuirgli informazioni non verificabili.

Tutti i work package completi di stato riportati nella tabella risultano chiusi e avevano priorità normale.

## Evoluzione del lavoro

Il 13 luglio sono confluite le attività di integrazione dell'interfaccia. Il pannello informativo (`#461`) ha reso consultabili i dettagli di nodi e archi, mentre la toolbar (`#462`) ha raccolto i comandi del ciclo di vita della simulazione. Questi elementi hanno contribuito alla chiusura del work package complessivo relativo alla visualizzazione (`#456`).

Nella stessa giornata è stata aggiornata e chiusa la raccolta delle statistiche (`#446`). Il relativo pannello (`#465`) collega i dati prodotti dal sistema di osservabilità alla loro rappresentazione nell'interfaccia, completando il percorso iniziato con il work package `#445` nella fase precedente.

## Risultato della fase

Al termine del periodo l'utente poteva configurare e avviare uno scenario, controllarne l'esecuzione, modificare la durata simulata di ogni step, osservare la rete tramite il grafo, selezionare nodi e archi per consultarne i dettagli e analizzare l'andamento mediante le statistiche.

Questa fase mostra anche il carattere adattivo del processo Kanban. Durante l'integrazione sono emerse attività più granulari, come il pannello delle statistiche, che sono state inserite e completate nel flusso senza attendere l'avvio di un'ulteriore iterazione temporale.

[Fase Kanban precedente](kanban-02-user-interface.md) · [Processo di sviluppo](../02-development_process.md)
