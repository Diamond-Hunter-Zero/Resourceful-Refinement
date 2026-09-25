---
title: Harmonic Cyclotron
category: Machine
status: Planned
introduced: v0.4
recipe_type: resourceful_refinement:harmonic_cyclotron
related:
  - "[[GLARE Networks]]"
tags:
  - machine
  - glare
  - multiblock
  - kinetic
  - fluid
---

The Harmonic Cyclotron is a multiblock processing machine which uses kinetic input and Lux to produce both items and fluids.

**ID:** *cyclotron_controller*

## Gameplay Role

The Harmonic Cyclotron is a high-end processing multiblock that combines Create kinetics with [[GLARE Networks|GLARE]] Lux to run recipes yielding both an item and a fluid at once. Its variable length and its Lux curve tie it into the wider GLARE power economy, and its recipes frequently pair a desirable product with a by-product that must be dealt with.

## Construction & Placement

A cyclotron assembly consists, at minimum, of an input-cap, and output-cap, and a series of intermediary coil segments in-between, all placed as horizontal slices. Each cap/slice measure 3 block wide by 3 blocks high, and the assembly has a total length of (2+N) blocks deep, where N is the number of coil segments.

## Inputs & Outputs

Both the input and output caps each have interfaces for items, fluids, and kinetic rotation, while the side faces any middle-edge block of the output cap acts as a GLARE receiver sockets for Lux input while assembled.

## Operation

Cyclotron recipes will specify a length that the assembly must match in order to craft that recipe. Cyclotron recipes also specific a Lux curve which determines the amount of Lux allocated to the cyclotron as it progresses over the crafting cycle. See [[GLARE Networks]] for how the Variable Lux curve is normalised over a processing cycle.

## Recipes

The Cyclotron accepts up to 2 item inputs and 2 fluid inputs in its recipes. Cyclotron recipes will often produce both item and fluid outputs at once. Typically, one is considered an undesirably by-product that must be disposed of.

Cyclotron recipes will specify a length that the assembly must match in order to craft that recipe. Cyclotron recipes also specific a Lux curve which determines the amount of Lux allocated to the cyclotron as it progresses over the crafting cycle.

## Implementation

Not yet implemented — design target for v0.4.

## Related

- [[GLARE Networks]]
