The Cyclotron Forge is a multiblock processing machine which uses kinetic input and Lux to produce both items and fluids.

**Controller ID:** *cyclotron_controller*
**Shielding ID:** *heavy_plate_shielding*
**Vertical Slice:** 0.4


### **Behaviour**
A cyclotron assembly consists, at minimum, of an input-cap, and output-cap, and a series of intermediary coil segments in-between, all placed as horizontal slices. Each cap/slice measure 3 block wide by 3 blocks high, and the assembly has a total length of (2+N) blocks deep, where N is the number of coil segments.

Both the input and output caps each have interfaces for items, fluids, and kinetic rotation, while the side faces of any middle-edge block of the output cap acts as a GLARE receiver sockets for Lux input while assembled.


### **Recipes**
The Cyclotron accepts up to 2 item inputs and 2 fluid inputs in its recipes. Cyclotron recipes will often produce both item and fluid outputs at once. Typically, one is considered an undesirable by-product that must be disposed of.

The Input-cap exposes two item-input-interfaces on the front faces of its top corner blocks, and two input-fluid-interfaces on the front faces of its bottom corner blocks. The input-cap also exposes a kinetic shaft input on its centre front face.

Cyclotron recipes will specify a length that the assembly must match in order to craft that recipe. Cyclotron recipes also specific a Lux curve which determines the amount of Lux allocated to the cyclotron as it progresses over the crafting cycle. Cyclotrons also require a minimum RPM speed in order to operate. Providing kinetic input above this speed does not effect the processing duration.

Cyclotron recipes also require an exact 'coil length' to be considered valid. If a cyclotron has all the inputs required for a recipe, but doesn't have the exact matching length, the recipe progress remains at 0, and a warning is shown in its goggle tooltips.

When the cyclotron has been provided with the correct item/fluid inputs for a recipe, minimum speed, and is connected to a Lux network, it will try to begin a processing cycle for that recipe, the duration of which is determined in the recipe itself.

If at any point, the RPM or Lux provided to the cyclotron stops (or the Lux network overloads), the cyclotron pauses it's current progress. If input amounts change such that a recipe is no longer valid, the processing cycle and attempted recipe are discarded, and the cyclotron waits for a new recipe to become valid again. If a cyclotron also doesn't have enough room in it's output tanks/inventory for all of the guaranteed products of a recipe, it pauses its processing cycle until conditions change.

If a processing cycle successfully completes, the inputs are consumed, and the cyclotron adds the recipe's outputs to it's output tanks and inventory slots. The cyclotron exposes an output-item-interface on the middle back face of the left column of the output-cap, and a output-fluid-interface on the middle back face of the right column of the output-cap.


### **Construction**
The Cyclotron is a multi-block assembly constructed from a series of horizontally placed slices. Each slice measures 3 x 3 blocks.

The output-cap consists of a 3x3 ring of *brass_casing* surrounding a *cyclotron_controller*. The intermediary coils consist of 3x3 rings of *heavy_plate_shielding* surrounding an air block gap. The input-cap consists of a 3x3 ring of *brass_casing*, with two *item_vault* blocks in the top corners, and two *fluid_tank* blocks in the bottom corners.

![[2026-07-01_13.56.11.png|393]]

Right-clicking on a cyclotron_controller attempts to assemble the structure. If any block in an assembled cyclotron is moved/removed, the structure disassembles and the original blocks are reinstantiated.

Cyclotron assembly and recipe logic should enforce a hard limit on a cyclotron length of 16 blocks.







