---
title: Conveyor Belt
category: Machine
status: Planned
introduced: v0.4
recipe_type: n/a
related:
  - "[[Conveyor Rotator]]"
tags:
  - machine
  - kinetic
---

The Conveyor Belt is a conveyor-like belt which, instead of moving items, moves blocks above it.

**ID:** *conveyor_belt*

## Gameplay Role

The Conveyor Belt is a block-transport primitive: a Create-style belt reworked to physically shuffle placed blocks along its length rather than carry items or entities. It underpins block-moving factory setups and pairs with the [[Conveyor Rotator]], which reorients directional blocks before they continue down a line.

## Construction & Placement

The Conveyor Belt places like a standard Create mechanical_belt, utilising the same logic and client mid-placement visuals. However, Conveyor Belts cannot form vertical or diagonal connections! They may only be placed horizontally. They also have a much lower maximum distance, spanning no more than 8 blocks.

Like Create Mechanical Belts, breaking a segment in a Conveyor Belt train destroys all other segments, and drops a single item. Players can also extend a placed belt by right-clicking an end-segment with the Conveyor Belt item (which does not consume the item). Additional shafts may also be inserted into any empty segment by right-clicking (which consumes the shaft, as per normal).

## Inputs & Outputs

The belt takes kinetic input via its shafts (as a Create belt does) and transports blocks placed directly above it. Its hitbox (and visuals) are a full block in height.

## Operation

The Conveyor Belt moves blocks, not items or entities.

While receiving kinetic input, a Conveyor Belt will move blocks directly above it as if they were pushed by a piston in the direction of the belt's rotation. A block will only move if the space it would be pushed into is empty, and the block is movable by piston. Blocks which are marked as immovable, or drop when pushed by pistons, will not be moved, and will block other blocks. For a belt train, blocks are moved in order from the front of the train to the back.

The rate at which blocks are moved is determined by the speed of the belt:

| Abs(Speed)    | Every X ticks |
| ----------------- | ------------- |
| speed >= 128      | 1             |
| 64 <= speed < 128 | 2             |
| 16 <= speed < 32  | 3             |
| speed < 16        | 4             |

## Rendering

The Conveyor Belt should use the same rendering pipeline/setup approach as Create's Mechanical Belts, as it will need to operate in much the same manner.

## Implementation

Not yet implemented — design target for v0.4.

## Related

- [[Conveyor Rotator]]
