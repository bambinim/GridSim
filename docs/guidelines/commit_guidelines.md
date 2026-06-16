# Linee guida per i Commit

Questo progetto segue la [Conventional Commits](https://www.conventionalcommits.org/) specification per i messaggi dei
commit.
Questo porta a messaggi più leggibili e facili da seguire quando si consulta la cronologia del progetto.

## Formato dei messaggi

Ogni messaggio di commit è composto da un'intestazione (header), un corpo (body) e un piè di pagina (footer).
L'intestazione segue un formato speciale che include un tipo (type), un ambito (scope) e un oggetto/descrizione sintetica (subject):

```text
<type>(<scope>): <subject>
<BLANK LINE>
<body>
<BLANK LINE>
<footer>
```

- **type**: Lo scopo del commit (vedi l'elenco seguente).
- **scope** (opzionale): La parte della codebase interessata (ad esempio, `core`, `api`, `deps`).
- **subject**: Una breve descrizione della modifica. Usa l'imperativo al tempo presente: "modifica" e non "modificato" o "modifica/modifiche".
- **body** (opzionale): Motivazione dettagliata della modifica e confronto con il comportamento precedente.
- **footer** (opzionale): Informazioni sulle modifiche incompatibili (Breaking Changes) o riferimenti alle issue di GitHub.

## Tipologie di commit

<figure class="table op-uc-figure_align-center op-uc-figure">
  <table class="op-uc-table">
    <thead class="op-uc-table--head">
      <tr class="op-uc-table--row">
      <th class="op-uc-table--cell op-uc-table--cell_head"><p class="op-uc-p">Type</p></th><th class="op-uc-table--cell op-uc-table--cell_head"><p class="op-uc-p">Description</p></th></tr>
    </thead>
    <tbody>
      <tr class="op-uc-table--row"><td class="op-uc-table--cell"><p class="op-uc-p"><strong>feat</strong></p></td><td class="op-uc-table--cell"><p class="op-uc-p">
        A new feature</p></td></tr>
      <tr class="op-uc-table--row"><td class="op-uc-table--cell"><p class="op-uc-p"><strong>fix</strong></p></td><td class="op-uc-table--cell"><p class="op-uc-p">
        A bug fix</p></td></tr>
      <tr class="op-uc-table--row"><td class="op-uc-table--cell"><p class="op-uc-p"><strong>docs</strong></p></td><td class="op-uc-table--cell"><p class="op-uc-p">
        Documentation only changes</p></td></tr>
      <tr class="op-uc-table--row"><td class="op-uc-table--cell"><p class="op-uc-p"><strong>style</strong></p></td><td class="op-uc-table--cell"><p class="op-uc-p">
        Changes that do not affect the meaning of the code (white-space, formatting, missing semi-colons, etc)</p></td></tr>
      <tr class="op-uc-table--row"><td class="op-uc-table--cell"><p class="op-uc-p"><strong>refactor</strong></p></td><td class="op-uc-table--cell"><p class="op-uc-p">
        A code change that neither fixes a bug nor adds a feature</p></td></tr>
      <tr class="op-uc-table--row"><td class="op-uc-table--cell"><p class="op-uc-p"><strong>perf</strong></p></td><td class="op-uc-table--cell"><p class="op-uc-p">
        A code change that improves performance</p></td></tr>
      <tr class="op-uc-table--row"><td class="op-uc-table--cell"><p class="op-uc-p"><strong>test</strong></p></td><td class="op-uc-table--cell"><p class="op-uc-p">
        Adding missing tests or correcting existing tests</p></td></tr>
      <tr class="op-uc-table--row"><td class="op-uc-table--cell"><p class="op-uc-p"><strong>build</strong></p></td><td class="op-uc-table--cell"><p class="op-uc-p">
        Changes that affect the build system or external dependencies (example scopes: sbt, gradle, npm)</p></td></tr>
      <tr class="op-uc-table--row"><td class="op-uc-table--cell"><p class="op-uc-p"><strong>ci</strong></p></td><td class="op-uc-table--cell"><p class="op-uc-p">
        Changes to our CI configuration files and scripts</p></td></tr>
      <tr class="op-uc-table--row"><td class="op-uc-table--cell"><p class="op-uc-p"><strong>chore</strong></p></td><td class="op-uc-table--cell"><p class="op-uc-p">
        Other changes that don't modify src or test files</p></td></tr>
      <tr class="op-uc-table--row"><td class="op-uc-table--cell"><p class="op-uc-p"><strong>revert</strong></p></td><td class="op-uc-table--cell"><p class="op-uc-p">
        Reverts a previous commit</p></td></tr>
    </tbody>
  </table>
</figure>

## Modifiche incompatibili

Le modifiche incompatibili (breaking changes) devono essere indicate con un punto esclamativo (`!`) dopo il type/scope
oppure iniziando il footer con `BREAKING CHANGE:`.

Example:

```text
feat(api)!: send an email to the customer when a product is shipped
```

## Examples

- Feature with scope:

```text
feat(core): add state monad implementation
```

- Bug fix:

```text
fix: resolve memory leak in stream processing
```

- Documentation update:

```text
docs: update ARCHITECTURE.md with new diagram
```

[Indice](../index.md) |
[Workflow](workflow.md)
