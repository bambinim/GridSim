# Introduzione

In questo progetto si intende realizzare un sistema software per la simulazione e l'analisi di una micro-grid energetica,
modellata come una rete di entità interconnesse in grado di produrre, consumare e scambiare energia elettrica.

Per garantire una descrizione coerente del dominio e facilitare la corrispondenza tra requisiti e implementazione, ogni
concetto del sistema viene associato a una terminologia univoca che sarà utilizzata in modo consistente sia nella
documentazione che nel codice.

La micro-grid è rappresentata come un grafo (**Graph**) costituito da nodi e archi:
- I nodi rappresentano le entità energetiche del sistema, come abitazioni (**House**), sistemi di produzione
  (**Producer**), sistemi di accumulo (**Storage**) e la rete esterna.
- Gli archi rappresentano i collegamenti fisici tra le entità, ovvero i cavi (**Cable**), caratterizzati da una capacità
  massima di trasporto energetico.

Ogni entità della rete contribuisce al bilancio energetico complessivo attraverso la produzione, il consumo o
l'immagazzinamento di energia.
Le abitazioni, in particolare, costituiscono sistemi complessi che integrano profili di consumo, eventuali fonti di
produzione locali e sistemi di accumulo interni.

Il sistema è progettato come una simulazione a tempo discreto, in cui l'evoluzione della micro-grid avviene attraverso
passi successivi denominati **tick**.
Ad ogni tick, il sistema aggiorna lo stato delle entità, calcola i flussi energetici e determina gli scambi tra i nodi
della rete e la rete esterna.

Il comportamento del sistema è deterministico e basato su trasformazioni di stato pure: a partire da uno stato iniziale
della simulazione, ogni transizione produce un nuovo stato coerente senza modificare direttamente i dati precedenti,
consentendo così la ricostruzione e l'analisi storica dell'evoluzione del sistema.

L'obiettivo del progetto è quindi fornire uno strumento didattico e sperimentale per lo studio del comportamento delle
micro-reti energetiche, permettendo la configurazione, l'esecuzione e l'analisi di scenari complessi attraverso un
linguaggio di dominio dedicato (DSL) e un motore di simulazione funzionale.

[Indice](index.md) |
[Capitolo successivo](02-development_process/02-development_process.md)
