# 8 Conclusioni

## Retrospettiva

Lo sviluppo di GridSim ha rappresentato un'importante esperienza sia dal punto di vista tecnico sia da quello
organizzativo.
Una delle decisioni che ha maggiormente influenzato l'andamento del progetto è stata la definizione di un'architettura
modulare fin dalle prime fasi di progettazione.

La suddivisione del sistema in moduli con responsabilità ben definite e interfacce chiaramente separate ha permesso ai
membri del team di lavorare in parallelo sulla maggior parte delle funzionalità, riducendo al minimo le dipendenze
reciproche.
Ogni componente è stato sviluppato in maniera relativamente indipendente, rendendo più semplice l'integrazione
progressiva delle diverse parti del sistema e limitando la necessità di modifiche incrociate tra i vari moduli.

L'adozione del pattern MVVM per l'interfaccia grafica e la separazione tra logica di simulazione, modello di dominio,
sistema di osservazione e modulo delle statistiche hanno contribuito a mantenere un basso accoppiamento tra i
componenti.
Questo ha favorito non solo una maggiore manutenibilità del codice, ma anche una più semplice estensione del progetto
con nuove funzionalità.

Dal punto di vista organizzativo, questa struttura ha consentito di distribuire il lavoro in modo efficace tra i
componenti del gruppo.
Le attività potevano essere sviluppate contemporaneamente, con momenti di integrazione limitati principalmente alla
definizione delle API condivise e alla verifica del corretto funzionamento complessivo del sistema.
Di conseguenza, il numero di conflitti durante lo sviluppo è stato contenuto e il processo di integrazione è risultato
fluido.

Nel complesso, il progetto ha confermato come un'attenta progettazione architetturale nelle fasi iniziali rappresenti
un investimento fondamentale per lo sviluppo collaborativo di software di media complessità.
Una buona modularizzazione non solo migliora la qualità del prodotto finale, ma rende anche il lavoro del team più
efficiente, favorendo parallelizzazione delle attività, riusabilità dei componenti e facilità di manutenzione futura.

---

[Sommario](index.md) |
[Capitolo precedente](07-testing.md)
