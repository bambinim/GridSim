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

---

[Sommario](index.md) |
[Capitolo precedente](06-implementation/06-implementation.md) |
[Capitolo successivo](08-conclusion.md)
