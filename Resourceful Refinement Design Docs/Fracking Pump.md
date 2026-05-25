#### **Description**
The Fracking Pump is a multiblock structure assembled on top of a geyser block. While assembled and provided sufficient RPM, the pump passively generates fluid according to the block it's placed on top of, in exchange for consuming another config-specific fluid being pumped in. The Fracking Pump stores its output in an internal inventory, or pushes to connected pipes.

The height of the pump determines the rate of production, and the minimum RPM required.

**ID:** *fracking_pump_outlet*

#### **Construction**
The Fracking Pump is a multiblock structure which uses a *fracking_pump_outlet* as it's controller block. To assemble a Fracking Pump, a *fracking_pump_outlet* is placed on top of a geyser block. On top of this are placed 2 brass casing blocks (*brass_casing*). Metal girders (*metal_girder*) are then stacked vertically on top of the casings. An andesite alloy block (*andesite_alloy_block*) is then placed on top of the stack. A ring (3x3 with centre hole) of industrial iron blocks (*industrial_iron_block*) is then built around the top half of the girder pole. This is its "Counterweight".

The height of the girder pole and its surrounding ring is dynamically scalable, but must satisfy two conditions; 
- The height girder pole must be exactly twice the height of the industrial iron block ring.
- The top-most layer of industrial iron must be level with the top block in the girder pole.

This arrangement is possible up to a ring 4 blocks high (a pole length of 8).

Right-clicking on an unassembled outlet will attempt to create an assembled Fracking Pump. If successful, all the blocks (except the outlet) are removed, and the centre column (the girder pole + andesite cap) are replaced with invisible proxy blocks. Destroying the outlet or proxy-blocks of an assembled Fracking Pump disassembles the multiblock and places its constituting blocks back into the world. If a placement space has since been occupied by another block, it instead drops as an item.

#### **I/O**
The *fracking_pump_outlet* is a horizontally rotatable block. It has a fluid output on its front face, and a fluid input on its back face. Its right-side face acts as a half-shaft, accepting rotational input from adjacent blocks. The *fracking_pump_outlet* will not accept fluid inputs or output is not assembled.


#### **Production**
The Fracking Pump uses a JSON-defined config lookup of all the possible fluids it can produce. Each entry consists of:
- A 'source block' that the pump must sit on. This must be unique.
- An input fluid ID with amount.
- An output fluid ID with amount.

*(e.g. magma_block, 1000 water, 666 lava)*

While the assembled pump is provided the required input fluid and above a threshold RPM, it continuously generates the output fluid. The height of the pump's Counterweight affects it's production rate and RPM threshold. For each additional layer above 1, the rate of production/consumption increases by 25%, and the RPM threshold increases by 80.

An RPM input above the threshold does not increase production rate.

##### **Geyser Source Blocks**
In addition to the source-block requirement, Fracking recipes targeting a Geyser source-block should also specify a fluid ID. Only geyers storing the matching fluid will then permit that recipe.

### **Rendering**
The assembled Fracking Pump consist of several different models:

-  *FrackingPumpOutletModel* - The block model for the *fracking pump outlet* when in its disassembled state.
- *FrackingPumpBaseModel* - The base of the pump, 1x3 blocks high, rendered in place of the outlet block and copper casings.
- *FrackingPumpShaftModel* - A vertically tileable model, rendered in place for each block in the girder pole.
- *FrackingPumpTopModel* - The top part of the pump shaft, rendered at the andesite alloy block's position.
- *FrackingPumpCounterweightModel* - A vertically tileable model, representing the counterweight assembly. Multiple layers of the counterweight model are used to imitate the original counterweight assembly. The counterweight assembly moves as one object. While the rotational input to the pump is below the required threshold, it moves to the top of the shaft and sits there. While the input RPM meets the threshold, the counterweight repeats a cycle, in which it rapidly drops down, before slowly rising back to the top of the shaft.

![[Fracking Pylon Master Render 1.png]]