#### **Description**
The Conveyor Rotator is a utility counterpart for the Conveyor Belt, which allows users to rotate directional blocks on top of it.

**ID:** *conveyor_rotator*

#### **Placement**
The Conveyor Rotator is a horizontally directional block. As well as the standard blockstate FACING property, its blockEntity also internally records an 'OutputDirection' property, which is aligned to match its FACING property when placed by a player.


#### **Behaviour**
While powered by kinetic rotation, the Conveyor Rotator will rotate any (movable) block placed onto of it, until they face its 'OutputDirection'. While rotating, the block is converted into a Create Contraption, as a Mechanical Bearing would, and returns to being a physical block once it faces the correct orientation. Once facing the correct direction, the Conveyor Rotator attempts to move the block forward like a Conveyor Belt would, passing it forward into any empty space in front. It performs this check every tick while a moveable block is above it like this.

While the Rotator is rotating a block, other blocks cannot enter or be pushed into the space above it - It should be occupied by a temporary invisible immoveable block.

If a non-directional block is placed on an operational Rotator, the Rotator skips the rotation phase and simply tries to move it as a conveyor in the OutputDirection.

If a fully-directional (6-way) block is placed on an operational Rotator, it is immediately set to face the OutputDirection, then undergoes a 360 degree rotation (to visually emulate rotation). It otherwise behaves as horizontally-directional blocks would.

The speed at which the Rotator rotates is dependent on its RPM. A Rotator which loses input speed while mid-way through rotating a block, pauses the contraption, and doesn't revert it to a block again until right-clicked by a player with an empty hand. If kinetic input resumes, it continues rotating from its current position.

### **Rendering**
The Conveyor Rotator should use a blockEntityRenderer for its own model. However, it may need to utilise a different pipeline for creating and displaying the contraptions its creates for rotating blocks.
