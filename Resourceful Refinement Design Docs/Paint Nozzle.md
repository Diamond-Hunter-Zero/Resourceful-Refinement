#### **Description**
The Paint Nozzle is a directional block which connects to Create pipe networks, and allows fluids to be directly turned into gel-blobs.

**ID:** *paint_nozzle*


#### **Behaviour**
Paint Nozzles can be placed in ant of the 6 facing directions, and auto-connect to pipes. They have a small internal fluid tank (500mb), capable of storing 1 fluid. The Paint Nozzle is only considered to have pipe interface on it's (local) south face.

Paint Nozzles have two states; Open or Closed. While closed, fluid can flow into the nozzle's internal tank, but no further action occurs. If open, the nozzle drains its internal tank to create gel-blobs at the same rate as a held-down hosegun.


#### **Rendering**
Players can right-click a Paint Nozzle to toggle it between 'Open' and 'Closed' states. Doing so updates an internal NBT flag, as well as a blockstate property called "valve_open", which in turn updates the block model used for the nozzle.