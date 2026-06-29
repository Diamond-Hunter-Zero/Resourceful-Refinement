#### **Description**
The Drill Pylon is a multiblock structure which extracts resources from Crystal Fissures. It uses kinetic input to passively produce resources over time.

**Controller ID:** *drill_pylon_head*
**Proxy ID:** *drill_pylon_proxy*


#### **Construction**
To build a Drill Pylon, players first place a Drill Pylon Head on top of a Crystal Fissure Bud block. Above the drill head, then stack 3 layers of a 3x3 ring of brass_casing with a gearbox in the middle. Then, on each of the corner blocks of a 5x5 square centred on the Crystal Fissure Bud block, players must place a girder block, and then vertically stack this layer until it reaches the same height as the brass_casing rings.

When the drill head is then right-clicked, it trays to assemble a Drill Pylon structure. 

The total  height is therefore 5 blocks.

If any block in an assembled Drill Pylon is removed/moved, the structure disassembles and reinstantiates all original blocks. This removes any Lux Sockets and breaks any GLARE links too.

![[2026-06-29_15.16.38.png|351]]


#### **Behaviour**
The Drill Pylon is a horizontally rotatable multiblock structure; Its 'front' is taken to be the lateral direction towards the activating player when the Drill Pylon Head is right-clicked to assemble the structure.

To assemble, the drill head must have a valid structure (as defined above), and be placed above a Crystal Fissure Bud block. An assembled Drill Pylon has several interface points; The middle front face of the lowest brass_casing ring exposes an item output interface. The middle left/right-side faces of the lowest brass_casing ring expose kinetic shafts which power the drill and transfer rotation. The middle back face of the 2nd brass_casing layer exposes a Lux Socket.

Crystal Fissure Bud blocks, (like Mineral Deposits and Geysers) internally store an item ID reference. This item ID determines the active DrillPylonRecipe of any Drill Pylon assembled above a Fissure Bud.

The Drill Pylon requires a minimum amount of RPM to operate. Once above that speed threshold, the drill will function, and any further speed increases will not improve efficiency. Drills have two operational modes; *Regular*, and *Amplified*. When a drill is provided with only kinetic input, it runs in *regular* mode. When a drill is provided with kinetic and has an attached transceiver on its socket, it runs in *amplified* mode. A drill will also not operate if it is not above a Crystal Fissure Bud blocks that supports a valid DrillPylonRecipe.

While operational, a drill repeatedly performs processing cycles. The duration of each cycle is determined by its recipe. If a recipe specifies a Lux Curve parameter, the drill consumes that amount of lux while running according to its progression along the processing cycle. 

If a drill's inputs ever fail to meet their required thresholds, processing pauses until requirements are met again. This includes lux; If a recipe requires a lux curve and the network ever over-allocates and shuts-down, the drill's operation is paused until enough lux can be provided again.

Once a processing cycle completes, the drill produces resources according to the recipe's results, which it stores in its internal inventory. A Drill Pylon has internal storage for 4 item slots. The drill then resets and continues on with the next cycle.


#### **Rendering**
The Drill Pylon Head uses a BlockEntityRenderer to render its entityModel visuals. While part of an assembled Drill Pylon, the head is responsible for rendering the entire model of the multiblock structure (and so must have valid bounds to do so).



#### **Drill Pylon Head**
The Drill Pylon Head acts as the controller blockEntity for a Drill Pylon multiblock, but can also function as an independent mechanical block when unassembled. It shares the same properties and behaviours as the regular Create Drill, with the exception that it mines in a 3x3 area in front of it, instead of the standard 1x1.

It is a fully directional block, with a kinetic shaft input on its local back face