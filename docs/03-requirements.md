# Requisiti

Questo capitolo ha come scopo quello di descrivere dettagliatamente tutti i requisiti del software implementato. La quasi totalità dei requisiti sono rimasti invariati sin dalle prime fasi del progetto, mentre alcuni sono stati leggermente modificati o eliminati. È bene precisare che qualunque requisito sottoelencato è stato selezionato in quanto verificabile.

## Requisiti di business

L'applicazione dovrà disporre delle seguenti caratteristiche:

- **Business**
  - Modellazione, configurazione ed esecuzione di simulazioni di micro-grid energetiche (micro-reti locali) costituite da nodi (utenze/generatori) ed archi (cavi);
  - Analisi dei flussi di produzione, consumo e accumulo all'interno della rete elettrica locale al variare delle condizioni meteo e degli attori coinvolti;
  - Definizione della topologia della micro-grid tramite un Domain Specific Language (DSL) embedded scritto in Scala, che risulti semplice, dichiarativo e leggibile.

In particolare il simulatore funzionerà come segue:
- Una simulazione si sviluppa a passi temporali discreti denominati **tick** (la cui granularità temporale è specificata dall'utente);
- Ad ogni tick, il sistema aggiorna l'ora del giorno e i parametri meteorologici dell'ambiente (irraggiamento e temperatura);
- I vari nodi risolvono autonomamente il proprio bilancio energetico locale prima di scambiare il surplus o deficit residuo con la micro-grid;
- L'energia in eccesso o in difetto non compensata internamente alla micro-grid viene scambiata con la Rete Esterna (External Grid), operante come sorgente o pozzo infinito.

## Requisiti utente
In particolare l'utente può usufruire dei seguenti aspetti:

- **2. Utente**
  - Parametrizzazione delle caratteristiche della simulazione tramite Domain Specific Language (DSL):
    - Definizione delle abitazioni collegate alla micro-grid;
    - Configurazione di pannelli solari fotovoltaici locali o standalone, indicando potenza di targa, superficie in mq ed efficienza;
    - Configurazione dei sistemi di accumulo locali per ciascuna casa, indicando capacità massima, potenza massima di carica/scarica e soglia minima di carica;
    - Definizione della topologia dei collegamenti tra nodi tramite cavi con relative portate massime di potenza;
    - Configurazione del passo temporale di simulazione;
  - Rappresentazione grafica dell'andamento della simulazione in tempo reale tramite interfaccia grafica (GUI);
    - Rappresentazione della micro-grid sotto forma di grafo dinamico;
      - Rappresentazione dei nodi della rete (abitazioni e rete esterna);
      - Rappresentazione dei collegamenti (cavi) che indicano graficamente i flussi e le direzioni dell'energia;
      - Evidenziazione cromatica (in rosso) di eventuali cavi in condizione di sovraccarico;
    - Rappresentazione degli indicatori della simulazione in real-time nella dashboard;
      - Visualizzazione dell'ora del giorno simulata corrente;
      - Visualizzazione dell'energia netta scambiata (importata o esportata) con la Rete Esterna;
    - Visualizzazione dettagliata dello stato di un elemento selezionato (nodo o cavo) all'interno del grafo:
      - Per la batteria: capacità massima, stato di carica attuale, energia corrente stoccata, soglia di sicurezza SoC minima e limiti di potenza di carica/scarica;
      - Per il pannello solare: superficie, efficienza nominale, irraggiamento istantaneo locale, temperatura locale e produzione di picco;
      - Per la casa: numero di componenti installati e bilancio energetico netto istantaneo;
      - Per i cavi: capacità massima e flusso elettrico istantaneo in transito;
  - Rappresentazione delle statistiche riguardanti l'andamento della simulazione nel pannello di riepilogo:
    - Visualizzazione dell'energia totale immessa ed estratta dalla Rete Esterna;
    - Visualizzazione dei picchi di carico massimo registrati su ciascun cavo della micro-grid;
  - Controllo interattivo dell'esecuzione della simulazione tramite pannello comandi della GUI:
    - Avvio della simulazione;
    - Messa in pausa della simulazione;
    - Avanzamento manuale passo-passo.

## Requisiti funzionali
Il simulatore prodotto dovrà:

- **Funzionali**
  - Essere composto da una sequenza discreta di tick di aggiornamento temporale in cui vengono ricalcolati lo stato dell'ambiente e delle risorse;
  - Supportare l'evoluzione di diverse tipologie di entità collegate alla rete:
    - Abitazione:
      - Calcolo del consumo domestico in base a un profilo orario giornaliero definito;
      - Calcolo della produzione dei pannelli fotovoltaici locali in base all'irraggiamento solare dell'ambiente;
      - Risoluzione del bilancio energetico interno consumo-produzione;
      - Gestione dei cicli di carica e scarica delle batterie locali per bilanciare surplus o deficit residui dell'abitazione;
      - Determinazione del flusso netto finale di interscambio con la micro-grid;
    - Pannello Solare (SolarPanel):
      - Calcolo della potenza elettrica generata, influenzata da irraggiamento ed efficienza;
    - Batteria:
      - Accumulo del surplus energetico locale rispettando la capacità massima e i limiti fisici della velocità massima di carica;
      - Erogazione per coprire il deficit locale rispettando i limiti fisici della velocità massima di scarica e impedendo la scarica al di sotto del valore minimo di SoC;
  - Risoluzione del bilancio globale della micro-grid tramite un apposito algoritmo;
  - Calcolo della distribuzione dei flussi di potenza sui cavi (load flow) tramite un apposito algoritmo;
  - Validazione del modello e dello stato iniziale prima dell'avvio della simulazione per garantire la coerenza topologica (es. assenza di cavi scollegati o riferimenti errati);
  - Storicizzazione immutabile di tutti gli stati intermedi per abilitare la visualizzazione di report e analisi storiche.

## Requisiti non funzionali

- Purezza Funzionale e Immutabilità dello Stato: lo stato della simulazione deve essere modellato in modo completamente immutabile (SimulationState), e le transizioni temporali devono essere funzioni pure espresse tramite monade di stato (State monad);
- Reattività dell'interfaccia utente: il calcolo dei tick di simulazione deve essere asincrono e non bloccante per preservare la fluidità di interazione dell'interfaccia utente (GUI) delegando il calcolo a thread pool in background;
- Modularità ed Estensibilità: l'architettura deve supportare l'aggiunta di nuovi risolutori fisici, strategie di consumo o componenti tramite Type Classes ed estensioni polimorfiche senza modifiche al motore di simulazione;
- Portabilità: l'applicazione deve poter essere eseguita su qualsiasi sistema operativo compatibile con la JVM.

## Requisiti di implementazione

- L'applicazione verrà sviluppata in Scala 3 (v3.3.7) e Java Development Kit (JDK) 21;
- Utilizzo di Gradle con Kotlin DSL come strumento di build e gestione dei moduli;
- Utilizzo della libreria Cats per i costrutti funzionali standard (es. State monad, Type Classes, Functor, Applicative). Framework complessi per gli effetti (es. Cats Effect, ZIO) rimangono esclusi, delegando la gestione degli effetti e dell'asincronismo a costrutti standard di Scala.
- Utilizzo di JavaFX/ScalaFX per lo sviluppo dei componenti dell'interfaccia utente grafica;
- Librerie di Test: Uso di ScalaTest per test unitari e di integrazione.

---

[Sommario](index.md) |
[Capitolo precedente](02-development_process/02-development_process.md) |
[Capitolo successivo](04-architectural_design.md)
