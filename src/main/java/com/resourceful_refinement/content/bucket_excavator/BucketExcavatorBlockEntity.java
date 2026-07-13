package com.resourceful_refinement.content.bucket_excavator;

import com.mojang.blaze3d.vertex.PoseStack;
import com.resourceful_refinement.ResourcefulRefinementMain;
import com.resourceful_refinement.content.bucket_excavator.recipe.ExcavationRecipeInput;
import com.resourceful_refinement.content.fracking_pump.recipe.FrackingPumpRecipeInput;
import com.resourceful_refinement.content.glare.DimensionalNodePos;
import com.resourceful_refinement.content.research.ResearchRecipeGate;
import com.resourceful_refinement.registry.ModRecipeTypes;
import com.resourceful_refinement.registry.ModStressValues;
import com.resourceful_refinement.utilities.GoggleUtilities;
import com.simibubi.create.content.kinetics.base.KineticBlockEntity;
import com.simibubi.create.content.processing.recipe.ProcessingOutput;
import com.simibubi.create.foundation.blockEntity.behaviour.BlockEntityBehaviour;
import com.simibubi.create.foundation.blockEntity.behaviour.ValueBoxTransform;
import com.simibubi.create.foundation.blockEntity.behaviour.scrollValue.INamedIconOptions;
import com.simibubi.create.foundation.blockEntity.behaviour.scrollValue.ScrollOptionBehaviour;
import com.simibubi.create.foundation.gui.AllIcons;
import com.simibubi.create.foundation.utility.CreateLang;
import dev.engine_room.flywheel.lib.transform.TransformStack;
import net.createmod.catnip.lang.Lang;
import net.createmod.catnip.math.VecHelper;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.particles.BlockParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.items.ItemHandlerHelper;
import net.neoforged.neoforge.items.ItemStackHandler;

import java.util.List;

import static com.resourceful_refinement.content.combustion_chamber.CombustionChamberBlock.FACING;

public class BucketExcavatorBlockEntity extends KineticBlockEntity {

    public static final int EXCAVATION_REGION_HEIGHT = 5;
    public static final int EXCAVATION_REGION_DEPTH = 5;
    public static final int EXCAVATION_REGION_WIDTH = 1;

    public static final int MIN_SPEED_THRESHOLD = 128;
    public static final int PROCESSING_CYCLE_DURATION = 300;
    public static final int DESTRUCTION_CYCLE_DURATION = 60;

    private static final int INVENTORY_SLOT_COUNT = 4;
    private static final int REGION_PARTICLE_INTERVAL = 2;
    private static final int WHEEL_ARC_PARTICLE_INTERVAL = 1;
    private static final int WHEEL_ARC_PARTICLES_PER_TICK = 5;

    public static TagKey<Block> EXCAVATOR_INDESTRUCTIBLE = BlockTags.create(ResourceLocation.fromNamespaceAndPath(ResourcefulRefinementMain.MOD_ID, "excavator_indestructible"));

    protected ScrollOptionBehaviour<ExcavationMode> excavationMode;
    public int timer;
    public boolean isExcavatorClear = true;    // True if this excavator's region does not overlap with any other
    public ExcavatorRegionSavedData.ExcavatorRecord excavationData;

    public final ItemStackHandler outputInv = new ItemStackHandler(INVENTORY_SLOT_COUNT) {
        @Override
        protected void onContentsChanged(int slot) {
            syncData();
        }
    };

    public BucketExcavatorBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
    }

    public static enum ExcavationMode implements INamedIconOptions {
        EXTRACTION(AllIcons.I_ROLLER_PAVE),
        DESTRUCTION(AllIcons.I_TOOL_DEPLOY);

        private String translationKey;
        private AllIcons icon;

        private ExcavationMode(AllIcons icon) {
            this.icon = icon;
            this.translationKey = "resourceful_refinement.enums.excavation." + Lang.asId(this.name());
        }

        public AllIcons getIcon() {
            return this.icon;
        }

        public String getTranslationKey() {
            return this.translationKey;
        }
    }


    // -------------------------------------------------------------------------
    // Block Entity Logic
    // -------------------------------------------------------------------------

    @Override
    public void addBehaviours(List<BlockEntityBehaviour> behaviours) {
        super.addBehaviours(behaviours);
        excavationMode = new ScrollOptionBehaviour<>(ExcavationMode.class,
                CreateLang.translateDirect("resourceful_refinement.enums.excavation.title"), this, new ExcavationModeSlot());

        excavationMode.withCallback(this::onExcavationModeChanged);
        behaviours.add(excavationMode);
    }

    @Override
    public void tick() {
        super.tick();

        if (level == null || level.isClientSide) {
            return;
        }

        // Do something...
        if (isExcavatorClear && Math.abs(getSpeed()) >= MIN_SPEED_THRESHOLD)
        {
            timer++;
            spawnWorkingParticles((ServerLevel) level);

            if (excavationMode.get() == ExcavationMode.EXTRACTION && timer >= PROCESSING_CYCLE_DURATION)
                processExtraction();
            else if (excavationMode.get() == ExcavationMode.DESTRUCTION)
            {
                processBreaking();
            }

            sendData();
        }
    }

    private void processExtraction()
    {
        // Produce resources...
        for (int dx = excavationData.excavationRegion.min().getX(); dx <= excavationData.excavationRegion.max().getX(); dx++)
        {
            for (int dy = excavationData.excavationRegion.min().getY(); dy <= excavationData.excavationRegion.max().getY(); dy++)
            {
                for (int dz = excavationData.excavationRegion.min().getZ(); dz <= excavationData.excavationRegion.max().getZ(); dz++)
                {
                    BlockPos targetPos = new BlockPos(dx, dy, dz);
                    BlockState targetState = level.getBlockState(targetPos);

                    // Skip empty blocks
                    if (targetState.isAir())
                        continue;

                    // Find a matching recipe for conditions
                    ExcavationRecipeInput input = new ExcavationRecipeInput(targetState.getBlock(), null);
                    var recipeHolder = level.getRecipeManager().getRecipeFor(ModRecipeTypes.EXCAVATION_TYPE.get(), input, level);
                    if (recipeHolder.isPresent())
                    {
                        if (!ResearchRecipeGate.canUseServerRecipe(level, recipeHolder.get())) {
                            continue;
                        }
                        var recipe = recipeHolder.get().value();
                        for (ItemStack result : recipe.rollResults(level.random)) {
                            if (!result.isEmpty()) {
                                ItemHandlerHelper.insertItemStacked(outputInv, result.copy(), false);
                            }
                        }
                    }
                }
            }
        }
        setChanged();

        timer = 0;
    }

    private void processBreaking()
    {
        // Break all non-deposit blocks in region ...
        if (excavationData == null)
            return;
        float currentStage = (float) timer / DESTRUCTION_CYCLE_DURATION;

        for (int dx = excavationData.excavationRegion.min().getX(); dx <= excavationData.excavationRegion.max().getX(); dx++)
        {
            for (int dy = excavationData.excavationRegion.min().getY(); dy <= excavationData.excavationRegion.max().getY(); dy++)
            {
                for (int dz = excavationData.excavationRegion.min().getZ(); dz <= excavationData.excavationRegion.max().getZ(); dz++)
                {
                    // Show breaking progress
                    BlockPos targetPos = new BlockPos(dx, dy, dz);
                    BlockState targetState = level.getBlockState(targetPos);

                    // Check if block is unbreakable (or blacklisted type). If so, skip
                    if (targetState.isAir() || targetState.is(EXCAVATOR_INDESTRUCTIBLE) || targetState.getDestroySpeed(level, targetPos) < 0)
                        continue;

                    level.destroyBlockProgress(targetPos.hashCode(), targetPos, (int) (currentStage * 10) - 1);

                    if (currentStage >= 1)
                        level.destroyBlock(targetPos, true);
                }
            }
        }

        if (currentStage >= 1)
            timer = 0;
    }

    private void onExcavationModeChanged(int newMode)
    {
        // Toggle excavation modes
        timer = 0;
        syncData();
    }

    public void setExcavatorClear(boolean clear) {
        if (isExcavatorClear == clear) return;
        isExcavatorClear = clear;
        syncData();
    }

    public boolean isOperational()
    {
        return (Math.abs(getSpeed()) >= MIN_SPEED_THRESHOLD && isExcavatorClear);
    }


    // -------------------------------------------------------------------------
    // Client Visuals
    // -------------------------------------------------------------------------

    private void spawnWorkingParticles(ServerLevel serverLevel) {
        if (excavationData == null || excavationData.excavationRegion == null) {
            return;
        }

        if (timer % REGION_PARTICLE_INTERVAL == 0) {
            spawnExcavationSurfaceParticles(serverLevel);
        }

        if (timer % WHEEL_ARC_PARTICLE_INTERVAL == 0) {
            spawnBucketWheelArcParticles(serverLevel);
        }
    }

    private void spawnExcavationSurfaceParticles(ServerLevel serverLevel) {
        for (int dx = excavationData.excavationRegion.min().getX(); dx <= excavationData.excavationRegion.max().getX(); dx++) {
            for (int dy = excavationData.excavationRegion.min().getY(); dy <= excavationData.excavationRegion.max().getY(); dy++) {
                for (int dz = excavationData.excavationRegion.min().getZ(); dz <= excavationData.excavationRegion.max().getZ(); dz++) {
                    BlockPos targetPos = new BlockPos(dx, dy, dz);
                    BlockState targetState = level.getBlockState(targetPos);
                    if (targetState.isAir()) {
                        continue;
                    }

                    BlockPos abovePos = targetPos.above();
                    if (!level.getBlockState(abovePos).isAir()) {
                        continue;
                    }

                    double px = targetPos.getX() + 0.5 + (level.random.nextDouble() - 0.5) * 0.7;
                    double py = targetPos.getY() + 1.03;
                    double pz = targetPos.getZ() + 0.5 + (level.random.nextDouble() - 0.5) * 0.7;
                    double vx = (level.random.nextDouble() - 0.5) * 0.035;
                    double vy = 0.045 + level.random.nextDouble() * 0.035;
                    double vz = (level.random.nextDouble() - 0.5) * 0.035;

                    serverLevel.sendParticles(new BlockParticleOption(ParticleTypes.BLOCK, targetState),
                            px, py, pz, 2, vx, vy, vz, 0.02);
                }
            }
        }
    }

    private void spawnBucketWheelArcParticles(ServerLevel serverLevel) {
        Direction facing = getBlockState().getValue(BucketExcavatorBlock.FACING);
        Direction side = facing.getClockWise();
        BlockState particleState = getRandomExcavatedParticleState();

        double centerX = worldPosition.getX() + 0.5 + facing.getStepX() * 1.75;
        double centerY = worldPosition.getY() + 0.75;
        double centerZ = worldPosition.getZ() + 0.5 + facing.getStepZ() * 2.5;
        double radius = 1.5;
        double sideJitterScale = 0.22;

        for (int i = 0; i < WHEEL_ARC_PARTICLES_PER_TICK; i++) {
            double t = level.random.nextDouble();
            double angle = Math.toRadians(25 + t * 155);

            double localBack = -Math.sin(angle) * radius;
            double localY = Math.cos(angle) * radius;
            double sideJitter = (level.random.nextDouble() - 0.5) * sideJitterScale;

            double px = centerX + facing.getStepX() * localBack + side.getStepX() * sideJitter;
            double py = centerY + localY;
            double pz = centerZ + facing.getStepZ() * localBack + side.getStepZ() * sideJitter;

            double tangentBack = -Math.cos(angle) * 0.06;
            double tangentY = -Math.sin(angle) * 0.06 - 0.025;
            double vx = facing.getStepX() * tangentBack + side.getStepX() * sideJitter * 0.08;
            double vy = tangentY;
            double vz = facing.getStepZ() * tangentBack + side.getStepZ() * sideJitter * 0.08;

            serverLevel.sendParticles(new BlockParticleOption(ParticleTypes.BLOCK, particleState),
                    px, py, pz, 1, vx, vy, vz, 0.015);
        }
    }

    private BlockState getRandomExcavatedParticleState() {
        if (excavationData == null || excavationData.excavationRegion == null) {
            return net.minecraft.world.level.block.Blocks.STONE.defaultBlockState();
        }

        int minX = excavationData.excavationRegion.min().getX();
        int minY = excavationData.excavationRegion.min().getY();
        int minZ = excavationData.excavationRegion.min().getZ();
        int sizeX = excavationData.excavationRegion.max().getX() - minX + 1;
        int sizeY = excavationData.excavationRegion.max().getY() - minY + 1;
        int sizeZ = excavationData.excavationRegion.max().getZ() - minZ + 1;

        for (int attempt = 0; attempt < 12; attempt++) {
            BlockPos samplePos = new BlockPos(
                    minX + level.random.nextInt(sizeX),
                    minY + level.random.nextInt(sizeY),
                    minZ + level.random.nextInt(sizeZ)
            );
            BlockState sampleState = level.getBlockState(samplePos);
            if (!sampleState.isAir()) {
                return sampleState;
            }
        }

        return Blocks.AIR.defaultBlockState();
    }

    @Override
    public boolean addToGoggleTooltip(List<Component> tooltip, boolean isPlayerSneaking) {
        float speed = Math.abs(getSpeed());

        tooltip.add(Component.literal("     Bucket Excavator:"));
        tooltip.add(Component.literal("     \u00a7b" + (int) (speed * ModStressValues.BUCKET_EXCAVATOR_STRESS) + "su \u00a78at current speed"));

        if (!isExcavatorClear)
            tooltip.add(Component.literal("     §cExcavator overlaps another excavator!"));
        else
        {
            if (excavationMode.get() == ExcavationMode.EXTRACTION)
                tooltip.add(Component.literal("     §8" + GoggleUtilities.BuildTextProgressBar(timer, PROCESSING_CYCLE_DURATION)));
            else
                tooltip.add(Component.literal("     §8" + GoggleUtilities.BuildTextProgressBar(timer, DESTRUCTION_CYCLE_DURATION)));
        }

        for (int i = 0; i < INVENTORY_SLOT_COUNT; i++)
        {
            ItemStack invItem = outputInv.getStackInSlot(i);
            if (!invItem.isEmpty())
                tooltip.add(Component.literal("§8     -> §7" + invItem.getCount() + " x " + invItem.getHoverName().getString()));
        }

        return true;
    }


    // -------------------------------------------------------------------------
    //  Excavation Mode Handling
    // -------------------------------------------------------------------------
    private static class ExcavationModeSlot extends ValueBoxTransform {
        @Override
        public Vec3 getLocalOffset(net.minecraft.world.level.LevelAccessor level, BlockPos pos, BlockState state) {
            if (!state.hasProperty(FACING)) {
                return null;
            }
            return rotateHorizontally(state, VecHelper.voxelSpace(8, 16.01, 2));
        }

        @Override
        public void rotate(net.minecraft.world.level.LevelAccessor level, BlockPos pos, BlockState state, PoseStack ms) {
            TransformStack.of(ms).rotateXDegrees(90);
            Direction facing = state.getValue(FACING);
            TransformStack.of(ms).rotateZDegrees(facing.toYRot());
        }

        @Override
        public boolean shouldRender(net.minecraft.world.level.LevelAccessor level, BlockPos pos, BlockState state) {
            if (!super.shouldRender(level, pos, state)) {
                return false;
            }
            return level.getBlockEntity(pos) instanceof BucketExcavatorBlockEntity;
        }

        @Override
        public boolean testHit(net.minecraft.world.level.LevelAccessor level, BlockPos pos, BlockState state, Vec3 localHit) {
            return shouldRender(level, pos, state) && super.testHit(level, pos, state, localHit);
        }
    }


    // -------------------------------------------------------------------------
    // Data Persistence
    // -------------------------------------------------------------------------

    @Override
    protected void write(CompoundTag tag, HolderLookup.Provider registries, boolean clientPacket) {
        super.write(tag, registries, clientPacket);

        tag.putInt("Timer", timer);
        tag.putBoolean("IsExcavatorClear", isExcavatorClear);
        tag.put("OutputInv", outputInv.serializeNBT(registries));
    }

    @Override
    protected void read(CompoundTag tag, HolderLookup.Provider registries, boolean clientPacket) {
        super.read(tag, registries, clientPacket);

        timer = tag.getInt("Timer");
        isExcavatorClear = tag.getBoolean("IsExcavatorClear");

        if (tag.contains("OutputInv")) {
            outputInv.deserializeNBT(registries, tag.getCompound("OutputInv"));
        }
    }

    // On load, refresh region
    @Override
    public void onLoad() {
        super.onLoad();
        if (level instanceof ServerLevel server && getBlockState().hasProperty(BucketExcavatorBlock.FACING)) {
            excavationData = ExcavatorRegionSavedData.RegisterOrUpdateExcavator(
                    server,
                    DimensionalNodePos.of(server, worldPosition),
                    getBlockState().getValue(BucketExcavatorBlock.FACING)
            );
        }
    }

    private void syncData() {
        setChanged();
        if (level != null && !level.isClientSide) {
            level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), 3);
        }
    }

}
