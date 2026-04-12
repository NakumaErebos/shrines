package net.nakumaerebos.shrines.entity;

import net.minecraft.core.registries.Registries;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.nakumaerebos.shrines.Shrines;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.DeferredHolder;

public class ModEntities {
    public static final DeferredRegister<EntityType<?>> ENTITIES =
            DeferredRegister.create(Registries.ENTITY_TYPE, Shrines.MOD_ID);

    public static final DeferredHolder<EntityType<?>, EntityType<ShrineItemEntity>> SHRINE_ITEM =
            ENTITIES.register("shrine_item", () -> EntityType.Builder.of(ShrineItemEntity::new, MobCategory.MISC)
                    .sized(1.0f, 1.0f) // Blockgröße
                    .build("shrine_item"));

    public static void register(IEventBus eventBus) {
        ENTITIES.register(eventBus);
    }
}