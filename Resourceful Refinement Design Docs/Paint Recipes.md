---
title: Paint Recipes
category: Recipe
status: Implemented
introduced: v0.2
recipe_type: resourceful_refinement:fluid_refinery
related:
  - "[[Paint Fluids]]"
  - "[[Fluid Refinery]]"
  - "[[Forge Mould]]"
  - "[[Fluid Processing Recipes]]"
tags:
  - recipe
  - paint
  - fluid
---

The recipes below produce the 16 [[Paint Fluids]] in the [[Fluid Refinery]], plus the liquid-glue
intermediate they depend on and the Forge Mould glue products that ship alongside them.

## Recipe Type

- **`resourceful_refinement:fluid_refinery`** — paint and liquid-glue recipes.
  Paint recipes live under `data/resourceful_refinement/recipe/fluid_refinery/paint/`; the glue
  recipes under `fluid_refinery/alloys/`.
- **`resourceful_refinement:mechanical_forge_mould`** — the liquid-glue Forge Mould products, under
  `recipe/mechanical_forge_mould/`.

## Format

Shipped paint recipe (`fluid_refinery/paint/red_paint_from_dye.json`):

```json
{
  "type": "resourceful_refinement:fluid_refinery",
  "heat_requirement": "none",
  "processing_time": 320,
  "ingredients": [
    { "item": "minecraft:red_dye" },
    { "type": "neoforge:single", "fluid": "minecraft:water", "amount": 500 },
    { "type": "neoforge:single", "fluid": "resourceful_refinement:liquid_glue", "amount": 20 }
  ],
  "results": [
    { "id": "resourceful_refinement:red_paint", "amount": 250 }
  ]
}
```

## Progression

### Paint fluids (Fluid Refinery)

Every colour uses the same recipe shape: **500mb water + 20mb liquid_glue + 1 `{colour}_dye` → 250mb
`{colour}_paint`**, no heat, 320 ticks. This holds for all 16 (verified across the shipped
`*_paint_from_dye.json` files).

| Fluid 1     | **Fluid 2**      | Input Item 1   | Heated? | Time | Output Fluid            |
| ----------- | ---------------- | -------------- | ------- | ---- | ----------------------- |
| water 500mb | liquid_glue 20mb | red_dye        |         | 320  | red_paint 250 mb        |
| water 500mb | liquid_glue 20mb | orange_dye     |         | 320  | orange_paint 250 mb     |
| water 500mb | liquid_glue 20mb | yellow_dye     |         | 320  | yellow_paint 250 mb     |
| water 500mb | liquid_glue 20mb | lime_dye       |         | 320  | lime_paint 250 mb       |
| water 500mb | liquid_glue 20mb | green_dye      |         | 320  | green_paint 250 mb      |
| water 500mb | liquid_glue 20mb | cyan_dye       |         | 320  | cyan_paint 250 mb       |
| water 500mb | liquid_glue 20mb | light_blue_dye |         | 320  | light_blue_paint 250 mb |
| water 500mb | liquid_glue 20mb | blue_dye       |         | 320  | blue_paint 250 mb       |
| water 500mb | liquid_glue 20mb | purple_dye     |         | 320  | purple_paint 250 mb     |
| water 500mb | liquid_glue 20mb | pink_dye       |         | 320  | pink_paint 250 mb       |
| water 500mb | liquid_glue 20mb | magenta_dye    |         | 320  | magenta_paint 250 mb    |
| water 500mb | liquid_glue 20mb | white_dye      |         | 320  | white_paint 250 mb      |
| water 500mb | liquid_glue 20mb | light_gray_dye |         | 320  | light_gray_paint 250 mb |
| water 500mb | liquid_glue 20mb | gray_dye       |         | 320  | gray_paint 250 mb       |
| water 500mb | liquid_glue 20mb | black_dye      |         | 320  | black_paint 250 mb      |
| water 500mb | liquid_glue 20mb | brown_dye      |         | 320  | brown_paint 250 mb      |

> [!note] Implementation
> The original design specified **25mb** liquid glue per paint; the shipped recipes use **20mb**.
> Water (500mb), output (250mb), heat (none) and time (320) all match the design.

### Liquid glue (Fluid Refinery)

Now shipped (originally flagged as "want to add"):

| Fluid 1     | **Fluid 2**  | Input Item 1 | Heated? | Time | Output Fluid       |
| ----------- | ------------ | ------------ | ------- | ---- | ------------------ |
| water 500mb |              | slime_ball   |         | 100  | liquid_glue 250 mb |
| water 500mb | honey 250 mb |              |         | 100  | liquid_glue 250 mb |

> [!note] Implementation
> Shipped as `fluid_refinery/alloys/liquid_glue_from_slime_ball` (slime_ball + 500mb water → 250mb,
> 100 ticks) and `liquid_glue_from_honey` (500mb water + 250mb `create:honey` → 250mb, 100 ticks).
> Matches the design. Liquid glue can also be produced elsewhere (e.g. the [[Distillery]] has a
> `liquid_glue_distilling` recipe).

### Liquid glue products (Forge Mould)

Now shipped (originally flagged as "want to add"):

| Input Fluid       | Input Item               | Casting? | Time | Output Item                     |
| ----------------- | ------------------------ | -------- | ---- | ------------------------------- |
| liquid_glue 400mb | create:iron_sheet        | No       | 60   | create:super_glue               |
| liquid_glue 175mb | piston                   | No       | 60   | sticky_piston                   |
| liquid_glue 175mb | create:mechanical_piston | No       | 60   | create:sticky_mechanical_piston |
| liquid_glue 175mb | string                   | No       | 40   | lead                            |

> [!note] Implementation
> Shipped as `mechanical_forge_mould/super_glue_from_liquid_glue` (400mb + create:iron_sheet → 1
> create:super_glue, 60), `sticky_piston_from_liquid_glue` (175mb + piston → sticky_piston, 60),
> `sticky_mechanical_piston_from_liquid_glue` (175mb, 60), and `lead_from_liquid_glue` (175mb + string
> → lead, 40). All `"casting": false`. Amounts and times match the design.

## Related

- [[Paint Fluids]]
- [[Fluid Refinery]]
- [[Forge Mould]]
- [[Fluid Processing Recipes]]
