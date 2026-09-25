---
title: Conveyor Rotator
category: Machine
status: Planned
introduced: v0.4
recipe_type: n/a
related:
  - "[[Conveyor Belt]]"
tags:
  - machine
  - kinetic
---

The Conveyor Rotator is a utility counterpart for the [[Conveyor Belt]], which allows users to rotate directional blocks on top of it.

**ID:** *conveyor_rotator*

## Gameplay Role

The Conveyor Rotator sits inline with [[Conveyor Belt]] lines and reorients directional blocks as they pass, then hands them onward. It lets block-transport setups control the facing of the blocks they move — orienting directional blocks to a target facing before they continue down the belt.

## Construction & Placement

The Conveyor Rotator is a horizontally directional block. As well as the standard blockstate FACING property, its block entity also internally records an 'OutputDirection' property, which is aligned to match its FACING property when placed by a player.

## Inputs & Outputs

The Rotator takes kinetic rotation as input to drive its rotation and conveying behaviour. It moves the block above it forward into any empty space in front, in its OutputDirection.

## Operation

While powered by kinetic rotation, the Conveyor Rotator will rotate any (movable) block placed on top of it, until they face its 'OutputDirection'. While rotating, the block is converted into a Create Contraption, as a Mechanical Bearing would, and returns to being a physical block once it faces the correct orientation. Once facing the correct direction, the Conveyor Rotator attempts to move the block forward like a Conveyor Belt would, passing it forward into any empty space in front. It performs this check every tick while a moveable block is above it like this.

While the Rotator is rotating a block, other blocks cannot enter or be pushed into the space above it — it should be occupied by a temporary invisible immoveable block.

If a non-directional block is placed on an operational Rotator, the Rotator skips the rotation phase and simply tries to move it as a conveyor in the OutputDirection.

If a fully-directional (6-way) block is placed on an operational Rotator, it is immediately set to face the OutputDirection, then undergoes a 360 degree rotation (to visually emulate rotation). It otherwise behaves as horizontally-directional blocks would.

The speed at which the Rotator rotates is dependent on its RPM. A Rotator which loses input speed while mid-way through rotating a block pauses the contraption, and doesn't revert it to a block again until right-clicked by a player with an empty hand. If kinetic input resumes, it continues rotating from its current position.

## Rendering

The Conveyor Rotator should use a block entity renderer for its own model. However, it may need to utilise a different pipeline for creating and displaying the contraptions it creates for rotating blocks.

## Implementation

Not yet implemented — design target for v0.4.

## Related

- [[Conveyor Belt]]
