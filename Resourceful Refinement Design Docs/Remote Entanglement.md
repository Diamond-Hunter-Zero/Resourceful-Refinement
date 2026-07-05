Remote Entanglement refers to a behaviour system implemented by two GLARE-powered blocks; The Remote Entanglement Depot, and the Remote Entanglement Transporter. Both blockEntities share similar logic and functions, but act on different subjects.

**Vertical Slice:** 0.4

### Remote Entanglement Depot
The Remote Entanglement Depot is a variant of Create's Depot block, functioning like one, just as the Casting Depot does. However, it can also be used to teleport itemstacks between linked depots on the same GLARE network. It is a horizontally directional block.

Entanglement Depots have Trio Address slots on their front face, used to set their Address ID. Entanglement Depots can be in either a 'Send' or 'Receive' blockstate - By default, depots are 'Send'.

If a Send-Depot is holding a non-empty itemstack, it starts to charge up for a short 2s period. Upon charging, it then attempts to send the itemstack to a random (chunk-loaded) Receive-Depot with the same address on this network, which isn't currently holding any items. If successful, the items are teleported to their destination. If no matching empty depot can be found, (or the item stack was removed from the depot), the teleportation fails, and a failure message is cached on the depot's goggle-tooltip until the next attempt.

Once a teleport attempt has been made, the depot then enters a cooldown period for 8s in which it can't begin any teleportation, before it returns to an idle state (or immediately begins a new cycle if holding an item-stack).

In order to both charge-up and cooldown, the Entanglement Depot must be receiving 4 Lux from an operating GLARE network. It must also have a '*chilled*' heat source adjacent to any of its faces (HeatUtilities.GetExtendedHeatLevel()). If either of these conditions cannot be met, the current state of the charge-up/cooldown remains paused until satisfied again.


### Remote Entanglement Transporter
The Remote Entanglement Transporter works in much the same way as the Remote Entanglement Depot, except that it teleports players.

The Remote Entanglement Transporter is actually a multiblock structure which self-places when its 'controller' block is placed (like a bed or door). The structure consists of the horizontally-direction 'Transporter Controller' block, a solid 'Tank Proxy' block which sits on top of the controller, and two non-solid 'Casing Proxy' blocks which occupy the spaces in-front-of and in-front-of-and-above the controller. This full assembly thus occupies a 1x2x2 volume. If any block in the assembly is broken, all blocks are removed and the controller block is dropped. If there is not enough space for the full assembly when the controller is placed, on the controller block is placed, the transporter is considered 'incomplete', and the plater receives a warning on their action bar.

None of the proxy block themselves render visible models - The controller renders a BlockEntityRenderer for the whole assembly.

To function, the Remote Entanglement Transporter requires 8 Lux from an operational GLARE network, and either it's controller or tank block must be '*chilled*' from an adjacent source. While these conditions are met, the transporter can charge-up (2s) and cooldown (10s). Transporters also require 150 mb of *liquid_chorus* present in their input tank upon charge-up completion in order to teleport a player. This fluid is consumed upon successful teleportation. The assembly has fluid interfaces on the back-face of the controller, or the top-face of the tank proxy.

The Address slots for a transporter are shown on the controller's front face. When a player enters the hitbox region defined by the casing proxies, the teleporter begins its charge-up sequence. If the player is still present upon charge-up, there is sufficient liquid_chorus, and a valid random destination exists on the network, the player is successfully teleported. Otherwise they remain put, and no liquid_chorus is consumed. The transporter then enters the cooldown phase. If multiple players are present inside a transporter upon charge-up, one player is randomly selected to teleport.

Note that destinations for transporters need not be chilled or fuelled with liquid_chorus themselves; They only need to be connected to the operational GLARE network. They don't even need to be loaded! Transporters on a network should store their connected Addresses and locations as part of the server-side network information when their chunks are unloaded, so that they remain valid addresses.

When a player is teleported, they suffer a status effect (with no particles) called "teleportation_sickness" for 5s. Players with teleportation_sickness cannot trigger a transporter's charge-up sequence, or be teleported.

Assembled transporters can also be cycled between 'Auto', 'Send-Only', and 'Receive-Only' modes with a wrench. Send-Only transporters should not be considered valid destinations, and Receive-Only should not be able to initiate teleportation sequences. These states need to be encoded on the server-side GLARE data for unloaded transporters too.

