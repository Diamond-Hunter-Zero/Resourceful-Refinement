#### **Description**
The Research Terminal is a depot-derived block, which allows users to slowly unlock recipes in the Research System, by providing it with items or fluids.
Research Terminals belong to specific players, and will only unlock nodes for their owner. Multiple Research Terminals can work together to unlock the same node.

**ID:** *research_terminal*

**Vertical Slice:** 0.5


#### **Behaviour**
The Research Terminal is a horizontally directional block. Like the Casting Depot, it inherits from Create's Depot block, exposing item interfaces on all its faces. Unlike the Casting Depot or regular Depot, the Research Terminal also has an internal fluid tank, and exposed fluid interfaces on all of its faces except for the top (UP) face.

When a Research Terminal is placed down, it assigns ownership of itself to the placing player, and internally stores their UUID. If a terminal is placed by a null player (i.e. by the server or block-placement logic), the terminal has no owner and will not perform any research functionality until assigned one.

Right-clicking on the top-face of the terminal places items into its single inventory slot (as per regular depot behaviour), while right-clicking anywhere else opens up the terminal's Research GUI.

Unlike the standard Research GUI opened from the universal hotkey, the Research GUI opened by a research terminal displays a few extra elements:

- Along the top edge of the screen is a small tab displaying the Owner of the terminal, and their player head avatar. If a terminal has no owner, this tab can instead be click to assign the local client user to this terminal.

- Above the Details panel on the bottom-edge of the screen for locked nodes, a "Research This Node" button will show. Pressing this selects the current node as the research target for the terminal. If viewing the targeted research node, this button is instead replaced by a progress bar showing the current research progress, and a label saying "Researching this node...". If a node has locked prerequisites, this button is instead uninteractable, and says "Node Locked". If a node is unlocked, no element should be present here.


Once a terminal has a valid owner and target research, it is permitted to perform research. If an item is present in the terminal's inventory, and the current unlock progression for the target node still requires an instance of that item, the terminal will begin a research cycle. This cycle takes a length of time (60s, server-config configurable); If the itsemstack stored in the terminal is changed mid-way through a cycle, the cycle is aborted.

Once the terminal reaches the end of a cycle, it checks again whether the target node still requires 1 of this item, and if so, consumes 1 count from the itemstack, and adds 1 count of that item to the node's progression. The terminal then rebegins the cycle.

If a terminal has no inventory, or its current inventory either doesn't match anything required by the target node, or the target node doesn't require any more of the terminal's current item, it idles until its inventory, ownership, or target node changes.

The same cycle process applied to the terminal's fluid inventory. Terminals process 1000mb (1 Bucket) worth of fluid at a time. If its inventory contains less than 1000mb, it ignores it. Terminals cannot process fluids and items at the same time; If a terminal has a valid fluid, the fluid takes priority over any items in tis inventory. If a terminal has fluids, but it isn't required or needed for the target node, the terminal then proceeds to item processing. Once a terminal has began a processing cycle (either fluid or item), it won't automatically switch types or cycles unless its current cycle becomes invalid.

When a node's progression is entirely finished, the research node is flagged as unlocked for that player, and any research terminals targeting that node will idle until reassigned.



#### **Rendering**
The Research Terminal uses a JSON block model for its base visuals, paired with a BlockEntityRenderer for it's more complex inventory visualization. Like the Depot and Casting Depot, it should utilise the Depot's standard method for displaying its inventory items on top of it's top-face.

However, when the terminal is processing a fluid, any inventory items should instead be rendered at a much smaller x0.25 scale, and a small cube of the processed fluid (using the FluidBoxRendering utility) is rendered above the top-face.