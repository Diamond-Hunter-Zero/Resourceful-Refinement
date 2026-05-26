package com.resourceful_refinement.data;

import com.resourceful_refinement.ResourcefulRefinementMain;
import com.resourceful_refinement.content.fluids.base.FluidGroup;
import com.resourceful_refinement.registry.FluidEntry;
import com.resourceful_refinement.registry.ModFluids;
import net.minecraft.data.PackOutput;
import net.neoforged.neoforge.client.model.generators.ItemModelProvider;
import net.neoforged.neoforge.common.data.ExistingFileHelper;

public class ModItemModelProvider extends ItemModelProvider {

    public ModItemModelProvider(PackOutput output, ExistingFileHelper existingFileHelper) {
        super(output, ResourcefulRefinementMain.MOD_ID, existingFileHelper);
    }

    @Override
    protected void registerModels() {
        for (FluidEntry entry : ModFluids.ENTRIES) {
            if (entry.group == FluidGroup.PAINT) {
                withExistingParent(entry.bucket.getId().getPath(), modLoc("item/paint_fluid_bucket"));
            }
        }
    }
}
