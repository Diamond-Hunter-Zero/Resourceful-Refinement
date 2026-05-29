package com.resourceful_refinement.content.gel_splatter;

import com.mojang.serialization.MapCodec;
import com.resourceful_refinement.registry.ModBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.game.ClientboundSetEntityMotionPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.MultifaceBlock;
import net.minecraft.world.level.block.MultifaceSpreader;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.FluidUtil;
import org.jetbrains.annotations.Nullable;

import java.util.OptionalDouble;

public class GelSplatterBlock extends MultifaceBlock implements EntityBlock {

    public static final MapCodec<GelSplatterBlock> CODEC = simpleCodec(GelSplatterBlock::new);

    public static final IntegerProperty FLUID_UPDATE_INDEX = IntegerProperty.create("fluid_update_index", 0, 7);

    /** Matches {@link MultifaceBlock} face thickness (one pixel). */
    private static final double GEL_FACE_THICKNESS = 0.0625D;

    /** How close the entity's feet must be to a gel surface to count as contact. */
    private static final double SURFACE_CONTACT_TOLERANCE = 0.15D;

    /** Cap bounce height so repeated landings cannot escalate without limit. */
    private static final double MAX_BOUNCE_SPEED = 0.55D;

    private static final double BOUNCE_DAMPING = 0.8D;

    /** Minimum downward speed before a bounce triggers (ignores gravity micro-jitter while standing). */
    private static final double MIN_FALL_SPEED = 0.08D;

    private static final String BOUNCED_THIS_TICK_KEY = "resourceful_refinement_bounced_tick";

    public GelSplatterBlock(Properties properties) {
        super(properties);

        // Set the default state
        BlockState defaultState = this.stateDefinition.any()
                .setValue(FLUID_UPDATE_INDEX, 0);

        this.registerDefaultState(defaultState);
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        // Register the property to the block state definition *before* super initializes it
        builder.add(FLUID_UPDATE_INDEX);
        super.createBlockStateDefinition(builder);
    }


    @Override
    protected MapCodec<? extends MultifaceBlock> codec() {
        return CODEC;
    }

    @Override
    public MultifaceSpreader getSpreader() {
        return new MultifaceSpreader(this);
    }

    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new GelSplatterBlockEntity(ModBlockEntities.GEL_SPLATTER_BE.get(), pos, state);
    }

    @Override
    protected ItemInteractionResult useItemOn(ItemStack stack, BlockState state, Level level, BlockPos pos, Player player,
            InteractionHand hand, BlockHitResult hitResult) {
        if (!player.isCreative()) {
            return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
        }

        Fluid fluid = FluidUtil.getFluidContained(stack).map(FluidStack::getFluid).orElse(Fluids.EMPTY);
        if (fluid == Fluids.EMPTY) {
            return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
        }

        // Note that we allow paints through for custom creative building
        GelType gelType = GelPropertiesManager.getGelType(fluid);
        if (gelType == GelType.POTION) {
            return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
        }

        if (!(level.getBlockEntity(pos) instanceof GelSplatterBlockEntity splatter)) {
            return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
        }

        if (!level.isClientSide) {
            if (gelType == GelType.CLEANSE) {
                splatter.clearTracking();
                level.setBlock(pos, Blocks.AIR.defaultBlockState(), Block.UPDATE_ALL);
            } else {
                splatter.creativeRetextureWithFluid(fluid);
            }
        }

        return ItemInteractionResult.sidedSuccess(level.isClientSide);
    }

    @Override
    public float getFriction(BlockState state, LevelReader level, BlockPos pos, @Nullable Entity entity) {

        if (level.isClientSide()) return super.getFriction(state, level, pos, entity);

        BlockEntity be = level.getBlockEntity(pos);
        if (!(be instanceof GelSplatterBlockEntityAccess splatterBe)) return super.getFriction(state, level, pos, entity);
        Fluid fluid = splatterBe.getFluid();
        if (fluid == Fluids.EMPTY) return super.getFriction(state, level, pos, entity);
        GelType gelType = GelPropertiesManager.getGelType(fluid);

        // Slippery surfaces — preserve horizontal momentum while contacting the gel
        if (gelType == GelType.SPEEDY) {
            return 0.96f;
        }
        if (gelType == GelType.BOUNCY) {
            return 0.925f;
        }

        return super.getFriction(state, level, pos, entity);
    }

    @Override
    public void fallOn(Level level, BlockState state, BlockPos pos, Entity entity, float fallDistance) {
        if (isBouncyGel(level, pos) && state.getBlock() instanceof GelSplatterBlock gelBlock
                && gelBlock.isContactingBouncyFloor(state, level, pos, entity)) {
            gelBlock.tryBounceEntity(level, pos, state, entity);
            return;
        }
        super.fallOn(level, state, pos, entity, fallDistance);
    }

    /**
     * Bouncy gel inverts vertical motion on landing (like slime blocks) without touching horizontal speed.
     * Thin multiface gels rarely receive {@code updateEntityAfterFallOn} — vanilla attributes movement to the
     * block below the 1-pixel slab — so {@link #entityInside} is the primary bounce path.
     */
    @Override
    public void updateEntityAfterFallOn(BlockGetter level, Entity entity) {
        if (tryBounceEntityOnGel(level, entity)) {
            return;
        }
        super.updateEntityAfterFallOn(level, entity);
    }

    private static boolean isBouncyGel(BlockGetter level, BlockPos pos) {
        BlockEntity be = level.getBlockEntity(pos);
        if (!(be instanceof GelSplatterBlockEntityAccess splatterBe)) {
            return false;
        }
        Fluid fluid = splatterBe.getFluid();
        return fluid != Fluids.EMPTY && GelPropertiesManager.getGelType(fluid) == GelType.BOUNCY;
    }

    /** Attempt bounce on any bouncy gel surface the entity is intersecting (floor gel in neighbouring cells included). */
    static boolean tryBounceEntityOnGel(BlockGetter level, Entity entity) {
        AABB box = entity.getBoundingBox();
        BlockPos min = BlockPos.containing(box.minX, box.minY, box.minZ);
        BlockPos max = BlockPos.containing(box.maxX, box.maxY, box.maxZ);

        for (BlockPos pos : BlockPos.betweenClosed(min, max)) {
            if (!isBouncyGel(level, pos)) {
                continue;
            }
            BlockState state = level.getBlockState(pos);
            if (state.getBlock() instanceof GelSplatterBlock gelBlock
                    && gelBlock.isContactingBouncyFloor(state, level, pos, entity)
                    && gelBlock.tryBounceEntity(level instanceof Level l ? l : null, pos, state, entity)) {
                return true;
            }
        }
        return false;
    }

    /** @return {@code true} if a bounce was applied this call */
    boolean tryBounceEntity(@Nullable Level level, BlockPos pos, BlockState state, Entity entity) {
        if (!applyBounce(entity)) {
            return false;
        }
        if (level instanceof ServerLevel server && pos != null && state != null) {
            Fluid fluid = Fluids.EMPTY;
            BlockEntity be = level.getBlockEntity(pos);
            if (be instanceof GelSplatterBlockEntityAccess splatter) {
                fluid = splatter.getFluid();
            }
            double impactY = findBouncyContactSurfaceY(state, level, pos, entity).orElse(entity.getY());
            BouncyGelEffects.spawn(server, entity.getX(), impactY, entity.getZ(), fluid);
        }
        return true;
    }

    private static boolean applyBounce(Entity entity) {
        if (entity.isSuppressingBounce()) {
            return false;
        }
        if (entity instanceof Player player && player.isCrouching()) {
            return false;
        }

        CompoundTag data = entity.getPersistentData();
        if (data.getInt(BOUNCED_THIS_TICK_KEY) == entity.tickCount) {
            return false;
        }

        Vec3 velocity = entity.getDeltaMovement();
        if (velocity.y >= -MIN_FALL_SPEED) {
            return false;
        }

        Vec3 horizontal = BouncyGelMomentumHelper.resolveHorizontalForBounce(entity, velocity);
        double damping = entity instanceof LivingEntity ? BOUNCE_DAMPING : BOUNCE_DAMPING * 0.8D;
        double bounceY = Math.min(-velocity.y * damping, MAX_BOUNCE_SPEED);
        Vec3 bounced = new Vec3(horizontal.x, bounceY, horizontal.z);

        data.putInt(BOUNCED_THIS_TICK_KEY, entity.tickCount);
        entity.setOnGround(false);
        entity.setDeltaMovement(bounced);
        entity.fallDistance = 0;
        entity.hasImpulse = true;
        entity.hurtMarked = true;

        if (entity instanceof ServerPlayer serverPlayer) {
            serverPlayer.connection.send(new ClientboundSetEntityMotionPacket(serverPlayer));
        }
        return true;
    }

    private boolean isContactingBouncyFloor(BlockState state, BlockGetter level, BlockPos pos, Entity entity) {
        return findBouncyContactSurfaceY(state, level, pos, entity).isPresent();
    }

    /**
     * Highest walkable gel surface the entity is landing on (DOWN-face floor gel or UP-face on block top).
     */
    private OptionalDouble findBouncyContactSurfaceY(BlockState state, BlockGetter level, BlockPos pos, Entity entity) {
        VoxelShape shape = state.getShape(level, pos, CollisionContext.of(entity));
        if (shape.isEmpty()) {
            return OptionalDouble.empty();
        }

        double entityBottom = entity.getBoundingBox().minY;
        double bestSurface = Double.NEGATIVE_INFINITY;
        boolean found = false;

        for (AABB localBox : shape.toAabbs()) {
            AABB worldBox = localBox.move(pos);
            double top = worldBox.maxY;
            double height = top - worldBox.minY;

            // Skip tall wall patches; only horizontal walkable slabs bounce
            if (height > GEL_FACE_THICKNESS * 3.0D) {
                continue;
            }

            if (entityBottom + SURFACE_CONTACT_TOLERANCE >= top
                    && entityBottom - SURFACE_CONTACT_TOLERANCE <= top + 0.25D) {
                if (top > bestSurface) {
                    bestSurface = top;
                    found = true;
                }
            }
        }

        return found ? OptionalDouble.of(bestSurface) : OptionalDouble.empty();
    }



    // Dynamic Entity collision/movement effects (e.g. bounce, slow, speed effects, damage)
    @Override
    public void entityInside(BlockState state, Level level, BlockPos pos, Entity entity) {
        super.entityInside(state, level, pos, entity);
        if (level.isClientSide) return;

        BlockEntity be = level.getBlockEntity(pos);
        if (!(be instanceof GelSplatterBlockEntityAccess splatterBe)) return;

        Fluid fluid = splatterBe.getFluid();
        if (fluid == Fluids.EMPTY) return;

        GelType gelType = GelPropertiesManager.getGelType(fluid);

        // Apply specific physics and status effects
        switch (gelType) {
            case MOLTEN -> {
                if (entity instanceof LivingEntity living) {
                    living.hurt(level.damageSources().lava(), 2.0F);
                }
                //entity.igniteForSeconds(3);
            }
            case SPEEDY -> {
                if (entity instanceof LivingEntity living) {
                    living.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SPEED, 10, 2, true, false));
                }
                else {
                    Vec3 velocity = entity.getDeltaMovement();

                    // Track horizontal speed magnitude (X and Z axis only)
                    double horizontalSpeed = velocity.horizontalDistance();

                    // Only apply slipperiness/acceleration if they are actively moving
                    if (horizontalSpeed > 0.1) {
                        // Define our caps (Ice default friction calculation reaches equilibrium around 0.25 - 0.35)
                        // 0.6D0 to 0.8D provides a very fast, satisfying sprint/slide without breaking physics
                        double maxHorizontalSpeed = 0.5D;

                        Vec3 newVelocity;
                        /*if (horizontalSpeed > maxHorizontalSpeed) {
                            // Keep their Y velocity (falling/jumping) intact but clamp the X and Z direction vectors
                            double ratio = maxHorizontalSpeed / horizontalSpeed;
                            newVelocity = new Vec3(velocity.x * ratio, velocity.y, velocity.z * ratio);
                        } else {
                            // Apply a clean 1.08x acceleration multiplier up until the cap is reached
                            newVelocity = new Vec3(velocity.x * 1.375D, velocity.y, velocity.z * 1.375D);
                        }
                        entity.setDeltaMovement(newVelocity);*/
                        if (horizontalSpeed < maxHorizontalSpeed)
                        {
                            newVelocity = velocity.add(velocity.normalize().scale(0.0375f));
                            entity.setDeltaMovement(newVelocity);
                            entity.hurtMarked = true;
                        }
                    }
                }
            }
            case GOOEY -> {
                // Sticky effect: Slow down entity velocity significantly
                /*entity.setDeltaMovement(entity.getDeltaMovement().multiply(0.5D, 0.7D, 0.5D));*/
            }
            case BOUNCY -> {
                if (isContactingBouncyFloor(state, level, pos, entity)) {
                    tryBounceEntity(level, pos, state, entity);
                }
            }
            case CURSED -> {
                if (entity instanceof LivingEntity living) {
                    living.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 40, 1, true, false));
                    living.addEffect(new MobEffectInstance(MobEffects.WEAKNESS, 40, 0, true, false));
                }
            }
            case BLESSED -> {
                if (entity instanceof LivingEntity living) {
                    living.addEffect(new MobEffectInstance(MobEffects.DAMAGE_BOOST, 40, 0, true, false));
                }
            }
        }
    }

    // Optional: Make it emit light like glow lichen (e.g., if Molten, Speedy, or Overcharged Carborax is active)
    @Override
    public int getLightEmission(BlockState state, BlockGetter level, BlockPos pos) {
        BlockEntity be = level.getBlockEntity(pos);
        if (be instanceof GelSplatterBlockEntityAccess splatterBe) {
            Fluid fluid = splatterBe.getFluid();
            GelType type = GelPropertiesManager.getGelType(fluid);
            if (type == GelType.MOLTEN) {
                return 10; // Bright molten light
            } else if (type == GelType.BOUNCY) {
                return 4; // Glow from carborax
            }
        }
        return 2;
    }

    // Tells lighting engines not to block sky/block light through this block
    @Override
    protected boolean propagatesSkylightDown(BlockState state, BlockGetter reader, BlockPos pos) {
        return true;
    }

    @Override
    protected float getShadeBrightness(BlockState state, BlockGetter level, BlockPos pos) {
        return 1.0F; // Prevents harsh internal shadows on the faces
    }


    // Static helpers
    public static int RegisterRendererTint(BlockState state, BlockAndTintGetter level, BlockPos pos, int tintIndex) {
        if (level != null && pos != null) {
            BlockEntity be = level.getBlockEntity(pos);
            if (be instanceof GelSplatterBlockEntityAccess splatterBe) {
                Fluid fluid = splatterBe.getFluid();
                if (fluid != Fluids.EMPTY) {
                    return GelFluidTintColors.getGelTint(fluid);
                }
            }
        }
        return 0xFFFFFFFF;
    }
}