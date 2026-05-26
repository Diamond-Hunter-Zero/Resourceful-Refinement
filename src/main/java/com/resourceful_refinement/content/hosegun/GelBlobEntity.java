package com.resourceful_refinement.content.hosegun;

import com.resourceful_refinement.content.gel_splatter.GelImpactConstants;
import com.resourceful_refinement.content.gel_splatter.GelPropertiesManager;
import com.resourceful_refinement.content.gel_splatter.GelSplatterBlock;
import com.resourceful_refinement.content.gel_splatter.GelSplatterBlockEntityAccess;
import com.resourceful_refinement.content.gel_splatter.GelSplatterBlocks;
import com.resourceful_refinement.content.gel_splatter.GelType;
import net.minecraft.world.phys.Vec3;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.animal.Sheep;
import net.minecraft.world.entity.projectile.ThrowableItemProjectile;
import net.minecraft.world.entity.projectile.ThrowableProjectile;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;

import java.util.ArrayDeque;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Queue;
import java.util.Set;

public class GelBlobEntity extends ThrowableItemProjectile {

    private static final EntityDataAccessor<String> FLUID_ID = SynchedEntityData.defineId(GelBlobEntity.class, EntityDataSerializers.STRING);

    private static final Map<Fluid, DyeColor> PAINT_FLUID_COLORS = new HashMap<>();

    static {
        // Map paint fluid names to minecraft colors
        registerPaintColor("white_paint", DyeColor.WHITE);
        registerPaintColor("orange_paint", DyeColor.ORANGE);
        registerPaintColor("magenta_paint", DyeColor.MAGENTA);
        registerPaintColor("light_blue_paint", DyeColor.LIGHT_BLUE);
        registerPaintColor("yellow_paint", DyeColor.YELLOW);
        registerPaintColor("lime_paint", DyeColor.LIME);
        registerPaintColor("pink_paint", DyeColor.PINK);
        registerPaintColor("gray_paint", DyeColor.GRAY);
        registerPaintColor("light_gray_paint", DyeColor.LIGHT_GRAY);
        registerPaintColor("cyan_paint", DyeColor.CYAN);
        registerPaintColor("purple_paint", DyeColor.PURPLE);
        registerPaintColor("blue_paint", DyeColor.BLUE);
        registerPaintColor("brown_paint", DyeColor.BROWN);
        registerPaintColor("green_paint", DyeColor.GREEN);
        registerPaintColor("red_paint", DyeColor.RED);
        registerPaintColor("black_paint", DyeColor.BLACK);
    }

    private static void registerPaintColor(String fluidPath, DyeColor color) {
        ResourceLocation id = ResourceLocation.fromNamespaceAndPath("resourceful_refinement", fluidPath);
        Fluid f = BuiltInRegistries.FLUID.get(id);
        if (f != Fluids.EMPTY) {
            PAINT_FLUID_COLORS.put(f, color);
        }
    }

    public GelBlobEntity(EntityType<? extends ThrowableItemProjectile> type, Level level) {
        super(type, level);
    }

    public GelBlobEntity(EntityType<? extends ThrowableItemProjectile> type, double x, double y, double z, Level level) {
        super(type, x, y, z, level);
    }

    public GelBlobEntity(EntityType<? extends ThrowableItemProjectile> type, LivingEntity shooter, Level level) {
        super(type, shooter, level);
    }

    @Override
    protected Item getDefaultItem() {
        return Items.SLIME_BALL;
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder); // Let parent register its data (e.g. item slot, ID 8)
        builder.define(FLUID_ID, "minecraft:empty");
    }

    public void setFluid(Fluid fluid) {
        this.entityData.set(FLUID_ID, BuiltInRegistries.FLUID.getKey(fluid).toString());
    }

    public Fluid getFluid() {
        return BuiltInRegistries.FLUID.get(ResourceLocation.parse(this.entityData.get(FLUID_ID)));
    }

    @Override
    public void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        tag.putString("Fluid", this.entityData.get(FLUID_ID));
    }

    @Override
    public void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        if (tag.contains("Fluid")) {
            this.entityData.set(FLUID_ID, tag.getString("Fluid"));
        }
    }

    @Override
    protected void onHit(HitResult result) {
        super.onHit(result);
        if (!this.level().isClientSide) {
            this.discard();
        }
    }

    @Override
    protected void onHitEntity(EntityHitResult result) {
        super.onHitEntity(result);
        if (this.level().isClientSide) return;

        Entity entity = result.getEntity();
        Fluid fluid = getFluid();
        GelType type = GelPropertiesManager.getGelType(fluid);

        switch (type) {
            case MOLTEN -> {
                entity.hurt(this.level().damageSources().thrown(this, this.getOwner()), 4.0F);
                entity.igniteForSeconds(4);
            }
            case BOUNCY -> {
                if (entity instanceof LivingEntity living) {
                    living.knockback(0.8D, -this.getDeltaMovement().x, -this.getDeltaMovement().z);
                }
            }
            case PAINT -> {
                DyeColor color = PAINT_FLUID_COLORS.get(fluid);
                if (color != null) {
                    if (entity instanceof Sheep sheep) {
                        sheep.setColor(color);
                    }
                }
            }
            case POTION -> {
                // Apply standard splash potion style impact if custom potion fluids exist
                if (entity instanceof LivingEntity living) {
                    living.addEffect(new MobEffectInstance(net.minecraft.world.effect.MobEffects.POISON, 100, 0));
                }
            }
        }
    }

    @Override
    protected void onHitBlock(BlockHitResult result) {
        super.onHitBlock(result);
        if (this.level().isClientSide) return;

        Fluid fluid = getFluid();
        GelType type = GelPropertiesManager.getGelType(fluid);
        BlockPos impactPos = result.getBlockPos();
        Direction face = result.getDirection();
        BlockPos placePos = impactPos.relative(face);

        spawnImpactSplash(result.getLocation(), fluid, type);

        if (type == GelType.CLEANSE) {
            // Water/Cleanse: Clear gel splatters within a 3-block radius
            clearGels(impactPos);
            return;
        }

        if (type == GelType.PAINT) {
            // Paint Fluid: dye colorable blocks within a 3-block radius
            DyeColor color = PAINT_FLUID_COLORS.get(fluid);
            if (color != null) {
                dyeBlocks(impactPos, color);
            }
            return;
        }

        // Try creating/extending Gel Splatters at the impact face and reachable neighbours
        BlockState hitState = this.level().getBlockState(impactPos);
        boolean hitGelSplatter = GelSplatterBlocks.is(hitState);

        if (hitGelSplatter) {
            // Multiface gel is not sturdy; seed BFS from the struck splatter so spread still runs
            Direction attachmentFace = face.getOpposite();
            for (BlockPos candidate : findReachablePlacementPositions(impactPos)) {
                attemptGelSplatterAt(candidate, candidate.equals(impactPos) ? attachmentFace : null, fluid);
            }
        } else if (hitState.isFaceSturdy(this.level(), impactPos, face)) {
            Direction attachmentFace = face.getOpposite();
            for (BlockPos candidate : findReachablePlacementPositions(placePos)) {
                attemptGelSplatterAt(candidate, candidate.equals(placePos) ? attachmentFace : null, fluid);
            }
        }
    }

    /** Passable positions within {@link GelImpactConstants#IMPACT_RADIUS_SMALL} of {@code start}. */
    private Set<BlockPos> findReachablePlacementPositions(BlockPos start) {
        Set<BlockPos> reachable = new HashSet<>();
        Queue<BlockPos> queue = new ArrayDeque<>();

        reachable.add(start);
        queue.add(start);

        while (!queue.isEmpty()) {
            BlockPos current = queue.poll();

            for (Direction direction : Direction.values()) {
                BlockPos next = current.relative(direction);
                if (reachable.contains(next) || !canWalkBetween(current, next)) {
                    continue;
                }
                if (!GelImpactConstants.isWithinImpactSphere(start, next, GelImpactConstants.IMPACT_RADIUS_SMALL)) {
                    continue;
                }
                reachable.add(next);
                queue.add(next);
            }
        }
        return reachable;
    }

    private void spawnImpactSplash(Vec3 hitPos, Fluid fluid, GelType type) {
        if (this.level() instanceof net.minecraft.server.level.ServerLevel server) {
            HosegunParticleEffects.spawnImpactSplash(server, hitPos, fluid, type, this.random);
        }
    }

    private boolean canWalkBetween(BlockPos from, BlockPos to) {
        BlockState fromState = this.level().getBlockState(from);
        BlockState toState = this.level().getBlockState(to);
        return isPassableForGelSpread(fromState) && isPassableForGelSpread(toState);
    }

    private boolean isPassableForGelSpread(BlockState state) {
        return state.isAir() || state.canBeReplaced() || GelSplatterBlocks.is(state);
    }

    private boolean canHostGelSplatter(BlockPos pos) {
        BlockGetter level = this.level();
        BlockState state = level.getBlockState(pos);
        if (!isPassableForGelSpread(state)) {
            return false;
        }
        for (Direction direction : Direction.values()) {
            if (GelSplatterBlock.canAttachTo(level, direction, pos, level.getBlockState(pos.relative(direction)))) {
                return true;
            }
        }
        return false;
    }

    private void attemptGelSplatterAt(BlockPos placePos, Direction attachmentFace, Fluid fluid) {
        Fluid resolvedFluid = GelPropertiesManager.resolveSourceFluid(fluid);
        BlockState currentPlaceState = this.level().getBlockState(placePos);

        if (GelSplatterBlocks.is(currentPlaceState)) {
            BlockState updated = GelSplatterBlocks.withVariantForFluid(currentPlaceState, resolvedFluid);
            boolean variantChanged = updated.getBlock() != currentPlaceState.getBlock();
            if (attachmentFace != null) {
                updated = updated.setValue(GelSplatterBlock.getFaceProperty(attachmentFace), true);
            }
            // Repopulate faces before syncing fluid — setFluid may setBlock and notify neighbours.
            placeGelSplat(placePos, updated);
            BlockEntity be = this.level().getBlockEntity(placePos);
            if (be instanceof GelSplatterBlockEntityAccess splatterBe) {
                if (variantChanged || !splatterBe.getFluid().isSame(resolvedFluid)) {
                    splatterBe.setFluid(resolvedFluid);
                }
            }
            return;
        }

        if (!canHostGelSplatter(placePos)) {
            return;
        }

        if (currentPlaceState.isAir() || currentPlaceState.canBeReplaced()) {
            BlockState newState = GelSplatterBlocks.defaultStateForFluid(resolvedFluid);
            if (attachmentFace != null) {
                newState = newState.setValue(GelSplatterBlock.getFaceProperty(attachmentFace), true);
            }
            placeGelSplat(placePos, newState);
            BlockEntity be = this.level().getBlockEntity(placePos);
            if (be instanceof GelSplatterBlockEntityAccess splatterBe) {
                splatterBe.setFluid(resolvedFluid);
            }
        }
    }

    private void placeGelSplat(BlockPos pos, BlockState state)
    {
        BlockState tempState = state;
        for (Direction direction : Direction.values()) {
            BlockGetter level = this.level();
            if (GelSplatterBlock.canAttachTo(level, direction, pos, level.getBlockState(pos.relative(direction)))) {
                // Apply the property for this specific face
                tempState = tempState.setValue(GelSplatterBlock.getFaceProperty(direction), true);
            } else {
                tempState = tempState.setValue(GelSplatterBlock.getFaceProperty(direction), false);
            }
        }

        this.level().setBlock(pos, tempState, 3);
    }

    private void clearGels(BlockPos center) {
        float radius = GelImpactConstants.IMPACT_RADIUS_LARGE;
        int extent = GelImpactConstants.getBlockSearchExtent(radius);
        for (BlockPos pos : BlockPos.betweenClosed(
                center.offset(-extent, -extent, -extent), center.offset(extent, extent, extent))) {
            if (!GelImpactConstants.isWithinImpactSphere(center, pos, radius)) {
                continue;
            }
            if (GelSplatterBlocks.is(this.level().getBlockState(pos))) {
                this.level().setBlock(pos, Blocks.AIR.defaultBlockState(), 3);
            }
        }
    }

    private void dyeBlocks(BlockPos center, DyeColor color) {
        float radius = GelImpactConstants.IMPACT_RADIUS_LARGE;
        int extent = GelImpactConstants.getBlockSearchExtent(radius);
        for (BlockPos pos : BlockPos.betweenClosed(
                center.offset(-extent, -extent, -extent), center.offset(extent, extent, extent))) {
            if (!GelImpactConstants.isWithinImpactSphere(center, pos, radius)) {
                continue;
            }
            BlockState state = this.level().getBlockState(pos);
            Block block = state.getBlock();

            Block dyedBlock = getDyedBlock(block, color);
            if (dyedBlock != null && dyedBlock != block) {
                this.level().setBlock(pos, dyedBlock.defaultBlockState(), 3);
            }
        }
    }

    private Block getDyedBlock(Block original, DyeColor color) {
        String name = BuiltInRegistries.BLOCK.getKey(original).getPath();

        // Match generic suffixes to colourize
        if (name.endsWith("_wool") || name.equals("wool")) return getBlockFromPath(color.getName() + "_wool");
        if (original instanceof ConcretePowderBlock) return getBlockFromPath(color.getName() + "_concrete_powder");
        if (original instanceof StainedGlassBlock) return getBlockFromPath(color.getName() + "_stained_glass");

        if (name.contains("concrete") && !name.contains("powder")) return getBlockFromPath(color.getName() + "_concrete");
        if (name.contains("glazed_terracotta")) return getBlockFromPath(color.getName() + "_glazed_terracotta");
        if (name.contains("terracotta") && !name.contains("glazed")) return getBlockFromPath(color.getName() + "_terracotta");

        return null;
    }

    private Block getBlockFromPath(String path) {
        return BuiltInRegistries.BLOCK.get(ResourceLocation.withDefaultNamespace(path));
    }
}
