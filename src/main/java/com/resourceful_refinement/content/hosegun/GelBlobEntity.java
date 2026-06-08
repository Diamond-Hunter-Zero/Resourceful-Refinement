package com.resourceful_refinement.content.hosegun;

import com.resourceful_refinement.content.gel_splatter.GelImpactConstants;
import com.resourceful_refinement.content.gel_splatter.GelPropertiesManager;
import com.resourceful_refinement.content.gel_splatter.GelSplatterBlock;
import com.resourceful_refinement.content.gel_splatter.GelSplatterBlockEntity;
import com.resourceful_refinement.content.gel_splatter.GelSplatterBlockEntityAccess;
import com.resourceful_refinement.content.gel_splatter.GelSplatterBlocks;
import com.resourceful_refinement.content.gel_tracking.GelTrackingService;
import com.resourceful_refinement.content.gel_splatter.GelType;
import com.resourceful_refinement.content.refill_station.FluidRefillStationBlockEntity;
import com.resourceful_refinement.registry.ModDamageTypes;
import com.resourceful_refinement.registry.ModItems;
import com.simibubi.create.AllBlocks;
import com.simibubi.create.foundation.blockEntity.behaviour.BlockEntityBehaviour;
import com.simibubi.create.foundation.blockEntity.behaviour.fluid.SmartFluidTankBehaviour;
import net.minecraft.world.phys.Vec3;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.ThrowableItemProjectile;
import net.minecraft.world.entity.projectile.ThrowableProjectile;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
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
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.capability.IFluidHandler;

import java.util.ArrayDeque;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Queue;
import java.util.Set;

public class GelBlobEntity extends ThrowableItemProjectile {

    private static final EntityDataAccessor<CompoundTag> FLUID_DATA = SynchedEntityData.defineId(GelBlobEntity.class, EntityDataSerializers.COMPOUND_TAG);
    private static final EntityDataAccessor<String> TRACKING_ID = SynchedEntityData.defineId(GelBlobEntity.class, EntityDataSerializers.STRING);
    private static final EntityDataAccessor<Boolean> GLOOPY = SynchedEntityData.defineId(GelBlobEntity.class, EntityDataSerializers.BOOLEAN);

    private FluidStack fluidStack = FluidStack.EMPTY;

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
        return ModItems.PAINT_BLOB.asItem();
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder); // Let parent register its data (e.g. item slot, ID 8)
        builder.define(FLUID_DATA, new CompoundTag());
        builder.define(TRACKING_ID, "");
        builder.define(GLOOPY, false);
    }

    public void setGloopy(boolean gloopy) {
        this.entityData.set(GLOOPY, gloopy);
    }

    public boolean isGloopy() {
        return this.entityData.get(GLOOPY);
    }

    public void setFluidStack(FluidStack stack) {
        this.fluidStack = stack == null || stack.isEmpty() ? FluidStack.EMPTY : stack.copy();
        syncFluidToEntityData();
        refreshProjectileItem();
    }

    /** Keeps the synced stack a plain paint blob; fluid tint is applied client-side in {@link GelBlobEntityRenderer}. */
    private void refreshProjectileItem() {
        setItem(new ItemStack(ModItems.PAINT_BLOB.get()));
    }

    public void setFluid(Fluid fluid) {
        setFluidStack(new FluidStack(fluid, 1000));
    }

    public FluidStack getFluidStack() {
        return fluidStack;
    }

    public Fluid getFluid() {
        return fluidStack.getFluid();
    }

    private void syncFluidToEntityData() {
        CompoundTag tag = new CompoundTag();
        if (!fluidStack.isEmpty() && level() != null) {
            tag = (CompoundTag) fluidStack.save(level().registryAccess());
        }
        this.entityData.set(FLUID_DATA, tag);
    }

    private void readFluidFromEntityData() {
        CompoundTag tag = this.entityData.get(FLUID_DATA);
        if (tag == null || tag.isEmpty() || level() == null) {
            fluidStack = FluidStack.EMPTY;
            return;
        }
        fluidStack = FluidStack.parseOptional(level().registryAccess(), tag);
    }

    @Override
    public void onSyncedDataUpdated(EntityDataAccessor<?> key) {
        super.onSyncedDataUpdated(key);
        if (FLUID_DATA.equals(key)) {
            readFluidFromEntityData();
            refreshProjectileItem();
        }
    }

    public void setTrackingId(String trackingId) {
        this.entityData.set(TRACKING_ID, FluidRefillStationBlockEntity.sanitiseTrackingId(trackingId));
    }

    public String getTrackingId() {
        return this.entityData.get(TRACKING_ID);
    }

    public boolean hasTrackingId() {
        String id = getTrackingId();
        return id != null && !id.isEmpty();
    }

    @Override
    public void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        if (!fluidStack.isEmpty() && level() != null) {
            tag.put("FluidStack", (CompoundTag) fluidStack.save(level().registryAccess()));
        }
        if (hasTrackingId()) {
            tag.putString("TrackingId", getTrackingId());
        }
        tag.putBoolean("Gloopy", isGloopy());
    }

    @Override
    public void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        if (tag.contains("FluidStack") && level() != null) {
            setFluidStack(FluidStack.parseOptional(level().registryAccess(), tag.getCompound("FluidStack")));
        } else if (tag.contains("Fluid")) {
            ResourceLocation fluidId = ResourceLocation.parse(tag.getString("Fluid"));
            setFluid(BuiltInRegistries.FLUID.get(fluidId));
        }
        if (tag.contains("TrackingId")) {
            setTrackingId(tag.getString("TrackingId"));
        }
        if (tag.contains("Gloopy")) {
            setGloopy(tag.getBoolean("Gloopy"));
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
        FluidStack fluidStack = getFluidStack();

        if (fluidStack.isEmpty() || fluidStack.getFluid() == Fluids.EMPTY)
            return;

        Fluid fluid = fluidStack.getFluid();
        GelType type = GelPropertiesManager.getGelType(fluid);

        switch (type) {
            case MOLTEN -> {
                entity.hurt(ModDamageTypes.moltenGel(this.level(), this, this.getOwner()), 2.0F);
                //entity.igniteForSeconds(4);
            }
            case BOUNCY -> {
                if (entity instanceof LivingEntity living) {
                    living.knockback(0.8D, -this.getDeltaMovement().x, -this.getDeltaMovement().z);
                }
            }
            case PAINT -> {
                if (!isGloopy()) {
                    DyeColor color = PAINT_FLUID_COLORS.get(fluid);
                    if (color != null && entity instanceof LivingEntity living && level() instanceof ServerLevel server) {
                        PaintGelCollarHelper.tryDyeEntity(server, living, color);
                    }
                }
            }
            case POTION -> {
                if (entity instanceof LivingEntity living && level() instanceof ServerLevel server) {
                    PotionGelImpactHandler.handleEntityImpact(server, living, this.position(), fluidStack, getOwner());
                }
            }
        }
    }

    @Override
    protected void onHitBlock(BlockHitResult result) {
        super.onHitBlock(result);
        if (this.level().isClientSide) return;

        FluidStack fluidStack = getFluidStack();

        if (fluidStack.isEmpty() || fluidStack.getFluid() == Fluids.EMPTY)
            return;

        BlockPos impactPos = result.getBlockPos();
        if (tryTransferFluidToCreateItemDrain(impactPos, fluidStack)) {
            return;
        }

        Fluid fluid = fluidStack.getFluid();
        GelType type = GelPropertiesManager.getGelType(fluid);
        Direction face = result.getDirection();
        BlockPos placePos = impactPos.relative(face);
        Vec3 impactLocation = result.getLocation();

        spawnImpactSplash(impactLocation, fluid, type);

        if (type == GelType.POTION) {
            if (level() instanceof ServerLevel server) {
                PotionGelImpactHandler.handleBlockImpact(server, impactLocation, fluidStack, getOwner());
            }
            return;
        }

        if (type == GelType.CLEANSE) {
            // Water/Cleanse: Clear gel splatters within a 3-block radius
            clearGels(impactPos);
            return;
        }

        if (type == GelType.PAINT && !isGloopy()) {
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

    /**
     * Deposits this blob's fluid into a Create item drain when the tank is empty, or already holds a
     * compatible fluid with spare capacity. Returns {@code true} when fluid was accepted and normal
     * block-hit effects should be skipped.
     */
    private boolean tryTransferFluidToCreateItemDrain(BlockPos impactPos, FluidStack fluidStack) {
        if (fluidStack.isEmpty() || !AllBlocks.ITEM_DRAIN.has(this.level().getBlockState(impactPos))) {
            return false;
        }

        SmartFluidTankBehaviour tankBehaviour = BlockEntityBehaviour.get(
                this.level(), impactPos, SmartFluidTankBehaviour.TYPE);
        if (tankBehaviour == null) {
            return false;
        }

        var tank = tankBehaviour.getPrimaryHandler();
        FluidStack stored = tank.getFluid();
        if (!stored.isEmpty() && !FluidStack.isSameFluidSameComponents(stored, fluidStack)) {
            return false;
        }

        FluidStack toFill = fluidStack.copy();
        toFill.setAmount(GelPropertiesManager.getGelAmmoCost(fluidStack.getFluid()));
        int accepted = tank.fill(toFill, IFluidHandler.FluidAction.SIMULATE);
        if (accepted <= 0) {
            return false;
        }

        toFill.setAmount(accepted);
        tankBehaviour.allowInsertion();
        tankBehaviour.getPrimaryHandler().fill(toFill, IFluidHandler.FluidAction.EXECUTE);
        tankBehaviour.forbidInsertion();
        return true;
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
            tagSplatterTracking(placePos);
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
            tagSplatterTracking(placePos);
        }
    }

    private void tagSplatterTracking(BlockPos placePos) {
        if (!hasTrackingId()) {
            return;
        }
        BlockEntity be = this.level().getBlockEntity(placePos);
        if (be instanceof GelSplatterBlockEntity splatter) {
            splatter.applyTrackingId(getTrackingId());
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
                GelTrackingService.onSplatterRemoved(this.level(), pos);
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
