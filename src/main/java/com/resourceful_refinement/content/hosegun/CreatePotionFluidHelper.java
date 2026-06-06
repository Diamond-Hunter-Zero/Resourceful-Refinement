package com.resourceful_refinement.content.hosegun;

import com.simibubi.create.AllDataComponents;
import com.simibubi.create.AllFluids;
import com.simibubi.create.content.fluids.potion.PotionFluid;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.alchemy.PotionContents;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.FlowingFluid;
import net.minecraft.world.level.material.Fluids;
import net.neoforged.neoforge.fluids.FluidStack;

import java.util.Optional;

/**
 * Resolves Create potion fluid stacks into {@link PotionContents} and a bottle delivery type
 * (regular, splash, or lingering).
 */
public final class CreatePotionFluidHelper {

    private static final ResourceLocation CREATE_POTION_ID =
            ResourceLocation.fromNamespaceAndPath("create", "potion");

    private CreatePotionFluidHelper() {}

    public enum PotionBottleType {
        REGULAR,
        SPLASH,
        LINGERING;

        public static PotionBottleType fromCreate(PotionFluid.BottleType bottleType) {
            return switch (bottleType) {
                case SPLASH -> SPLASH;
                case LINGERING -> LINGERING;
                default -> REGULAR;
            };
        }
    }

    public record ResolvedPotionFluid(PotionContents contents, PotionBottleType bottleType) {}

    public static boolean isCreatePotionFluid(Fluid fluid) {
        if (fluid == null || fluid == Fluids.EMPTY) {
            return false;
        }
        Fluid source = fluid;
        if (fluid instanceof FlowingFluid flowing) {
            Fluid resolved = flowing.getSource();
            if (resolved != null && resolved != Fluids.EMPTY) {
                source = resolved;
            }
        }
        return BuiltInRegistries.FLUID.getKey(source).equals(CREATE_POTION_ID)
                || source.isSame(AllFluids.POTION.get().getSource());
    }

    public static boolean hasPotionContents(FluidStack stack) {
        if (stack.isEmpty()) {
            return false;
        }
        PotionContents contents = stack.get(DataComponents.POTION_CONTENTS);
        return contents != null && !contents.equals(PotionContents.EMPTY);
    }

    public static Optional<ResolvedPotionFluid> resolve(FluidStack stack) {
        if (stack.isEmpty()) {
            return Optional.empty();
        }

        PotionContents contents = stack.getOrDefault(DataComponents.POTION_CONTENTS, PotionContents.EMPTY);
        if (contents.equals(PotionContents.EMPTY)) {
            return Optional.empty();
        }

        PotionBottleType bottleType = resolveBottleType(stack);
        return Optional.of(new ResolvedPotionFluid(contents, bottleType));
    }

    private static PotionBottleType resolveBottleType(FluidStack stack) {
        PotionFluid.BottleType createType = stack.get(AllDataComponents.POTION_FLUID_BOTTLE_TYPE);
        if (createType != null) {
            return PotionBottleType.fromCreate(createType);
        }

        ResourceLocation id = BuiltInRegistries.FLUID.getKey(stack.getFluid());
        if (id != null) {
            return inferBottleTypeFromId(id.getPath());
        }
        return PotionBottleType.REGULAR;
    }

    private static PotionBottleType inferBottleTypeFromId(String path) {
        if (path.contains("lingering")) {
            return PotionBottleType.LINGERING;
        }
        if (path.contains("splash")) {
            return PotionBottleType.SPLASH;
        }
        return PotionBottleType.REGULAR;
    }
}
