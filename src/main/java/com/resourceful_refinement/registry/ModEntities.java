package com.resourceful_refinement.registry;

import com.resourceful_refinement.ResourcefulRefinementMain;
import com.resourceful_refinement.content.hosegun.GelBlobEntity;
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
}
