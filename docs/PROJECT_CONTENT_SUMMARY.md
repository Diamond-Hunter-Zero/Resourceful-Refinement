# Resourceful Refinement Project Content Summary

Resourceful Refinement is a NeoForge 1.21.1 addon for Create, using the mod id `resourceful_refinement` and root package `com.resourceful_refinement`. Its core design is a factory-style resource processing loop: naturally sourced molten fluids are extracted, refined, catalysed, alloyed, cast back into items, and later reused in gel, paint, coating, heat, fuel, logistics, and more advanced factory systems.

The design vault in `Resourceful Refinement Design Docs` describes product intent and roadmap. The Java source is the authority for what is currently implemented. When docs and code diverge, prefer the code for concrete implementation details.

## Technical Shape

- Main entry point: `ResourcefulRefinementMain`.
- Registry hub: `registry/ModRegistries`, registering blocks, items, block entities, menus, fluids, recipe types, creative tabs, data components, structure types, entities, and Create display sources.
- Current Java footprint: about 253 source files, with feature packages under `src/main/java/com/resourceful_refinement/content`.
- The project uses plain NeoForge deferred registers, not Create Registrate.
- Create integration points include kinetic block entities, stress values, Create processing recipe patterns, fluid capabilities, depots/belts, Ponder scenes, JEI categories, display links, boiler heater registration, and selected Create mixins.
- Capabilities are registered centrally in `ResourcefulRefinementMain`, with fluid and item handlers exposed for refinery controllers/proxies, sieves, forge moulds, casting depots, fracking pumps, paint nozzles, refill stations, distilleries, radiators, combustion chambers, fuel tanks, milking stations, brewer's taps, and hoseguns.
- Client renderers and layer definitions are registered in `ResourcefulRefinementMain.ClientModEvents`; additional client events and tint handling live in `registry/ModClientEvents`.

## Implemented Content Areas

### Resource Refinement

The original v0.1 core is built around industrial fluid processing:

- `refinery`: Fluid Refinery multiblock, refinery access port controller, invisible/kinetic proxies, blender blade component, segmented renderer, and `fluid_refinery` recipes.
- `sieve`: Mechanical Fluid Sieve, including stack-aware behavior and `mechanical_fluid_sieve` recipes.
- `forge_mould` and `casting_depot`: Mechanical Forge Mould, Casting Depot, casting and moulding recipes, and tool coating recipe support.
- `fracking_pump` and `geyser`: Fracking Pump/Pylon-style multiblock over geyser blocks, worldgen-backed geyser sources, and `fracking_pump` recipes.
- `moulds` and core materials: consumable mould items, ferrous crystal, flux dust, durasteel ingot, and durasteel sheet.

### Fluids

Fluids are declared through `ModFluids` and `FluidEntry`, which register the fluid type, source, flowing variant, liquid block, and bucket together. Fluid behavior is grouped by `FluidGroup`: `RAW`, `CATALYSED`, `ALLOYED`, `PURIFIED`, `CARBORAX`, `DRINK`, and `PAINT`.

The current registry includes raw molten Create-stone fluids, catalysed metal/redstone/sparkpowder fluids, alloy/intermediate fluids such as silica substrate and molten blends, purified fluids, carborax fuels, liquid glue, coolant, liquid concrete, poured cement, organic slush, polymer sludge, drink fluids, and all 16 dye-colored paint fluids.

`PouredCementPlacement` plus Create pipe mixins provide special handling for liquid concrete becoming poured cement when released through open-ended pipe behavior.

### Paint, Gel, and Tools

The v0.2 systems expand the mod from factory processing into deployable fluid effects:

- `hosegun`: A fluid-storing ranged item that fires `gel_blob` entities carrying fluid identity and gel behavior.
- `paint_nozzle`: A pipe-facing block that converts piped fluids into sprayed gel blobs while open.
- `gel_splatter`: Multi-face gel blocks with tint and physical properties derived from fluid-to-gel mappings.
- `gel_tracking`: World-level tracking for gel splatter counts, used by refill stations and Create display links.
- `refill_station`: Fluid Refill Station with a GUI/menu, internal tank, hosegun tracking ID binding, display source integration, and network payloads.
- `plunger`: A thrown utility item/entity for interacting with and draining fluid containers.

Paint fluids are used for dyeing block families and entities. Non-paint fluids can create functional gels such as inert, molten, speedy, gooey, bouncy, cursed, blessed, cleanse, and potion-like effects according to tags and helper logic.

### Coatings

Tool coatings are stored as a NeoForge data component, `coating_data`, rather than plain NBT. Coating recipes live under the forge mould recipe package, while client decorators, tooltips, and server behavior are split between `content/coating`, registry event classes, and an `ItemStackMixin`.

### Heat, Fuel, and v0.3 Machinery

The project contains substantial implementation for the v0.3 thermal and fluid infrastructure layer:

- `distillery`: Stackable distillery blocks and `distillery` recipe type for time/heat/fluid processing.
- `radiator`: Pipe-like heat/cooling blocks tied to `ExtendedHeatCondition`.
- `combustion_chamber`: Carborax-fuel kinetic engine blocks with block entity, renderer, model, and item support.
- `fuel_tank`: Fluid tank block with renderer and fluid capability support.
- `advanced_pump`: Create pump variant with doubled pump range, redstone flow reversal, throughput tracking, goggles tooltip, and custom cog rendering.
- `milking_station`: Kinetic mob-processing station with item/fluid outputs, seat entity support, renderer/model, JEI category, and `milking_station` recipes.
- `brewers_tap`: Drink/flavour processing block with item capability, renderer, JEI category, and `brewers_tap` recipes.
- `utilities/heating`: Extended heat states beyond Create's default heated/superheated model, including chilled, cooled, passive, heated, and superheated.

Related newer materials include drinks glass, compacted biomatter, polymer residue, graphite, graphene mesh, and incomplete graphene mesh.

### Decorative and Miscellaneous

- `plushie`: Fox plushie block, item, block entity, model, and renderer.
- `worldgen`: Geyser offset/structure support.
- `network`: Refill station tracking payload registration.
- `ponders`: Create Ponder scenes for refinery, fracking, sieve, forge/casting, hosegun/gel, refill station, glue pot, and paint nozzle topics.

## v0.5 Conveyor Work

The current v0.5 design focus is block logistics and later horizontal item processing:

- `conveyor_belt`: Initial pass implemented in `content/conveyor`. It is a horizontal Create-like belt that moves blocks above it using piston-like movement rules rather than moving items. It supports short shaft-connected runs, front-to-back line movement, shaft insertion into belt segments, and belt chain teardown.
- `conveyor_rotator`: Initial pass implemented in `content/conveyor`. It is a kinetic directional block that rotates movable directional blocks above it toward an internal output direction, then forwards them like a conveyor. Its current backend applies server-side block-state rotation directly, while preserving a serializable rotation-session model for future Create contraption-entity rotation/rendering.
- `mechanical_stamper`: Planned. A horizontal Mechanical Press-like machine with optional stamp item, fill medium, and fill fluid inputs.

Implementation notes for the conveyor work:

- Do not directly subclass Create's `BeltBlockEntity` for Conveyor Belt behavior without care; Create belt code checks `AllBlocks.BELT` in important paths.
- Prefer a dedicated `content/conveyor` implementation that borrows placement and chain concepts from Create while owning block movement behavior locally.
- For the Conveyor Rotator initial pass, target a functional server-authoritative implementation with lightweight client animation. Full Create `ControlledContraptionEntity` rendering can be added later.
- Minimal GameTests exist in `ConveyorGameTests`, backed by a generated empty structure from `build.gradle`. They cover belt block movement, rotator directional movement, and immovable-block rejection.

## v0.4 Reference Implementation

The `Resourceful-Refinement-v0.4` workspace is a read-only reference branch with about 428 Java source files. It contains the v0.1-v0.3 systems plus a large experimental v0.4 slice that is not fully present in the v0.5 workspace. Use it for implementation patterns and system intent, but do not edit it from this workspace.

Important v0.4 implemented areas:

- `bucket_excavator`: Kinetic Bucket Excavator block, block entity, renderer/model, `bucket_excavator` item, `mineral_deposit` block, and `excavation` recipe type. It exposes item output capability from its facing side and uses JEI integration for excavation recipes.
- `drill_pylon`: Drill Pylon head, proxy, kinetic proxy, Crystal Fissure Bud, renderer, assembly/proxy structure support, `drill_pylon` recipes, and JEI category. The head acts as the controller and proxies expose controller capabilities.
- `cyclotron_forge`: Cyclotron controller, regular/kinetic proxies, custom renderer/models, `cyclotron_forge` recipes, and JEI category. The system is multiblock/proxy oriented and has item/fluid capability routing through proxies.
- `glare`: GLARE network foundation with relays, emitter dishes, kinetic receivers, chromatic transceivers, telemetry terminals, Lux transceivers, resonance/artificial resonance crystals, relay wrench, node targeting data components, network saved data, line-of-sight checks, commands/debug helpers, power terminal GUI support, and GameTests.
- `glare.remote`: Remote Entangler Depot, Remote Entanglement Transporter, transporter tank/casing proxies, remote endpoint state/mode helpers, teleport/item-transfer services, renderers, and chilled/GLARE-operational checks.
- `pug`: Launchpad controller/proxies, launchpad GUI/menu, PUG entity, route calculation, saved data, launch/flight/landing/crash services, chunk-loading support, sounds, renderer/model, and GameTests.
- `gui`: Reusable GLARE power terminal widgets, Lux monitor/history graph, square button textures, and network snapshot DTOs used by GLARE screens.

v0.4-registered blocks and items include `bucket_excavator`, `mineral_deposit`, `crystal_fissure_bud`, `drill_pylon_head`, `heavy_plate_shielding`, `cyclotron_controller`, `glare_relay`, `glare_emitter_dish`, `glare_kinetic_receiver`, `glare_chromatic_transceiver`, `glare_telemetry_terminal`, `lux_transceiver`, `remote_entangler_depot`, `remote_entanglement_transporter`, `launchpad_controller`, `resonance_crystal`, and `artificial_resonance_crystal`. Internal proxy blocks include drill pylon proxies, cyclotron proxies, launchpad proxies, and remote transporter tank/casing proxies.

v0.4 adds `liquid_chorus` to the fluid registry, uses `PUG` as a registered entity alongside gel/plunger/milking-seat entities, and registers menu screens for GLARE chromatic transceivers, telemetry terminals, launchpads, and the power terminal. It also registers PUG launch/flight/land/crash sounds.

## Recipes and Data

Custom recipe types currently registered in code are:

- `resourceful_refinement:fluid_refinery`
- `resourceful_refinement:mechanical_fluid_sieve`
- `resourceful_refinement:mechanical_forge_mould`
- `resourceful_refinement:coating`
- `resourceful_refinement:fracking_pump`
- `resourceful_refinement:distillery`
- `resourceful_refinement:milking_station`
- `resourceful_refinement:brewers_tap`

The v0.4 reference branch additionally registers these recipe types:

- `resourceful_refinement:excavation`
- `resourceful_refinement:drill_pylon`
- `resourceful_refinement:cyclotron_forge`

Datapack content under `src/main/resources/data/resourceful_refinement` includes recipes for fluid refinery, mechanical fluid sieve, mechanical forge mould, fracking, coating, distillery, milking station, brewer's tap, Create mixing, mechanical crafting, sequence assembly, shaped/shapeless crafting, paint production, worldgen structures, structure sets, tags, and damage types.

The v0.4 branch additionally contains datapack recipes for excavation, drill pylon, and cyclotron forge systems, plus GLARE/PUG-related block loot and assets.

The build has a `data` run configured to emit generated resources into `src/generated/resources`.

## Rendering and Client Systems

The mod uses both static JSON assets and custom renderers:

- Block entity renderers for refinery, proxies, blender blade, sieve, forge mould, casting depot, fracking pump, geyser, plushie, refill station, distillery, combustion chamber, advanced pump, milking station, fuel tank, brewer's tap, and radiator.
- Entity renderers for gel blobs, thrown plungers, and the milking station seat entity.
- Layer definitions for segmented multiblock and animated machine models.
- Item renderers for several machine/tool items that need custom 3D presentation.
- Client tinting for fluid buckets, gel splatters, and hosegun/refill station visuals.
- `ModPartialModels` contains reusable Create partial models such as shafts, geyser casings, industrial heater stand, advanced pump cog, and combustion fan variants.

## Build and Verification

- Java toolchain: Java 21.
- Minecraft/NeoForge/Create versions are defined in `gradle.properties` and `build.gradle`; at the time of this summary the design doc targets Minecraft 1.21.1, NeoForge 21.1.219, and Create 6.0.11-283+.
- Useful local commands include `.\gradlew.bat compileJava`, `.\gradlew.bat processResources`, `.\gradlew.bat runClient`, and `.\gradlew.bat runGameTestServer`.
- `build.gradle` enables `neoforge.enabledGameTestNamespaces` for the mod id in the client run, but there are currently no committed `@GameTest` classes in `src/main/java`.

## Reference Workspaces

The current workspace is `Resourceful-Refinement-v0.5\Resourceful-Refinement`. Other nearby workspaces are useful references but should be treated as read-only unless the user explicitly says otherwise:

- `E:\Game Dev\Minecraft Dev\Forge Modding\Resourceful Refinement`: older Resourceful Refinement workspace with v0.3-era content.
- `E:\Game Dev\Minecraft Dev\Forge Modding\Resourceful-Refinement-v0.4`: v0.4 slice reference, including Bucket Excavator, Drill Pylon, Cyclotron Forge, GLARE, remote entanglement, PUG launchpads, Power Terminal UI, and related GameTests.
- `E:\Game Dev\Minecraft Dev\Forge Modding\Create-mc1.21.1-dev`: decompiled/source reference for Create 1.21.1 API and implementation patterns.

## Design Document Relationship

The Obsidian vault provides high-level product direction and planned roadmap. Important design documents include `Primary Design Doc.md`, `Fluid Refinery.md`, `Mechanical Sieve.md`, `Forge Mould.md`, `Fracking Pump.md`, `Geyser Block.md`, `Fluid Processing Recipes.md`, `Coating.md`, `Hosegun.md`, `Paint Nozzle.md`, `Gel Splatter.md`, `Fluid Refill Station.md`, `Radiator.md`, `ExtendedHeatCondition.md`, `Conveyor Belt.md`, `Conveyor Rotator.md`, and `Mechanical Stamper.md`.

When design docs and code diverge, prefer code for implemented behavior and docs for feature intent.
