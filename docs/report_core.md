# 📘 Report Tecnico e Guida Operativa: GridSim Core

Benvenuto in GridSim. Questo documento funge da guida tecnica per comprendere l'architettura del simulatore e come estenderlo mantenendo l'alta qualità del codice funzionale.

---

## 1. Il Linguaggio Energetico: Units e Flow

Il sistema utilizza un sistema di tipi protetto per evitare errori di calcolo fisici (es. sommare potenza ed energia).

*   **Power vs Energy:** Distinguiamo tra Potenza (`Power`, es. kW) ed Energia (`Energy`, es. kWh). Tramite estensioni, puoi scrivere `10.kw` o `5.kwh`. Il passaggio avviene tramite la durata del passo di simulazione (`delta`).
*   **Flow[T]:** Rappresenta il bilancio energetico:
    *   `Surplus(energy)`: Energia in eccesso (Produzione > Consumo).
    *   `Deficit(energy)`: Bisogno di energia (Consumo > Produzione).
    *   `Balanced`: Equilibrio perfetto.

---

## 2. Il Motore Energetico: EnergyResolver

In GridSim, tutto ciò che partecipa alla simulazione è un **EnergyResolver[T]**. È l'interfaccia universale per la risoluzione energetica:

*   **Componenti Atomici (es. Battery):** Implementano il resolver per definire la loro fisica (carica/scarica).
*   **Aggregatori (es. House):** Coordinano i componenti interni secondo un ordine fisico preciso:
    1.  Calcolo del consumo interno della casa.
    2.  Interrogazione dei **Producers** (es. fotovoltaico) per coprire il carico.
    3.  Interrogazione degli **Storages** (es. batterie) per gestire il residuo.

---

## 3. Gestione dello Stato: State Monad

Il simulatore è **puramente funzionale** e non usa stati mutabili. Utilizziamo la monade **`State[S, A]`**:
*   **S (Stato):** L'entità che stiamo trasformando (es. `BatteryState`).
*   **A (Risultato):** L'output dell'operazione (il `Flow` residuo).

Ogni operazione energetica è una "ricetta" pura che descrive come passare dallo stato vecchio allo stato nuovo, delegando al sistema il compito di "cucire" insieme le varie transizioni.

---

## 4. Modellazione delle Entità

Le entità sono **case class immutabili** che agiscono come meri contenitori di dati. Il loro ruolo nel sistema è definito da Marker Traits:
*   **Producer:** Entità che generano energia (Fotovoltaico, Eolico).
*   **Storage:** Entità che accumulano energia (Batterie, ...).
Questa separazione permette al resolver della casa di applicare le priorità fisiche corrette indipendentemente dall'ordine della lista.

---

## 5. Il Modulo di Validation

Garantiamo l'integrità fisica tramite **Smart Constructors**:
*   Non è possibile creare entità con dati assurdi (es. capacità negativa).
*   Metodi come `Battery.make(...)` restituiscono un **`ValidatedNec`**, che accumula tutti gli errori di validazione invece di lanciare eccezioni.

[Sommario](index.md)
