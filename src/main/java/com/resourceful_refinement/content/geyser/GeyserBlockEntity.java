package com.resourceful_refinement.content.geyser;

import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.game.ClientboundSetEntityMotionPacket;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.phys.AABB;

import java.util.List;

public class GeyserBlockEntity extends BlockEntity {

    private Fluid associatedFluid = Fluids.LAVA;
    private int blastTimer = 0;
    private int eruptionTimer = 0;

    public GeyserBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
        blastTimer = 1000 + (int)(Math.random() * 2000);
        eruptionTimer = 6000 + (int)(Math.random() * 12000);
    }

    public Fluid getAssociatedFluid() {
        return associatedFluid;
    }

    public void setAssociatedFluid(Fluid fluid) {
        if (fluid == Fluids.EMPTY) return;
        this.associatedFluid = fluid;
        syncData();
    }

    private int activeBlastTimer = 0;

    public void tick() {
        if (level == null) return;

        if (!level.isClientSide) {
            if (activeBlastTimer > 0) {
                activeBlastTimer--;
                if (level instanceof ServerLevel serverLevel) {
                    double px = worldPosition.getX() + 0.5 + level.random.nextIntBetweenInclusive(-10,10)*0.02;
                    double py = worldPosition.getY() + 1.0;
                    double pz = worldPosition.getZ() + 0.5+ level.random.nextIntBetweenInclusive(-10,10)*0.02;
                    // Constant stream of cloud particles upwards
                    serverLevel.sendParticles(ParticleTypes.CLOUD, px, py, pz, 0, 0, 0.25, 0, 1);
                    serverLevel.sendParticles(ParticleTypes.DUST_PLUME, px, py, pz, 0, 0, 0.25, 0, 1);
                    serverLevel.sendParticles(ParticleTypes.SMALL_GUST, px, py + 4*(1f - ((double) activeBlastTimer /25)), pz, 0, 0, 0.25, 0, 1);
                    
                    // Loop Create's steam sound (~4 times a second)
                    if (activeBlastTimer % 4 == 0) {
                        level.playSound(null, worldPosition, com.simibubi.create.AllSoundEvents.STEAM.getMainEvent(), SoundSource.BLOCKS, 0.5f, 0.8f + level.random.nextFloat() * 0.4f);
                    }
                }
            }

            blastTimer--;
            if (blastTimer <= 0) {
                triggerBlast();
                blastTimer = (15 + level.random.nextInt(30))*20; // Random delay 15s to 45s
            }

            eruptionTimer--;
            if (eruptionTimer <= 0) {
                triggerEruption();
                eruptionTimer = (5 + level.random.nextInt(10)) * 20 * 60; // Random delay 5 min to 15 min
            }
        }
    }

    private void triggerBlast() {
        if (level == null) return;
        
        BlockPos pos = getBlockPos();
        // Trigger the sustained blast stream
        activeBlastTimer = 25; 
        
        // Launch entities
        AABB aabb = new AABB(pos.above()).inflate(0.5, 1.0, 0.5);
        List<Entity> entities = level.getEntities(null, aabb);
        for (Entity entity : entities) {
            entity.setDeltaMovement(entity.getDeltaMovement().add(0, 1.3, 0));

            // Delta applied to players must be synced for their client to authorise it
            if (entity instanceof ServerPlayer serverPlayer) {
                serverPlayer.connection.send(new ClientboundSetEntityMotionPacket(serverPlayer));
            }

            entity.hasImpulse = true;
        }

        if (level instanceof ServerLevel serverLevel)
            serverLevel.sendParticles(ParticleTypes.GUST, pos.getX() + 0.5, pos.getY() + 1, pos.getZ() + 0.5, 0, 0, 0.25, 0, 1);
        level.playSound(null, pos, SoundEvents.GENERIC_EXPLODE.value(), SoundSource.BLOCKS, 0.3f, 1.2f);
    }

    private void triggerEruption() {
        if (level == null || associatedFluid == null || associatedFluid == Fluids.EMPTY) return;
        
        BlockPos abovePos = getBlockPos().above();
        BlockState aboveState = level.getBlockState(abovePos);

        if (aboveState.canBeReplaced() || aboveState.isAir()) {
            level.setBlock(abovePos, associatedFluid.defaultFluidState().createLegacyBlock(), 3);
            level.playSound(null, abovePos, SoundEvents.BUCKET_EMPTY, SoundSource.BLOCKS, 1.0f, 1.0f);
            
            // Initial eruption burst (can be expanded to a stream if desired, but request specified "When a geyser errupts" which might refer to the blast)
            if (level instanceof ServerLevel serverLevel)
            {
                for (int i = 0; i < 10; i++) {
                    double px = abovePos.getX() + 0.5 + (level.random.nextDouble() - 0.5) * 0.8;
                    double py = abovePos.getY() + 0.5;
                    double pz = abovePos.getZ() + 0.5 + (level.random.nextDouble() - 0.5) * 0.8;
                    serverLevel.sendParticles(ParticleTypes.LARGE_SMOKE, px, py, pz, 1, 0, 0.2, 0, 0.05);
                }
                serverLevel.sendParticles(ParticleTypes.GUST, abovePos.getX() + 0.5, abovePos.getY() + 0.6, abovePos.getZ() + 0.5, 0, 0, 0.25, 0, 1);
            }
        }
    }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        tag.putString("Fluid", BuiltInRegistries.FLUID.getKey(associatedFluid).toString());
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        if (tag.contains("Fluid")) {
            ResourceLocation fluidLoc = ResourceLocation.tryParse(tag.getString("Fluid"));
            if (fluidLoc != null) {
                Fluid fluid = BuiltInRegistries.FLUID.get(fluidLoc);
                if (fluid != null) {
                    associatedFluid = fluid;
                }
            }
        }
    }

    @Override
    public CompoundTag getUpdateTag(HolderLookup.Provider registries) {
        CompoundTag tag = new CompoundTag();
        saveAdditional(tag, registries);
        return tag;
    }

    @Override
    public net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket getUpdatePacket() {
        return net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket.create(this);
    }

    private void syncData() {
        setChanged();
        if (level != null) {
            level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), 3);
        }
    }
}
