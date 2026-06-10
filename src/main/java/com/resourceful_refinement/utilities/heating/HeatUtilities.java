package com.resourceful_refinement.utilities.heating;

import com.resourceful_refinement.ResourcefulRefinementMain;
import com.simibubi.create.api.boiler.BoilerHeater;
import com.simibubi.create.content.processing.recipe.HeatCondition;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;

public class HeatUtilities {

    public static TagKey<Block> COOLED_BLOCK_TAG = BlockTags.create(ResourceLocation.fromNamespaceAndPath(ResourcefulRefinementMain.MOD_ID, "cooled_source"));
    public static TagKey<Block> CHILLED_BLOCK_TAG = BlockTags.create(ResourceLocation.fromNamespaceAndPath(ResourcefulRefinementMain.MOD_ID, "chilled_source"));

    public static ExtendedHeatCondition ConvertHeatLevelToExtendedCondition(int heatLevel)
    {
        if (heatLevel < -2)
            return ExtendedHeatCondition.CHILLED;
        else if (heatLevel == -2)
            return ExtendedHeatCondition.COOLED;
        else if (heatLevel == -1)
            return ExtendedHeatCondition.NONE;
        else if (heatLevel == 0)
            return ExtendedHeatCondition.PASSIVE;
        else if (heatLevel == 1)
            return ExtendedHeatCondition.HEATED;
        else
            return ExtendedHeatCondition.SUPERHEATED;
    }

    public static HeatCondition ConvertHeatLevelToCondition(int heatLevel)
    {
        return HeatCondition.values()[Math.clamp(heatLevel, 0, 2)];
    }

    public static String GetHeatTitle(int heatLevel)
    {
        return ConvertHeatLevelToExtendedCondition(heatLevel).getSerializedName();
    }

    public static int GetHeatColour(int heatLevel)
    {
        return ConvertHeatLevelToExtendedCondition(heatLevel).getColor();
    }

    public static int GetExtendedHeatLevel(Level level, BlockPos pos)
    {
        // Use custom tags/logic to look for cooled/chilled/none sources
        if (level.getBlockState(pos).is(COOLED_BLOCK_TAG))
            return -2;
        if (level.getBlockState(pos).is(CHILLED_BLOCK_TAG))
            return -3;

        // Use base logic to look for heated or superheated sources
        return  (int) BoilerHeater.findHeat(level, pos, level.getBlockState(pos));
    }

    public static int GetGaugeRotation(int heatLevel)
    {
        if (heatLevel <= -3)
            return 120;
        else if (heatLevel == -2)
            return 60;
        else if (heatLevel == -1)
            return 180;
        else if (heatLevel == 0)
            return 0;
        else if (heatLevel == 1)
            return -60;
        else
            return -120;
    }

}
