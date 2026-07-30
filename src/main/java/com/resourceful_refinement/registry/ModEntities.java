package com.resourceful_refinement.registry;

import com.resourceful_refinement.ResourcefulRefinementMain;
import com.resourceful_refinement.content.milking_station.MilkingStationSeatEntity;
import com.resourceful_refinement.content.plunger.ThrownPlunger;
import com.resourceful_refinement.content.hosegun.GelBlobEntity;
import com.resourceful_refinement.content.sports_ball.SportsBallEntity;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public class ModEntities {
    public static final DeferredRegister<EntityType<?>> ENTITY_TYPES = DeferredRegister.create(Registries.ENTITY_TYPE, ResourcefulRefinementMain.MOD_ID);

    public static final DeferredHolder<EntityType<?>, EntityType<GelBlobEntity>> GEL_BLOB = ENTITY_TYPES.register("gel_blob",
            () -> EntityType.Builder.<GelBlobEntity>of(GelBlobEntity::new, MobCategory.MISC)
                    .sized(0.25F, 0.25F)
                    .clientTrackingRange(4)
                    .updateInterval(10)
                    .build("gel_blob")
    );

    public static final DeferredHolder<EntityType<?>, EntityType<ThrownPlunger>> THROWN_PLUNGER = ENTITY_TYPES.register("thrown_plunger",
            () -> EntityType.Builder.<ThrownPlunger>of(ThrownPlunger::new, MobCategory.MISC)
                    .sized(0.5F, 0.5F)
                    .clientTrackingRange(4)
                    .updateInterval(20)
                    .build("thrown_plunger")
    );

    public static final DeferredHolder<EntityType<?>, EntityType<SportsBallEntity>> SPORTS_BALL = ENTITY_TYPES.register("sports_ball",
            () -> EntityType.Builder.<SportsBallEntity>of(SportsBallEntity::new, MobCategory.MISC)
                    .sized(11.0F / 16.0F, 11.0F / 16.0F)
                    .clientTrackingRange(8)
                    .updateInterval(3)
                    .build("sports_ball")
    );

    public static final DeferredHolder<EntityType<?>, EntityType<MilkingStationSeatEntity>> MILKING_STATION_SEAT = ENTITY_TYPES.register("milking_station_seat",
            () -> EntityType.Builder.<MilkingStationSeatEntity>of(MilkingStationSeatEntity::new, MobCategory.MISC)
                    .sized(0.25F, 0.35F)
                    .clientTrackingRange(4)
                    .updateInterval(20)
                    .noSave()
                    .build("milking_station_seat")
    );
}
