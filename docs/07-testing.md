# 7 Testing

L'attività di testing è stata guidata dal principio di verificare automaticamente tutta la logica applicativa
indipendentemente dall'interfaccia grafica. Lo sviluppo del modulo statistiche ha seguito un approccio prevalentemente
**Test-Driven Development** (TDD): i casi di test sono stati definiti contestualmente (e, nei componenti più critici,
anche prima) dell'implementazione, così da formalizzare il comportamento atteso delle diverse statistiche e guidarne lo
sviluppo.

L'interfaccia grafica ScalaFX non è stata invece sottoposta a test automatici.
Le componenti della GUI svolgono prevalentemente il ruolo di presentazione dei dati prodotti dal motore di simulazione e
dal sottosistema delle statistiche, già verificati mediante test unitari e di integrazione.
Il corretto funzionamento dell'interfaccia è stato quindi validato attraverso prove manuali durante lo sviluppo.

## Strumenti di Testing

Gli strumenti utilizzati per il testing sono **ScalaTest** (stile `AnyFlatSpec` con `Matchers`) eseguito tramite il
runner **JUnit** (`@RunWith(classOf[JUnitRunner])`), coerentemente con il resto del progetto, integrati nella pipeline
di Continuous Integration.

## Moduli Testati

### Enrico Marchionni

#### Statistiche

I test del modulo `statistics` definiscono le proprietà che l'accumulatore doveva rispettare — leggi del monoide
(identità, combinazione), casi limite del campionamento (snapshot vuoto, entità non pertinenti, valori esattamente
al limite) — per poi far collassare l'implementazione su quelle proprietà.

- **`FlowStatisticSpec` / `CablesOverloadStatisticSpec` / `BatteriesChargeStatisticSpec`**: verificano le leggi di
  monoide (`empty` come identità, associatività della `combine`) e il comportamento del relativo `Sampler` su
  snapshot vuoti, entità non pertinenti, casi limite (es. carico di un cavo esattamente pari alla capacità massima,
  che non deve essere considerato sovraccarico) e casi di nesting multi-livello per le batterie annidate nelle
  abitazioni.
- **`SimulationTimeStatisticSpec`**: esercita direttamente `initial`/`step`/`extract` del `Fold` (non essendo
  presente un `Sampler` separato), verificando l'incremento dei tick, l'accumulo del tempo simulato a partire dal
  `delta` di ciascuno snapshot e la corretta lettura delle date di calendario dall'ambiente della simulazione.
- **`NetFlowHistoryStatisticSpec`**: verifica il comportamento a finestra mobile (FIFO), inclusa la rimozione dei
  campioni più vecchi al superamento della capacità.
- **`StatisticsRegistrySpec` / `StatisticsEngineSpec`**: test di integrazione a livello di motore, che verificano
  che ciascuna statistica registrata in `StatisticsRegistry.allStatistics` sia correttamente raggiungibile tramite
  la propria `StatKey` nello `StatsBoard` prodotto da `engine`, e che uno stesso stream di snapshot venga elaborato
  correttamente e in modo indipendente da tutte le statistiche registrate contemporaneamente (guardia di
  regressione contro bug di wiring, ad esempio un fold registrato due volte o una chiave associata al fold
  sbagliato).

#### Ambiente e Modello Solare

I test di questa area combinano proprietà astronomiche note (equinozi, solstizi, casi limite polari) a verifiche
di validazione e di composizione, per statistiche identiche a quelle già adottate nel modulo `statistics`: leggi di
identità/associatività dove applicabile, e casi limite espliciti su ogni smart constructor.

- **`GeographicPointSpec` / `IrradianceSpec` / `TemperaturesSpec`**: verificano gli smart constructor dei rispettivi
  opaque/phantom type — accettazione dei valori validi, rifiuto dei valori fuori dominio (latitudine/longitudine
  fuori range, irraggiamento negativo, temperature sotto lo zero assoluto per ciascuna delle tre unità), e la
  correttezza delle conversioni e degli operatori di confronto/aritmetica esposti come extension method.
- **`SolarModelSpec`**: esercita in isolamento ciascuna formula astronomica pura, verificandone il comportamento nei
  punti notevoli del dominio fisico — declinazione solare ai solstizi/equinozio, lunghezza del giorno all'equatore e
  ai poli (giorno/notte polare), simmetria di alba/tramonto rispetto al mezzogiorno solare, irraggiamento nullo
  prima dell'alba e dopo il tramonto.
- **`EnvironmentSpec`**: verifica l'immutabilità di `Environment` (`advance` restituisce una nuova istanza senza
  mutare l'originale), la coerenza tra `time` e `currentDateTime`, la normalizzazione di `hourOfDay` oltre le 24 ore
  e la determinismo di `weather` a parità di input.
- **`SolarPanelSpec` / `SolarPanelValidatorSpec`**: verificano l'accumulo degli errori di validazione (potenza di
  picco, area ed efficienza fuori range) e i casi limite dello stato dinamico del pannello (produzione a zero,
  produzione pari alla potenza di picco, produzione superiore al limite fisico del pannello).
- **`SolarPanelStrategySpec` / `SolarPanelEvolutionSpec`**: verificano rispettivamente la formula di produzione STC
  lineare (proporzionalità a irraggiamento/area/efficienza, saturazione alla potenza di picco, produzione nulla a
  irraggiamento zero) e l'integrazione tra `Environment.weather` e la strategia di produzione durante un tick di
  evoluzione.

---

[Sommario](index.md) |
[Capitolo precedente](06-implementation/06-implementation.md) |
[Capitolo successivo](08-conclusion.md)
