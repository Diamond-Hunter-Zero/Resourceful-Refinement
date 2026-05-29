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