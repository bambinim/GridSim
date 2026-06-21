# Specifica dei requisiti

## Requisiti di business

L'obiettivo principale del progetto è realizzare un sistema software che consenta di modellare, configurare, eseguire e analizzare simulazioni di **micro-grid energetiche**. I requisiti di business descrivono il valore di alto livello del sistema:

- **Scopo didattico e sperimentale:** Consentire lo studio e l'analisi di scenari di produzione, consumo e accumulo all'interno di una rete elettrica locale (micro-grid) per comprenderne il comportamento al variare delle condizioni e degli attori coinvolti.
- **Modellazione tramite DSL:** Offrire un linguaggio specifico del dominio (DSL) semplice e leggibile per permettere la definizione iniziale della topologia della grid (entità presenti, connessioni, parametri iniziali).
- **Controllo della simulazione:** Fornire all'utente la possibilità di avviare, mettere in pausa e riprendere la simulazione in tempo reale. Per garantire la coerenza della simulazione, una volta configurata ed avviata, i parametri non sono più modificabili.
- **Analisi dei dati storici:** Raccogliere e aggregare statistiche sull'andamento temporale dei flussi per supportare studi di ottimizzazione energetica.

## Modello di dominio

Il dominio del simulatore è composto dalle seguenti entità concettuali:

- **Ambiente (Environment):** Definisce lo stato del mondo esterno alla griglia in un dato istante temporale. Contiene variabili come l'ora del giorno e le condizioni meteorologiche (es. livello di irraggiamento solare, intensità del vento).
- **Casa (House):** Un sotto-sistema autonomo complesso collegato alla micro-grid. Internamente incapsula:
  - Un *profilo di consumo* energetico realistico (es. basato su fasce orarie o curve giornaliere).
  - Eventuali *produttori locali* (es. pannelli fotovoltaici sul tetto, influenzati dall'irraggiamento solare).
  - Eventuali *sistemi di accumulo locali* (batterie).
    Essa risolve prima il proprio bilancio energetico interno e poi scambia solo il surplus o il deficit residuo con la micro-grid.
- **Produttore Standalone (Standalone Producer):** Impianto di generazione indipendente collegato direttamente alla micro-grid (es. parchi eolici, centrali solari). La sua produzione è influenzata dalle condizioni ambientali (es. vento per l'eolico, sole per il solare).
- **Sistema di accumulo (Storage System / Batteria):** Dispositivo per immagazzinare energia. Può essere integrato in una Casa o essere indipendente. È caratterizzato da:
  - *Capacità massima* (kWh).
  - *Livello di carica corrente* (kWh).
  - *Velocità massima di carica* e *velocità massima di scarica* (kW per tick).
- **Rete Esterna (External Grid):** Funge da sorgente o pozzo infinito a cui la micro-grid fa riferimento per assorbire l'energia in eccesso non accumulata o per attingere energia in caso di deficit non coperto dai produttori.
- **Micro-Grid (Micro-rete):** L'infrastruttura complessiva che modella i nodi e i collegamenti della rete elettrica locale.
- **Cavo di collegamento (Cable):** Collegamento elettrico (arco) tra due nodi della micro-grid. È caratterizzato da una *capacità di trasporto massima* (kW) e dal *carico corrente* (potenza effettivamente trasferita in quel tick).

## Requisiti funzionali

### Utente
- **Definizione della rete tramite DSL:** L'utente deve poter definire la struttura della micro-grid (numero di case, presenza di batterie e pannelli locali, generatori standalone, parametri iniziali, i cavi di collegamento tra di essi con le relative portate massime, e la granularità del tempo) attraverso un linguaggio specifico del dominio (DSL) embedded scritto in Scala.
- **Controllo dell'esecuzione:** L'utente deve poter avviare la simulazione, metterla in pausa e riprenderla in qualsiasi momento.
- **Visualizzazione in tempo reale:** L'utente deve poter visualizzare lo stato istantaneo della micro-grid sotto forma di **grafo dinamico**, in cui i nodi rappresentano le utenze/produttori e gli archi rappresentano i cavi (mostrando graficamente i flussi e colorando in rosso eventuali cavi in sovraccarico).

- **Interrogazione delle statistiche:** L'utente deve poter richiedere report e statistiche sull'andamento storico dell'intera simulazione, specificamente:
  - Energia totale immessa ed estratta dalla Rete Esterna.
  - Picchi di carico massimo registrati sulla micro-grid.
  - Percentuale di autosufficienza energetica delle case (rapporto tra consumo coperto da fonti locali/batteria e consumo totale).
  - Frequenza e durata dei blackout o dei periodi di criticità energetica.

### Sistema
- **Gestione del tempo (Tick):** Il sistema deve far avanzare la simulazione a passi discreti ("tick"). La granularità temporale di ciascun tick (es. 15 minuti, 1 ora) è definita dall'utente. Ad ogni tick, il sistema aggiorna coerentemente l'ora del giorno e i parametri ambientali (meteo).
- **Risoluzione energetica locale (Case):** Ad ogni tick, per ciascuna casa, il sistema deve:
  1. Calcolare il consumo domestico in base al profilo orario.
  2. Calcolare la produzione locale dei pannelli solari in base al soleggiamento corrente dell'ambiente.
  3. Bilanciare produzione e consumo interni.
  4. Gestire la carica o la scarica della batteria locale in caso di surplus o deficit, rispettando la capacità massima e i limiti fisici di velocità massima di carica/scarica (espressi in kW per tick).
  5. Calcolare il surplus o deficit residuo da scambiare con la micro-grid.
- **Risoluzione produttori standalone:** Calcolare l'energia prodotta da ciascun generatore standalone in base alle condizioni meteorologiche correnti.
- **Bilanciamento globale della Micro-Grid:** Calcolare la somma algebrica di tutti i flussi energetici delle case e dei generatori standalone. L'eventuale deficit/surplus globale viene scambiato con la Rete Esterna.
- **Calcolo dei Flussi nei Cavi:** Ad ogni tick, il sistema deve calcolare la potenza di carico transitata su ciascun cavo in base al bilancio energetico dei nodi collegati, rilevando se la capacità massima del cavo è stata superata (segnalazione di sovraccarico).
- **Storicizzazione dello stato:** Il sistema deve registrare in modo immutabile l'intera sequenza di stati passati della simulazione per consentire l'analisi delle statistiche.

## Requisiti non funzionali

- **Purezza Funzionale e Immutabilità:** Lo stato della simulazione deve essere completamente immutabile. Le transizioni di stato devono essere funzioni pure, prive di effetti collaterali.
- **Modularità ed Estensibilità:** L'architettura deve consentire l'aggiunta semplificata di nuovi tipi di dispositivi (es. nuovi tipi di generatori o profili di carico) e nuove metriche statistiche senza richiedere modifiche al motore di simulazione.
- **Portabilità:** Il sistema deve essere eseguibile su qualsiasi sistema operativo compatibile con la JVM (Java Virtual Machine).
- **Reattività dell'interfaccia:** L'interfaccia grafica o testuale deve rispondere tempestivamente alle richieste dell'utente (avvio, pausa, ripresa) senza bloccare il thread principale di visualizzazione durante il calcolo dei tick.

## Requisiti di implementazione

- **Linguaggio di Programmazione:** Scala 3 (v3.3.7) snd JDK 21.
- **Build Tool:** Gradle (con Kotlin DSL per la configurazione dei moduli).
- **Uso di Librerie Funzionali:** Utilizzo della libreria Cats per i costrutti funzionali standard (es. `State` monad, Type Classes, Functor, Applicative). Framework complessi per gli effetti (es. Cats Effect, ZIO) rimangono esclusi, delegando la gestione degli effetti e dell'asincronismo a costrutti standard di Scala.
- **Librerie di Test:** Uso di ScalaTest per test unitari e di integrazione.
- **Interfaccia Grafica:** JavaFX / ScalaFX, per supportare la visualizzazione di grafici ad andamento temporale in tempo reale ed elementi grafici avanzati.
- **Concorrenza e Asincronismo:** Costrutti standard della libreria di Scala (`Future`, `Promise` ed `ExecutionContext` per la gestione asincrona dei tick senza blocco della GUI).

[Sommario](index.md) |
[Capitolo precedente](02-development_process/02-development_process.md) |
[Capitolo successivo](04-architectural_design.md)
