
#### **Description**
The Fluid Refinery is a large multiblock structure that functions as another crating station for Create, specializing in combining fluids and items with each other. The Fluid Refinery has internal tanks for two input fluids and 1 output fluid, and can accept up to 2 kinds of input items as well. The refinery is used to improve the quality of molten minerals, create molten alloys, and even refine combustible fuel.

#### **Construction**
The Fluid Refinery is assembled as a multiblock structure with a base of either 3x3 or 5x5 blocks. It must eb constructed with a minimum height of 3 blocks, but can be extended up to a configurable maximum height. The top and bottom layers of the multiblock always follow the same pattern, while the middle layers are just repeated vertically.

The base of the multiblock structure consists of a square of Copper Casings (*copper_casing*), with a blaze burner (*blaze_burner*) in each corner. In the centre of one edge should be placed a Refinery Access Port (*refinery_access_port*). This denotes the 'front face' of the refinery, to which all other block placement should be orientated.

The middle layers of the structure should consist of a solid square of glass, with a blender blade (*blender_blade*) placed in the centre.

The top layer consist of another square of glass. Its centre block should again be a blender blade. But the two front corners should each be an item vault (*item_vault*), and the two back corner should each be a fluid tank (*fluid_tank*).

Right-clicking an unassembled refinery should remove all blocks in the structure's region except the Refinery Access Port, and replace them with invisible proxy blocks, while the Refinery Access Port renders a procedural BlockEntityRenderer to represent the entire multiblock. If the Refinery Access Port is unable to find a valid structure around it, it should instead print a warning message to the user's toolbar.


#### **I/O**
The top faces of the back (south) two corners of the refinery (where the fluid tanks were placed), are the only faces which accept fluid inputs (each accepting either of the up-to-two fluids a recipe can support). The two outward side faces (west and east) of the top two corners of the refinery (where the item vaults were placed) are the only faces which accept item inputs for any of the refinery's item ingredients. The outward side faces of any of the corner blocks on the bottom layer of refinery accept fuel items, which increase the heating level of the refinery for a time equal to the burn time. The front (north) face of the Refinery Access Port is the only face which allows fluid output. The top face and bottom face of the centre block in the top and bottom layers accepts and transfers rotational input from connected shafts.


#### **Multiblock Interaction**
The multiblock structure should behave as one giant block. When any block in the assembled refinery is right-clicked, it should open the refinery's GUI. If any block in the assembled refinery is broken, moved, or replaced, the refinery should disassemble, returning the region back to it's constituting blocks.

The Refinery Access Port and the proxy blocks should use a **Controller/Proxy** relationship. Each proxy block should know where its controller (Refinery Access Port) is. The controller should handle things such as fluid and item inventories, crafting logic, and sided handlers, and the proxy blocks simply hand any checks point to their locations or faces over to the controller to appropriately process.


**Rendering**
To handle variable height while maintaining a custom model, we should avoid making one giant model for every possible height. Instead, use a **Segmented Rendering** approach.
- **Base (3x3 or 5x5):** Contains the bottom output port and blaze-burner heaters
- **Middle (Repeatable):** A 1-block tall slice that tiles vertically. In Blockbench, the textures at the top and bottom edges of this slice are seamless so they don't "seam" when stacked.
- **Top (Cap):** The "exhaust" or "control head." This caps off the structure regardless of how many middle segments exist.

Rather than each block having its own model, use a **BlockEntityRenderer (BER)** on the "Controller" block.
1. **Blockbench:** Export three separate `.json` or `.obj` models.
2. **BER Logic:** When the multiblock is assembled, the BER checks the recorded height in the Controller's data and loops through the rendering:
    
    - Render `base_model` at $y=0$.
    - For $i = 1$ to $height - 2$: Render `middle_model` at $y=i$.
    - Render `top_model` at $y=height-1$.


#### **Recipes**
The Fluid Refinery is a Create-style crafting station. It uses the associated recipe type "*fluid_refinery*", which must be serializable as a JSON for recipe definitions within the mod or datapacks. 

A Fluid Refinery recipe takes in up to 2 fluids and up to 2 types of items. It requires rotational input in order to process items, and recipes can specify a heating level required for the recipe to proceed. It always outputs a fluid. Fuel for raising the heat level can be inserted into the refinery by right-clicking with a fuel item on the side face of any corner block on the bottom layer. Fuel items may also be inserted into these particular faces via other standard item I/O methods.

Recipes can use the following JSON format:

{
  "type": "resourceful_refinement:fluid_refinery",
  "heat_requirement": "HEATING REQUIREMENT ID",
  "processing_time": "PROCESSING TIME",
  "ingredients": [
    {
	  "item": "ITEM ID HERE"
      "count": "ITEM COUNT HERE"
    },
    {
	  "item": "ITEM ID HERE"
      "count": "ITEM COUNT HERE"
    },
    {
      "type": "neoforge:single",
      "amount": FLUID AMOUNT HERE,
      "fluid": "FLUID ID HERE"
    },
    {
      "type": "neoforge:single",
      "amount": FLUID AMOUNT HERE,
      "fluid": "FLUID ID HERE"
    }
  ],
  "results": [
    {
      "fluid": "minecraft:mud",
      "amount": FLUID AMOUNT HERE,
    }
  ]
}

