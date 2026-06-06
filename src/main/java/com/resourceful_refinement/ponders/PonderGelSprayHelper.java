package com.resourceful_refinement.ponders;

import com.resourceful_refinement.content.hosegun.GelBlobEntity;
import com.resourceful_refinement.registry.FluidEntry;
import com.resourceful_refinement.registry.ModEntities;
import com.resourceful_refinement.registry.ModFluids;
import com.simibubi.create.foundation.ponder.CreateSceneBuilder;
import net.createmod.ponder.api.element.ElementLink;
import net.createmod.ponder.api.element.EntityElement;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.fluids.FluidStack;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * Client-side ponder helpers for gel sprays and travelling item entities.
 * Entities are spawned through {@link CreateSceneBuilder#world()} and updated each tick via
 * {@code modifyEntity} — no server-side projectile or item physics is relied upon.
 */
public final class PonderGelSprayHelper {

    private static final double GRAVITY_PER_TICK = 0.03D;
    private static final int SPAWN_INTERVAL_TICKS = 2;
    /** Peak bounce height as a fraction of horizontal travel distance. */
    private static final double BOUNCE_HEIGHT_SCALE = 0.18D;
    private static final double MAX_BOUNCE_HEIGHT = 0.85D;
    /**
     * How much of the ballistic vertical curve to apply ({@code 1} = full arc, lower = flatter spray).
     * Endpoints are unchanged; only mid-flight height and initial vertical speed are reduced.
     */
    private static final double ARC_VERTICAL_BLEND = 0.45D;

    private PonderGelSprayHelper() {}

    /**
     * Spawns gel blobs every {@value #SPAWN_INTERVAL_TICKS} ticks for {@code sprayDurationTicks}, each following
     * a ballistic arc from {@code start} to {@code end} over {@code flightTicks}. This method blocks for
     * {@code sprayDurationTicks + flightTicks} ponder ticks (so the last blob can finish its arc).
     *
     * @param start             world-space spawn position for each blob
     * @param end               world-space landing position at the end of the arc
     * @param flightTicks       ticks each blob spends in flight before being removed
     * @param gelTintColor      RGB gel tint ({@code 0xRRGGBB}); used to pick a matching mod fluid for rendering
     * @param sprayDurationTicks how long to keep spawning blobs (one blob every 2 ticks)
     */
    public static void playGelSpray(
            CreateSceneBuilder scene,
            Vec3 start,
            Vec3 end,
            int flightTicks,
            int gelTintColor,
            int sprayDurationTicks
    ) {
        playGelSpray(scene, start, end, flightTicks, gelTintColor, sprayDurationTicks, null);
    }

    /**
     * Like {@link #playGelSpray(CreateSceneBuilder, Vec3, Vec3, int, int, int)}, but runs {@code onFirstImpact}
     * once when the first spawned blob reaches {@code end} (at the end of its {@code flightTicks} arc).
     */
    public static void playGelSpray(
            CreateSceneBuilder scene,
            Vec3 start,
            Vec3 end,
            int flightTicks,
            int gelTintColor,
            int sprayDurationTicks,
            @Nullable Runnable onFirstImpact
    ) {
        int resolvedFlightTicks = Math.max(1, flightTicks);
        int resolvedSprayDuration = Math.max(0, sprayDurationTicks);
        Fluid fluid = fluidForGelTint(gelTintColor);

        List<TrackedBlob> active = new ArrayList<>();
        int totalTicks = resolvedSprayDuration + resolvedFlightTicks;
        boolean awaitingFirstImpact = onFirstImpact != null;
        boolean nextBlobIsFirst = true;

        for (int tick = 0; tick < totalTicks; tick++) {
            if (tick < resolvedSprayDuration && tick % SPAWN_INTERVAL_TICKS == 0) {
                ElementLink<EntityElement> link = scene.world().createEntity(level -> {
                    GelBlobEntity blob = new GelBlobEntity(
                            ModEntities.GEL_BLOB.get(),
                            start.x,
                            start.y,
                            start.z,
                            level
                    );
                    configurePonderBlob(blob, fluid);
                    return blob;
                });
                active.add(new TrackedBlob(link, nextBlobIsFirst));
                nextBlobIsFirst = false;
            }

            Iterator<TrackedBlob> iterator = active.iterator();
            while (iterator.hasNext()) {
                TrackedBlob tracked = iterator.next();
                tracked.age++;

                if (tracked.age >= resolvedFlightTicks) {
                    if (awaitingFirstImpact && tracked.firstInSpray) {
                        scene.world().modifyEntity(tracked.link, entity -> entity.setPos(end));
                        onFirstImpact.run();
                        awaitingFirstImpact = false;
                    }
                    scene.world().modifyEntity(tracked.link, Entity::discard);
                    iterator.remove();
                    continue;
                }

                Vec3 position = arcPosition(start, end, tracked.age, resolvedFlightTicks);
                scene.world().modifyEntity(tracked.link, entity -> {
                    entity.setPos(position);
                    entity.setDeltaMovement(Vec3.ZERO);
                });
            }

            scene.idle(1);
        }
    }

    /** Position along a gravity arc that reaches {@code end} when {@code age == flightTicks}. */
    public static Vec3 arcPosition(Vec3 start, Vec3 end, int age, int flightTicks) {
        if (flightTicks <= 0 || age >= flightTicks) {
            return end;
        }

        double progress = age / (double) flightTicks;
        double vx = (end.x - start.x) / flightTicks;
        double vz = (end.z - start.z) / flightTicks;

        double linearY = start.y + (end.y - start.y) * progress;
        double vy = (end.y - start.y + 0.5D * GRAVITY_PER_TICK * flightTicks * flightTicks) / flightTicks;
        double ballisticY = start.y + vy * age - 0.5D * GRAVITY_PER_TICK * age * age;
        double y = linearY + ARC_VERTICAL_BLEND * (ballisticY - linearY);

        double x = start.x + vx * age;
        double z = start.z + vz * age;
        return new Vec3(x, y, z);
    }

    /**
     * Spawns an item entity at {@code start}, animates it to {@code end} over {@code travelTicks}, then returns
     * the {@link ElementLink} (still valid — discard with {@code modifyEntity(link, Entity::discard)} when done).
     *
     * @param bounceCount number of parabolic bounces along the path; {@code 0} is a straight line. The horizontal
     *                    route is split into {@code bounceCount + 1} segments; each bounce is shorter and lower
     *                    than the last, lerping to no bounce on the final segment.
     */
    public static ElementLink<EntityElement> playTravelingItem(
            CreateSceneBuilder scene,
            ItemStack stack,
            Vec3 start,
            Vec3 end,
            int travelTicks,
            int bounceCount
    ) {
        int resolvedTicks = Math.max(1, travelTicks);
        int resolvedBounces = Math.max(0, bounceCount);

        ElementLink<EntityElement> link = scene.world().createItemEntity(start, Vec3.ZERO, stack.copy());

        for (int age = 0; age <= resolvedTicks; age++) {
            Vec3 position = travelingItemPosition(start, end, age, resolvedTicks, resolvedBounces);
            scene.world().modifyEntity(link, entity -> {
                entity.setPos(position);
                entity.setDeltaMovement(Vec3.ZERO);
            });
            if (age < resolvedTicks) {
                scene.idle(1);
            }
            else
            {
                scene.world().modifyEntity(link, Entity::discard);
            }
        }

        return link;
    }

    /**
     * World position for a travelling item at {@code age} ticks into a {@code travelTicks}-long path.
     * Horizontal motion is linear; vertical motion adds segmented bounce arcs when {@code bounceCount > 0}.
     */
    public static Vec3 travelingItemPosition(Vec3 start, Vec3 end, int age, int travelTicks, int bounceCount) {
        if (travelTicks <= 0 || age >= travelTicks) {
            return end;
        }

        double progress = age / (double) travelTicks;
        double x = start.x + (end.x - start.x) * progress;
        double y = start.y + (end.y - start.y) * progress;
        double z = start.z + (end.z - start.z) * progress;

        if (bounceCount <= 0) {
            return new Vec3(x, y, z);
        }

        int segments = bounceCount + 1;
        double segmentIndex = progress * segments;
        int segment = Math.min((int) segmentIndex, segments - 1);
        double localProgress = segmentIndex - segment;

        double horizontalDistance = Math.sqrt(
                (end.x - start.x) * (end.x - start.x) + (end.z - start.z) * (end.z - start.z));
        double maxBounceHeight = Math.min(MAX_BOUNCE_HEIGHT, horizontalDistance * BOUNCE_HEIGHT_SCALE);

        double bounceOffset = 0.0D;
        if (segment < bounceCount) {
            double heightScale = 1.0D - segment / (double) bounceCount;
            bounceOffset = maxBounceHeight * heightScale * Math.sin(Math.PI * localProgress);
        }

        return new Vec3(x, y + bounceOffset, z);
    }

    private static void configurePonderBlob(GelBlobEntity blob, Fluid fluid) {
        blob.setNoGravity(true);
        blob.setDeltaMovement(Vec3.ZERO);
        if (fluid != null && fluid != Fluids.EMPTY) {
            blob.setFluidStack(new FluidStack(fluid, 1000));
        }
    }

    private static Fluid fluidForGelTint(int gelTintColor) {
        int rgb = gelTintColor & 0xFFFFFF;
        for (FluidEntry entry : ModFluids.ENTRIES) {
            if ((entry.color & 0xFFFFFF) == rgb) {
                return entry.source.get();
            }
        }
        return ModFluids.RED_PAINT.source.get();
    }

    private static final class TrackedBlob {
        private final ElementLink<EntityElement> link;
        private final boolean firstInSpray;
        private int age;

        private TrackedBlob(ElementLink<EntityElement> link, boolean firstInSpray) {
            this.link = link;
            this.firstInSpray = firstInSpray;
        }
    }
}
