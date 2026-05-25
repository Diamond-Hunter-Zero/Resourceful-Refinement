#### **Description**
The Mechanical sieve is a rotation-powered block, which processes an input fluid and produces an output fluid and/or an item by-product. 

**ID:** *mechanical_sieve*

#### **Construction**
The Mechanical Sieve is a single block. It allows pipe connection on its top and bottom faces, and item interface connection on its front face. It accepts rotational input from cogs on any of its non-front faces.


#### **I/O**
The Mechanical Sieve is a horizontally-rotatable block (i.e. furnace-like). It always accepts fluid input from the top face, and pushes output fluids through its bottom face. By-product items can only be extracted from the block's front face. Items cannot be inserted into the block. Rotational input can be provided to any side face except for the front, by adjacent cogs.

A Create filter slot is positioned on the back face of the block. Right-clicking a Mechanical Sieve with no held item will extract any items held in the block's inventory.


### Multiblocking
When multiple Mechanical Sieves are placed vertically on top of each other, they form a sieve stack. Sieve-stacks can be built up to a configurable height (let's hardcode this to 4 for now). Sieves should check for stack construction whenever they are placed, or a vertically neighbouring sieve is placed/removed. Sieve stacks should form from bottom up.

Sieves in a sieve-stack act as a single multiblock structure, with only one sieve (the bottom block) utilising it's inventory. When sieves are added to a stack, clear any fluids in their inventory, and drop any stored items into the world. The sieve stack accepts input fluids from its top, and pushes outputs through it's bottom face and bottom-front face.

Any sieve in the stack can receive rotational input in order to power the whole stack, and sieves in a stack transfer rotational input between each other and to adjacent blocks (so that the stack behaves as a column of cogs).

When a sieve-stack processes a recipe, the whole stack behaves in the same way as a single sieve would, with a few exceptions:
- The duration of the recipe is increased by 25% for each additional sieve in the stack
- The 'by-product' item of a recipe (if it has one) repeats its chance roll for every sieve in the stack. Once successfully 'rolled' though, it no longer makes any further rolls. (i.e. if a recipe had a chance to drop a by-product of 5 stone at a 10% drop-rate, a stack of 4 sieves would make that roll up to 4 times, producing 5 stone once a roll was successful).

When part of a stack, the sieve's casing model should switch to a different model according to where it is in the stack (bottom, middle, top), and the block should always be rotated to face the same direction as the bottom block.


### **Rendering**
The Mechanical Sieve consists of two model classes:
The *MechanicalSieveCasingModel* class is the block's main block model.
The *MechanicalSieveCogModel* class is used for the block's rotational components. There are three instances of the CogModel needed; All three are stacked vertically on top of each other, 2 pixels up from the base of the block. The middle cog should be animated as if its the connecting cog in the rotational network, while the other two rotating at half the speed and in the opposite direction to the middle cog.


#### **Recipes**
The Mechanical Sieve is a Create-style crafting station. It uses the associated recipe type "*mechanical_fluid_sieve*", which must be serializable as a JSON for recipe definitions within the mod or datapacks. 

A Mechanical Fluid Sieve recipe takes an amount of 1 fluid input, and produce an optional amount of 1 fluid, as well as a percentile chance of producing an amount of 1 item. It requires rotational input in order to process items, and recipes can specify a duration (as standard). A recipe may not commence if the output tank does not contain enough room for the next production of fluid. If the output item slot has insufficient room for a by-product item, the recipe can progress as usual, and the excess items are discarded (ignored).

Recipes can use the following JSON format:

{
	"type": "resourceful_refinement:mechanical_fluid_sieve",
	"processing_time": 200,
	"ingredients": 
	[
		{
			"type": "neoforge:single",
			"fluid": "minecraft:lava",
			"amount": 250
		}
	],
	"results": 
	[
		{
		"id": "resourceful_refinement:molten_crimsite",
		"amount": 500
		},
		{
		"chance": 0.25,
		"item": "minecraft:stone",
		"amount": 2
		}
	]
}