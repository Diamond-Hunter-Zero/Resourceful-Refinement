#### **Description**
The Paint Nozzle is a directional block which connects to Create pipe networks, and allows fluids to be directly turned into gel-blobs.

**ID:** *paint_nozzle*


#### **Behaviour**
Paint Nozzles can be placed in ant of the 6 facing directions, and auto-connect to pipes. They have a small internal fluid tank (500mb), capable of storing 1 fluid. The Paint Nozzle is only considered to have pipe interface on it's (local) south face.

Paint Nozzles have two states; Open or Closed. While closed, fluid can flow into the nozzle's internal tank, but no further action occurs. If open, the nozzle drains its internal tank to create gel-blobs at the same rate as a held-down hosegun.

**Flow Speed**
The Paint Nozzle also has a configurable 'flow speed' variable, which can be in one of 3 states; '*Low*', '*Medium*', or '*High*'. By default, the nozzle uses 'Medium'. The flow speed of a nozzle is a multiplication factor which is applied to the speed of gel-blobs produced by the nozzle. To change a nozzle's flow speed, the user can right-click with a Create wrench, cycling through all speeds.

| **Speed** | **Factor** |
| --------- | ---------- |
| Low       | 0.45       |
| Medium    | 0.75       |
| High      | 1.2        |

**Gel Blobs**
Projectiles created by the Paint Nozzle and the [[Hosegun]] are called 'Gel Blobs'. Gel blobs carry the fluid ID and FluidStack of the fluid used to instantiate them.

Gel blobs have unique on-hit-behaviours according to the type of fluid they carry, and whether they impact a block surface or entity. Most gels types result in creating a Gel Splatter block when impacting a block surface.


**Drains**
When a gel-blob impacts a Create drain (*create:drain*), instead of creating a gel-splatter, the blob attempts to fill the drain with the fluid it was carrying. If the drain already contains an incompatible fluid, the gel-blob is instead discarded without instantiating anything.

#### **Rendering**
Players can right-click a Paint Nozzle to toggle it between 'Open' and 'Closed' states. Doing so updates an internal NBT flag, as well as a blockstate property called "valve_open", which in turn updates the block model used for the nozzle.