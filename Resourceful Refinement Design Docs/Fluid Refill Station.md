#### **Description**
The Fluid Refill Station is a horizontally directional block, which allows the user to quickly refill fluid containers, such as Hoseguns, buckets, or glass bottles.

It also allows players to track the number of gel splatters created by linked hoseguns

**ID:** *fluid_refill_station*

#### **Behaviour**
A Fluid Refill Station is a horizontally directional block with solid faces. It has an internal fluid tank of 1000mb, and accepts pipe interfacing on any of its faces, except for the north (front facing) face.

If a refill station contains fluid, a player can right-click while holding a fluid container item to fill that item with the fluid from the refill station. Discrete containers (like buckets or bottle) will required the minimum amount of fluid. Other dynamic containers (like the Hosegun), will simply take whatever is available. As per standard fluid behaviour, the containers must be empty or already contain the same fluid.

A refill station cannot itself be refilled by right-clicking with a filled or partially-filled container.


**Gel Tracking**
Right-clicking a refill station without a fluid container in hand allows the user to instead set a 'Tracking ID' label for the refill station, achieved through a GUI menu with a text input field. Refill stations with a tracing ID form part of a gel tracking network. By default, refill stations don't start with any assigned ID.

A user can then crouch-right-click on a labelled refill station with a Hosegun - Doing so binds the Hosegun to the station's Tracking ID (or unbinds it if the hosegun already has the exact matching ID). When gel-blobs from a bound hosegun create or update gel splatter blocks, those gel splatter blocks increment a global track for the number of gel blocks associated with that Tracking ID. When a gel splatter block associated with a Tracking ID is removed, it decrements the global track.

This global gel-count tracking system is world-level data structure (i.e. not stored on individual refill stations, but globally accessible), and can be used as part of minigames, to track the number of created gel splatters associated with an ID (such as a team or player). This means that multiple refill stations can subscribe to the same ID and all be used as refill points for the same team.

Refill Stations can be attached to Create Display Links as information sources. When a Display Link reads from a Refill Station in this way:
- If the refill station has a Tracking ID, the link displays the text "{Tracking ID}: {Number of associated gel blocks}"
- If the refill station doesn't have a Tracking ID, the link displays the text "{current tank fluid amount}mb {fluid name}"

Refill Stations also accept redstone input, allowing a form of global 'reset'. If a refill station receives a redstone signal, and it has a Tracking ID, any gel splatter blocks associated with that Tracking ID are instantly destroyed (so that the global count returns to 0). This destruction should be as optimized and server performant as possible.


#### **Rendering**
Refill Stations use a BlockEntityRenderer for their models. Inside their outer casing model, an interior box is rendered using the animated still texture for the fluid its tanks contain. The height of this box dynamically scales from 0% to 100% according to the fill amount of its tank.