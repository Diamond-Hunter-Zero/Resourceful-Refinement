The Bucket Excavator is a blockEntity consisting of a cuboid mount and an extended bucket-wheel, which passively generates resources when placed over mineral deposits, or slowly mines blocks when switched to 'destruction' mode.

**ID:** *bucket_excavator*
**Vertical Slice:** 0.4

#### **Physical Design**
The Bucket Excavator is a single blockEntity, which can be horizontally rotated. Its back face exposes an item-output interface, and its left/right side faces act as a shaft transferring kinetic input.

Extending beyond the front face of the block (or "mount") is a large polyhedral bucket-wheel, which measures 3 blocks tall, 3 blocks deep, and 1 block wide. The wheel is positioned in front of the mount, its axis of rotation parallel to the mount's shaft axis and at the same y-position, such that the entire bounding volume of the bucket excavator appears to be 3x4x1.

![[bucket_wheel_excavator_block_test_render.png|388]]

Bucket Excavators occupy a 5x4x1 'excavation volume' encapsulating their wheel. If the excavation-volumes of two or more Bucket Excavators overlap, they won't function or produce resources. Bucket Excavators can be tiled laterally.

![[bucket_wheel_excavator_volumes.png|498]]

When provided with kinetic input, the bucket-wheel rotates, indefinitely producing resources, or mining blocks within this 5x4 volume.


#### **Mineral Deposits**
To produce resources, a bucket excavator's excavation-volume must overlap with a Mineral Deposit block (*mineral_deposit_node*).

A Mineral Deposit is a blockEntity which stores a reference to a block type. It uses the texture asset of this block for its own texture resource. Players in Creative mode can set the source block stored inside a Mineral Deposit by right-clicking with any block. By default, it stores stone. (This entire behaviour is the block equivalent of the geyser block and its functionality with fluids).

Mineral Deposits can be mined by pickaxes, but take a while, and won't drop anything except their source block.



#### **Excavations**
The Bucket Excavator has two modes; "Excavation" and "Destruction". The user can toggle between them by using a Create ScrollOptionBehaviour on the top face of the mount blockEntity.

The bucket excavator defines a special recipe-type called *bucketExcavationRecipe*, which allows the mod to define a complex extraction relationship between Mineral Deposits/Blocks and Excavators. A bucketExcavationRecipe consists of:
- An input block ID (the block which must be inside the excavation volume for this recipe to match)
- A **optional** source block ID as an item ID or ItemTag (the item a Mineral Deposit must store for this recipe to match. If not provided, only the input block is used for matching)
- A SizedIngredient output item produced (the output(s) produced by a production cycle)

The duration of a processing cycle is fixed, regardless of input speed. While provided with kinetic input (above a minimum speed threshold), the bucket excavator repeatedly processes production cycles.

**If in 'Excavation' Mode:**
	At the end of each cycle, it checks all blocks overlapping with its excavation-volume, and produces resources according to any matching recipes for each block.
	
**If in 'Destruction' Mode:**
	Throughout the processing cycle, the wheel slowly incrementally breaks all blocks in its excavation volume, like a Create Drill. It will not break any blocks that a Create Drill would not break, or any Mineral Deposits or Geysers.

Outputs from the excavation wheel always try to be placed inside its inventory, not dropped on the ground.


#### **Mount Behaviour**
The Bucket Excavator mount has an internal inventory of up to 4 slots. If a processing cycle cannot fit all outputs into this inventory, they are dropped as item entities below the wheel. The mount has an output interface on its back face.

The excavator blockEntity also responds to redstone input from any of its faces; When powered, the wheel freezes, and pauses processing.
