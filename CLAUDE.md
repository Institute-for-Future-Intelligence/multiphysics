# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

Energy2D is an interactive heat transfer simulation platform built in Java. It simulates 2D transient heat transfer through conduction, convection, and radiation, combined with incompressible fluid dynamics. The software is primarily used for educational purposes and research in thermal physics, building energy analysis, and computational fluid dynamics.

## Build Commands

### Building the Project

The project uses Apache ANT with JavaFX packaging:

```bash
cd energy2d
ant default          # Full build: clean → compile → jar → deploy
ant compile          # Compile source only
ant jar              # Create JAR file
ant deploy           # Create platform-specific native bundles
ant clean            # Clean build artifacts
```

**Build Requirements:**
- Java 8 JDK (with JavaFX)
- Apache ANT
- Platform-specific JAVA_HOME configured in build.xml

**Build Outputs:**
- Compiled classes: `energy2d/classes/`
- JAR file: `energy2d/exe/energy2d.jar`
- Native bundles: `C:\dist/` (Windows .exe, macOS .dmg, Linux .all)

### Running the Application

```bash
# After building
java -jar energy2d/exe/energy2d.jar

# To disable auto-update checks
java -DNoUpdate=true -jar energy2d/exe/energy2d.jar
```

**Main Entry Point:** `org.energy2d.system.System2D`

## Architecture Overview

### Multi-Solver Physics Engine

Energy2D uses a composition of specialized solver engines within `Model2D`:

- **HeatSolver2D** - Thermal conduction and convection
- **FluidSolver2D** - Incompressible fluid dynamics with buoyancy
- **PhotonSolver2D** - Solar radiation via ray-tracing
- **RadiositySolver2D** - Thermal radiation using view factors
- **ParticleSolver2D** - Particle tracking for flow visualization

Each solver operates on shared 2D grid arrays (temperature, velocity, material properties) using finite difference methods.

### Core Components

**System2D** (`system/System2D.java`)
- Application controller and orchestrator
- Manages lifecycle, file I/O, XML serialization
- Handles menu bar, toolbar, scripting interface
- Uses ExecutorService for threading

**Model2D** (`model/Model2D.java`)
- Pure physics simulation logic
- Maintains 2D grid arrays: `float[][] t` (temperature), `float[][] u, v` (velocity)
- Contains all parts, sensors, boundaries, and agents
- Orchestrates solver execution

**View2D** (`view/View2D.java`)
- Rendering and visualization
- Mouse/keyboard interaction handling
- Manages undo/redo system (UndoManager with 36 undo command classes)
- UI overlay elements (rulers, grids, sensors)

**TaskManager** (`system/TaskManager.java`)
- Thread pool for background computations
- Executes system tasks and custom script tasks

### Package Structure

Located in `energy2d/src/org/energy2d/`:

- `system/` (24 files) - Application control, UI management, scripting engine
- `view/` (44 files) - Visualization, rendering, dialogs, property editors
- `model/` (34 files) - Physics simulation, solver implementations, parts, boundaries
- `undo/` (36 files) - Complete undo/redo system for every editable property
- `util/` (27 files) - Utility functions, rendering helpers, UI components
- `math/` (8 files) - Geometric primitives (polygons, circles, vectors)
- `event/` (8 files) - Custom event system (ManipulationListener, GraphListener, etc.)
- `com.apple.eawt/` (5 files) - macOS-specific application integration

### Key Architectural Patterns

**Model-View Separation**
- Model2D contains pure simulation logic, no UI dependencies
- View2D handles all rendering and user interaction
- Communication via listener interfaces and property change events

**Interface-Based Design**
- ThermalBoundary, MassBoundary - Boundary condition abstractions
- FillPattern - Texture/pattern rendering interface
- Multiple listener interfaces for event-driven communication

**Performance-Oriented Data Layout**
- Grid properties stored as 2D float arrays (not objects) for cache efficiency
- Enables SIMD-friendly memory access patterns
- Critical for real-time simulation performance

**Comprehensive Undo System**
- Separate undo command class for each editable property type
- Based on Swing's UndoableEdit framework
- Examples: UndoAddManipulable, UndoEditPolygon, UndoResizePart

## Data Persistence

**File Format:** XML-based `.e2d` files
- Uses Java's XMLEncoder/XMLDecoder for serialization
- SAX parser for efficient reading
- Contains complete model state: parts, boundaries, properties, solver settings

**Example Models:** 40+ examples in `energy2d/src/org/energy2d/system/examples/`
- advect1.e2d, advect2.e2d - Advection demonstrations
- benard-cell.e2d - Rayleigh-Bénard convection
- chimney.e2d, chimney2.e2d - Natural convection
- insulation*.e2d - Thermal insulation studies
- radiation*.e2d - Thermal radiation examples

## Important Implementation Notes

### Coordinate System and Units
- Base units: meters, seconds, Celsius
- Grid origin: top-left corner
- X-axis: left to right, Y-axis: top to bottom
- Physical constants defined in `model/Constants.java`

### Solver Timestep Configuration
- Configurable timestep (Δt) in Model2D
- Fluid solver uses 5 relaxation steps by default
- Adaptive timestepping not implemented - fixed Δt

### Physics Limitations
- 2D only (no 3D capabilities)
- Incompressible flow assumption (no density changes from pressure)
- Boussinesq approximation for buoyancy
- No turbulence modeling (laminar flow only)

### Mac-Specific Code
- `com.apple.eawt` package provides macOS application menu integration
- Gracefully degrades on other platforms via reflection checks

### Deployment System
- GetDown framework (`exe/lib/getdown.jar`) handles auto-updates
- Manifest includes `Permissions: all-permissions` for security
- Native bundle creation requires platform-specific JAVA_HOME in build.xml

## Testing

**Current State:** No automated test framework integrated.

**Testing Approach:**
- Manual testing via example models in `system/examples/`
- Visual inspection of simulation results
- Comparison with theoretical predictions and infrared imaging (see README.md)

**To Add Tests:** Would require adding JUnit dependency and creating test directory structure.

## Scripting Interface

**Scripter2D** (`system/Scripter2D.java`)
- Regex-based command parsing system
- Supports simulation control, property modification, task scheduling
- Commands executed via TaskManager thread pool

## Resources and Documentation

**Academic Citation:**
Charles Xie, Interactive Heat Transfer Simulations for Everyone, The Physics Teacher, Volume 50, Issue 4, pp. 237-240, 2012. https://doi.org/10.1119/1.3694080

**Technical Documentation:**
- Numerical algorithms: https://intofuture.org/energy2d-equations.html
- Fluid-particle coupling: https://intofuture.org/energy2d-particle-dynamics.html
- Thermal bridge modeling: https://intofuture.org/energy2d-thermal-bridges.html
- IR imaging validation: https://intofuture.org/ie-thermal-equilibrium.html

**Applications:** See README.md for 90+ research publications using Energy2D (2012-2025)
