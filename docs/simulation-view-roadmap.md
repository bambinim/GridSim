# Simulation View Roadmap

This roadmap describes a pragmatic way to implement the full simulation view
without over-engineering the GUI. The GUI is not the core of the project, so the
goal is to keep the structure clean, testable where useful, and easy to evolve,
without introducing too many abstractions too early.

The main idea is:

```text
SimulationController
        |
        v
SimulationGuiController
        |
        v
SimulationViewModel / presentation data
        |
        v
SimulationView and small reusable panels
```

The simulation view should be rich enough to show the simulation clearly, but it
should not become a second application architecture beside the core model.

## Current Situation

The current simulation screen is a good starting point but only as a minimal
prototype.

Current files:

```text
app/src/main/scala/org/gridsim/gui/view/SimulationView.scala
app/src/main/scala/org/gridsim/gui/controller/SimulationGuiController.scala
app/src/main/scala/org/gridsim/gui/runtime/SimulationFactory.scala
app/src/main/scala/org/gridsim/gui/app/Router.scala
app/src/main/scala/org/gridsim/gui/app/SceneBuilder.scala
```

What already works:

- the scenario selection can start a simulation;
- the app routes from scenario selection to simulation view;
- `SimulationGuiController` hides the core controller from the view;
- updates are sent to the JavaFX thread with `Platform.runLater`;
- the core simulation already exposes useful data through `SimulationState`.

Main missing parts:

- the view does not show grid entities;
- the view does not show energy flows;
- the view does not show cable loads;
- there is no entity detail panel;
- the play/pause button is too generic;
- there is no step button;
- there is no cleanup when the app closes or changes route;
- the CSS has no real simulation-specific styling.

## Design Goals

The implementation should follow these principles:

- keep core simulation logic out of the GUI;
- keep JavaFX code out of core simulation packages;
- keep the GUI structure simple;
- use small helper classes only where they remove real complexity;
- make the view easy to update when an observable pattern is introduced later;
- avoid direct pattern matching on all entity types inside `SimulationView`;
- avoid building a large framework for a course/project GUI.

## Recommended Package Structure

Use the existing GUI package and add only a small simulation-specific area.

```text
org.gridsim.gui.controller
  SimulationGuiController.scala

org.gridsim.gui.model
  RunningSimulation.scala
  SimulationDashboardState.scala
  SimulationViewData.scala

org.gridsim.gui.view
  SimulationView.scala

org.gridsim.gui.view.simulation
  SimulationToolbar.scala
  SimulationSummaryPanel.scala
  GridMapPane.scala
  EntityDetailsPanel.scala
```

This is enough separation for the project without introducing too many layers.

## File Responsibilities

### `RunningSimulation.scala`

Location:

```text
app/src/main/scala/org/gridsim/gui/model/RunningSimulation.scala
```

Responsibility: keep together the static simulation model and the live
controller.

```scala
final case class RunningSimulation(
  model: SimulationModel,
  controller: SimulationController
)
```

Why it is needed:

`SimulationController` exposes the current dynamic state, but the view also
needs `SimulationModel` to draw the topology. The static grid nodes and cables
are in `SimulationModel.grid`.

Affected files:

```text
SimulationFactory.scala
DslScenarioPresetLoader.scala
Router.scala
SceneBuilder.scala
ScenarioSelectionView.scala
```

### `SimulationDashboardState.scala`

Location:

```text
app/src/main/scala/org/gridsim/gui/model/SimulationDashboardState.scala
```

Responsibility: represent the whole GUI state of the simulation screen as plain
data.

Suggested shape:

```scala
final case class SimulationDashboardState(
  controllerState: SimulationControllerState,
  simulatedMinutes: Long,
  hourOfDay: Int,
  totalSurplusKwh: Double,
  totalDeficitKwh: Double,
  netFlowKwh: Double,
  overloadedCables: Int,
  nodes: Seq[GridNodeViewData],
  cables: Seq[CableViewData],
  selectedEntity: Option[EntityDetailsViewData]
)
```

This keeps `SimulationView` simple. The view receives display-ready data and
does not need to understand the whole domain model.

### `SimulationViewData.scala`

Location:

```text
app/src/main/scala/org/gridsim/gui/model/SimulationViewData.scala
```

Responsibility: collect small view-data classes used only by the GUI.

Suggested content:

```scala
enum NodeKind:
  case House, Battery, SolarPanel, ExternalGrid, Unknown

enum FlowDirection:
  case Importing, Exporting, Balanced

enum Severity:
  case Normal, Warning, Error

final case class GridNodeViewData(
  id: String,
  label: String,
  kind: NodeKind,
  flowDirection: FlowDirection,
  flowKwh: Double,
  x: Double,
  y: Double,
  severity: Severity
)

final case class CableViewData(
  fromId: String,
  toId: String,
  loadKwh: Double,
  capacityKw: Double,
  utilization: Double,
  severity: Severity
)

final case class MetricViewData(
  label: String,
  value: String,
  severity: Severity = Severity.Normal
)

final case class EntityDetailsViewData(
  id: String,
  title: String,
  kind: NodeKind,
  metrics: Seq[MetricViewData]
)
```

These classes are intentionally simple. They are not a separate domain model;
they are only display data.

### `SimulationGuiController.scala`

Location:

```text
app/src/main/scala/org/gridsim/gui/controller/SimulationGuiController.scala
```

Responsibility: be the single bridge between core simulation and GUI.

Keep this class. Expand it instead of introducing many new ports.

It should:

- hold a `RunningSimulation`;
- expose the latest `SimulationDashboardState`;
- convert `SimulationSnapshot` into GUI state;
- expose GUI commands: play/pause, step, stop;
- notify the view when display state changes;
- use `Platform.runLater` in one place;
- be easy to replace internally when an observable pattern arrives.

Suggested API:

```scala
class SimulationGuiController(running: RunningSimulation):

  def currentDashboard: SimulationDashboardState

  def setOnChanged(callback: SimulationDashboardState => Unit): Unit

  def togglePlayPause(): Unit

  def stepOnce(): Unit

  def stop(): Unit

  def selectEntity(entityId: String): Unit

  def clearSelection(): Unit
```

Observable-ready part:

Today:

```scala
coreController.addStateListener { snapshot =>
  Platform.runLater {
    ...
  }
}
```

Later, only this class should change:

```scala
snapshotObservable.subscribe { snapshot =>
  Platform.runLater {
    ...
  }
}
```

The view should not care whether updates come from a callback or from an
observable.

### `SimulationDashboardMapper.scala`

Location:

```text
app/src/main/scala/org/gridsim/gui/controller/SimulationDashboardMapper.scala
```

Responsibility: convert core simulation objects into GUI data.

This can be a single class/object for now. No need for a full mapper registry
unless entity types grow significantly.

Suggested API:

```scala
object SimulationDashboardMapper:

  def toDashboard(
    model: SimulationModel,
    snapshot: SimulationSnapshot,
    selectedEntityId: Option[String]
  ): SimulationDashboardState
```

It should:

- calculate simulated minutes;
- calculate total surplus and deficit;
- calculate net flow;
- map grid nodes to `GridNodeViewData`;
- map cables to `CableViewData`;
- create selected entity details;
- assign simple positions to nodes.

This is the most important helper to keep the view clean.

### `SimulationView.scala`

Location:

```text
app/src/main/scala/org/gridsim/gui/view/SimulationView.scala
```

Responsibility: compose the simulation screen.

It should:

- create the main layout;
- create toolbar, summary panel, grid pane, and details panel;
- register one `controller.setOnChanged(...)` callback;
- call child components' `render(...)` methods;
- avoid domain logic.

Suggested layout:

```text
top:    SimulationToolbar
left:   SimulationSummaryPanel
center: GridMapPane
right:  EntityDetailsPanel
```

Keep the first version compact. A timeline can be added later if time allows.

### `SimulationToolbar.scala`

Location:

```text
app/src/main/scala/org/gridsim/gui/view/simulation/SimulationToolbar.scala
```

Responsibility: simulation controls.

Controls:

- play/pause button;
- step button;
- stop/back button if route navigation is implemented;
- simulated time label;
- lifecycle status label.

Suggested API:

```scala
final class SimulationToolbar(
  onTogglePlayPause: () => Unit,
  onStep: () => Unit,
  onStop: () => Unit
) extends HBox:

  def render(state: SimulationDashboardState): Unit
```

The button text should change:

- `Play` when paused;
- `Pause` when running.

### `SimulationSummaryPanel.scala`

Location:

```text
app/src/main/scala/org/gridsim/gui/view/simulation/SimulationSummaryPanel.scala
```

Responsibility: show high-level simulation metrics.

Metrics:

- total surplus;
- total deficit;
- net flow;
- overloaded cables;
- number of entities;
- simulated hour.

Suggested API:

```scala
final class SimulationSummaryPanel extends VBox:
  def render(state: SimulationDashboardState): Unit
```

### `GridMapPane.scala`

Location:

```text
app/src/main/scala/org/gridsim/gui/view/simulation/GridMapPane.scala
```

Responsibility: draw the grid topology.

First implementation should use a `Pane`, not `Canvas`, because `Pane` makes
clickable nodes easier.

It should render:

- one line per cable;
- one node per grid entity;
- node color based on entity kind and flow direction;
- cable stroke based on load severity;
- click on node to select entity;
- optional simple tooltip with id and flow.

Suggested API:

```scala
final class GridMapPane(
  onEntitySelected: String => Unit
) extends Pane:

  def render(state: SimulationDashboardState): Unit
```

Positioning:

Start with a simple deterministic layout. For example:

- place nodes evenly on a circle;
- keep external grid near the top or left;
- later improve layout if needed.

Do not implement a complex graph layout algorithm unless it becomes necessary.

### `EntityDetailsPanel.scala`

Location:

```text
app/src/main/scala/org/gridsim/gui/view/simulation/EntityDetailsPanel.scala
```

Responsibility: show details about the selected entity.

It should render:

- selected entity id;
- entity type;
- current flow;
- battery charge if battery;
- solar efficiency if solar panel;
- house component count if house;
- warning/error status.

Suggested API:

```scala
final class EntityDetailsPanel extends VBox:
  def render(details: Option[EntityDetailsViewData]): Unit
```

If no entity is selected, show a small neutral message.

## Mapping Rules

Keep these rules inside `SimulationDashboardMapper`, not inside view classes.

### Flow Mapping

Use `Flow[Energy]` as:

```text
Surplus -> Exporting
Deficit -> Importing
Balanced -> Balanced
```

Display value:

```text
Surplus: positive kWh
Deficit: positive kWh, but direction is importing
Balanced: 0 kWh
```

### Entity Mapping

Suggested mapping:

```text
House       -> NodeKind.House
Battery     -> NodeKind.Battery
SolarPanel  -> NodeKind.SolarPanel
ExternalGrid -> NodeKind.ExternalGrid
other       -> NodeKind.Unknown
```

Use pattern matching in the mapper only.

### Cable Severity

Calculate:

```text
utilization = load / maxCapacity
```

Suggested severity:

```text
0.00 - 0.79 -> Normal
0.80 - 0.99 -> Warning
1.00+       -> Error
```

This gives the GUI useful visual feedback without needing a complex alert
system.

## CSS Plan

Extend:

```text
app/src/main/resources/gui/gridsim.css
```

Add these classes:

```css
.simulation-root
.simulation-toolbar
.simulation-summary
.simulation-grid
.simulation-details
.status-running
.status-paused
.node-house
.node-battery
.node-solar
.node-external-grid
.flow-importing
.flow-exporting
.flow-balanced
.severity-normal
.severity-warning
.severity-error
```

Keep CSS semantic. The Scala code should assign classes based on state; CSS
should decide colors and styling.

## App Lifecycle Cleanup

This is important even if the GUI stays simple.

The simulation uses a scheduler thread. When the app closes or leaves the
simulation route, the controller should stop.

Update:

```text
GuiApp.scala
Router.scala
```

Recommended approach:

- `Router` keeps track of the active simulation controller;
- when replacing a simulation route, it stops the previous controller;
- `GuiApp` calls `router.dispose()` on close.

Suggested API:

```scala
class AppRouter(...):
  def dispose(): Unit =
    activeController.foreach(_.stop())
```

## Observable Pattern Compatibility

Do not implement the full observable pattern now if it is not part of the
current task.

Instead, prepare for it by keeping update handling inside
`SimulationGuiController`.

Current shape:

```scala
class SimulationGuiController(running: RunningSimulation):
  private var onChanged: Option[SimulationDashboardState => Unit] = None

  running.controller.addStateListener { snapshot =>
    Platform.runLater {
      val dashboard = SimulationDashboardMapper.toDashboard(...)
      onChanged.foreach(_(dashboard))
    }
  }
```

Future shape:

```scala
observableSnapshots.subscribe { snapshot =>
  Platform.runLater {
    val dashboard = SimulationDashboardMapper.toDashboard(...)
    onChanged.foreach(_(dashboard))
  }
}
```

Only the internals of `SimulationGuiController` should change. The view
components should stay the same.

## Implementation Order

1. Add `RunningSimulation`.
2. Change scenario loading and routing to pass `RunningSimulation`.
3. Add `SimulationViewData` and `SimulationDashboardState`.
4. Add `SimulationDashboardMapper`.
5. Expand `SimulationGuiController` to expose dashboard state and commands.
6. Refactor `SimulationView` into a layout composed of smaller panels.
7. Implement `SimulationToolbar`.
8. Implement `SimulationSummaryPanel`.
9. Implement `GridMapPane`.
10. Implement `EntityDetailsPanel`.
11. Add CSS classes for simulation view.
12. Add lifecycle cleanup in router/app close.
13. Add focused tests for `SimulationDashboardMapper`.

## Tests To Add

The most useful tests are for the mapper, because it contains most of the GUI
logic without requiring JavaFX.

Create:

```text
app/src/test/scala/org/gridsim/gui/controller/SimulationDashboardMapperSpec.scala
```

Test cases:

- simulated time maps correctly;
- surplus flow maps to exporting;
- deficit flow maps to importing;
- balanced flow maps to balanced;
- cable utilization maps to normal/warning/error;
- selected entity details are produced when an entity is selected;
- selected entity details are empty when the id does not exist;
- totals are calculated correctly.

Avoid heavy JavaFX tests unless the GUI becomes complex enough to justify them.

## What To Avoid

Avoid putting this kind of logic directly in `SimulationView`:

```scala
snapshot.state.entityStates.foreach {
  case (_, battery: BatteryState) => ...
  case (_, house: HouseState) => ...
}
```

Use `SimulationDashboardMapper` instead.

Avoid creating many interfaces too early. For this project, a single controller
plus a mapper is enough.

Avoid building a complex graph layout algorithm. A deterministic circular or
grid layout is acceptable for the first complete version.

Avoid making every panel subscribe independently to the simulation. Use one
update path:

```text
SimulationController -> SimulationGuiController -> SimulationView -> panels
```

## First Complete Milestone

The first complete simulation view should support:

- start from selected scenario;
- play/pause;
- step once;
- stop on close;
- show simulated time;
- show controller state;
- show total surplus, deficit, and net flow;
- draw grid entities;
- draw cables;
- color entities by type and flow;
- color cables by load severity;
- select an entity;
- show selected entity details.

This is a substantial GUI, but still proportionate to the project. It respects
basic design patterns and best practices without adding unnecessary framework
layers.
