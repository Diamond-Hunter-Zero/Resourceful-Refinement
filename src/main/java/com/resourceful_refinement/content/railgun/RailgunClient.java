package com.resourceful_refinement.content.railgun;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import com.resourceful_refinement.client.fx.lightning.ClientLightning;
import com.resourceful_refinement.client.fx.lightning.LightningStyle;
import com.resourceful_refinement.network.RailgunFirePayload;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;
import org.joml.Vector3f;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Iterator;

/**
 * Client-side railgun state, per holder entity: drum/barrel spool, the live beam, and the first-person charge shake
 * and recoil. Render/client thread only.
 *
 * <p>Spool: rises at the charge rate while charging (reaching full exactly as the shot fires), stays full while the
 * beam persists, then winds down over {@link #SPOOL_DOWN_TICKS} unless charging resumes. Integrated on the render
 * clock, so it is smooth at any frame rate.</p>
 */
public final class RailgunClient {

    /** Tighter, brighter take on ELECTRIC: two strands and less wander, so it reads as a beam, not a bolt. */
    public static final LightningStyle BEAM_STYLE = LightningStyle.ELECTRIC.toBuilder()
            .clearLayers()
            .layer(0.14F, 0x3C2A6CFF, 0.0F)
            .layer(0.05F, 0x9A5CD8FF, 0.0F)
            .layer(0.016F, 0xFFF4FCFF, 0.55F)
            .strands(2)
            .jaggedness(0.35F)
            .maxOffset(0.4F)
            .restrikeInterval(1.0F)
            .endpointGlow(0.12F)
            .build();

    // --- Spool tuning -------------------------------------------------------------------------------------------
    /** Ticks to wind down from full spool to stopped. */
    public static final float SPOOL_DOWN_TICKS = 30.0F;
    /** Drum spin at full spool, degrees per tick (48 = 2.7 rev/s). Spin scales with spool squared. */
    public static final float DRUM_MAX_DEGREES_PER_TICK = 48.0F;
    /** Barrel spin relative to the drum; negative counter-rotates. */
    public static final float BARREL_SPEED_RATIO = -0.5F;

    // --- First-person motion tuning -----------------------------------------------------------------------------
    public static final float CHARGE_SHAKE = 0.006F;
    public static final float CHARGE_PULLBACK = 0.03F;
    public static final float FIRE_SHAKE = 0.004F;
    public static final float RECOIL_TICKS = 6.0F;
    public static final float RECOIL_KICK_BACK = 0.12F;
    public static final float RECOIL_PITCH_DEGREES = 9.0F;

    /** Snapshot handed to the item renderer. */
    public record SpoolState(float drumAngle, float barrelAngle, float speed) {}

    private static final class State {
        float drumAngle;
        float barrelAngle;
        float speed;
        double lastTime = Double.NaN;
        long fireTick = Long.MIN_VALUE / 4;
        InteractionHand fireHand = InteractionHand.MAIN_HAND;
        float range;
        @Nullable ClientLightning.Beam beam;

        boolean firing(double now) {
            return now < fireTick + RailgunItem.FIRE_TICKS;
        }
    }

    private static final Int2ObjectMap<State> STATES = new Int2ObjectOpenHashMap<>();
    @Nullable private static ClientLevel statesLevel;

    /** Living entities currently being rendered (third-person item contexts don't otherwise know their holder). */
    private static final Deque<LivingEntity> RENDERING = new ArrayDeque<>();

    private static double worldFov = 70.0D;
    private static double handFov = 70.0D;

    private RailgunClient() {}

    // --- Firing ---------------------------------------------------------------------------------------------

    public static void onFire(RailgunFirePayload payload) {
        ClientLevel level = Minecraft.getInstance().level;
        if (level == null || !(level.getEntity(payload.entityId()) instanceof LivingEntity shooter)) {
            return;
        }
        State state = state(shooter);
        InteractionHand hand = payload.mainHand() ? InteractionHand.MAIN_HAND : InteractionHand.OFF_HAND;
        float range = payload.range();
        state.fireTick = level.getGameTime();
        state.fireHand = hand;
        state.range = range;
        state.speed = 1.0F;
        if (state.beam != null) {
            state.beam.discard();
        }
        state.beam = ClientLightning.spawn(
                partialTick -> muzzle(shooter, hand, partialTick),
                partialTick -> RailgunTargeting.beamEnd(shooter, range, partialTick),
                BEAM_STYLE, RailgunItem.FIRE_TICKS);
    }

    public static void clientTick() {
        ClientLevel level = Minecraft.getInstance().level;
        if (level == null) {
            STATES.clear();
            statesLevel = null;
            return;
        }
        syncLevel(level);
        long now = level.getGameTime();
        for (Iterator<Int2ObjectMap.Entry<State>> it = STATES.int2ObjectEntrySet().iterator(); it.hasNext(); ) {
            Int2ObjectMap.Entry<State> entry = it.next();
            State state = entry.getValue();
            Entity entity = level.getEntity(entry.getIntKey());
            if (!(entity instanceof LivingEntity shooter) || entity.isRemoved()) {
                if (state.beam != null) {
                    state.beam.discard();
                }
                it.remove();
                continue;
            }
            if (state.beam == null) {
                continue;
            }
            if (!state.firing(now) || !(shooter.getItemInHand(state.fireHand).getItem() instanceof RailgunItem)) {
                state.beam.discard();
                state.beam = null;
            } else {
                spawnImpactSparks(level, shooter, state.range);
            }
        }
    }

    private static void spawnImpactSparks(ClientLevel level, LivingEntity shooter, float range) {
        BlockHitResult hit = RailgunTargeting.trace(shooter, range, 1.0F);
        if (hit.getType() != HitResult.Type.BLOCK) {
            return;
        }
        Vec3 at = hit.getLocation();
        RandomSource random = level.random;
        for (int i = 0; i < 2; i++) {
            level.addParticle(ParticleTypes.ELECTRIC_SPARK, at.x, at.y, at.z,
                    (random.nextDouble() - 0.5D) * 0.4D, random.nextDouble() * 0.3D, (random.nextDouble() - 0.5D) * 0.4D);
        }
    }

    // --- Spool ----------------------------------------------------------------------------------------------

    /** Advances {@code holder}'s spool to the current frame (idempotent within a frame) and returns it. */
    public static SpoolState updateSpool(LivingEntity holder) {
        State state = state(holder);
        double now = holder.level().getGameTime() + partialTick();
        double dt = Double.isNaN(state.lastTime) ? 0.0D : Mth.clamp(now - state.lastTime, 0.0D, 10.0D);
        state.lastTime = now;

        if (state.firing(now)) {
            state.speed = 1.0F;
        } else if (RailgunItem.isCharging(holder)) {
            state.speed = Math.min(1.0F, state.speed + (float) dt / RailgunItem.CHARGE_TICKS);
        } else {
            state.speed = Math.max(0.0F, state.speed - (float) dt / SPOOL_DOWN_TICKS);
        }
        float spin = state.speed * state.speed * DRUM_MAX_DEGREES_PER_TICK * (float) dt;
        state.drumAngle = Mth.wrapDegrees(state.drumAngle + spin);
        state.barrelAngle = Mth.wrapDegrees(state.barrelAngle + spin * BARREL_SPEED_RATIO);
        return new SpoolState(state.drumAngle, state.barrelAngle, state.speed);
    }

    /** The entity holding the railgun being drawn in {@code context}, or {@code null} if there is none (GUI etc.). */
    @Nullable
    public static LivingEntity holderFor(ItemDisplayContext context) {
        if (context.firstPerson()) {
            return Minecraft.getInstance().player;
        }
        if (context == ItemDisplayContext.THIRD_PERSON_RIGHT_HAND || context == ItemDisplayContext.THIRD_PERSON_LEFT_HAND) {
            return RENDERING.peek();
        }
        return null;
    }

    public static void pushRendered(LivingEntity entity) {
        RENDERING.push(entity);
    }

    public static void popRendered(LivingEntity entity) {
        if (RENDERING.peek() == entity) {
            RENDERING.pop();
        }
    }

    public static void beginFrame() {
        RENDERING.clear();
    }

    // --- First person ---------------------------------------------------------------------------------------

    /** Replaces vanilla's first-person item placement: the standard arm offset plus charge shake and recoil. */
    public static void applyFirstPersonTransform(PoseStack poseStack, LivingEntity player, HumanoidArm arm,
                                                 float partialTick, float equipProgress) {
        int side = arm == HumanoidArm.RIGHT ? 1 : -1;
        poseStack.translate(side * 0.56F, -0.52F + equipProgress * -0.6F, -0.72F);
        applyFirstPersonMotion(poseStack, player, partialTick);
    }

    private static void applyFirstPersonMotion(PoseStack poseStack, LivingEntity player, float partialTick) {
        double now = player.level().getGameTime() + partialTick;
        float charge = RailgunItem.isCharging(player)
                ? Mth.clamp((player.getTicksUsingItem() + partialTick) / RailgunItem.CHARGE_TICKS, 0.0F, 1.0F)
                : 0.0F;
        State state = STATES.get(player.getId());
        float sinceFire = state == null ? Float.MAX_VALUE : (float) (now - state.fireTick);

        float shake = CHARGE_SHAKE * charge * charge;
        if (sinceFire >= 0.0F && sinceFire < RailgunItem.FIRE_TICKS) {
            shake = Math.max(shake, FIRE_SHAKE * (1.0F - sinceFire / RailgunItem.FIRE_TICKS));
        }
        if (shake > 0.0F) {
            float t = (float) (now % 1000.0D);
            poseStack.translate(Mth.sin(t * 5.7F) * shake, Mth.sin(t * 7.3F + 1.3F) * shake, 0.0F);
        }
        poseStack.translate(0.0F, 0.0F, CHARGE_PULLBACK * charge);

        if (sinceFire >= 0.0F && sinceFire < RECOIL_TICKS) {
            float kick = 1.0F - sinceFire / RECOIL_TICKS;
            kick *= kick;
            poseStack.translate(0.0F, 0.015F * kick, RECOIL_KICK_BACK * kick);
            poseStack.mulPose(Axis.XP.rotationDegrees(RECOIL_PITCH_DEGREES * kick));
        }
    }

    /** Beam start: the on-screen barrel tip for the local first-person player, else the approximate held muzzle. */
    private static Vec3 muzzle(LivingEntity shooter, InteractionHand hand, float partialTick) {
        Minecraft minecraft = Minecraft.getInstance();
        if (shooter == minecraft.player && shooter == minecraft.getCameraEntity()
                && minecraft.options.getCameraType().isFirstPerson()) {
            return firstPersonMuzzle(minecraft.player, hand, partialTick);
        }
        return RailgunTargeting.muzzle(shooter, hand, partialTick);
    }

    /**
     * Runs the first-person item transform on the model's muzzle point to get it in view space, then maps that into
     * the world through the camera. The hand is drawn with a fixed FOV and the world with the player's, so the
     * sideways/vertical offsets are rescaled to land on the same screen position.
     */
    private static Vec3 firstPersonMuzzle(LocalPlayer player, InteractionHand hand, float partialTick) {
        HumanoidArm arm = hand == InteractionHand.MAIN_HAND ? player.getMainArm() : player.getMainArm().getOpposite();
        PoseStack poseStack = new PoseStack();
        applyFirstPersonTransform(poseStack, player, arm, partialTick, 0.0F);
        RailgunItemRenderer.applyDisplayTransform(arm == HumanoidArm.RIGHT
                ? ItemDisplayContext.FIRST_PERSON_RIGHT_HAND : ItemDisplayContext.FIRST_PERSON_LEFT_HAND, poseStack);
        RailgunItemRenderer.applyModelSpace(poseStack);
        Vector3f view = poseStack.last().pose().transformPosition(
                RailgunModel.MUZZLE_X / 16.0F, RailgunModel.MUZZLE_Y / 16.0F, RailgunModel.MUZZLE_Z / 16.0F,
                new Vector3f());

        double fovScale = Math.tan(Math.toRadians(worldFov) * 0.5D) / Math.tan(Math.toRadians(handFov) * 0.5D);
        double right = view.x * fovScale;
        double up = view.y * fovScale;
        double forward = -view.z;

        Camera camera = Minecraft.getInstance().gameRenderer.getMainCamera();
        Vector3f look = camera.getLookVector();
        Vector3f upAxis = camera.getUpVector();
        Vector3f left = camera.getLeftVector();
        return camera.getPosition().add(
                look.x * forward - left.x * right + upAxis.x * up,
                look.y * forward - left.y * right + upAxis.y * up,
                look.z * forward - left.z * right + upAxis.z * up);
    }

    public static void captureFov(double fov, boolean world) {
        if (world) {
            worldFov = fov;
        } else {
            handFov = fov;
        }
    }

    // --- Internals ------------------------------------------------------------------------------------------

    private static State state(LivingEntity entity) {
        if (entity.level() instanceof ClientLevel level) {
            syncLevel(level);
        }
        return STATES.computeIfAbsent(entity.getId(), id -> new State());
    }

    /** Entity ids are per level; drop everything when the client changes level. */
    private static void syncLevel(ClientLevel level) {
        if (level != statesLevel) {
            STATES.clear();
            statesLevel = level;
        }
    }

    private static float partialTick() {
        return Minecraft.getInstance().getTimer().getGameTimeDeltaPartialTick(true);
    }
}
