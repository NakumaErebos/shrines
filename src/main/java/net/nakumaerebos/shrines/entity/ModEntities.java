package net.nakumaerebos.shrines.entity;

import net.minecraft.core.registries.Registries;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.nakumaerebos.shrines.Shrines;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.event.entity.EntityAttributeCreationEvent;

public class ModEntities {
    public static final DeferredRegister<EntityType<?>> ENTITIES =
            DeferredRegister.create(Registries.ENTITY_TYPE, Shrines.MOD_ID);

    public static final DeferredHolder<EntityType<?>, EntityType<ShrineItemEntity>> SHRINE_ITEM =
            ENTITIES.register("shrine_item", () -> EntityType.Builder.of(ShrineItemEntity::new, MobCategory.MISC)
                    .sized(1.0f, 1.0f)
                    .build("shrine_item"));

    public static final DeferredHolder<EntityType<?>, EntityType<GuardianScoutProjectileEntity>> GUARDIANSCOUT_PROJECTILE =
            ENTITIES.register("guardianscout_projectile", () -> EntityType.Builder.<GuardianScoutProjectileEntity>of(GuardianScoutProjectileEntity::new, MobCategory.MISC)
                    .sized(0.5f, 0.5f)
                    .clientTrackingRange(4)
                    .updateInterval(1)
                    .build("guardianscout_projectile"));

    public static final DeferredHolder<EntityType<?>, EntityType<GuardianScoutIMobEntity>> GUARDIAN_SCOUT_I =
            ENTITIES.register("guardian_scout_i", () -> EntityType.Builder.of(GuardianScoutIMobEntity::new, MobCategory.CREATURE)
                    .sized(0.6f, 1.8f)
                    .build("guardian_scout_i"));

    // Bei deinen Registern hinzufügen:
    public static final DeferredHolder<EntityType<?>, EntityType<GuardianScoutIIMobEntity>> GUARDIAN_SCOUT_II =
            ENTITIES.register("guardian_scout_ii", () -> EntityType.Builder.of(GuardianScoutIIMobEntity::new, MobCategory.CREATURE)
                    .sized(0.6f, 1.8f) // Selbe Hitbox
                    .build("guardian_scout_ii"));



    public static void register(IEventBus eventBus) {
        ENTITIES.register(eventBus);
        // Wir fügen den Listener für die Attribute direkt hier dem Bus hinzu
        eventBus.addListener(ModEntities::registerAttributes);
    }

    // Diese Methode verknüpft dein Entity mit den Werten (HP, Speed etc.)
    private static void registerAttributes(EntityAttributeCreationEvent event) {
        event.put(GUARDIAN_SCOUT_I.get(), GuardianScoutIMobEntity.createAttributes().build());
        event.put(GUARDIAN_SCOUT_II.get(), GuardianScoutIIMobEntity.createAttributes().build());
    }
}