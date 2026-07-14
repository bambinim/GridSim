# Sprint 1 — Modellazione iniziale del dominio

## Informazioni generali

Il primo sprint ha rappresentato la fase iniziale dello sviluppo di GridSim ed è stato organizzato adottando Scrum. Il suo obiettivo principale era costruire le fondamenta del modello di dominio necessarie alle successive funzionalità del simulatore. Il lavoro si è concentrato sulla rappresentazione dell'ambiente, delle entità della rete elettrica e della relativa topologia, includendo i componenti interni delle abitazioni e i primi meccanismi per il calcolo dei flussi energetici.

### Deadline

La Deadline di questo Sprint è stata fissata al 06/12/2023. \
La durata eccezionale di questo Sprint è di quasi due settimane, poiché include anche l’organizzazione e la configurazione del progetto, le quali non sono di diretto interesse dell’utente.

## Sprint backlog

| ID | Tipo | Attività | Parent | Owner             |
|---|---|---|---|-------------------|
| #433 | Task | Modellazione ambiente | Modellazione del dominio | Enrico Marchionni |
| #435 | Task | Modellazione casa | Modellazione "entità" grid | Michele Nardini   |
| #436 | Task | Modellazione impianto fotovoltaico | Modellazione "entità" grid | Enrico Marchionni |
| #437 | Task | Modellazione batteria | Modellazione "entità" grid | Michele Nardini   |
| #441 | Task | Modellazione dei profili di consumo | Modellazione casa | Michele Nardini   |
| #442 | Task | Modellazione cavi | Modellazione topologia grid | Matteo Bambini    |
| #447 | Task | Modellazione topologia grid | — | Matteo Bambini    |
| #449 | Task | Calcolo flussi di potenza sui cavi | Modellazione topologia grid | Matteo Bambini    |
| #455 | Task | Unificare "locale" per stampa stringhe | — | Enrico Marchionni |


## Risultati dello sprint

Alla scadenza prevista, una parte dei work package pianificati non risultava ancora completata. Gli impegni lavorativi e personali dei membri del gruppo avevano infatti ridotto e reso meno prevedibile la disponibilità effettiva rispetto a quella ipotizzata durante la pianificazione dello sprint. Le attività rimanenti sono state portate a termine successivamente.

Nel complesso sono state realizzate le principali strutture del dominio relative all'ambiente, alle entità della grid, alla casa, agli impianti fotovoltaici, alle batterie, ai profili di consumo, ai cavi e alla topologia. È stato inoltre completato il primo calcolo dei flussi di potenza sui cavi.

## Valutazione conclusiva

Il primo sprint ha consentito al gruppo di definire una base condivisa e di verificare concretamente la prima organizzazione del lavoro. Scrum è risultato utile nella fase di avvio perché ha fornito un obiettivo temporale comune e ha favorito la scomposizione iniziale del dominio in attività tracciabili.

È stato proprio durante questa prima esperienza che il gruppo ha riscontrato le difficoltà concrete nell'applicare stabilmente Scrum. Gli impegni lavorativi e personali dei membri rendevano variabile la disponibilità individuale e impedivano di garantire con continuità la capacità richiesta da una pianificazione basata su intervalli temporali fissi. Il fatto che una parte dei task non risultasse completata entro la scadenza dello sprint ha rappresentato un'evidenza diretta di questa difficoltà. Di conseguenza, risultava complesso organizzare il lavoro e i momenti di coordinamento secondo una cadenza regolare, nonostante l'utilità iniziale della metodologia per definire obiettivi e priorità condivisi.

Le criticità osservate non dipendevano quindi dai principi di Scrum, ma dalla difficoltà di usufruire efficacemente della metodologia nelle condizioni concrete del gruppo. Sulla base di quanto emerso durante lo sprint, il processo è stato adattato passando a Kanban e a una gestione continua dei task, maggiormente compatibile con disponibilità non uniformi. Le attività successive non sono state ricostruite artificialmente come ulteriori sprint, ma organizzate attraverso il flusso della lavagna OpenProject, le priorità correnti e le review effettuate al completamento dei task.

[Processo di sviluppo](../02-development_process.md)
