

GLARE Networks are a graph network formed by in-world blocks, connected to each other by line-of-sight laser beams, and synchronously handled by a global data structure. A GLARE network provides and allocates power called 'Lux Capacity', stores and transfers messages in an e-mail-like system, and enables teleportation between locations.

**Vertical Slice:** 0.4


### **GLARE Network Rules**
A GLARE network consist of a graph of links between BlockEntities. A network encodes no sense of directionality; Only membership to the network. At a high-level, a network exists as a structural data-object server-side, associating nodes (blocks) in the network with the network ID, properties, and their immediate neighbours.

When a link addition or subtraction occurs, a network should query whether it needs to rebuilt itself and split into multiple networks, or merge existing networks together, as well as update nodes of their neighbours or new Lux conditions. Limits on the number of connections per nodes (and handling duplicate connections), should preferably be done at a BlockEntity level.

Links between two nodes requires clear line of sight (treating air, fluids, and replaceable blocks as 'clear'). When establishing a link, connection should be blocked if LoS is not available. Nodes with established links should occasionally query their LoS validity every so often (possibly from random tick updates to optimise performance). When LoS is broken, a network may need to split, just as if a node was removed. Similarly, is LoS is regained, it may have to merge networks together.


### Establishing Connections
To establish connection an existing node (BlockEntity) and a newly-placed node, we should utilise Create's targeting approach it employs for blocks such as Ejectors and Display Links; An item with the IGlareNode interface can right-click on any existing BlockEntity with the IGlareNode to visually highlight that position and add it to it's temporary list of potential connection-targets. The number of connection points a node can link to (as an actual block or held-item) should be limited to a configurable count (say 8). If the item is right-clicked anywhere while crouching, the temporary list and highlighted locations are cleared.

Once placed, the new node will then try to form a link with each recorded position, failing and discarding it if a node no longer exists at that location, and either forming a link or not with other depending on their LoS, and updating for forming networks as required.

If a targeted node is already at the limit of links it can support, existing links are broken in order to make room for the new link

Links between GLARE nodes visually appear as a laser-beam-like line-renderer, similar to the beam produced by Guardians and Elder Guardians. It tiles between its two end-points, and uses a distinct texture while LoS is broken.


<<<<<<< Updated upstream
=======

### Implementation Protocols
Because the GLARE system is widely used and very versatile, it's code implementation should be made as generalised and universal as possible, taking full advantage of class inheritance, shared utility classes, and interfaces where possible. There are likely to be many kinds of emitters, receivers, and nodes.


>>>>>>> Stashed changes
### Server Protocol
GLARE networks need to be persistent across sessions and clients. Therefore, they need to function as data structures on the server. Each network should have an identifiable slug/GUID, and contain the previously discussed information. Server management should also exist for cleaning up empty or stale network objects.


### Lux
Lux is a power resource, analogous to Create's Stress, but for GLARE networks. Like kinetic networks, GLARE networks have an available Lux Capacity, dependent on the number of 'emitters' linked to the network.

'Emitters' are nodes on a GLARE network which produce an amount of Lux (int >=0), and may supply a Colour-Charge. Emitters add their contribution to the total amount of Lux Capacity on the network, and the total Colour-Charge counts.  When an emitter is added, removed, or updates its amount of produced Lux, the network should update its capacity (Likewise for Colour-Charge).
Emitters implement an IGlareEmitter interface.

'Receivers' are nodes on a GLARE network which allocate a portion of a network's Lux to themselves, in order to do work. Some receivers may require 0 Lux, while others require an integer amount. If the amount of 'allocated' Lux ever exceeds the network's capacity, the entire network enters an 'Overload' state, where nothing requiring Lux operates. 
Receivers implement an IGlareReceiver interface.

To restart an overloaded network, Lux allocations must be reduced to below or equal to the network's capacity, and a player must interact with any Receivers to toggle its 'Operation Status' back to 'Online'. If successful, this re-enables operation across all nodes and removes the overload state.
If the network is still over-allocated when a player tries to switch it to online, it instead returns to the Overload state. This behaviour replicate the 'Power-Grid Overloads' system from Satisfactory.


### Variable Lux
Some receivers don't just require a constant amount of Lux. Instead, they consume a fluctuating amount of Lux depending on where they might be in a crafting/processing cycle.

Certain recipe types or machine can vary the amount of Lux their IGlareReceiver allocates/consumes. Typically these BlockEntities will define a base 'idle' allocation amount (used when not performing work), and then broadcast a 'processing' amount while progressing through a recipe.

This amount is defined, either in the recipe JSON, or as a final in the class, in the form of an array of ints. Each entry represents an equalised portion, whose value is the amount of Lux allocated to the receiver during that portion of the processing cycle. These arrays are normalised over the processing duration of the recipe, meaning that a processing cycle gradually moves across a curve of Lux values dictated by this array.

E.g.) A recipe lasting 200 ticks might define a Lux consumption array as: [2,2,4,8,10,8,4,2], creating a crude bell curve


### Colour Charge
Emitters on a GLARE network also add a 'colour charge' to their network's total. A Colour-Charge is an enum value equivalent to Minecraft's 16 dyes/colours. By default, emitters produce White Lux.

A network tracks the total amount of each (non-zero) type of Colour-Charge being produced across its network. This data can be accessed by specialised receivers, who can utilise the information to run things like filtering or boolean logic, allowing long-distance redstone or communications logic.


### Telemetry Messages & Inboxes
The GLARE network also supports a secondary server-wide system called 'Telemetry Messaging'.
A Telemetry system is a child of a GLARE network, and access to it is mediated through GLARE network behaviour.

The Telemetry system manages a dynamic list of 'Inbox Addresses' (defined as unique ordered codes consisting of 3 item IDs which act as a unique key/UUID). Each address has an associated 'Inbox', which is a collection of string messages pair with the address they were sent from, stored in order of arrival.
Addresses on a GLARE network will only communicate between themselves - They will not communicate with matching Addresses on other GLARE networks.

The number of messages an inbox can hold is limited to 16 (with newer messages replacing older ones), and messages cannot contain more than 512 characters.

The Telemetry system exposes functionality for BlockEntities or other classes to interact with Inboxes and Messages (such as sending, viewing, or discarding them), as well as ways to subscribe to updates to a specific Inbox.

The Telemetry system should also handle creating new Inboxes when a message is first sent to an address, as well as cleaning up empty Inboxes once they are no longer used. 

 When GLARE networks merge, matching Addresses/Inboxes are merged together (temporarily bypassing inbox size limits). When GLARE networks split, each new network retains a copy of any existing  Addresses/Inboxes.



### **Specific Implementation**
The following entries are specific implementations of blocks utilising the GLARE network framework, or are related to its construction/operation.


##### **Resonance Crystals**
Resonance Crystals are terrain features which can be found rarely throughout the overworld and nether, and commonly in the end. Alternatively, players can also craft Artificial Resonance Crystals themselves using resources obtained from Crystal Drills. Placing Artificial Resonance Crystals in the End Dimension causes a deadly explosion. 

GLARE Emitter Dishes can be placed on to of Resonance Crystals to power a GLARE network.


##### **GLARE Emitter Dish**
Emitters provide an integer amount of power to a Glare network. Emitters themselves must be placed on top of *Resonance Crystals* to function, and can be toggled on or off by redstone.  More powerful variants of the emitter also require kinetic or fluid input to function.
Emitters can also emit a colour-code as part of their connection. The colour-code produced by an emitter can be set by right-clicking with a stained glass block. By default, emitters produce White. 
Emitter Dishes can only target 1 other connection point.


##### **GLARE Relay**
Relays act as universal connection nodes in Glare Networks. They can accept up to 8 different connections.


##### **GLARE Kinetic-Receiver**
Kinetic Receivers consume 1 Lux, and produce 256 units of Stress at 128 RPM when powered by a GLARE network. They can only target 1 other connection point.

##### **GLARE Chromatic-Transceivers**
Transceivers produce a redstone output according to the colour-charges present on their network. Receivers can be configured to filter for a select list of colour-charges using AND/OR/XOR logic and count thresholds. Chromatic-Transceivers can only target 1 other connection point.


##### **Telemetry Terminals**
Telemetry Terminals allow players to use GLARE networks for communication. A Telemetry Terminal can connect to any relay node in a network. Each terminal supports a 3-item Address ID, used to identify its address.

The Telemetry Terminal GUI allows players to enter an item-code (or select one from a list of saved contacts), and then compose a text message which can be sent over the network. Sent messages are delivered to the Address's inbox, and remain there until read and explicitly discarded from another terminal. Multiple terminals can access the same Inbox.

Terminals can be cycled between 3 modes with a Wrench; MANUAL, AUTOMATIC SEND, and AUTOMATIC RECEIVE. In AUTOMATIC SEND mode, players can set a predetermined address and message which will be sent whenever the terminal receives a redstone signal. In AUTOMATIC RECEIVE mode, players can set a filter keyword - Any time that address receives a new message with that keyword, it automatically discards the message and outputs a redstone signal pulse.


##### **Remote Entangler Depot**
The Remote Entangler Depot allows players to slowly teleport items between depots linked to a GLARE network. Entangler Depots are assigned a 3-item ID code. If one depot is set to 'SEND' mode, and the other set to 'RECEIVE', and both are connected to the same network, then items can be teleported from one to the other. Entangler Depots must be *Chilled* to operate, and have a cooldown after every operation.


##### **Remote Entanglement Transporter**
The Remote Entanglement Transporter allows players to teleport to other transporters on a GLARE network using a 3-item ID code. Transporters must be connected to the same GLARE network, and must be fuelled using Liquid Chorus. If multiple transporters share the same ID, a destination is randomly selected.