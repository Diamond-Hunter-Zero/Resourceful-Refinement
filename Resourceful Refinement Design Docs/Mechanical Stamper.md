#### **Description**
Mechanical Stampers function like a horizontal Mechanical Press. While powered by kinetic rotation, they can act on itemEntities in front of them, consuming input items, fluids, and a specific 'stamp', to replace the input target with a new output.

**ID:** *mechanical_stamper*
**Vertical Slice:** 0.5

#### **Placement**
Mechanical Stampers function like a horizontal Mechanical Press. They can act upon either itemEntities or items on a belt, depot, or workspace in front them (like the Mechanical Press, but horizontally), usually in pairs. Both Stampers must be placed with an empty (Air) 3 block gap between them, and facing each other. Their work item (whether on a belt/depot, or as a floating itemEntity) must be positioned in the middle block gap (This means that for items on belts/depots, the stampers must technically check the block below the middle-gap block). Like a press, they will halt item flow on belts while processing an item.


#### **Behaviour**
When processing recipes, Mechanical Stampers function similar to Create's Deployer. They use input items and fluids in their own inventory, to act on an item placed in front of them and turn it into something else.

As well as the primary target ingredient, Mechanical Stampers can specify three other optional inputs; A 'Stamp Item' item, a 'Fill Medium' item, and/or a 'Fill Fluid' fluid.

- The Stamp Item is a single-count itemstack represented as a ChancedIngredient. Most Stamp Items have a 0% chance of being consumed when used. Stamp Items can only be inserted into the Mechanical Stamper by clicking (or inserting through) its front-face.

- The Fill Fluid can only be inserted into the Mechanical Stamper through its back face.

- The Fill Medium can be inserted through any face except for the front or back, and is represented as a SizedIngredient.

Mechanical Stampers accepts and transfer kinetic rotation along a horizontal (local) X-Axis. They only operate while receiving kinetic input, and their processing time is proportional to the stamper in the pair with the lowest RPM.


Like the Mechanical Press, the Mechanical Stamper uses a staged processing cycle;
1. Once a valid recipe has been detected by either stamper, it then check whether it has a valid partner, and if so, whether that partner is available to begin  processing cycle. If so, the process proceeds:
2. The stampers halt the item (if on a belt), and begin to extend their heads towards each other
3. Once the heads are fully extended, the stampers check that the recipe is still valid, and process inputs/outputs
4. The belt in then unhalted (regardless of successful processing), and the heads begin to retract
5. Once each stamper's head is back to its retracted resting state, it then idles until a new valid recipe is detected again


### **Partnering**
Mechanical Stampers typically work in pairs. When in the correct placement (as described above), either stamper in a pair can initiate a processing sequence. Both stampers need to have the valid inputs for the recipe and kinetic input, and both stampers consume these ingredients on a successful output. The stampers do not individually produce outputs though (i.e. only one set of results is produced). If either stamper is rotated or removed prior to the processing step, the recipe fails.

Whenever a stamper is removed, placed, or rotated, it should update any partners it was previously linked to, or look for a new partner to connect to.


### **Isolated Stampers**
The Stamper recipe type should contain a boolean field called "isolatedStamper", with a default value of FALSE. Recipes utilising this field with a value of TRUE do not require stampers to work in pairs, and only need a single stamper with the valid inputs to be considered a valid recipe.


### **Rendering**
The Mechanical Stamper uses a blockEntityRenderer for visual rendering. Like a Mechanical Press, it consists of a static 'casing' model, a shaft partial model, and a 'head' model which moves in and out depending of the processing cycle phase.