package com.resourceful_refinement.content.sports_ball;

import com.resourceful_refinement.registry.ModDataComponents;
import com.resourceful_refinement.registry.ModItems;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import org.joml.Quaternionf;

/**
 * A persistent, kickable ball. Not a projectile — it lives in the world until picked up, and
 * bounces off surfaces reflecting its momentum rather than despawning on impact.
 *
 * <p>Physics runs identically on both sides (the {@code ItemEntity} approach) so the client can
 * animate smoothly without a bespoke motion packet; vanilla motion/position sync corrects drift.
 */
public class SportsBallEntity extends Entity {

    /** Half the 11px width — the rolling radius used to convert linear speed into spin. */
    public static final float RADIUS = 11.0F / 32.0F;

    private static final double GRAVITY = 0.04D;
    /** Fraction of speed kept when reflecting off a wall or ceiling. */
    private static final double WALL_RESTITUTION = 0.65D;
    /** Fraction of speed kept when bouncing off a floor. */
    private static final double FLOOR_RESTITUTION = 0.45D;
    /** Portion of momentum retained while rolling on a surface. */
    private static final double ROLL_FRICTION = 0.975D;
    /** Portion of momentum retained while moving through the air. */
    private static final double AIR_FRICTION = 0.995D;
    /** Below this downward speed the ball settles instead of bouncing again. */
    private static final double MIN_BOUNCE_VY = 0.08D;
    /** Horizontal speed imparted by a player attack. */
    private static final double PUNCH_IMPULSE = 1D;
    /** Vertical component of a punch, so kicks pop the ball off the ground slightly. */
    private static final double PUNCH_LIFT = 0.3D;
    /** Speeds below this count as stopped, for both spin and settle detection. */
    private static final double EPSILON = 1.0E-4D;

    /**
     * Orientation the ball came to rest at. Only written when it settles, so this costs one packet
     * per stop rather than one per tick — enough to keep a reloaded or newly-visible ball facing
     * the way it actually landed.
     */
    private static final EntityDataAccessor<Quaternionf> RESTING_ROTATION =
            SynchedEntityData.defineId(SportsBallEntity.class, EntityDataSerializers.QUATERNION);

    /** Current visual spin, integrated from velocity each tick. Not synced while rolling. */
    private final Quaternionf visualRotation = new Quaternionf();
    /** Previous tick's spin, for partial-tick interpolation in the renderer. */
    private final Quaternionf visualRotationO = new Quaternionf();

    private static final EntityDataAccessor<Integer> BALL_TYPE =
            SynchedEntityData.defineId(SportsBallEntity.class, EntityDataSerializers.INT);

    private int ballType = 0;

    private boolean settled;
    private boolean clientRotationInitialised;

    public SportsBallEntity(EntityType<? extends SportsBallEntity> type, Level level) {
        super(type, level);
        this.blocksBuilding = false;
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        builder.define(RESTING_ROTATION, new Quaternionf());
        builder.define(BALL_TYPE, 0);
    }

    // ------------------------------------------------------------------ physics

    @Override
    public void tick() {
        super.tick();

        Vec3 wanted = getDeltaMovement().add(0.0D, -GRAVITY, 0.0D);
        // Must be stored before move(): move() zeroes collided axes on the *stored* delta, not on
        // the vector it is handed, so skipping this would silently discard gravity every tick.
        setDeltaMovement(wanted);
        move(MoverType.SELF, wanted);

        // move() zeroes whichever axes collided, so comparing intent against the result tells us
        // which faces we hit without re-running the collision query ourselves.
        Vec3 resolved = getDeltaMovement();
        double vx = resolved.x;
        double vy = resolved.y;
        double vz = resolved.z;

        if (collidedOn(wanted.x, resolved.x)) {
            vx = -wanted.x * WALL_RESTITUTION;
        }
        if (collidedOn(wanted.z, resolved.z)) {
            vz = -wanted.z * WALL_RESTITUTION;
        }
        if (collidedOn(wanted.y, resolved.y)) {
            if (wanted.y < 0.0D) {
                vy = Math.abs(wanted.y) < MIN_BOUNCE_VY ? 0.0D : -wanted.y * FLOOR_RESTITUTION;
            } else {
                vy = -wanted.y * WALL_RESTITUTION;
            }
        }

        double friction = onGround() ? ROLL_FRICTION : AIR_FRICTION;
        vx *= friction;
        vz *= friction;

        if (Math.abs(vx) < EPSILON) vx = 0.0D;
        if (Math.abs(vz) < EPSILON) vz = 0.0D;

        setDeltaMovement(vx, vy, vz);

        // It lands constantly; never let fall damage bookkeeping accumulate.
        resetFallDistance();

        updateSpin();
        updateSettleState();
    }

    /** True when an axis had motion going into {@link #move} and none coming out of it. */
    private static boolean collidedOn(double wanted, double resolved) {
        return Math.abs(wanted) > EPSILON && Math.abs(resolved) < EPSILON;
    }

    /**
     * Integrates the tumble from horizontal velocity. For a ball rolling without slipping,
     * {@code v = w x r} with {@code r} pointing down to the contact point, which resolves to a
     * spin axis of {@code (-vz, 0, vx)} and an angle of {@code speed / radius}.
     */
    private void updateSpin() {
        visualRotationO.set(visualRotation);

        Vec3 v = getDeltaMovement();
        double speed = Math.sqrt(v.x * v.x + v.z * v.z);
        if (speed <= EPSILON) {
            return;
        }

        double invLength = 1.0D / speed;
        float axisX = (float) (v.z * invLength);
        float axisZ = (float) (-v.x * invLength);
        float angle = (float) (speed / RADIUS);

        // Premultiply so each tick's spin is applied in world space rather than ball-local space.
        visualRotation.premul(new Quaternionf().rotationAxis(angle, axisX, 0.0F, axisZ));
        visualRotation.normalize();
    }

    private void updateSettleState() {
        boolean stopped = onGround() && getDeltaMovement().lengthSqr() < EPSILON * EPSILON;

        if (level().isClientSide) {
            // Snap to the authoritative resting orientation the first time we see this ball, in
            // case the initial data bundle landed before onSyncedDataUpdated was hooked up.
            if (!clientRotationInitialised) {
                clientRotationInitialised = true;
                applyRestingRotation();
            }
            return;
        }

        if (stopped && !settled) {
            entityData.set(RESTING_ROTATION, new Quaternionf(visualRotation));
        }
        settled = stopped;
    }

    @Override
    public void onSyncedDataUpdated(EntityDataAccessor<?> key) {
        super.onSyncedDataUpdated(key);
        if (RESTING_ROTATION.equals(key) && level().isClientSide) {
            clientRotationInitialised = true;
            applyRestingRotation();
        }

        if (BALL_TYPE.equals(key) && level().isClientSide)
        {
            ballType = entityData.get(BALL_TYPE);
        }
    }

    private void applyRestingRotation() {
        visualRotation.set(entityData.get(RESTING_ROTATION));
        visualRotationO.set(visualRotation);
    }

    /** Interpolated tumble for the renderer. Writes into {@code dest} to avoid touching entity state. */
    public Quaternionf getVisualRotation(float partialTick, Quaternionf dest) {
        return dest.set(visualRotationO).slerp(visualRotation, partialTick);
    }

    public int getBallType() {return ballType;}

    public void setBallType(int newType)
    {
        entityData.set(BALL_TYPE, newType);
        ballType = newType;

        level().playSound(null, this, SoundEvents.SHEEP_SHEAR, SoundSource.PLAYERS, 0.4F,
                1.6F + random.nextFloat() * 0.2F);
    }

    // ------------------------------------------------------------------ interaction

    /**
     * Repurposes the damage hook as the kick. The ball has no health — returning {@code true}
     * just tells the vanilla attack pipeline the hit landed, so the player gets the swing and
     * attack cooldown that make kicking feel right.
     */
    @Override
    public boolean hurt(DamageSource source, float amount) {
        if (level().isClientSide) {
            return false;
        }
        if (!(source.getEntity() instanceof Player player)) {
            return false;
        }

        Vec3 look = player.getLookAngle();
        double horizontal = Math.sqrt(look.x * look.x + look.z * look.z);
        if (horizontal < EPSILON) {
            // Looking straight up or down — pop it upward rather than dividing by ~zero.
            setDeltaMovement(0.0D, PUNCH_LIFT * 2.0D, 0.0D);
        } else {
            double scale = PUNCH_IMPULSE / horizontal;
            if (player.isCrouching())
                scale *= 0.25;

            setDeltaMovement(look.x * scale, PUNCH_LIFT, look.z * scale);
        }

        hasImpulse = true;
        settled = false;
        markHurt();
        level().playSound(null, this, SoundEvents.WOOL_HIT, SoundSource.PLAYERS, 0.8F,
                1.2F + random.nextFloat() * 0.2F);
        return true;
    }

    @Override
    public InteractionResult interact(Player player, InteractionHand hand) {
        if (level().isClientSide) {
            return InteractionResult.SUCCESS;
        }

        if (player.isHolding(Items.SHEARS))
        {
            setBallType((ballType + 1)%3);
            return InteractionResult.SUCCESS;
        }

        ItemStack ball = new ItemStack(ModItems.SPORTS_BALL.get());
        ball.set(ModDataComponents.BALL_TYPE.get(), ballType);
        if (!player.addItem(ball)) {
            // Inventory full — leave the ball where it is rather than dropping a duplicate.
            return InteractionResult.FAIL;
        }

        level().playSound(null, this, SoundEvents.ITEM_PICKUP, SoundSource.PLAYERS, 0.4F,
                1.6F + random.nextFloat() * 0.2F);
        discard();
        return InteractionResult.SUCCESS;
    }

    @Override
    public boolean isPickable() {
        // Required for the player's attack/use raytrace to see the ball at all.
        return !isRemoved();
    }

    @Override
    public boolean isPushable() {
        return false;
    }

    @Override
    public boolean canBeCollidedWith() {
        // Deliberately intangible: an 11px solid that shoves players around doorways is worse
        // than one you can walk through. Motion comes from punches and throws.
        return false;
    }

    @Override
    public boolean causeFallDamage(float fallDistance, float multiplier, DamageSource source) {
        return false;
    }

    @Override
    public boolean fireImmune() {
        return true;
    }

    // ------------------------------------------------------------------ persistence

    @Override
    protected void readAdditionalSaveData(CompoundTag tag) {
        // Deliberately not "Rotation" — vanilla Entity already uses that key for yaw/pitch.
        if (tag.contains("Spin", Tag.TAG_LIST)) {
            ListTag list = tag.getList("Spin", Tag.TAG_FLOAT);
            if (list.size() == 4) {
                visualRotation.set(list.getFloat(0), list.getFloat(1), list.getFloat(2), list.getFloat(3));
                visualRotation.normalize();
                visualRotationO.set(visualRotation);
                entityData.set(RESTING_ROTATION, new Quaternionf(visualRotation));
            }
        }

        // Read ball type
        if (tag.contains("BallType", Tag.TAG_INT))
        {
            ballType = tag.getInt("BallType");
            entityData.set(BALL_TYPE, ballType);
        }
    }

    @Override
    protected void addAdditionalSaveData(CompoundTag tag) {
        tag.put("Spin", newFloatList(visualRotation.x, visualRotation.y, visualRotation.z, visualRotation.w));
        tag.putInt("BallType", ballType);
    }
}
