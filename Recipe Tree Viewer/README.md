# Recipe Tree Viewer

Isolated tooling for exploring **Resourceful Refinement** processing chains. Reads recipe JSON from the mod datapack and builds dependency trees for a target item or fluid.

## Supported recipe types

| JSON `type` | Machine |
|---|---|
| `resourceful_refinement:fracking_pump` | Fracking Pump |
| `resourceful_refinement:fluid_refinery` | Fluid Refinery |
| `resourceful_refinement:mechanical_fluid_sieve` | Mechanical Fluid Sieve |
| `resourceful_refinement:mechanical_forge_mould` | Mechanical Forge Mould |
| `resourceful_refinement:coating` | Mechanical Forge Mould (Coating) |
| `create:mixing` | Mixing Basin |
| `create:mechanical_crafting` | Mechanical Crafter |

Vanilla crafting (`minecraft:crafting_*`, `minecraft:stonecutting`) is **not** expanded — those inputs appear as **external/raw**.

## Requirements

- Python 3.10+ (stdlib only; no pip packages)

## CLI

From this directory:

```bash
python cli.py resourceful_refinement:purified_iron 750
python cli.py liquid_glue 250 --kind fluid
python cli.py create:super_glue 4 --json
```

Options:

- `--kind item|fluid|coating` — force resource type
- `--recipe-root PATH` — override recipe folder (default: `../src/main/resources/data/resourceful_refinement/recipe`)
- `--json` — machine-readable output
- `--max-depth N` — recursion limit (default 32)
- `--provided ID` — unlimited-supply inputs (repeatable)
- `--select PATH:RECIPE` — recipe override at a branch or shared path
- `--simplify` — merge duplicate producible ingredients into shared production nodes

## Web UI

```bash
python server.py
```

Open [http://127.0.0.1:8765](http://127.0.0.1:8765), enter a target ID and amount, then **Build tree**. Enable **Simplify tree** to merge duplicate production into shared nodes (see below).

Use **Refresh recipes** to reload JSON from disk without restarting the server. If a tree is already displayed, it is rebuilt with your current selections preserved.

### Session config (export / import)

Save and restore your full view state as a JSON config file:

- **Export config** — downloads target, amount, kind, simplify flag, provided inputs, recipe selections, and (if a tree is built) a snapshot of cumulative totals, duration, and index stats.
- **Import config** — restores the saved view, rebuilds the tree, and shows a **Changes since exported config** panel when the snapshot differs (e.g. after recipe JSON edits).

Example config shape:

```json
{
  "version": 1,
  "name": "Purified iron 750 mB",
  "exported_at": "2026-06-03T12:00:00.000Z",
  "view": {
    "target": "resourceful_refinement:purified_iron",
    "amount": 750,
    "kind": "",
    "simplify": true,
    "provided_inputs": ["minecraft:water"],
    "recipe_selections": { "root": "purified_iron" }
  },
  "snapshot": {
    "index_stats": { "total_recipes": 120 },
    "total_duration_seconds": 42.5,
    "summary": { "inputs": [], "outputs": [] },
    "recipe_selections": { "root": "purified_iron" }
  }
}
```

API: `POST /api/reload` forces the server to re-read recipe JSON from disk.

## Tree behaviour

- **Proportional scaling:** each step uses `scale = needed / recipe_primary_output`. Inputs, byproducts, and duration are multiplied by that factor — no rounding up to whole crafts for dependency math.
- **Operational reference:** each recipe step also shows how many **whole in-game batches** would be required (`ceil(scale)`) and the **primary output excess** that minimum batching would waste.
- **Byproducts** are scaled proportionally (including chance) and listed on the step that creates them.
- **Fracking** adds a world-source leaf for `source_block` / optional `source_fluid`.
- **Cycles** stop expansion and mark the node to avoid infinite loops.
- When multiple recipes produce the same resource, the tool picks the **shortest proportional duration** path and notes alternates.

## Provided inputs

Optionally mark item/fluid IDs as **provided** (assumed unlimited supply). The tree stops expanding at those nodes but still shows the **scaled amount required** at each step.

CLI:

```bash
python cli.py create:super_glue 1 --provided minecraft:water --provided create:honey
python cli.py purified_iron 150 --provided minecraft:water,purified_gold
```

Web UI: use the **Provided inputs** list in the sidebar (add/remove rows).

Matching is by normalised id (`water` → `minecraft:water` only if you omit namespace; `purified_gold` → `resourceful_refinement:purified_gold`).

## Recipe selection (interactive UI)

When multiple recipes produce the same resource, each recipe node includes a **dropdown** listing all alternatives (with machine, proportional time, and scale preview).

- Selections are keyed by **branch path** (e.g. `root`, `root.c2`, `root.c1.c0`) so the same fluid can use different recipes in different parts of the tree.
- Changing a dropdown rebuilds the tree from the server with your choices preserved elsewhere.
- Default selection is the fastest proportional route when no choice is set.

## Cumulative overview

The bottom of each tree shows **scaled totals** aggregated across the active branch:

- **Combined inputs** — leaf requirements only (external/raw, provided, and world sources). Intermediate fluids/items produced and re-used within the tree are not double-counted.
- **Combined outputs** — scaled byproducts plus primary **batch excess** from whole-batch rounding at each step.

CLI and JSON responses include the same data under `summary`.

CLI override:

```bash
python cli.py purified_iron 150 --select root.c2:purified_gold_alt
```

## Simplify mode (shared production)

When **Simplify** is enabled (CLI: `--simplify`, web UI checkbox, API: `simplify=1`), identical producible ingredients across branches are merged into **shared production nodes** instead of duplicating the same subtree.

- The main tree shows `shared_ref` leaves pointing at combined demand (e.g. `~ 222 mB lava [shared ref -> 358.667 total production]`).
- Full production subgraphs live under **Shared production nodes**, built once at the **summed** amount required by all consumers.
- Recipe dropdowns on shared nodes use paths like `shared/fluid/minecraft/lava` (colons in IDs become `/`).
- The **cumulative overview** walks each shared subgraph once, so raw inputs and batch excess are not double-counted when multiple branches need the same fluid or item.

```bash
python cli.py resourceful_refinement:purified_iron 150 --simplify
python cli.py resourceful_refinement:purified_iron 150 --simplify --select shared/fluid/minecraft/lava:lava_sieving
```

## Examples

```bash
# Purified iron chain (refinery + upstream fluids)
python cli.py resourceful_refinement:purified_iron 750

# Super glue from liquid glue (forge mould)
python cli.py create:super_glue 1

# Molten asurine from fracking
python cli.py resourceful_refinement:molten_asurine 100 --kind fluid
```

## Limitations

- Tag ingredients (`create:valve_handles`, etc.) are shown as external tags — not resolved.
- Coating recipes apply a named coating to tools; output is `coating:<name>`, not a normal item stack.
- Create / Minecraft recipes outside this mod's datapack folder are not indexed.
- Does not model RPM, heat fuel, sieve stack size, or fracking counterweight tiers — only JSON `processing_time` and listed ingredients.
