package com.resourceful_refinement.client.ct;

import com.resourceful_refinement.ResourcefulRefinementMain;
import com.simibubi.create.foundation.block.connected.AllCTTypes;
import com.simibubi.create.foundation.block.connected.CTSpriteShiftEntry;
import com.simibubi.create.foundation.block.connected.CTSpriteShifter;
import net.minecraft.resources.ResourceLocation;

/**
 * Connected-texture sprite shifts, mirroring Create's AllSpriteShifts. Entries must exist before the block
 * atlas is stitched, so {@link #init()} is called from the mod constructor on the client.
 */
public final class ModSpriteShifts {

    /** Heavy Plate Shielding: 8x8 omnidirectional sheet, same template as Create's casings. */
    public static final CTSpriteShiftEntry PLATE_SHIELDING = omni("plate_shielding");

    private ModSpriteShifts() {
    }

    public static void init() {
        // Class-load trigger for the static entries above
    }

    private static CTSpriteShiftEntry omni(String name) {
        return CTSpriteShifter.getCT(AllCTTypes.OMNIDIRECTIONAL,
                ResourceLocation.fromNamespaceAndPath(ResourcefulRefinementMain.MOD_ID, "block/" + name),
                ResourceLocation.fromNamespaceAndPath(ResourcefulRefinementMain.MOD_ID, "block/" + name + "_connected"));
    }
}
