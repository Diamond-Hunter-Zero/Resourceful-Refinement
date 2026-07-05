#### **Description**
The Conveyor Belt is a conveyor-like belt which instead of moving items, moves blocks above it.

**ID:** *conveyor_belt*

**Vertical Slice:** 0.5

#### **Placement**
The Conveyor Belt places like a standard Create mechanical_belt, utilising the same logic and client mid-placement visuals. However, Conveyor Belts cannot form vertical or diagonal connections! They may only be placed horizontally. They also have  a much lower maximum distance, spanning no more than 8 blocks.

Like Create Mechanical Belts, breaking a segment in a Conveyor Belt train destroys all other segments, and drops a single item. Players can also extend a placed belt by right-clicking an end-segment with the Conveyor Belt item (which does not consume the item). Additional shafts may also be inserted into any empty segment by right-clicking (which consumes the shaft, as per normal).


#### **Behaviour**
The Conveyor Belt moves blocks, not items or entities. Its hitbox (and visuals) are a full block in height. 

While receiving kinetic input, a Conveyor Belt will move blocks directly above it as if they were pushed by a piston in the direction of the belt's rotation. A block will only move if the space it would be pushed into is empty, and the block is movable by piston. Blocks which are marked as immovable, or drop when pushed by pistons, will not be moved, and will block over blocks. For a belt train, blocks are moved in order from the front of the train to the back.

The rate at which blocks are moved is determined by the speed of the belt:

| Abs(Speed)    | Every X ticks |
| ----------------- | ------------- |
| speed >= 128      | 1             |
| 64 <= speed < 128 | 2             |
| 16 <= speed < 32  | 3             |
| speed < 16        | 4             |

Belt content is defined in Create under "simibubi.create.content.kinetics.belt".

### **Rendering**
The Conveyor Belt should use the same rendering pipeline/setup approach as Create's Mechanical Belts, as it will need to operate in much the same manner.
