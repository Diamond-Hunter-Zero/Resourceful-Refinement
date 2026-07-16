#### **Description**
The Manifold is a fully directional blockEntity, which serves as a dynamic ingredient for Artefact Deliveries. Manifolds are a unique blockEntity which record a list of 'Assembly-Actions' applied to them by blocks from Create and Resourceful Refinement.

Each Assembly-Action is expressed in some way on the Manifold's renderer, as either a toggled model layer, tinted texture, or enabled multipart. Each combination of Assembly-Actions serves as a distinct GUID, which is preserved between breaking and placing Manifolds.

**ID:** *manifold_block*

**Vertical Slice:** 0.5



#### **Behaviour**
The Manifold acts as a fully directional block, to which Assembly-Actions can be applied to alter it's visual appearance. Manifolds internally store the list of all actions applied to them, and preserve this list between itemStacks and blockEntity instances. The order in which actions are applied is irrelevant, so the sorting of entries must follow a uniform order.

The ultimate goal for Manifolds is to act as complex, multi-step, ingredients for a research deposit system (like *Satisfactory* or *Shapez*), where the types and amount of manifolds needed to complete a task can be randomly generated.

##### **Assembly-Actions**
Manifolds support the following Assembly actions. Each action requires an acting machine/block, add an entry to the record with a stored value, and may require item or fluid inputs depending on the machine type. 

Some actions are specific to only one face, and are distinct entries to other actions of the same type on different faces. Face-specific entries/actions refer to the block's local orientation, not the absolute world face.

If a Manifold already has a matching action type applied to it, subsequent applications override the previous value.

The list of possible actions are:
	(*Type Name - Target Type - Machine BlockEntity - Inputs - Stored Value Type*)

- **Etching** - Whole Block - Mechanical Saw - None - Boolean (true if applied)
- **Indent** - Any Single Face - Mechanical Press - None - Boolean (true if applied)
- **Fill** - Whole Block - Spout - 1000mb of FluidStack - Fluid ID
- **Stamp** - Any Single Face - Mechanical Stamper - Stamp Item and Fill Fluid (1000 mb)/Medium - Stamp item ID, and Fill Fluid/Medium ID
- **Temperature** - Whole Block - Radiator - None - ExtendedHeatCondition blazeHeatLevel
- **Embedding** - Any Single Face - Deployer - ItemStack - Item ID

Assembly-Actions are applied when:
- A manifold block is placed over a Mechanical Saw. Once etched, it then tries to move in the saw's forward direction, as if on a Conveyor Belt. This operation takes time, and will halt conveyor flow.
- A manifold block is placed below a Mechanical Press. It indents the current UP face of the block. This operation takes time and will halt conveyor flow.
- A manifold block is placed below a Spout with 1000mb of fluid. This operation takes time and will halt conveyor flow.
- A manifold block is placed in front of a single isolated Mechanical Stamper. It stamps the face of the block facing the stamper. This operation takes time and will halt conveyor flow.
- A manifold block is placed adjacent to any non-idle Radiator. This operation takes time and will halt conveyor flow.
- A manifold block is placed next to a Deployer with an item. It embeds the item in the face of the block facing the deployer. This operation takes time and will halt conveyor flow.


#### **Rendering**
The Manifold appears as a full block, and uses a blockEntityRenderer for both its block visuals and item visuals. The visual appearance of a manifold is procedurally determined by its record of applied Assembly-Actions.

Each Assembly-Action has a visual impact on either its target face, or the whole block, as follows:

- **Etching** - An overlay texture is applied to the whole block
- **Indent** - The target face switches one flat multipart, for another indented multipart
- **Fill** - The base texture of the block is tinted to match the tint of the fill fluid
- **Stamp** - An overlay texture is enabled on the target face. Its tint and texture path is determined by the fill fluid/medium and stamp item used respectively
- **Temperature** - The tint of a model-part is altered according to the temperature
- **Embedding** - An itemStack representation of the embedded item is rendered on the target face