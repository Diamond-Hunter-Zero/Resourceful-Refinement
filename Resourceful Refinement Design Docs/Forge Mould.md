#### **Description**
The Forge Mould is a rotation-powered mechanical block, which turns a fluid input and/or an item input into an item output on top of a belt or depot, behaving like a Spout combined with a Mechanical Press.

**ID:** *mechanical_forge_mould*

#### **Construction**
The Forge Mould is a single block. It allows pipe connection on its top face, and item interface connection on its front face. It accepts rotational input from shafts on either of its left or right sides. Like a Mechanical Press, it must be placed with  a one block gap above a working surface in order to craft items.

#### **I/O**
The Forge Mould is a horizontally-rotatable block (i.e. furnace-like). It always accepts fluid input from the top face, and item inputs through its front face. Rotational input can be provided to either the left or right face by a shaft, and the block acts as a shaft transferring rotation in this axis.

A Create filter slot is positioned on the back face of the block. Right-clicking a Forge Mould with no held item will extract any items held in the block's inventory.


#### **Crafting**
The Forge Mould behaves similarly to a Mechanical Press. Like a Press, it requires a workspace underneath it to craft and takes time to extend. Once extended (called the 'impact period'), it then waits a period of time defined by the recipe before spawning the product and consuming inputs, before retracting. However, unlike the Press or Spout, the Forge Mould works exclusively on empty surfaces. It will not operate if an item exists below it, and crafting recipes will fail (and must wait for the forge to retract and reset before the next cycle tries again) if an item is inserted underneath it during the impact-period. If an item intercepts the forge while in its impact-period, it immediately moves into the retract phase, and spawns black smoke particles.

Some Forge recipes specify that the Forge Mould must be placed above a Casting Depot. These recipes are distinct from ones without the 'casting' flag, regardless of inputs/outputs.

Increasing the RPM provided to the block increases the speed of it's extension and retraction periods, as well as its impact-period.

### **Rendering**
The Forge Mould consists of three model classes:
The *ForgeMouldCasing* class is the block's main block model.
The *ForgeMouldPress* class is used as the base of the forge's extendable arm. It's base should be level with the surface of the belt/depot/worktop when crafting an item, and level with the bottom of the forge block when retracted.
The *ForgeMouldTube* class is used as the connecting repeatable segment of the forge's extendable arm. It should begin from the top of the *ForgeMouldPress*, and be stacked vertically until the top of the top-most tube is at least 4 pixels above the bottom of the forge block.

The forge should animate like a Mechanical Press; When idle, the press is fully retracted inside the casing and remains stationary. When attempting to process an item, the press and it's connecting tubes should lower down (proportional to the processing speed), pause for a moment on top of the working surface, then retract once the item as been spawned. When the press impacts with the working surface, it should also spawn several (white) smoke particles around the press.

![[Forge Mould Render.png|253]]

#### **Recipes**
The Forge Mould is a Create-style crafting station. It uses the associated recipe type "*mechanical_forge_mould*", which must be serializable as a JSON for recipe definitions within the mod or datapacks. 

A Forge Mould recipe takes an amount of 1 fluid input, and 1 optional itemStack input, and produces 1 itemStack.

Recipes can use the following JSON format:

`{`
	`"type": "resourceful_refinement:mechanical_forge_mould",`
	"casting": true,
	`"processing_time": 200,`
	`"ingredients":` 
	`[`
		`{`
			`"type": "neoforge:single",`
			`"fluid": "minecraft:lava",`
			`"amount": 50`
		`},`
		`{`
		`"item": "minecraft:cobblestone",`
		`"amount": 2`
		`}`
	`],`
	`"results":` 
	`[`
		`{`
		`"item": "minecraft:magma_block"`,
		"count": 5
		`}`
	`]`
`}`