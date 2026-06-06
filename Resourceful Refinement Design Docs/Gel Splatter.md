#### **Description**
Gel Splatters are a multi-face block (i.e. block with configurable interior faces like cave-lichen or amber) which represent an inworld layers of fluids, placed down by the Hosegun. They are heavily inspired by the gels from Portal 2, or the paints from Splatoon. A Gel Splatter block is linked to a fluid ID, which determines its tint colour and physical properties. Any fluid except water or potions can be turned into a Gel Splatter

**ID:** *gel_splatter*

#### **Shape & Placement**
The Gel Splatter is a multi-face block. To create Gel Splatters, players must fill a Hosegun with any non-water/non-potion fluid, then use the hosegun to shoot out gel-blob projectile entities.

When a gel-blob projectile impacts a solid block face facing into a empty or replaceable space, it attempts to create a gel splatter block in that corresponding empty space. This new gel splatter will automatically populate any valid faces adjacent to solid blocks with the corresponding regions of it's model. 

- If a gel splatter already exists in the impacted space, and is of the same fluid type, it repopulates all valid faces.

- If a gel splatter already exists in the impacted space, and is of a different fluid type, it replaces the data held by that gel splatter with the new fluid, and repopulates all valid faces.

- Like other multi-face blocks, the individual faces of a gel splatter can be broken by hand. 

- Gel-blob projectiles carrying the water fluid do not create gel-splatters, but instead replace any gel splatter blocks they impact into with air, allowing players to 'clean' up gels.


#### **Model**
The Gel Splatter block uses a custom java block model, instead of the standard 6-face cube. Each 'face' element of the model is named after it's respective parent face however (i.e. 'north', 'south', 'east'... etc.)


#### **Gel Properties**
All fluids are associated with a 'Gel' type, which determines the properties of the gel splatter created by these fluids. These properties may be physical characteristics (such as slipperiness, stickiness, or bounciness), or tick effects which run while a player or living entity is colliding with the splatter (such as granting a status effect or attribute modifier).

For custom fluids implemented by this mod, this gel-type property is assigned as part of its `register()` construction. For other fluids, gel-types are determined and mapped during runtime by using custom JSON tags - This include vanilla Lava and Create fluids. Fluids can also be listed under the `doesNotMakeGel` tag, which prevents gel-blobs using these fluids from making gels splatters, like water. Any fluid (such as those added by other mods) not listed in a tag JSON defaults to the `Inert` gel type.

The following gel types are to be implemented:

| **Gel Type ID** | **Properties**                                                                                                 | **Creates Gel Splatter?** | **Tag ID**        | **Registered Fluids**                                                                                                                         |
| --------------- | -------------------------------------------------------------------------------------------------------------- | ------------------------- | ----------------- | --------------------------------------------------------------------------------------------------------------------------------------------- |
| `Molten`        | - Deals contact damage (like magma blocks)<br>- Deals damage to any entities its gel-blobs collide with        | Yes                       | `makesMoltenGel`  | molten_crimsite, molten_veridium, molten_ochrum, molten_asurine, molten_scorchia, lava                                                        |
| `Speedy`        | - Slippery<br>- Applies Speed 1                                                                                | Yes                       | `makesSpeedyGel`  | catalysed_iron, catalysed_copper, catalysed_gold, catalysed_zinc, catalysed_redstone, catalysed_sparkpowder, catalysed_carborax, builders_tea |
| `Gooey`         | - Sticky                                                                                                       | Yes                       | `makesGooeyGel`   | honey, chocolate, carborax_diesel                                                                                                             |
| `Bouncy`        | - High bounciness<br>- Gel-blob applies moderate knockback on impacting entities                               | Yes                       | `makesBouncyGel`  | durasteel_alloy, purified_durasteel, overcharged_carborax                                                                                     |
| `Cursed`        | - Applies Slowness 2<br>- Applies Weakness 1                                                                   | Yes                       | `makesCursedGel`  | unrefined_carborax                                                                                                                            |
| `Blessed`       | - Applies Strength 1                                                                                           | Yes                       | `makesBlessedGel` |                                                                                                                                               |
| `Inert`         | - No effect                                                                                                    | Yes                       | `makesInertGel`   | silica_substrate, purified_iron, purified_copper, purified_gold, purified_zinc,                                                               |
| `Cleanse`       | - Removes gel splatters                                                                                        | No                        | `makesCleanseGel` | water                                                                                                                                         |
| `Potion`        | - Gel-blob applies potion effect on impacting entity                                                           | No                        | `makesPotionGel`  | *Any potion fluids*                                                                                                                           |
| `Paint`         | - Gel-blob dyes entities on impact (sheep wool, dog & cat collars)<br>- Gel-blob dyes colored blocks on impact | No                        | `makesPaintGel`   | *Any paint fluids*                                                                                                                            |


