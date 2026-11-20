package com.resourceful_refinement.content.tesla_coil;

import com.resourceful_refinement.content.tesla_coil.recipe.TeslaCoilRecipe;
import com.resourceful_refinement.registry.ModDamageTypes;
import com.resourceful_refinement.registry.ModRecipeTypes;
import com.resourceful_refinement.utilities.recipe.RecipeSelection;
import com.simibubi.create.AllBlocks;
import com.simibubi.create.content.kinetics.base.KineticBlockEntity;
import com.simibubi.create.content.kinetics.belt.behaviour.BeltProcessingBehaviour;
import com.simibubi.create.content.kinetics.belt.behaviour.BeltProcessingBehaviour.ProcessingResult;
import com.simibubi.create.content.kinetics.belt.behaviour.TransportedItemStackHandlerBehaviour;
import com.simibubi.create.content.kinetics.belt.behaviour.TransportedItemStackHandlerBehaviour.TransportedResult;
import com.simibubi.create.content.kinetics.belt.transport.TransportedItemStack;
import com.simibubi.create.content.processing.sequenced.SequencedAssemblyRecipe;
import com.simibubi.create.foundation.blockEntity.behaviour.BlockEntityBehaviour;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.SectionPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LightningBolt;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.SingleRecipeInput;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static com.simibubi.create.content.kinetics.belt.behaviour.BeltProcessingBehaviour.ProcessingResult.HOLD;
import static com.simibubi.create.content.kinetics.belt.behaviour.BeltProcessingBehaviour.ProcessingResult.PASS;

/**
 * Tesla Coil logic.
 * <ul>
 *   <li><b>Arcing.</b> A randomised cooldown (12-18 s at {@link #REFERENCE_RPM}) drains in proportion to RPM. When it
 *       empties, the coil arcs to a random Tesla Coil within {@link #ARC_RANGE} blocks (powered or not), hitting its
 *       head ({@link #HEAD_TARGET_CHANCE}) or else any block of that coil or its ring shafts. The arc lasts
 *       {@link #ARC_DURATION} ticks and hurts living entities on its line every {@link #ARC_DAMAGE_INTERVAL} ticks;
 *       then the cooldown starts over. Each arc may also call down real lightning in a storm.</li>
 *   <li><b>Charging.</b> Two coils facing across a one-block air gap along X or Z, with the gap directly above a belt
 *       or depot, form a charging pair (any orientations; at least one powered). Through Create's
 *       {@link BeltProcessingBehaviour} (reached via the belt/depot mixins, see {@link #findChargingBehaviour}) the
 *       negative-side coil halts items matching a {@link TeslaCoilRecipe}, strikes them once per charge, then swaps
 *       them for the result. Strike and pause lengths scale with the pair's faster RPM against
 *       {@link #CHARGE_REFERENCE_RPM}.</li>
 * </ul>
 * Arc and charge state is synced, and {@link TeslaCoilRenderer} draws the lightning from it; nothing is ticked
 * client-side except charge progress.
 */
public class TeslaCoilBlockEntity extends KineticBlockEntity {

    /** Pure shafts below the coil that get rings (and can be struck). */
    public static final int MAX_RING_SHAFTS = 8;
    public static final double ARC_RANGE = 12.0D;
    public static final int ARC_DURATION = 30;
    public static final int ARC_DAMAGE_INTERVAL = 5;
    public static final float ARC_DAMAGE = 2.5F;
    public static final float HEAD_TARGET_CHANCE = 0.25F;
    public static final float STORM_LIGHTNING_CHANCE = 0.05F;
    /** Extra radius added to entity hitboxes when testing them against the arc line. */
    public static final double ARC_HIT_RADIUS = 0.2D;

    /** Cooldown is measured in ticks at this speed; faster networks drain it proportionally faster. */
    public static final float REFERENCE_RPM = 64.0F;
    public static final int MIN_COOLDOWN = 10 * 20;
    public static final int MAX_COOLDOWN = 16 * 20;

    /** Charge strike and pause lengths are in ticks at this speed; faster pairs charge proportionally faster. */
    public static final float CHARGE_REFERENCE_RPM = 128.0F;
    public static final int CHARGE_STRIKE_TICKS = 20;
    public static final int CHARGE_PAUSE_TICKS = 10;

    /** Distance from the block centre to the middle of the dome, along FACING. */
    public static final double HEAD_CENTRE_OFFSET = 1.5D / 16.0D;

    protected BeltProcessingBehaviour beltProcessing;

    /** Remaining cooldown in reference ticks; negative until first rolled. Server only. */
    private float cooldown = -1.0F;

    // Synced arc state: the arc is live while gameTime - arcStartTick < ARC_DURATION.
    private long arcStartTick = Long.MIN_VALUE / 2;
    @Nullable
    private Vec3 arcTarget;

    // Synced charge state. Progress is in reference ticks (at CHARGE_REFERENCE_RPM) and advances on both sides by the
    // pair's RPM; -1 = idle, chargeDuration = charged and ready to swap the held item.
    private float chargeProgress = -1.0F;
    private int chargeDuration;
    @Nullable
    private BlockPos chargePartner;
    @Nullable
    private Vec3 processingTarget;
    @Nullable
    private RecipeHolder<TeslaCoilRecipe> lastRecipe;

    public TeslaCoilBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
    }

    public static int chargingDuration(int charges) {
        return Math.max(1, charges) * (CHARGE_STRIKE_TICKS + CHARGE_PAUSE_TICKS) - CHARGE_PAUSE_TICKS;
    }

    @Override
    public void addBehaviours(List<BlockEntityBehaviour> behaviours) {
        super.addBehaviours(behaviours);
        beltProcessing = new BeltProcessingBehaviour(this)
                .whenItemEnters(this::onItemReceived)
                .whileItemHeld(this::whenItemHeld);
        behaviours.add(beltProcessing);
    }

    @Override
    protected AABB createRenderBoundingBox() {
        // Rings run up to 8 blocks down the shaft; arcs and charge strikes reach up to ARC_RANGE away.
        return new AABB(worldPosition).inflate(ARC_RANGE + 1.0D);
    }

    public Direction getFacing() {
        return getBlockState().getValue(TeslaCoilBlock.FACING);
    }

    public boolean isPowered() {
        return getSpeed() != 0;
    }

    // -------------------------------------------------------------------------
    // Ticking
    // -------------------------------------------------------------------------
    @Override
    public void tick() {
        super.tick();
        if (level == null) {
            return;
        }

        tickCharging();

        if (level.isClientSide) {
            return;
        }

        long arcAge = level.getGameTime() - arcStartTick;
        if (arcAge >= 0 && arcAge < ARC_DURATION) {
            if (arcAge > 0 && arcAge % ARC_DAMAGE_INTERVAL == 0) {
                damagePulse();
            }
            return;
        }

        if (cooldown < 0) {
            rollCooldown(level.random);
        }
        float speed = Math.abs(getSpeed());
        if (speed == 0) {
            return;
        }
        cooldown -= speed / REFERENCE_RPM;
        if (cooldown <= 0) {
            discharge();
        }
    }

    private void rollCooldown(RandomSource random) {
        cooldown = MIN_COOLDOWN + random.nextInt(MAX_COOLDOWN - MIN_COOLDOWN + 1);
        setChanged();
    }

    // -------------------------------------------------------------------------
    // Arcing between coils
    // -------------------------------------------------------------------------
    private void discharge() {
        if (!(level instanceof ServerLevel server)) {
            return;
        }
        RandomSource random = server.random;
        rollCooldown(random);

        List<TeslaCoilBlockEntity> candidates = findCoilsInRange(server);
        if (candidates.isEmpty()) {
            return;
        }
        TeslaCoilBlockEntity target = candidates.get(random.nextInt(candidates.size()));
        arcTarget = target.pickStrikePoint(random);
        arcStartTick = server.getGameTime();
        sendData();

        Vec3 source = getHeadCentre();
        server.playSound(null, source.x, source.y, source.z, SoundEvents.GUARDIAN_ATTACK, SoundSource.BLOCKS, 0.7F, 1.35F);
        server.playSound(null, arcTarget.x, arcTarget.y, arcTarget.z, SoundEvents.GUARDIAN_ATTACK, SoundSource.BLOCKS, 0.6F, 1.45F);
        damagePulse();

        if (random.nextFloat() < STORM_LIGHTNING_CHANCE) {
            summonStormLightning(server, random.nextBoolean() ? worldPosition : target.getBlockPos());
        }
    }

    /** Every other loaded Tesla Coil whose head is within {@link #ARC_RANGE} of this one's. */
    private List<TeslaCoilBlockEntity> findCoilsInRange(ServerLevel server) {
        List<TeslaCoilBlockEntity> coils = new ArrayList<>();
        Vec3 head = getHeadCentre();
        int range = (int) Math.ceil(ARC_RANGE);
        int minChunkX = SectionPos.blockToSectionCoord(worldPosition.getX() - range);
        int maxChunkX = SectionPos.blockToSectionCoord(worldPosition.getX() + range);
        int minChunkZ = SectionPos.blockToSectionCoord(worldPosition.getZ() - range);
        int maxChunkZ = SectionPos.blockToSectionCoord(worldPosition.getZ() + range);
        for (int cx = minChunkX; cx <= maxChunkX; cx++) {
            for (int cz = minChunkZ; cz <= maxChunkZ; cz++) {
                LevelChunk chunk = server.getChunkSource().getChunkNow(cx, cz);
                if (chunk == null) {
                    continue;
                }
                for (BlockEntity be : chunk.getBlockEntities().values()) {
                    if (be != this && !be.isRemoved() && be instanceof TeslaCoilBlockEntity coil
                            && coil.getHeadCentre().distanceToSqr(head) <= ARC_RANGE * ARC_RANGE) {
                        coils.add(coil);
                    }
                }
            }
        }
        return coils;
    }

    /** The point another coil's arc lands on: usually anywhere along this coil and its ring shafts, sometimes the head. */
    private Vec3 pickStrikePoint(RandomSource random) {
        if (random.nextFloat() < HEAD_TARGET_CHANCE) {
            return getHeadCentre();
        }
        List<BlockPos> shafts = findRingShafts(level, worldPosition, getFacing());
        int index = random.nextInt(shafts.size() + 1);
        return index == 0 ? getHeadCentre() : Vec3.atCenterOf(shafts.get(index - 1));
    }

    private void damagePulse() {
        if (!(level instanceof ServerLevel server) || arcTarget == null) {
            return;
        }
        Vec3 start = getHeadCentre();
        Vec3 end = arcTarget;
        AABB sweep = new AABB(start, end).inflate(ARC_HIT_RADIUS + 1.0D);
        for (Entity entity : server.getEntities((Entity) null, sweep,
                e -> e instanceof LivingEntity && e.isAlive() && !e.isSpectator())) {
            AABB box = entity.getBoundingBox().inflate(ARC_HIT_RADIUS);
            if (!box.contains(start) && !box.contains(end) && box.clip(start, end).isEmpty()) {
                continue;
            }
            LivingEntity target = (LivingEntity) entity;
            // Pulses land every 5 ticks, inside the 10-tick hurt cooldown; clear it so each one counts.
            target.invulnerableTime = 0;
            if (target.hurt(ModDamageTypes.teslaCoil(server), ARC_DAMAGE)) {
                server.sendParticles(ParticleTypes.ELECTRIC_SPARK, target.getX(), target.getY(0.5D), target.getZ(),
                        6, target.getBbWidth() * 0.4D, target.getBbHeight() * 0.3D, target.getBbWidth() * 0.4D, 0.15D);
            }
        }
    }

    /** Real lightning at a coil, only where it is actually raining on that coil (exposed to the sky, rainy biome). */
    private static void summonStormLightning(ServerLevel server, BlockPos coilPos) {
        if (!server.isRainingAt(coilPos.above())) {
            return;
        }
        LightningBolt bolt = EntityType.LIGHTNING_BOLT.create(server);
        if (bolt != null) {
            bolt.moveTo(Vec3.atBottomCenterOf(coilPos));
            server.addFreshEntity(bolt);
        }
    }

    // -------------------------------------------------------------------------
    // Belt / depot charging
    // -------------------------------------------------------------------------

    /**
     * The coil that runs charging for the processing space {@code space} (the block directly above a belt segment or
     * depot), or null. Charging needs a Tesla Coil on both sides of the space along X or Z, any orientation, with the
     * space itself free of collision. The coil on the negative side is the controller, so the choice is stable.
     */
    @Nullable
    public static TeslaCoilBlockEntity findChargingController(@Nullable BlockGetter level, BlockPos space) {
        if (level == null || !level.getBlockState(space).getCollisionShape(level, space).isEmpty()) {
            return null;
        }
        for (Direction.Axis axis : new Direction.Axis[]{Direction.Axis.X, Direction.Axis.Z}) {
            BlockPos negative = space.relative(Direction.fromAxisAndDirection(axis, Direction.AxisDirection.NEGATIVE));
            BlockPos positive = space.relative(Direction.fromAxisAndDirection(axis, Direction.AxisDirection.POSITIVE));
            if (level.getBlockEntity(negative) instanceof TeslaCoilBlockEntity controller && !controller.isRemoved()
                    && level.getBlockEntity(positive) instanceof TeslaCoilBlockEntity partner && !partner.isRemoved()) {
                return controller;
            }
        }
        return null;
    }

    /** The charging behaviour serving {@code space}, for the belt/depot mixins. */
    @Nullable
    public static BeltProcessingBehaviour findChargingBehaviour(@Nullable BlockGetter level, BlockPos space) {
        TeslaCoilBlockEntity controller = findChargingController(level, space);
        return controller != null ? controller.beltProcessing : null;
    }

    /** The coil opposite this one across {@code space}, if it is still there. */
    @Nullable
    private TeslaCoilBlockEntity partnerAcross(BlockPos space) {
        BlockPos partner = space.offset(space.subtract(worldPosition));
        return level != null && level.getBlockEntity(partner) instanceof TeslaCoilBlockEntity coil ? coil : null;
    }

    /** Charging runs at the faster of the two coils, so only one of them needs power. */
    private float pairSpeed() {
        float speed = Math.abs(getSpeed());
        if (chargePartner != null && level != null && level.getBlockEntity(chargePartner) instanceof TeslaCoilBlockEntity partner) {
            speed = Math.max(speed, Math.abs(partner.getSpeed()));
        }
        return speed;
    }

    /** Charge progress gained per tick, in reference ticks: 1 at {@link #CHARGE_REFERENCE_RPM}. */
    public float getChargeRate() {
        return pairSpeed() / CHARGE_REFERENCE_RPM;
    }

    /** Space served by a belt/depot callback, or null when this coil isn't its charging controller. */
    @Nullable
    private BlockPos servedSpace(TransportedItemStackHandlerBehaviour handler) {
        BlockPos space = handler.blockEntity.getBlockPos().above();
        return findChargingController(level, space) == this ? space : null;
    }

    protected ProcessingResult onItemReceived(TransportedItemStack transported, TransportedItemStackHandlerBehaviour handler) {
        if (handler.blockEntity.isVirtual()) {
            return PASS;
        }
        BlockPos space = servedSpace(handler);
        if (space == null) {
            return PASS;
        }
        resetProcessing();
        TeslaCoilBlockEntity partner = partnerAcross(space);
        boolean powered = isPowered() || (partner != null && partner.isPowered());
        if (!powered || findRecipe(transported.stack) == null) {
            return PASS;
        }
        return HOLD;
    }

    protected ProcessingResult whenItemHeld(TransportedItemStack transported, TransportedItemStackHandlerBehaviour handler) {
        BlockPos space = servedSpace(handler);
        TeslaCoilBlockEntity partner = space != null ? partnerAcross(space) : null;
        if (partner == null || !(isPowered() || partner.isPowered())) {
            resetProcessing();
            return PASS;
        }
        RecipeHolder<TeslaCoilRecipe> holder = findRecipe(transported.stack);
        if (holder == null) {
            resetProcessing();
            return PASS;
        }
        TeslaCoilRecipe recipe = holder.value();

        if (chargeProgress < 0) {
            chargeDuration = recipe.getProcessingDuration();
            chargeProgress = 0.0F;
            chargePartner = partner.getBlockPos();
            processingTarget = handler.getWorldPositionOf(transported).add(0.0D, 0.15D, 0.0D);
            playChargeStrike();
            sendData();
            return HOLD;
        }
        if (chargeProgress < chargeDuration) {
            return HOLD;
        }

        // Charged: swap the consumed inputs for the result, leaving any surplus held for another cycle.
        // rollResults honours a sequenced assembly's enforced next result (the transitional item or final output).
        List<ItemStack> results = recipe.rollResults(level.random);
        ItemStack remainder = transported.stack.copy();
        remainder.shrink(recipe.getIngredient().count());

        transported.clearFanProcessingData();
        List<TransportedItemStack> outputs = new ArrayList<>();
        for (ItemStack result : results) {
            TransportedItemStack output = transported.copy();
            output.stack = result;
            outputs.add(output);
        }
        TransportedItemStack held = null;
        if (!remainder.isEmpty()) {
            held = transported.copy();
            held.stack = remainder;
        }
        handler.handleProcessingOnItem(transported, TransportedResult.convertToAndLeaveHeld(outputs, held));

        if (level instanceof ServerLevel server && processingTarget != null) {
            server.sendParticles(ParticleTypes.ELECTRIC_SPARK, processingTarget.x, processingTarget.y, processingTarget.z,
                    12, 0.15D, 0.1D, 0.15D, 0.2D);
            server.playSound(null, worldPosition, SoundEvents.AMETHYST_BLOCK_CHIME, SoundSource.BLOCKS, 0.8F, 1.6F);
        }
        resetProcessing();
        return HOLD;
    }

    @Nullable
    private RecipeHolder<TeslaCoilRecipe> findRecipe(ItemStack stack) {
        if (level == null || stack.isEmpty()) {
            return null;
        }
        // A sequenced assembly step comes first, like Create's press. Never cached: the same transitional item maps
        // to different steps as it progresses, and each lookup arms the step's enforced result.
        Optional<RecipeHolder<TeslaCoilRecipe>> sequenced = SequencedAssemblyRecipe.getRecipe(level, stack,
                ModRecipeTypes.TESLA_COIL_TYPE.get(), TeslaCoilRecipe.class);
        if (sequenced.isPresent()) {
            return sequenced.get();
        }

        SingleRecipeInput input = new SingleRecipeInput(stack);
        if (lastRecipe != null && lastRecipe.value().matches(input, level)) {
            return lastRecipe;
        }
        lastRecipe = RecipeSelection.findBestMatch(level, ModRecipeTypes.TESLA_COIL_TYPE.get(), input,
                "TeslaCoil@" + worldPosition.toShortString()).orElse(null);
        return lastRecipe;
    }

    private void resetProcessing() {
        if (chargeProgress < 0 && processingTarget == null && chargePartner == null) {
            return;
        }
        chargeProgress = -1.0F;
        processingTarget = null;
        chargePartner = null;
        sendData();
    }

    /** Advances charging by the pair's RPM; both sides run this so the client's strikes stay in step. */
    private void tickCharging() {
        if (chargeProgress < 0 || chargeProgress >= chargeDuration) {
            return;
        }
        float previous = chargeProgress;
        chargeProgress = Math.min(chargeDuration, chargeProgress + getChargeRate());
        int cycle = CHARGE_STRIKE_TICKS + CHARGE_PAUSE_TICKS;
        if (!level.isClientSide && chargeProgress < chargeDuration
                && Mth.floor(chargeProgress / cycle) > Mth.floor(previous / cycle)) {
            playChargeStrike();
        }
    }

    private void playChargeStrike() {
        if (processingTarget == null) {
            return;
        }
        level.playSound(null, processingTarget.x, processingTarget.y, processingTarget.z,
                SoundEvents.GUARDIAN_ATTACK, SoundSource.BLOCKS, 1F, 1.9F);
    }

    // -------------------------------------------------------------------------
    // Geometry
    // -------------------------------------------------------------------------
    public Vec3 getHeadCentre() {
        return headCentre(worldPosition, getFacing());
    }

    public static Vec3 headCentre(BlockPos pos, Direction facing) {
        return Vec3.atCenterOf(pos).add(Vec3.atLowerCornerOf(facing.getNormal()).scale(HEAD_CENTRE_OFFSET));
    }

    /**
     * The exposed shafts the coil sits on: consecutive pure Create shafts (not encased or shaft-derived blocks) running
     * away from the coil's local-down face along its axis, up to {@link #MAX_RING_SHAFTS}.
     */
    public static List<BlockPos> findRingShafts(@Nullable BlockGetter level, BlockPos coilPos, Direction facing) {
        List<BlockPos> shafts = new ArrayList<>(MAX_RING_SHAFTS);
        if (level == null) {
            return shafts;
        }
        Direction down = facing.getOpposite();
        for (int i = 1; i <= MAX_RING_SHAFTS; i++) {
            BlockPos pos = coilPos.relative(down, i);
            BlockState state = level.getBlockState(pos);
            if (state.getBlock() != AllBlocks.SHAFT.get()
                    || state.getValue(BlockStateProperties.AXIS) != facing.getAxis()) {
                break;
            }
            shafts.add(pos);
        }
        return shafts;
    }

    // -------------------------------------------------------------------------
    // Client-visible state
    // -------------------------------------------------------------------------
    public long getArcStartTick() {
        return arcStartTick;
    }

    @Nullable
    public Vec3 getArcTarget() {
        return arcTarget;
    }

    @Nullable
    public BlockPos getChargePartner() {
        return chargePartner;
    }

    @Nullable
    public Vec3 getProcessingTarget() {
        return processingTarget;
    }

    /** Reference ticks into the current charge, interpolated by partial ticks, or -1 when not charging. */
    public float getChargeElapsed(float partialTicks) {
        if (chargeProgress < 0 || chargeProgress >= chargeDuration || processingTarget == null) {
            return -1.0F;
        }
        return Math.min(chargeDuration, chargeProgress + getChargeRate() * partialTicks);
    }

    // -------------------------------------------------------------------------
    // Data Persistence
    // -------------------------------------------------------------------------
    @Override
    protected void write(CompoundTag tag, HolderLookup.Provider registries, boolean clientPacket) {
        super.write(tag, registries, clientPacket);
        if (!clientPacket) {
            tag.putFloat("Cooldown", cooldown);
        }
        tag.putLong("ArcStart", arcStartTick);
        if (arcTarget != null) {
            writeVec(tag, "ArcTarget", arcTarget);
        }
        tag.putFloat("ChargeProgress", chargeProgress);
        tag.putInt("ChargeDuration", chargeDuration);
        if (chargePartner != null) {
            tag.putLong("ChargePartner", chargePartner.asLong());
        }
        if (processingTarget != null) {
            writeVec(tag, "ProcessingTarget", processingTarget);
        }
    }

    @Override
    protected void read(CompoundTag tag, HolderLookup.Provider registries, boolean clientPacket) {
        super.read(tag, registries, clientPacket);
        if (!clientPacket) {
            cooldown = tag.contains("Cooldown") ? tag.getFloat("Cooldown") : -1.0F;
        }
        arcStartTick = tag.contains("ArcStart") ? tag.getLong("ArcStart") : Long.MIN_VALUE / 2;
        arcTarget = readVec(tag, "ArcTarget");
        chargeProgress = tag.contains("ChargeProgress") ? tag.getFloat("ChargeProgress") : -1.0F;
        chargeDuration = tag.getInt("ChargeDuration");
        chargePartner = tag.contains("ChargePartner") ? BlockPos.of(tag.getLong("ChargePartner")) : null;
        processingTarget = readVec(tag, "ProcessingTarget");
    }

    private static void writeVec(CompoundTag tag, String key, Vec3 vec) {
        CompoundTag v = new CompoundTag();
        v.putDouble("X", vec.x);
        v.putDouble("Y", vec.y);
        v.putDouble("Z", vec.z);
        tag.put(key, v);
    }

    @Nullable
    private static Vec3 readVec(CompoundTag tag, String key) {
        if (!tag.contains(key)) {
            return null;
        }
        CompoundTag v = tag.getCompound(key);
        return new Vec3(v.getDouble("X"), v.getDouble("Y"), v.getDouble("Z"));
    }
}
