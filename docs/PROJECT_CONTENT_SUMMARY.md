# Resourceful Refinement Project Content Summary

Resourceful Refinement is a NeoForge 1.21.1 addon for Create, using the mod id `resourceful_refinement` and root package `com.resourceful_refinement`. Its core design is a factory-style resource processing loop: naturally sourced molten fluids are extracted, refined, catalysed, alloyed, cast back into items, and later reused in gel, paint, coating, heat, and fuel systems. The design vault in `Resourceful Refinement Design Docs` describes the gameplay intent; the Java source is the authority for what is currently implemented.

## Technical Shape

- Main entry point: `ResourcefulRefinementMain`.
- Registry hub: `registry/ModRegistries`, registering blocks, items, block entities, menus, fluids, recipe types, creative tabs, data components, structure types, entities, and Create display sources.
- Current Java footprint: about 211 source files, with feature packages under `src/main/java/com/resourceful_refinement/content`.
- Create integration points include kinetic block entities, stress values, Create processing recipe patterns, Create fluid capabilities, depots/belts, ponder scenes, JEI categories, display links, and boiler heater registration.
- Capabilities are registered centrally in `ResourcefulRefinementMain`, with fluid and item handlers exposed for refinery controllers/proxies, sieves, forge moulds, casting depots, fracking pumps, paint nozzles, refill stations, distilleries, radiators, combustion chambers, and hoseguns.

## Implemented Content Areas

### Resource Refinement

The original v0.1 core is built around industrial fluid processing:

- `refinery`: Fluid Refinery multiblock, refinery access port controller, invisible/kinetic proxies, blender blade component, segmented renderer, and `fluid_refinery` recipes.
- `sieve`: Mechanical Fluid Sieve, including stack-aware behavior and `mechanical_fluid_sieve` recipes.
- `forge_mould` and `casting_depot`: Mechanical Forge Mould, Casting Depot, casting and moulding recipes, and tool coating recipe support.
- `fracking_pump` and `geyser`: Fracking Pump/Pylon-style multiblock over geyser blocks, worldgen-backed geyser sources, and `fracking_pump` recipes.
- `moulds` and core materials: consumable mould items, ferrous crystal, flux dust, durasteel ingot, and durasteel sheet.

### Fluids

Fluids are declared through `ModFluids` and `FluidEntry`, which register the fluid type, source, flowing variant, liquid block, and bucket together. Fluid behavior is grouped by `FluidGroup`: `RAW`, `CATALYSED`, `ALLOYED`, `PURIFIED`, `CARBORAX`, and `PAINT`.

The current registry includes raw molten Create-stone fluids, catalysed metal/redstone/sparkpowder fluids, alloy/intermediate fluids such as silica substrate and molten blends, purified fluids, carborax fuels, liquid glue, coolant, and all 16 dye-colored paint fluids.

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

The project contains early/current implementation for the next layer of fluid infrastructure:

- `distillery`: Stackable distillery blocks and `distillery` recipe type for time/heat/fluid processing.
- `radiator`: Pipe-like heat/cooling blocks tied to `ExtendedHeatCondition`.
- `combustion_chamber`: Carborax-fuel kinetic engine blocks with block entity, renderer, model, and item support.
- `utilities/heating`: Extended heat states beyond Create's default heated/superheated model, including chilled, cooled, passive, heated, and superheated.

These align with the v0.3 design docs, where coolant, carborax fuels, radiators, distilleries, and combustion chambers become a more advanced thermal and power layer.

### Decorative and Miscellaneous

- `plushie`: Fox plushie block, item, block entity, model, and renderer.
- `worldgen`: Geyser offset/structure support.
- `network`: Refill station tracking payload registration.
- `ponders`: Create Ponder scenes for refinery, fracking, sieve, forge/casting, hosegun/gel, refill station, glue pot, and paint nozzle topics.

## Recipes and Data

Custom recipe types currently registered in code are:

- `resourceful_refinement:fluid_refinery`
- `resourceful_refinement:mechanical_fluid_sieve`
- `resourceful_refinement:mechanical_forge_mould`
- `resourceful_refinement:coating`
- `resourceful_refinement:fracking_pump`
- `resourceful_refinement:distillery`

Datapack content under `src/main/resources/data/resourceful_refinement` includes recipes for fluid refinery, mechanical fluid sieve, mechanical forge mould, fracking, coating, distillery, Create mixing, mechanical crafting, shaped/shapeless crafting, paint production, worldgen structures, structure sets, tags, and damage types.

## Rendering and Client Systems

The mod uses both static JSON assets and custom renderers:

- Block entity renderers for refinery, proxies, blender blade, sieve, forge mould, casting depot, fracking pump, geyser, plushie, refill station, distillery, and combustion chamber.
- Entity renderers for gel blobs and thrown plungers.
- Layer definitions for segmented multiblock and animated machine models.
- Item renderers for several machine/tool items that need custom 3D presentation.
- Client tinting for fluid buckets, gel splatters, and hosegun/refill station visuals.

## Design Document Relationship

The Obsidian vault provides the high-level product direction and planned roadmap. The most important current design documents are `Primary Design Doc.md`, `Fluid Refinery.md`, `Mechanical Sieve.md`, `Forge Mould.md`, `Fracking Pump.md`, `Geyser Block.md`, `Fluid Processing Recipes.md`, `Coating.md`, `Hosegun.md`, `Paint Nozzle.md`, `Gel Splatter.md`, `Fluid Refill Station.md`, `Radiator.md`, and `ExtendedHeatCondition.md`.

When design docs and code diverge, prefer the code for implementation details. For example, the source currently includes the paint/gel/refill-station systems and v0.3 heat machinery packages in addition to the original refinery-focused content.

