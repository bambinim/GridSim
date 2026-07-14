# 2 Processo di sviluppo adottato

Descrizione del processo di sviluppo adottato per lo sviluppo del progetto.

## Introduzione

Lo sviluppo di GridSim è stato organizzato in modo incrementale e collaborativo. Il lavoro è stato suddiviso in aree funzionali relativamente indipendenti, sviluppate su branch dedicati e integrate progressivamente nel ramo comune. Questo approccio ha permesso di far evolvere in parallelo il modello di dominio, il motore di simulazione, il DSL, il sistema di osservabilità e statistiche, e l'interfaccia grafica.

Anche la metodologia di lavoro è stata sottoposta a revisione. Inizialmente il gruppo aveva scelto Scrum e ha organizzato secondo tale impostazione il primo sprint. Successivamente, la diversa disponibilità dei membri, dovuta a impegni lavorativi e personali, ha reso poco efficace mantenere una cadenza rigida e uniforme. Il gruppo ha quindi adottato Kanban, ritenuto più adatto a sostenere un flusso continuo di lavoro e a conciliare le attività progettuali con disponibilità variabili.

Le sezioni seguenti descrivono il processo ricostruibile dallo stato attuale del progetto.

## Evoluzione della metodologia: da Scrum a Kanban

### Adozione iniziale di Scrum

Nella fase iniziale GridSim è stato gestito secondo il tipico ciclo iterativo di Scrum. Il lavoro previsto per il primo sprint è stato selezionato e pianificato in anticipo, con l'obiettivo di produrre un incremento verificabile entro una finestra temporale condivisa. Questa impostazione è stata utile soprattutto all'avvio del progetto: ha favorito una prima definizione delle priorità, la scomposizione del problema e la creazione di un obiettivo comune per il gruppo.

Il backlog, i risultati e la valutazione di questa prima iterazione sono descritti nel documento dedicato allo [Sprint 1](iterations/sprint-01.md).

Al termine del primo sprint è tuttavia emersa una difficoltà organizzativa. Scrum presuppone sprint di durata stabile, un impegno relativamente prevedibile e momenti di sincronizzazione ricorrenti. Nel caso del gruppo, gli impegni lavorativi e personali rendevano la disponibilità disomogenea sia tra i membri sia tra una settimana e l'altra. Di conseguenza, una quantità di lavoro realistica all'inizio di uno sprint poteva non esserlo più durante il suo svolgimento.

Mantenere formalmente gli sprint in queste condizioni avrebbe prodotto una pianificazione solo apparente: task completati in anticipo da alcuni membri, attività trascinate allo sprint successivo e meeting fissati in momenti poco compatibili con le disponibilità effettive. Il problema non riguardava quindi gli obiettivi di qualità o collaborazione promossi da Scrum, ma la sua cadenza temporale rispetto al contesto concreto del team.

### Passaggio a Kanban

Per questi motivi, dopo il primo sprint il gruppo ha scelto di passare a Kanban. Il cambiamento non è stato interpretato come un abbandono dei principi Agile, bensì come l'applicazione del principio di ispezione e adattamento al processo stesso. Invece di preservare una metodologia non più adatta, il team ha modificato il proprio modo di lavorare sulla base delle evidenze raccolte durante il primo ciclo.

Con Kanban il lavoro non viene vincolato a sprint con scadenze uniformi, ma procede come un flusso continuo. La lavagna Kanban e l'organizzazione delle funzionalità sono state mantenute su **OpenProject**, che ha fornito al gruppo una vista condivisa dello stato dei task, delle priorità e del lavoro ancora da affrontare. I task venivano presi in carico quando un membro disponeva della capacità necessaria e portati fino al completamento prima di aumentare il lavoro contemporaneamente in corso. Questo modello si è adattato meglio al gruppo perché consente di:

- assorbire variazioni di disponibilità senza dover ridefinire continuamente uno sprint;
- evitare che un'attività incompleta perda visibilità soltanto perché termina una finestra temporale;
- aggiornare le priorità quando l'integrazione fa emergere bug, dipendenze o refactoring urgenti;
- permettere ai membri di contribuire in momenti differenti mantenendo chiaro lo stato del lavoro;
- concentrare l'attenzione sul completamento e sull'integrazione dei task, anziché sul rispetto formale della durata di uno sprint;
- mantenere un ritmo sostenibile e compatibile con gli altri impegni del gruppo.

### Elementi mantenuti e cambiati

Il passaggio non ha eliminato quanto di utile era stato introdotto con Scrum. Sono rimasti:

- lo sviluppo incrementale;
- la scomposizione delle funzionalità in task osservabili;
- il confronto sulle priorità;
- la verifica frequente degli incrementi;
- l'attenzione a blocchi, dipendenze e qualità del risultato.

Sono invece cambiati:

- la gestione del lavoro, da un insieme chiuso di task per sprint a una coda continuamente aggiornata;
- l'assegnazione, resa più flessibile e collegata alla capacità disponibile;
- la revisione, svolta al completamento e all'integrazione dei singoli task anziché concentrata soltanto a fine sprint;
- la sincronizzazione, maggiormente asincrona e affiancata da incontri organizzati quando richiesti da decisioni, dipendenze o blocchi.

In questo senso Kanban ha reso esplicito il processo che meglio rispondeva alle condizioni del gruppo: pianificazione continua, responsabilità sul task preso in carico, integrazione frequente e adattamento delle priorità.

Per documentare l'avanzamento senza trasformare retroattivamente il periodo Kanban in una successione di sprint, il lavoro successivo è stato organizzato in tre finestre descrittive basate sulle date di aggiornamento dei work package:

- [nucleo della simulazione](iterations/kanban-01-simulation-core.md), dedicato a DSL, model, state ed esecuzione;
- [osservabilità e accesso alla simulazione](iterations/kanban-02-user-interface.md), dedicato alla distribuzione degli aggiornamenti, all'avvio, al grafo e alla configurazione temporale;
- [interfaccia, controllo e statistiche](iterations/kanban-03-interface-statistics.md), dedicato al completamento dell'esperienza utente e alla presentazione dei risultati.

Questi raggruppamenti hanno esclusivamente una funzione documentale: descrivono l'evoluzione funzionale del prodotto mantenendo l'ordine generale degli aggiornamenti osservati in OpenProject, senza introdurre deadline o backlog chiusi propri di Scrum.

## Organizzazione e divisione dei task

### Suddivisione per aree funzionali

Il lavoro è stato suddiviso principalmente per funzionalità o sottosistema. La struttura dei branch e della codebase evidenzia, tra le altre, le seguenti aree di lavoro:

- modello del dominio e contratti comuni;
- ambiente e gestione del tempo simulato;
- modello delle abitazioni e dei relativi componenti;
- produzione fotovoltaica e sistemi di accumulo;
- topologia della rete e risoluzione dei flussi;
- motore e controller della simulazione;
- DSL per la definizione degli scenari;
- osservabilità e raccolta delle statistiche;
- interfaccia grafica e visualizzazione della rete;

### Assegnazione e responsabilità

Il lavoro è stato distribuito tra i membri attraverso i task visibili sulla lavagna OpenProject. La presa in carico rimaneva flessibile, così da rispettare la disponibilità effettiva, ma veniva coordinata considerando priorità, competenze richieste e dipendenze tra sottosistemi. La conclusione di un task costituiva il principale punto di sincronizzazione: il gruppo ne verificava il risultato e decideva come aggiornare il flusso di lavoro.

L’organizzazione del personale all’interno del processo:

- Product Owner/Service Request Manager: si occupa della gestione della lavagna Kanban e di verificare l’adeguatezza del sistema realizzato. Il ruolo è stato assunto da Matteo Bambini;
- Development Team: si occupa di progettare soluzioni adeguate ai task definiti dal Product Owner/Service Request Manager, stimando tempi di realizzazione e proponendo modifiche sul sistema Il team di sviluppo sarà composto da:
  - Matteo Bambini
  - Michele Nardini
  - Enrico Marchionni
## Meeting e interazioni pianificate

Durante il primo sprint, impostato secondo Scrum, la pianificazione e il controllo dell'avanzamento erano collegati alla cadenza dello sprint. Il passaggio a Kanban ha reso intenzionalmente più flessibile anche la sincronizzazione del gruppo. A causa degli impegni lavorativi e personali non era conveniente imporre incontri frequenti in orari fissi. I meeting sono stati quindi collegati al completamento dei task, utilizzando **Microsoft Teams** come piattaforma per gli incontri a distanza.

Questa scelta non ha eliminato il controllo sull'avanzamento, ma lo ha collegato a eventi significativi del flusso. Quando un task veniva concluso, il meeting aveva una duplice funzione:

- **review**, per presentare il risultato, verificarne la coerenza con quanto richiesto e discutere eventuali correzioni o conseguenze sugli altri moduli;
- **aggiornamento e pianificazione continua**, per riallineare lo stato della lavagna OpenProject, valutare le priorità e decidere come organizzare le funzionalità e i task successivi.

## Strumenti di sviluppo adottati

### Gestione del lavoro e comunicazione

Il gruppo ha utilizzato **OpenProject** per organizzare le funzionalità e mantenere la lavagna Kanban. La piattaforma rappresentava il riferimento condiviso per visualizzare il lavoro, aggiornare lo stato dei task e discutere quali attività affrontare successivamente.

Per gli incontri sincroni è stato adottato **Microsoft Teams**. I meeting venivano organizzati alla conclusione dei task e combinavano review tecnica, aggiornamento dello stato del progetto e pianificazione continua del lavoro successivo.

### Build Automation

Per automatizzare le attività di compilazione, esecuzione dei test e gestione delle dipendenze, il progetto utilizza Gradle come strumento di build automation.

Gradle è stato scelto perché consente di configurare in modo flessibile il processo di build e si integra bene con l’ecosistema JVM e Scala. Il progetto utilizza il plugin Scala per la compilazione del codice e il plugin Application per l’esecuzione dell’applicazione principale. Le dipendenze vengono gestite attraverso il sistema di dependency management di Gradle e centralizzate mediante un version catalog, così da mantenere più ordinata la configurazione del progetto.

### Testing

Per verificare il corretto funzionamento del sistema sono stati adottati test automatici basati su ScalaTest e JUnit.

I test coprono diverse parti del progetto, tra cui il modello di dominio, il motore di simulazione, la validazione, il solver dei flussi, la DSL e il modulo di statistiche. Questa scelta permette di controllare in modo automatico il comportamento delle componenti principali e riduce il rischio di introdurre regressioni durante lo sviluppo.

### Continuous Integration

Per mantenere il codice stabile durante lo sviluppo è stato configurato un workflow di Continuous Integration tramite GitHub Actions.

Il workflow viene eseguito automaticamente in seguito ad aggiornamenti del repository e in occasione delle pull request. In questo modo, prima dell’integrazione delle modifiche, il progetto viene compilato e testato automaticamente. La pipeline viene eseguita su più sistemi operativi, così da verificare la compatibilità del progetto in ambienti differenti.

### Processo di versionamento e collaborazione

Per la gestione del codice sorgente è stato adottato Git con repository ospitato su GitHub. Il flusso di lavoro segue un’impostazione ispirata a GitFlow, distinguendo un ramo stabile, destinato al codice pronto al rilascio, e un ramo di sviluppo, utilizzato per integrare le funzionalità completate. Le nuove funzionalità vengono sviluppate su branch dedicati e successivamente integrate tramite pull request.

Per rendere più leggibile e tracciabile la cronologia del progetto, il team ha inoltre adottato la convenzione Conventional Commits, che consente di classificare le modifiche in base alla loro natura, ad esempio nuove funzionalità, correzioni, refactoring, modifiche alla documentazione o aggiornamenti alla configurazione di build e CI.
## Informazioni da raccogliere prima della versione finale

Prima di considerare concluso il capitolo, il gruppo dovrebbe concordare e inserire:

- eventuali ulteriori responsabilità prevalenti degli altri membri;
- eventuali limiti espliciti al numero di task contemporaneamente in corso;
- durata media e partecipanti abituali dei meeting su Microsoft Teams;
- canali per le interazioni non pianificate;
- politica effettiva di code review e approvazione delle PR;
- approccio adottato nella scrittura dei test;
- eventuali test manuali della GUI;
- eventuali strumenti non presenti nel repository;
- criticità incontrate nel processo e miglioramenti introdotti durante il progetto.

---

[Sommario](../index.md) |
[Capitolo precedente](../01-introduction.md) |
[Capitolo successivo](../03-requirements.md)
