
The PUG (Payload Uncrewed Gadget) system is a mechanism which allows players to remotely transfer resources over long distances or between dimensions. It consists of Launch Pads (Which define send and receive locations, and provide interfaces for cargo, destination points, and fuel), and PUG entities (Which actually travel and transfer cargo).

**Vertical Slice:** 0.4

### Launch Pads

Launch Pads are a flat 3x3 multiblock structure, constructed when a Launchpad Controller is placed down in the world. The Launchpad Controller is a horizontally-rotatable block, meaning that launchpads can take on any orientation. The Launchpad Controller places it's Launchpad Proxies behind it, such that the controller sits in the center-front edge of the pad. If a controller is unable to find enough space in it's placement, it fails to 'assemble', and remains non-functional. A player can right-click to try and reassemble an incomplete controller instead of opening any of its GUI. If any proxy block or controller in a launchpad is broken, the whole assembly is destroyed and the controller drops.

The back face of the back-most proxy block in an assembled launchpad is an intake fluid interface for the controller's fuel tanks. The side faces of all other proxy block can act as in/out item interfaces.

Assembled launchpads are globally recorded and known to the server. This session persistent data includes their position, mode, and address. Assembled launchpads also act as chunk loaders for the chunk their controller is placed in.


Launchpads have an inventory of 12 item slots. A single PUG launch can carry up to 6 slots of items.



##### Using Launch Pads and PUGs
Launch Pads can be used alongside PUGs to transport items between locations or dimensions outside the nether.

Each Launch-Controller can be assigned a 3-item Address code using address slots along the top of the block, and placed in either SEND or RECEIVE mode via a wrench. An assembled launchpad also has a GUI which can be opened by right-clicking the controller. Inside this screen, the user can also toggle between SEND and RECIEVE modes, each showing a different view.


A controller in SEND mode then has several configurations which can be adjusted. This are shown in a large panel on the left-hand side:
- Destination ID (an address input configuration similar to that used by the Telemetry Terminal GUI)
- Launch Condition (*When PUG full, On redstone input, On timer*)
- Launch Timer (0s-1hr, uses Create's time fields, only shows if on '*On timer*' mode)

The GUI also shows an indicator top-right for the launchpad's current fuel reserves and fuel needed to reach current address, a printout bottom-right of any conditions halting the next PUG launch, inventory slots middle-left for the launchpad's current contents, and inventory slots bottom-left for the player's inventory.
  
  ![[Launchpad Controller Send GUI Mock-up.png]]

In RECEIVE mode, the GUI shows the inventory slots of the launch-pad, as well as a track which updates infrequently, showing the count and progress of all PUGs currently flying to this pad.

![[Launchpad Controller Receive GUI Mock-up.png]]


When items begin to flow into a SEND launch-pad, the pad instantiates a PUG vehicle entity. Once the controller has a valid destination, is filled with sufficient fuel to reach that destination (the Launchpad uses any fluid tagged with the "*carborax_fuel*" tag), has LoS access to the sky, and meets its Launch Condition, the PUG is deployed and flies upwards, carrying 6 slots of inventory from the launch-pad's inventory with it, and removing the required fuel from the pad's tank.

A PUG launch will always fail if it isn't carrying any item cargo.

If multiple RECEIVE launch-pads share the same code, the launch-pad will not consider them valid targets, and will also halt launch.



### PUG Flight
PUGs do not literally travel as entities the entire distance between locations, in order to save on performance, and bypass chunk loading restrictions. When a PUG launch occurs, the PUG entity flies directly upwards to world height. It then de-instantiates and converts itself and its contents into a data object handled by the PUG System.

Once having reached a certain altitude, PUG entities are removed, and instead simulate travel as data objects. PUGs take an amount of time to 'travel' according to the distance between pads, as well as whether they have to travel across dimensions. The server-side system increments this progress every tick over all simulated PUGs. Once this travel time is completed, a PUG will wait for its destination pad to be 'unoccupied' - If available, the PUG 'claims' the pad (marking it as occupied), instantiates itself, and descends before lands on the pad, transferring its inventory to the pad (or remaining as an entity until the pad's own inventory is able to transfer across all items). The PUG is then de-instantiated and the pad marked as unoccupied once its inventory is emptied.

Multiple simulated PUGs may be held in a queue while waiting for a pad to become unoccupied.

If a PUG's destination no longer exists at the end of its travel (the controller has been removed, changed its ID, or moved location), the PUG instead crash-lands at a random block position within 2 blocks of its last known location, never at the exact intended coordinates, and becomes a world entity with a lootable inventory. A crashed PUG can also be destroyed by attacking it, which likewise drops its contents.

PUGs are immune to all damage while not is a crashed state, and cannot be leashed, placed in a boat/minecart, or spawned with an egg.
