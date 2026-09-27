---
title: Codebase Overview
category: Framework
status: Implemented
introduced: v0.1
recipe_type: n/a
related:
  - "[[Primary Design Doc]]"
  - "[[Fluid Properties]]"
  - "[[Fluid Processing Recipes]]"
tags:
  - overview
  - architecture
  - agent-onboarding
---

Consolidated architecture and content map for **Resourceful Refinement**, a NeoForge 1.21.1 addon for
**Create** that adds a factory-style fluid/item refinement loop. This page is the single source of truth
for *where things live and what currently exists*, and supersedes the older `docs/AGENT_PROJECT_OVERVIEW.md`
and `docs/PROJECT_CONTENT_SUMMARY.md`. Use it alongside the per-feature design docs in this vault for
gameplay intent. **When this page and the code disagree, the code wins** — prefer a quick grep over trusting
a stale table here.

## Identity & Stack

| Field | Value |
|---|---|
| Mod ID | `resourceful_refinement` |
| Root package | `com.resourceful_refinement` |
| Group ID | `com.directdeletegames` |
| Entry class | `ResourcefulRefinementMain` |
| Loader / MC | NeoForge 21.1.219 / Minecraft 1.21.1 |
| Java | 21 (toolchain pinned in `gradle.properties`) |
| Hard dependency | Create `6.0.11-283` (kinetics, processing recipes, ponder, JEI) |
| Optional | JEI `19.27.0.336` (compile-only API + runtime jar), Registrate `MC1.21-1.3.3` |
| Current version | `0.4.0` (Alpha, branch `Alpha_v0.4-GLARE_Networks`) |
| Java footprint | Several hundred source files under `src/main/java` (v0.4 added the large `glare` system plus PUG, Drill Pylon, Cyclotron Forge and Bucket Excavator) |

**Design goal:** convert Create's mineral blocks and alloys back and forth between item and fluid form,
routing them through new machines and multiblocks to build a factory-game progression — molten ores →
catalysed → alloyed/purified → cast items, then branching into paint, gel, coating, heat, fuel, and (as of
v0.4) the **GLARE** power/messaging/teleport network and PUG logistics systems.

## Source Layout

```
src/main/java/com/resourceful_refinement/
├── ResourcefulRefinementMain.java   # @Mod entry; capability + client (BER/layer) registration
├── registry/                        # Registration hub — start here for IDs
├── content/<feature>/               # Feature packages: block → block entity → recipe → renderer
├── utilities/                       # Shared helpers (heating, fluid boxes, goggles, shafts)
├── network/                         # Payload registration
├── worldgen/                        # Geyser structure placement
├── config/                          # ServerConfig (ModConfigSpec)
├── mixin/                           # Create/vanilla patches
├── ponders/                         # Create Ponder scenes
└── data/                            # ModDataGenerators (GatherDataEvent → src/generated/resources)

src/main/resources/
├── assets/resourceful_refinement/   # models, blockstates, lang, textures
└── data/resourceful_refinement/     # recipe, tags, worldgen, structure, loot_table, damage_type
src/generated/resources/             # datagen output, folded into the main resources source set
```

## Bootstrap & Registration

`ModRegistries.init(bus)` is the aggregator. Registration order matters: blocks → items → block
entities → menus → fluid types → fluids → recipe types/serializers → creative tabs → data components →
structure types → entities → damage types → display sources → stress values.

`ResourcefulRefinementMain`:
- **Common:** `ModStressValues::register` (Create kinetic impacts/capacities).
- **Capabilities** (`registerCapabilities`): NeoForge fluid/item handlers wired per block entity —
  refinery controller + proxies, sieve stack, forge mould, casting depot, fracking outlet, paint nozzle,
  refill station, distillery, radiator, combustion chamber, fuel tank, milking station, hosegun/fuel-tank
  items. Multiblocks delegate to a **controller** BE.
- **Client** (`ClientModEvents`): BER registration, layer definitions, fluid/bucket tinting, partial models.

### Registry classes

| Class | Role |
|---|---|
| `ModBlocks` | All placeable blocks (machines, decoratives, 5 gel-splatter variants). Fluid liquid-blocks come via `FluidEntry`. |
| `ModItems` | Block items, tools, moulds, materials, drinks/foods. Buckets come via `FluidEntry`. |
| `ModBlockEntities` | BE type suppliers (one shared `GEL_SPLATTER_BE` across all gel variants). |
| `ModMenus` | Fluid Refill Station, PUG `launchpad`, GLARE `power_terminal`, `glare_chromatic_transceiver`, `glare_telemetry_terminal`. |
| `ModEntities` | `GEL_BLOB`, `THROWN_PLUNGER`, `SPORTS_BALL`, `MILKING_STATION_SEAT`, `PUG` (v0.4). |
| `ModFluids` / `FluidEntry` / `ModFluidTypes` | Declarative fluid registration (type + source + flowing + block + bucket in one call). |
| `ModRecipeTypes` | Custom recipe types, serializers, Create `IRecipeTypeInfo` wrappers. |
| `ModDataComponents` | `coating_data`, `hosegun_fluid`, `fuel_tank_fluid`, `hosegun_tracking_id`, `hosegun_gloopy`, `plunger_charging`, `flavour`, `ball_type`, and (v0.4) `glare_targets`, `relay_wrench_target`. |
| `ModCreativeTab` | Two tabs: `main` (items/blocks) and `fluids` (buckets). |
| `ModStructureTypes` | `nether_ground_jigsaw` → `NetherSurfaceJigsawStructure`. |
| `ModDamageTypes` | `molten_gel_damage`. |
| `ModDisplaySources` | Create display source for the Refill Station. |
| `ModStressValues` | Impacts: Refinery 12, Fracking 16, Sieve 4, Forge 8, Advanced Pump 8, Milking 4, Bucket Excavator 16, Drill Pylon 16, Cyclotron 32, GLARE Emitter 8. Capacity: Combustion Chamber 10. |
| `ModSounds` (v0.4) | PUG sounds: `pug_launch`, `pug_flight`, `pug_land`, `pug_crash`. |
| `ModEffects` (v0.4) | Mob effect registration. |
| `ModNetworking` | C2S/S2C payloads incl. Refill Station tracking id and the GLARE payloads (`glare_link_sync`, `configure_glare_transceiver`, `telemetry_terminal_action`/`_state`, `toggle_glare_network`). |
| `ModPartialModels` | Flywheel partials (shafts, geyser casings, heater stand, pump cog, combustion fans). |
| `ModJeiPlugin` | JEI recipe categories (incl. virtual `RadiatorVirtualHeatingCategory`). |
| `ModClientEvents` / `ModClientGameEvents` | Fluid fog/tint, gel colours, coating decorator, ponder plugin, tooltips. |
| `ModToolEvents` | Server-side coating and drink-flavour effects. |

## Content by System

Feature packages under `content/`. Category tags match the doc-template categories.

### Resource refinement (v0.1 core)
- **`refinery`** — [[Fluid Refinery]] multiblock (access-port controller + invisible/kinetic proxies),
  [[Blender Blade]] kinetic component, segmented renderer, `fluid_refinery` recipes.
- **`sieve`** — [[Mechanical Sieve]] (vertical stack), `mechanical_fluid_sieve` recipes.
- **`forge_mould`** + **`casting_depot`** — [[Forge Mould]], Casting Depot, `mechanical_forge_mould`
  recipes (with `"casting": true` for depot mode). Also hosts the `coating` recipe type.
- **`fracking_pump`** + **`geyser`** — [[Fracking Pump]] multiblock over [[Geyser Block]] worldgen
  sources, `fracking_pump` recipes.
- **`moulds`** + materials — consumable mould items (`ingot_mould`, `shaft_mould`), `ferrous_crystal`,
  `flux_dust`, `durasteel_ingot`/`_sheet`, `graphite`, `graphene_mesh`, `paint_blob`, etc.

### Fluids
Declared via `ModFluids` + `FluidEntry`, grouped by `FluidGroup`: `RAW`, `CATALYSED`, `ALLOYED`,
`PURIFIED`, `CARBORAX`, `CONCRETE`, `DRINK`, `PAINT`. Framework in `content/fluids/base/`
(`GeneralizedFlowingFluid`, `GeneralizedFluidType`). `PouredCementBlock` is a special liquid block that
solidifies. v0.4 adds `liquid_chorus` (ALLOYED) — fuel for the Remote Entanglement Transporter. See
[[Fluid Properties]] for group-by-group tables.

### GLARE network & logistics (v0.4)
- **`glare`** — the [[GLARE Networks]] system: a server-side graph of line-of-sight-linked nodes carrying
  **Lux** power, colour charge, an email-style telemetry system, and teleportation. Subpackages `graph`,
  `lux`, `remote`, `telemetry`, `terminal`, `common`, `rendering`, `ui`. Blocks: [[GLARE Emitter Dish]],
  [[GLARE Relay]], [[GLARE Kinetic Receiver]], [[GLARE Chromatic Transceiver]], [[GLARE  Lux Transceiver]]
  (LuxSocket bridge), [[Telemetry Terminal GUI]], [[Remote Entanglement]] (depot + transporter),
  [[Resonance Crystal]]. Linked with the [[Relay Wrench]]. `GlareService`/`GlareSavedData` own the graph;
  `TelemetryService` the inboxes; `GlareCommands` (`/rrglare`) and `GlareGameTests` support it.
- **`gui`** — shared GUI layer incl. the GLARE **Power Terminal** (`power_terminal` menu) and
  `GlareNetworkSnapshot(Provider)` that machines expose for the Lux overlay. `client/gui/widget/` holds
  reusable widgets (address editor, scrollbar, text area).
- **`cyclotron_forge`** — [[Cyclotron Forge]] linear multiblock (item+fluid processing), `cyclotron_forge`
  recipes; kinetic + variable-Lux driven via a LuxSocket. (The vault's "Harmonic Cyclotron" is the design
  name for this same block.)
- **`drill_pylon`** — [[Drill Pylon]] multiblock over a [[Crystal Fissure Bud]], `drill_pylon` recipes;
  kinetic + variable-Lux (turbo) driven.
- **`bucket_excavator`** — [[Bucket Excavator]] kinetic block extracting from a [[Mineral Deposit]] via
  `excavation` recipes (extraction/destruction modes).
- **`pug`** — [[PUG & Launch Pads]]: a 3×3 launch-pad multiblock and the `PUG` vehicle entity that carries
  cargo between addressed pads (partly as a live entity, partly as simulated flight data).

> [!note] v0.4 acquisition gaps
> The v0.4 resource nodes have **no natural worldgen yet**: [[Mineral Deposit]], [[Crystal Fissure Bud]]
> and [[Resonance Crystal]] are placeable/creative-only, and there is no Choral Clusters biome or Chorus
> Crystal in code. The [[Resource Resonator]] and [[Conveyor Belt]]/[[Conveyor Rotator]] remain design-only.

### Paint, gel & tools (v0.2)
- **`hosegun`** — [[Hosegun]] fluid tool firing `GelBlobEntity` projectiles; gloopy mode, tracking ids.
- **`paint_nozzle`** — [[Paint Nozzle]] pipe-facing sprayer.
- **`gel_splatter`** — [[Gel Splatter]] multi-face gel blocks; `GelType` + tag-driven `GelPropertiesManager`.
- **`gel_tracking`** — server saved-data tracking for gel counts, feeding refill stations + display links.
- **`refill_station`** — [[Fluid Refill Station]] with menu/screen, tracking-id binding, display source.
- **`plunger`** — [[Plunger]] thrown tool that drains fluid containers.

### Coatings
Tool coatings stored as the `coating_data` **data component** (not raw NBT). `CoatingType` enum,
`CoatingData` record, `CoatingItemDecorator` (durability overlay), `CoatingRecipeCategory`, gameplay in
`ModToolEvents`, durability interplay via `ItemStackMixin`. See [[Coating]] / [[Coating Variants]].

### Heat, fuel & v0.3 machinery
- **`distillery`** — stackable heat-driven fluid tower, `distillery` recipe type.
- **`radiator`** — pipe-like heat/cool block, implements Create `BoilerHeater`; consumption is config-driven.
  See [[Radiator]].
- **`combustion_chamber`** — carborax-fuel kinetic engine (stress capacity), Create fan integration.
- **`fuel_tank`** — portable/placeable fluid tank; item stores fluid via data component + capability.
- **`milking_station`** — seats a mob (seat entity) and extracts fluid/item output; `milking_station` recipes.
- **`brewers_tap`** — [[Brewer's Tap]] dispenses `DrinkItem`s carrying `FlavourType` effects; `brewers_tap` recipes.
- **`advanced_pump`** — doubles Create pump range; redstone reverses flow; goggle throughput readout.
- **`utilities/heating`** — `ExtendedHeatCondition` (CHILLED / COOLED / NONE / PASSIVE / HEATED /
  SUPERHEATED) + `HeatUtilities`. See [[ExtendedHeatCondition]].

### Decorative & misc
- **`plushie`** — Fox Plushie block + BE.
- **`sports_ball`** — bouncing entity item with dispense behaviour.
- **`worldgen`** — `GeyserOffsetManager`, `NetherSurfaceJigsawStructure` (anchors geysers to the nether
  cave floor).
- **`network`** — `ModNetworking`; C2S `SetRefillStationTrackingIdPayload`.
- **`ponders`** — Ponder scenes for nearly every machine (see `ModPonders`).

## Recipe Types

Registered in `ModRecipeTypes` (namespace `resourceful_refinement`):

| JSON `type` | Recipe class | Serializer | Machine |
|---|---|---|---|
| `fluid_refinery` | `FluidRefineryRecipe` | bespoke | [[Fluid Refinery]] |
| `mechanical_fluid_sieve` | `MechanicalSieveRecipe` | Create `StandardProcessingRecipe.Serializer` | [[Mechanical Sieve]] |
| `mechanical_forge_mould` | `MechanicalForgeMouldRecipe` | bespoke (`casting` bool) | [[Forge Mould]] / Casting Depot |
| `coating` | `CoatingRecipe` | bespoke | Applied via forge, not a station GUI |
| `fracking_pump` | `FrackingPumpRecipe` | bespoke | [[Fracking Pump]] |
| `distillery` | `DistilleryRecipe` | bespoke | Distillery |
| `milking_station` | `MilkingStationRecipe` | bespoke | Milking Station |
| `brewers_tap` | `BrewersTapRecipe` | bespoke | [[Brewer's Tap]] |
| `excavation` | `ExcavationRecipe` | bespoke (Create `StandardProcessingRecipe`-based) | [[Bucket Excavator]] |
| `drill_pylon` | `DrillPylonRecipe` | bespoke | [[Drill Pylon]] |
| `cyclotron_forge` | `CyclotronForgeRecipe` | bespoke | [[Cyclotron Forge]] |

That is **11** registered recipe types. Datapack folders under `data/resourceful_refinement/recipe/` also
carry `shaped_crafting`, Create `mixing`, `mechanical_crafting`, and the per-machine folders above. See
[[Fluid Processing Recipes]].

## Rendering Architecture

Two client paths:
1. **Block entity renderers (BER)** — registered in `ClientModEvents`. Used for world multiblocks and
   kinetic animation.
2. **Item BEWLR** — `BlockEntityWithoutLevelRenderer` subclasses wired through
   `Item.initializeClient(IClientItemExtensions)` for items needing 3D in hand/GUI.

**Segmented multiblocks** (refinery, fracking pump, sieve, distillery): a `*Layers` enum registers layer
definitions; the controller/outlet BER iterates height and draws base + repeated middle + top models.
Proxy blocks are invisible collision/connection shells. Kinetic BEs extend `KineticBlockEntity`;
controllers extend `SmartBlockEntity`; stress via `ModStressValues`.

## Multiblock & Controller Pattern

Multiblocks use a **controller + proxy** layout. Proxies store an offset from the controller and forward
capabilities/interaction; the controller holds inventories, recipe progress, and render state. Assembly
replaces structure blocks with proxies (storing original states for disassembly); breaking any proxy or
invalid block disassembles. `RefineryStructureHelper` validates the refinery footprint (**3×3** in code,
despite some docs saying 3×3-or-5×5). Sieve/distillery stacks share one controller at `stackIndex == 0`.

## Utilities, Config, Mixins, Worldgen

- **`utilities/`** — `FluidBoxRendering`, `FluidGelTooltipHelper`, `GoggleUtilities`, `ShaftUtilities`,
  and `heating/` (`ExtendedHeatCondition`, `HeatUtilities`).
- **`config/ServerConfig`** — `ModConfigSpec`; radiator coolant-consumption rates + example setting.
- **`mixin/`** — Create/vanilla patches: basin operating, belt inventory, cat/wolf collar accessors
  (gel paint collars), simple registry, encased fan (combustion integration), fluid network, hose pulley
  and open-ended pipe fluid handlers, item stack (coating durability), jigsaw placement + structure pool
  (geyser offset), and `client/LocalPlayerMixin` (removes hosegun use-slowdown).
- **`worldgen/`** — `GeyserOffsetManager` (loads template-pool bottom offsets), `NetherSurfaceJigsawStructure`.

## Ponders & JEI

- **Ponders:** `ModPonders` registers scenes across forge/casting, sieve, refinery, fracking, gel/hosegun,
  radiator, distillery, combustion chamber, milking station, pump, brewer's tap, fluid/cement.
- **JEI:** `ModJeiPlugin` registers a `*RecipeCategory` per recipe type plus the virtual
  `RadiatorVirtualHeatingCategory`.

## Agent Cheat Sheet — Where to Edit

| Task | First files |
|---|---|
| New block/item/BE ID | `ModBlocks`, `ModItems`, `ModBlockEntities` |
| New fluid | `ModFluids.register(...)`, `FluidGroup`, tint in `ModClientEvents` |
| New machine recipe | `ModRecipeTypes`, recipe JSON, `*Recipe.java`, BE tick/match logic |
| Multiblock layout | `RefineryStructureHelper` or the outlet/controller BE's assembly code |
| Face IO / capabilities | `ResourcefulRefinementMain.registerCapabilities` + controller handler methods |
| World rendering | `*Renderer`, `*Layers`, `*Model`, register in `ClientModEvents` |
| Hand/item 3D | `*Item` + `*ItemRenderer` |
| Stress / capacity | `ModStressValues` |
| Heat states | `utilities/heating/ExtendedHeatCondition`, `HeatUtilities` |
| Coatings | `content/coating`, `ModToolEvents`, `ItemStackMixin` |
| Worldgen geyser | `data/.../worldgen/`, `GeyserOffsetManager`, `GeyserBlockEntity` |
| Balance / chains | `data/.../recipe/` + [[Fluid Processing Recipes]] |
| Generated assets | `data/ModDataGenerators` → run `runData` |

## Known Implementation Notes

- `casting_mould` from early design is **not** a separate recipe type — use `mechanical_forge_mould`
  with `"casting": true`.
- `REFINERY_PROXY`, `REFINERY_KINETIC_PROXY`, `FRACKING_PUMP_PROXY` are assembly-only (no survival item).
- Coatings use **data components** (1.21+), not NBT attachments.
- Heat uses the six-state `ExtendedHeatCondition`, not Create's two-state model.
- The refinery footprint is **3×3** in code.

## Planned / Not Yet Implemented

The v0.4 branch implemented most of the former v0.4 design targets — GLARE (networks, emitter dish, relay,
kinetic receiver, chromatic transceiver, lux transceiver, telemetry terminal, remote entanglement),
[[Cyclotron Forge]], [[Drill Pylon]], [[Bucket Excavator]] and [[PUG & Launch Pads]] are all in code now.

Still design-only (docs exist, no code): [[Conveyor Belt]], [[Conveyor Rotator]], [[Resource Resonator]].

Partially implemented / known gaps:
- **Worldgen for the v0.4 resource nodes** — [[Mineral Deposit]], [[Crystal Fissure Bud]] and
  [[Resonance Crystal]] have no natural generation; no Choral Clusters biome or Chorus Crystal exists yet.
- **[[Resonance Crystal]]** has no crafting recipe (artificial variant) or worldgen — acquisition unimplemented.
- **[[Mineral Deposit]]** is a plain block, not yet the stored-resource-type block entity the design describes.

---

*Maintained for agent onboarding. Update when adding major features or changing multiblock/capability
contracts, and re-run a code sweep before trusting the tables above.*
