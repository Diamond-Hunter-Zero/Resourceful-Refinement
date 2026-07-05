#### **Description**
Mechanical Stampers function like a horizontal Mechanical Press. While powered by kinetic rotation, they can act on itemEntities in front of them, consuming input items, fluids, and a specific 'stamp', to replace the input target with a new output.

**ID:** *mechanical_stamper*
**Vertical Slice:** 0.5

#### **Placement**
Mechanical Stampers function like a horizontal Mechanical Press. They can act upon either itemEntities or items on a belt, depot, or workspace in front them (like the Mechanical Press, but horizontally), but there must be an air gap between them. Like a press, they will halt item flow on belts while processing an item.


#### **Behaviour**
When processing recipes, Mechanical Stampers function similar to Create's Deployer. They use input items and fluids in their own inventory, to act on an item placed in front of them and turn it into something else.

As well as the primary target ingredient, Mechanical Stampers can specify three other optional inputs; A 'Stamp Item' item, a 'Fill Medium' item, and/or a 'Fill Fluid' fluid.

- The Stamp Item is a single-count itemstack represented as a ChancedIngredient. Most Stamp Items have a 0% chance of being consumed when used. Stamp Items can only be inserted into the Mechanical Stamper by clicking (or inserting through) its front-face.

- The Fill Fluid can only be inserted into the Mechanical Stamper through its back face.

- The Fill Medium can be inserted through any face except for the front or back, and is represented as a SizedIngredient.

Mechanical Stampers accepts and transfer kinetic rotation along a horizontal (local) X-Axis. They only operate while receiving kinetic input, and their processing time is proportional to their RPM.


Like the Mechanical Press, the Mechanical Stamper uses a staged processing cycle;
1. Once a valid recipe has been detected, the stamper halts the item (if on a belt), and begins to extend its head
2. Once the head is fully extended, the stamp checks that the recipe is still valid, and processing inputs/outputs
3. The belt in then unhalted (regardless of successful processing), and the head begins to retract
4. Once the head is back to tis retracted resting state, it then idles until a new valid recipe is detected again


### **Rendering**
The Mechanical Stamper uses a blockEntityRenderer for visual rendering. Like a Mechanical Press, it consists of a static 'casing' model, a shaft partial model, and a 'head' model which moves in and out depending of the processing cycle phase.