# Kanban — Visualizzazione e statistiche

## Collocazione nel flusso

Questa terza finestra descrittiva raccoglie i work package dedicati alla visualizzazione della rete, alla configurazione temporale dell'esecuzione e alla presentazione delle statistiche.

Le attività sono state completate e revisionate progressivamente, mentre la lavagna veniva aggiornata in funzione dei risultati ottenuti e delle nuove necessità emerse durante l'integrazione.

## Work package completati

| ID   | Tipo | Work package                                              | Assegnatario      |
|------|------|-----------------------------------------------------------|-------------------|
| #461 | Task | Pannello informazioni dettagliate su nodi e archi         | Michele Nardini   |
| #460 | Task | Pannello grafo simulazione                                | Matteo Bambini    |
| #465 | Task | Pannello visualizzazione statistiche                      | Enrico Marchionni |
| #446 | Task | Raccolta statistiche                                      | Enrico Marchionni |
| #463 | Task | Rendere configurabile il delta time tra gli step          | Michele Nardini   |
| #464 | Task | Aumentare la granularità del tick delta almeno ai secondi | Enrico Marchionni |

## Evoluzione del lavoro

Il pannello del grafo (`#460`) ha reso visibile la topologia della rete, mentre il pannello informativo (`#461`) ha permesso di consultare i dettagli di nodi e archi. Questi elementi hanno completato la visualizzazione interattiva della simulazione.

La raccolta delle statistiche (`#446`) ha elaborato i dati distribuiti dal sistema di osservabilità, mentre il relativo pannello (`#465`) li ha resi consultabili nell'interfaccia, completando il percorso iniziato con il work package `#445` nella fase precedente.

La configurazione del delta tra gli step (`#463`) e l'aumento della sua granularità almeno ai secondi (`#464`) hanno infine consentito di adattare il ritmo di avanzamento della simulazione alle esigenze dello scenario.

## Risultato della fase

Al termine della fase l'utente poteva modificare la durata simulata di ogni step, osservare la rete tramite il grafo, selezionare nodi e archi per consultarne i dettagli e analizzare l'andamento mediante le statistiche.

Questa fase mostra anche il carattere adattivo del processo Kanban. Durante l'integrazione sono emerse attività più granulari, come il pannello delle statistiche, che sono state inserite e completate nel flusso senza attendere l'avvio di un'ulteriore iterazione temporale.

[Fase Kanban precedente](kanban-02-user-interface.md) · [Processo di sviluppo](../02-development_process.md)
