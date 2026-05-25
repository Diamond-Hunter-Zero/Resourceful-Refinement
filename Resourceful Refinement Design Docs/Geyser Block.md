
#### **Description**
The Geyser Block is a block which naturally generates in geyser structures upon the overworld's surface and within caves. Geysers periodically spawn fluid source blocks above them, as well as jets of particles which launch entities up into the air.

**ID:** *geyser_block*

#### **Behaviour**
Geysers appear as stone blocks with transparent cracks on them, which expose a cube of animated fluid texture within them. Geyser blocks are associated with a registered fluid, which can be set in Creative mode by right-clicking with a bucket. This is stored in the block entity's data. Geysers do not drop when broken, are highly explosion resistant, and take a while to mine.

Ctrl-MiddleMouse clicking to select a geyser block in creative should preserve its NBT data (i.e. it's associated fluid).

**Blasts**
Geysers frequently exhibit 'Blasts', which occur on a random but configurable basis. When a geyser erupts, it spawns a plume of smoke particles, and causes any entities on top of the block to be launched up into the air.

**Eruptions**
Geysers also occasionally spawn fluid source blocks at the block position above them, on a random cooldown. The type of fluid placed is determined by the fluid associated with the geyser. When a new fluid block is placed, the geyser spawns a puff of smoke particles and plays a sound.


#### **Rendering**
Geysers use a BlockEntityRenderer to render an outer cutout stone texture (a standard solid block model, no custom shapes), and an interior cube of fluid. The interior fluid cube is almost flush with the bounds of the outer block texture, and uses the animated fluid texture of the fluid associated with the block. Be default, geysers are associated with lava.

The orientation of the outer stone casing is randomised.