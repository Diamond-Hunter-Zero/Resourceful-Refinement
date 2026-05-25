
#### **Description**
The hosegun is an item tool which has an internal fluid tank, and can be used to fire a spray of gel-blob projectile entities. Gel-blobs carry a fluid ID, and execute different functions or effects when they impact blocks or entities, according to their gel-type.

**ID:** *housegun*


#### **Usage & Ammunition**
The hosegun is considered a ranged weapon, which continuously fires while the user holds down left-click. To fire gel-blobs though, the hosegun must first be filled with a fluid using Create's spout. The hosegun has an internal tank of 1000mb. The hosegun can also be drained using Create's drain.

Each projectile created by the hosegun expends 5mb of fluid. Projectiles travel fast and far, but are inaccurate, creating a slight random spray with a solid-angle. The distance (i.e. horizontal speed) the spray travels also takes time to lerp up from when the hosegun is first activated.

Gel-blobs are destroyed when they impact entities. Unless their gel-type dictates otherwise, gel-blobs do not deal damage to impacted entities.


#### **Miscellaneous Gel types**
Gel-blobs carry a gel-type ID, determined by the fluid ID they also carry. Most fluids create gel splatters when they impact blocks. Some gel types though have particular impact behaviours.

Cleanse gels instead remove any gel splatters within a 3-block radius of the position they impact.

Potion gels instead apply the effects of the potion carried by the blob to any entity they impact.

Paint gels instead switch any colourable block, within a 3 block radius of impact, not matching their mapped colour to the corresponding coloured variant. The following vanilla blocks are considered colourable:
	- *wool*
	- *concrete*
	- *concrete_powder*
	- *terracotta*
	- *glazed_terracotta*
	- *stained_glass*
They will also dye valid entities they impact.