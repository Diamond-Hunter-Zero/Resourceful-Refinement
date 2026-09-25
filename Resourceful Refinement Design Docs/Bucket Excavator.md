---
title: Bucket Excavator
category: Machine
status: Planned
introduced: v0.4
recipe_type: n/a
related:
  - "[[Drill Pylon]]"
  - "[[Primary Design Doc]]"
tags:
  - machine
  - kinetic
  - worldgen
---

The Bucket Excavator is a block entity consisting of a cuboid mount and an extended bucket-wheel, which passively generates resources when placed over mineral deposits, or slowly mines blocks when switched to 'destruction' mode.

**ID:** *bucket_excavator*

## Gameplay Role

The Bucket Excavator is the surface-extraction machine of the v0.4 tier: parked over [[Mineral Deposits]] and driven by kinetic input, it passively produces simple resources (stone variants, dyes, and other recipe outputs), or acts as a wide area-miner in destruction mode. It pairs with the [[Drill Pylon]], which handles deeper crystal extraction.

## Construction & Placement

The Bucket Excavator is a single block entity, which can be horizontally rotated.

Extending beyond the front face of the block (or "mount") is a large polyhedral bucket-wheel, which measures 3 blocks tall, 3 blocks deep, and 1 block wide. The wheel is positioned in front of the mount, its axis of rotation parallel to the mount's shaft axis and at the same y-position, such that the entire bounding volume of the bucket excavator appears to be 3x4x1.

![[bucket_wheel_excavator_block_test_render.png|388]]

Bucket Excavators occupy a 5x4x1 'excavation volume' encapsulating their wheel. If the excavation-volumes of two or more Bucket Excavators overlap, they won't function or produce resources. Bucket Excavators can be tiled laterally.

![[bucket_wheel_excavator_volumes.png|498]]

## Inputs & Outputs

The mount's back face exposes an item-output interface, and its left/right side faces act as a shaft transferring kinetic input.

The Bucket Excavator mount has an internal inventory of up to 4 slots. Outputs from the excavation wheel always try to be placed inside its inventory, not dropped on the ground. If a processing cycle cannot fit all outputs into this inventory, they are dropped as item entities below the wheel.

## Operation

When provided with kinetic input, the bucket-wheel rotates, indefinitely producing resources, or mining blocks within its 5x4 volume.

The Bucket Excavator has two modes; "Excavation" and "Destruction". The user can toggle between them by using a Create ScrollOptionBehaviour on the top face of the mount block entity.

The duration of a processing cycle is fixed, regardless of input speed. While provided with kinetic input (above a minimum speed threshold), the bucket excavator repeatedly processes production cycles.

**If in 'Excavation' Mode:** At the end of each cycle, it checks all blocks overlapping with its excavation-volume, and produces resources according to any matching recipes for each block.

**If in 'Destruction' Mode:** Throughout the processing cycle, the wheel slowly incrementally breaks all blocks in its excavation volume, like a Create Drill. It will not break any blocks that a Create Drill would not break, or any Mineral Deposits or Geysers.

The excavator block entity also responds to redstone input from any of its faces; when powered, the wheel freezes, and pauses processing.

### Mineral Deposits

To produce resources, a bucket excavator's excavation-volume must overlap with a Mineral Deposit block (*mineral_deposit_node*).

A Mineral Deposit is a block entity which stores a reference to a block type. It uses the texture asset of this block for its own texture resource. Players in Creative mode can set the source block stored inside a Mineral Deposit by right-clicking with any block. By default, it stores stone. (This entire behaviour is the block equivalent of the [[Geyser Block]] and its functionality with fluids.)

Mineral Deposits can be mined by pickaxes, but take a while, and won't drop anything except their source block.

## Recipes

The bucket excavator defines a special recipe-type (design name *bucketExcavationRecipe*), which allows the mod to define a complex extraction relationship between Mineral Deposits/Blocks and Excavators. A bucketExcavationRecipe consists of:
- An input block ID (the block which must be inside the excavation volume for this recipe to match)
- An **optional** source block ID as an item ID or ItemTag (the item a Mineral Deposit must store for this recipe to match. If not provided, only the input block is used for matching)
- A SizedIngredient output item produced (the output(s) produced by a production cycle)

## Rendering

The Bucket Excavator uses a large protruding block entity renderer for the bucket-wheel, which extends beyond the mount's own block (the visible bounding volume is 3x4x1). The wheel rotates with kinetic input.

## Implementation

Not yet implemented — design target for v0.4.

## Related

- [[Drill Pylon]]
- [[Mineral Deposits]]
- [[Geyser Block]]
- [[Primary Design Doc]]
