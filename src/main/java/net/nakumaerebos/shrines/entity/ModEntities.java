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

    public static final DeferredHolder<EntityType<?>, EntityType<StasisEffectEntity>> STASIS_EFFECT =
            ENTITIES.register("stasis_effect", () -> EntityType.Builder.of(StasisEffectEntity::new, MobCategory.MISC)
                    .sized(1.0f, 1.0f)
                    .build("stasis_effect"));

    public static final DeferredHolder<EntityType<?>, EntityType<StasisArrowEffectEntity>> STASIS_ARROW_EFFECT =
            ENTITIES.register("stasis_arrow_effect", () -> EntityType.Builder.of(StasisArrowEffectEntity::new, MobCategory.MISC)
                    .sized(1.0f, 1.0f)
                    .build("stasis_arrow_effect"));

    public static final DeferredHolder<EntityType<?>, EntityType<CryonisPillarEntity>> CRYONIS_PILLAR =
            ENTITIES.register("cryonis_pillar", () -> EntityType.Builder.<CryonisPillarEntity>of(CryonisPillarEntity::new, MobCategory.MISC)
                    .sized(2.0f, 3.0f)
                    .clientTrackingRange(10) // Wie viele Chunks entfernt der Client das Entity sehen kann
                    .updateInterval(20)      // Wie oft (in Ticks) Positions-Updates gesendet werden (20 Ticks = 1 Sekunde, reicht bei einer festen Säule völlig)
                    .build("cryonis_pillar"));

    public static final DeferredHolder<EntityType<?>, EntityType<GuardianScoutProjectileEntity>> GUARDIANSCOUT_PROJECTILE =
            ENTITIES.register("guardianscout_projectile", () -> EntityType.Builder.<GuardianScoutProjectileEntity>of(GuardianScoutProjectileEntity::new, MobCategory.MISC)
                    .sized(0.5f, 0.5f)
                    .clientTrackingRange(4)
                    .updateInterval(1)
                    .build("guardianscout_projectile"));

    public static final DeferredHolder<EntityType<?>, EntityType<RemoteBombRoundEntity>> REMOTE_BOMB_ROUND =
            ENTITIES.register("remote_bomb_round",
                    () -> EntityType.Builder.<RemoteBombRoundEntity>of(RemoteBombRoundEntity::new, MobCategory.MISC)
                            .sized(0.5F, 0.5F)
                            .build("remote_bomb_round"));

    public static final DeferredHolder<EntityType<?>, EntityType<RemoteBombCubedEntity>> REMOTE_BOMB_CUBED =
            ENTITIES.register("remote_bomb_cubed",
                    () -> EntityType.Builder.<RemoteBombCubedEntity>of(RemoteBombCubedEntity::new, MobCategory.MISC)
                            .sized(0.5F, 0.5F)
                            .build("remote_bomb_cubed"));

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
        eventBus.addListener(ModEntities::registerAttributes);
    }

    private static void registerAttributes(EntityAttributeCreationEvent event) {
        event.put(GUARDIAN_SCOUT_I.get(), GuardianScoutIMobEntity.createAttributes().build());
        event.put(GUARDIAN_SCOUT_II.get(), GuardianScoutIIMobEntity.createAttributes().build());
        event.put(REMOTE_BOMB_ROUND.get(), RemoteBombRoundEntity.createAttributes().build());
        event.put(REMOTE_BOMB_CUBED.get(), RemoteBombRoundEntity.createAttributes().build());
    }
}