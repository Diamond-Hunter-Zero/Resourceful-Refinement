### **Overview**
I want to create a Minecraft NeoForge mod for 1.21.1, which is an addon for the Create Mod. This mod (under the ID "resourceful_refinement") introduces a number of new 'crafting station' blocks for Create, which will be used to craft new types of recipes and process fundamental mineral resources at a higher output rate. The mod focuses on converting ores and alloys back and forth between items and fluids, and passing them through several new blocks and multi-block structures, to create a factory-game-like progression.

### **Technical Specifications**
**Mod ID:** resourceful_refinement
**Minecraft Version:** 1.21.1
**Mod Loader:** NeoForge 21.1.219
**Create Version:** 6.0.11-283+


### **Content (v0.1)**

**Molten Minerals**
The mod adds a number of new fluids, representing liquid states for several of Create's mineral blocks or other alloys. These fluids behave like lava, but are not considered fuels with the exception of Carborax variants.
- Molten Crimsite
- Molten Ochrum
- Molten Veridium
- Molten Scorchia
- Molten Asurine
- Unrefined Carborax

**Refined Fluids**
In addition or raw 'ore' fluids, there are a number of refined fluids which represent varying intermediary steps in the processing chain for various materials.
- Silica Substrate
- Molten Andesite Blend
- Molten Brass Blend
- Molten Netherite Blend
- Catalysed Iron
- Catalysed Gold
- Catalysed Copper
- Catalysed Zinc
- Catalysed Redstone
- Catalysed Carborax
- Purified Iron
- Purified Gold
- Purified Copper
- Purified Zinc
- Purified Durasteel
- Durasteel Alloy
- Overcharged Carborax

**Blender Blade**
A shaft-like block with two wide blades extending from it's length, which rotate when provided with rotational input. While spinning, pushes entities in the blade plane tangentially to rotation and deals contact damage. See [[Blender Blade]] for full behaviour, geometry, and implementation detail.

**Fluid Refinery**
A large multiblock structure which serves as a crafting station from combining fluids and items. The structure accepts up to 2 item input and 2 fluid inputs, and has 1 fluid output. It requires rotational-input to operate, and has three levels of heating like a blaze-burner. Its recipes belong to the "*fluid_refinery*" type.

**Refinery Access Port**
A controller block for the Fluid Refinery, which as acts as the access-point for the refinery's fluid output. When right-clicked, it assembles an appropriate structure of blocks into the multiblock entity.

**Forge Mould**
A single block machine (similar to a Mechanical Press crossed with a Spout) which accepts a fluid input and item input, and outputs an item onto an empty belt or depot below. Requires rotational input. These recipes belong to the "*forge_mould*" type. If placed above a *Casting Depot*, it instead produces an item from a fluid input alone. These recipes belong to the "*casting_mould*" type.

**Casting Depot**
A variant of the Depot block, which when placed under a *Forge Mould*, allows it to instead produce items.

**Mechanical Sieve**
Fluids can be piped into its top face. When provided rotational input from cogs on any of it's sides except the front, it transforms the input fluid into an output fluid pushed out from its bottom face, producing by-product items that can be extracted from its front face. Its recipes belong to the "*mechanical_fluid_sieve*" type.

**Geyser Deposits**
Naturally generating blocks within geyser structures that spawn throughout and beneath the surface. Geyser blocks spawn source blocks of molten fluids above them on random tick updates. There is a variant for each of the molten ore fluids defined above (Crimsite through to Carborax). Geyser blocks cannot be broken, and do not drop if destroyed.

**Fracking Pylon/Pump**
A multiblock structure assembled with glue, which must be built atop of a geyser block. When provided with a rotational input, it passively outputs a steady supply of the corresponding fluid.



### **Content (v0.2)**

**Paints**
Paints are fluid forms of Minecraft's dyes. They are crafted in the Fluid Refinery, and can be used to dye blocks and entities with the Hosegun

**Hosegun**
The hosegun is a tool which stores fluids, and fires them as gel-blob projectiles. Gel-blobs carry the fluid used to create them, and execute different effects upon impact, such as dyeing blocks, or creating gel splatters

**Gel Splatters**
Gel Splatters are multi-face blocks created when gel-blobs impact solid blocks. They store a linked fluid ID and gel-type, which determines their physical properties and tint color.

**Paint Nozzle**
A directional pipe block, which sprays gel-blobs while open.

**Fluid Refill Station**
A directional fluid tank block, which allows players to quickly refill fluid storing items such as Hoseguns. Can also be paired with Hoseguns and Display Links to track linked gel splatter blocks for minigames.

**Liquid Glue**
A fluid form of glue, replacing slime-balls and honey in some recipes.

**Plunger**
A thrown trident-like weapon, which can be used to right-click on fluid containing blocks to empty their fluid tanks.

### **Content (v0.3)**

**Distillery**
Distillery tanks are vertically stackable blocks, which placed on top of each other, will form a Distillery tower. Distilleries are used to refine fluids and input items into fluid outputs in combination with heat and time.

Distillery recipes will specific an exact distillery height needed for the recipe to function. Distillery towers also require a heat-source beneath them. This heat source can either be passive (fire, campfire, magma, lava, etc...), a Chilled radiator, or a Heated or Superheated blaze burner. Once the conditions of a distillery recipe are met, the distillery will begin a fermentation process, which takes a long time. Ingredients are only consumed and outputs produced, once the fermentation process finishes.


**Radiators**
Radiators are pipe-like blocks which heat or cool their surroundings. Most fluids which flow through a radiator won't change its heat. Those which doe are partially consumed as they flow through.
- If water is pumped through the radiator, it becomes 'Cooled'
- If coolant is pumped through the radiator, it becomes 'Chilled'
- If Overcharged Carborax is pumped through the radiator it becomes 'Heated'

Cooled and Chilled radiators are used to cool Combustion Chambers or Distillery towers. Heated radiators can be used in place of Blaze Burners for any heating purpose.


**Combustion Chambers**
Combustion Chambers are industrial engines which turn carborax fuels into kinetic rotation. Combustion Chambers have a fluid intake on one face, and a shaft output on the opposing face. When placed in a line, chambers share a single fluid intake and shaft output, but combine their consumption and output.

Combustion Chambers require cooling according to their fuel.
- Unrefined carborax requires no cooling, but produces very little stress at low speed for large amounts of fuel.
- Catalysed carborax produces moderate stress and medium speed when 'Cooled', and half as much when running passively.
- Overcharged carborax produces large amounts of stress at high speed when 'Chilled', and a third as much when 'Cooled'. It will not run passively.


**Milking Station**
The Milking Station is a production block which produces fluids or items from mobs at regular intervals. A player holding a leashed mob can right-click on the milking station to secure that mob above the station. Once it holds a secured mob and has been provided with rotational input, the milking station slowly produces outputs based on the mob it holds.
- Cow -> Milk
- Mooshroom/Brown Mooshroom -> Stew
- Blaze -> Blaze Powder
- Creeper -> Gunpowder
- Squid -> Ink Sacs
- Glow Squid -> Glow Ink
- Ghast -> Ghast Tears
- Player -> Milk (special interaction, occurs when right-clicking and empty milking-station with an empty hand. Crouch to exit)
Mobs held by a milking station have no AI, and remember any NBT data they held.

Players can right-click a Milking Station with an empty leash to eject the mob and leash it again.


**Advanced Pump**
The Advanced Pump transfers fluids at twice the config distance limit of standard Create pumps, allowing it to support much larger fluid networks. The Advanced Pump is a fully directional block, accepting fluid inputs from its south (behind) face, and outputting fluids to its north (front) face. Like normal pumps, it requires kinetic input in the form of a cog connection to any of it's side faces.

In addition, when a redstone signal is applied, it reverses its flow direction, allowing it to be used as a directional switch or valve. 

The Advanced Pump also shows its fluid passthrough rate in mb/s when viewed with goggles.


**Stuffing & Stuffing Sleaves**
A fluid created from mixing wool or string with water. Unlike other fluids, its source block is just white wool.
The Stuffing Sleave is a multi-variant block which attaches to pipes and acts as a fluid container which only accepts stuffing. Once filled with enough stuffing, a Stuffing Sleave will transform into a block or entity according to its variant (Plushie, Sports Ball, etc...).


**Sports Ball**
A decorative entity, which bounces around like a bouncy ball. When hit by an attack, it is propelled forwards. Created by placing a "Sports Ball Stuffing Sleave" on a pipe outlet, and allowing stuffing to fill it.


### **Content (v0.4)**

**Excavator Wheels**
Excavator Wheels are kinetic blocks with a large protruding blockEntity renderer, which extract resources from *Mineral Deposits*. When provided with rotational input, Excavator Wheels will passively produce resources according to any *Mineral Deposits* they intersect with. Excavator Wheels will not operate if intersecting with another Excavator Wheel.

**Mineral Deposits**
Mineral Deposits are geological features which spawn on the surface of the overworld and end dimension, and can be mined via Excavator Wheels to passively produce simple resources such as stone variants, and dyes.

**Chorus Crystal**
Chorus Crystal is a translucent block which generates in large crystal-spike formations within the Choral Clusters biome, out in the End Dimension.

**Choral Cluster Biome**
The Choral Cluster is a mountainous biome which generates in the outer End Dimension. Large crystalline spikes and pillars of Chorus Crystal generate here, as well as Resonance Crystals and Chorus Crystal Hearts.

**Crystal Drills**
Crystal Drills are large multiblock structures which allow the extraction of resources from *Crystal Hearts*. Once constructed on top of a Crystal Heart, drills must be provided with kinetic input from the top, and fluid from behind. Outputs are then transferred from the front of the drill. Drills can also have their efficiency increased by switching to 'turbo mode', and powering its GLARE Receivers on their side faces - While in turbo mode, a drill consumes an amount of power from the GLARE network according to its position on a curve dictated by the current progression of its cycle.

**Resonance Crystals**
Resonance Crystals are terrain features which can be found rarely throughout the overworld and nether, and commonly in the end. Alternatively, players can also craft Artificial Resonance Crystals themselves using resources obtained from Crystal Drills. Placing Artificial Resonance Crystals in the End Dimension causes a deadly explosion.
GLARE Emitters can be placed on to of Resonance Crystals to power a GLARE network.

**G.L.A.R.E. Networks**
Players can construct networks of emitters, receivers, and relays connected by 'GLARE Beams', to empower machines, construct new resources, or communicate over long distance.

Glare Beams link elements in a network together. Pair of elements forming a link in a network each contain a reference to each other - GLARE networks are bidirectional. If a beam's path is blocked by a solid block, it it not considered to form a valid path.

**GLARE Emitter**
Emitters provide an integer amount of power to a Glare network. Emitters themselves must be placed upon *Resonance Crystals*, and can be toggled on or off by redstone.  More powerful variants of the emitter require kinetic or fluid input to function.
Emitters can also emit a colour-code as part of their connection. The colour-code produced by an emitter can be set by right-clicking with a stained glass block. By default, emitters produce white light. A GLARE network records the current colours being supplied to its network, and their counts.
Emitters can only target 1 other connection point.

**GLARE Relay**
Relays act as universal connection points in Glare Networks. They can accept up to 8 different connections.

**GLARE Chromatic-Transceivers**
Transceivers produce a redstone output according to the colour-charges present on their network. Receivers can be configured to filter for a select list of colour-charges using AND/OR/XOR logic and count thresholds. Receivers can only target 1 other connection point.

**Telemetry Terminals**
Telemetry Terminals allow players to use GLARE networks for communication. A Telemetry Terminal can connect to any relay node in a network. Each terminal supports a 3-item ID Code, used to identify its address.

The Telemetry Terminal GUI allows players to enter an item-code (or select one from a list of saved contacts), and then compose a text message which can be sent over the network. Sent messages are delivered to the code's inbox, and remain there until read and explicitly discarded from another terminal. Multiple terminals can access the same inbox.

Terminals can be cycled between 3 modes; MANUAL, AUTOMATIC SEND, and AUTOMATIC RECEIVE. In AUTOMATIC SEND mode, players can set a predetermined address and message which will be sent whenever the terminal receives a redstone signal. In AUTOMATIC RECEIVE mode, players can set a filter keyword - Any time that address receives a new message with that keyword, it automatically discards the message and outputs a redstone signal pulse.

**Remote Entangler Depot**
The Remote Entangler Depot allows players to slowly teleport items between depots linked to a GLARE network. Entangler Depots are assigned a 3-item ID code. If one depot is set to 'SEND' mode, and the other set to 'RECEIVE', and both are connected to the same network, then items can be teleported from one to the other. Entangler Depots must be *Chilled* to operate, and have a cooldown after every operation.

**Remote Entanglement Transporter**
The Remote Entanglement Transporter allows players to teleport to other transporters on a GLARE network using a 3-item ID code. Transporters must be connected to the same GLARE network, and must be fuelled using Liquid Chorus. If multiple transporters share the same ID, a destination is randomly selected.

**Launch Pads & the P.U.G.**
Launch Pads are a 3x3 multiblock structures constructed from launch pad blocks and a launch-controller. Launch Pads can be used alongside PUGs to transport items between locations or dimensions outside the nether.

Each Launch-Controller can be assigned a 3-item ID code, and placed in either SEND or RECEIVE mode. A controller in SEND mode then has several configurations which can be adjusted:
- Destination ID
- Launch Condition (*When full, On redstone input, On timer*)
- Launch Timer (0s-1hr)

Other launch-pad blocks can be configured to act as item or fluid (fuel) interfaces. When items begin to flow into a SEND launch-pad, the pad instantiates a PUG vehicle. Once the controller has a valid destination, is filled with sufficient fuel to reach that destination, has LoS access to the sky, and meets its Launch Condition, the PUG is deployed and flies upwards, carrying the launch-pad's inventory with it.

If multiple RECEIVE launch-pads share the same code, the launch-pad will not consider them valid targets.

Once having reached a certain altitude, PUG entities are removed, and instead simulate travel as data objects. PUGs take an amount of time to 'travel' according to the distance between pads, and whether they have to travel across dimensions. Once this travel time is completed, a PUG will wait for its destination pad to be 'unoccupied' - If available, the PUG 'claims' the pad (marking it as occupied), instantiates itself, and lands on the pad, transferring its inventory to the pad. The PUG is then de-instantiated and the pad marked as unoccupied once its inventory is emptied.

If a PUG's destination no longer exists at the end of its travel (the controller has been removed, changed its ID, or moved location), the PUG instead crash-lands at its last known location, and becomes a world entity with a lootable inventory.



### **Content (v1)**

**Manifolds**
Distillery tanks ar

**Delivery Monoliths**