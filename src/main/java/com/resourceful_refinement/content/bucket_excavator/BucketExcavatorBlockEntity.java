package com.resourceful_refinement.content.bucket_excavator;

import com.mojang.blaze3d.vertex.PoseStack;
import com.resourceful_refinement.content.glare.DimensionalNodePos;
import com.resourceful_refinement.registry.ModStressValues;
import com.simibubi.create.content.kinetics.base.KineticBlockEntity;
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
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;

import java.util.List;

import static com.resourceful_refinement.content.combustion_chamber.CombustionChamberBlock.FACING;

public class BucketExcavatorBlockEntity extends KineticBlockEntity {

    public static final int EXCAVATION_REGION_HEIGHT = 5;
    public static final int EXCAVATION_REGION_DEPTH = 5;
    public static final int EXCAVATION_REGION_WIDTH = 1;

    public static final int MIN_SPEED_THRESHOLD = 128;
    public static final int PROCESSING_CYCLE_DURATION = 300;

    protected ScrollOptionBehaviour<ExcavationMode> excavationMode;
    public int timer;
    public boolean isExcavatorClear = true;    // True if this excavator's region does not overlap with any other

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
        if (isExcavatorClear && getSpeed() > MIN_SPEED_THRESHOLD)
        {
            timer++;
            if (timer >= PROCESSING_CYCLE_DURATION)
                process();

            sendData();
        }
    }

    private void process()
    {
        // Produce resources...

        timer = 0;
    }

    private void onExcavationModeChanged(int newMode)
    {
        // Toggle excavation modes
    }

    public void setExcavatorClear(boolean clear) {
        if (isExcavatorClear == clear) return;
        isExcavatorClear = clear;
        syncData();
    }


    // -------------------------------------------------------------------------
    // Client Visuals
    // -------------------------------------------------------------------------

    @Override
    public boolean addToGoggleTooltip(List<Component> tooltip, boolean isPlayerSneaking) {
        float speed = Math.abs(getSpeed());

        tooltip.add(Component.literal("     Bucket Excavator:"));
        tooltip.add(Component.literal("     \u00a7b" + (int) (speed * ModStressValues.BUCKET_EXCAVATOR_STRESS) + "su \u00a78at current speed"));

        if (!isExcavatorClear)
            tooltip.add(Component.literal("     &cExcavator overlaps another excavator!"));

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
    }

    @Override
    protected void read(CompoundTag tag, HolderLookup.Provider registries, boolean clientPacket) {
        super.read(tag, registries, clientPacket);

        timer = tag.getInt("Timer");
        isExcavatorClear = tag.getBoolean("IsExcavatorClear");
    }

    // On load, refresh region
    @Override
    public void onLoad() {
        super.onLoad();
        if (level instanceof ServerLevel server && getBlockState().hasProperty(BucketExcavatorBlock.FACING)) {
            ExcavatorRegionSavedData.RegisterOrUpdateExcavator(
                    server,
                    DimensionalNodePos.of(server, worldPosition),
                    getBlockState().getValue(BucketExcavatorBlock.FACING)
            );
        }
    }

    private void syncData() {
        setChanged();
        if (level != null) {
            level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), 3);
        }
    }

}
