package net.nakumaerebos.shrines.entity;

import net.minecraft.client.Minecraft;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.game.ClientboundExplodePacket;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Mth;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.item.PrimedTnt;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Explosion;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.nakumaerebos.shrines.client.sound.RemoteBombLoopSoundInstance;
import net.nakumaerebos.shrines.particles.ModParticles;
import net.nakumaerebos.shrines.sound.ModSounds;
import net.neoforged.api.distmarker.Dist;
import software.bernie.geckolib.animatable.GeoEntity;
import software.bernie.geckolib.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.animation.AnimatableManager;
import software.bernie.geckolib.util.GeckoLibUtil;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public class RemoteBombCubedEntity extends PathfinderMob implements GeoEntity {

    // DataAccessor um die UUID des Erstellers zu synchronisieren und zu tracken
    private static final EntityDataAccessor<Optional<UUID>> DATA_OWNER_UUID =
            SynchedEntityData.defineId(RemoteBombCubedEntity.class, EntityDataSerializers.OPTIONAL_UUID);

    private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);
    private float lastGleitenYaw = 0.0F;
    private float currentRollPitch = 0.0F;
    private boolean loopSoundStarted = false;

    public RemoteBombCubedEntity(EntityType<? extends PathfinderMob> entityType, Level level) {
        super(entityType, level);
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(DATA_OWNER_UUID, Optional.empty());
    }

    // Getter und Setter für den Besitzer (UUID)
    public Optional<UUID> getOwnerUUID() {
        return this.entityData.get(DATA_OWNER_UUID);
    }

    public void setOwnerUUID(UUID uuid) {
        this.entityData.set(DATA_OWNER_UUID, Optional.ofNullable(uuid));
    }

    @Override
    public void addAdditionalSaveData(CompoundTag compound) {
        super.addAdditionalSaveData(compound);
        if (this.getOwnerUUID().isPresent()) {
            compound.putUUID("OwnerUUID", this.getOwnerUUID().get());
        }
    }

    @Override
    public void readAdditionalSaveData(CompoundTag compound) {
        super.readAdditionalSaveData(compound);
        if (compound.hasUUID("OwnerUUID")) {
            this.setOwnerUUID(compound.getUUID("OwnerUUID"));
        }
    }

    // Methode um die Explosion von außen (durch das Item) zu triggern
    public void explode() {
        if (!this.level().isClientSide) {
            float explosionRadius = 4.0F;
            createCustomExplosion(this.level(), this, this.getX(), this.getY(), this.getZ(), explosionRadius);
            this.discard();
        }
    }

    @Override
    protected void registerGoals() {
        // Leer lassen
    }

    public static AttributeSupplier.Builder createAttributes() {
        return PathfinderMob.createMobAttributes()
                .add(Attributes.MAX_HEALTH, 10.0D)
                .add(Attributes.MOVEMENT_SPEED, 0.0D);
    }

    @Override
    public boolean isInvulnerableTo(DamageSource source) {
        if (source.is(DamageTypes.FALL) ||
                source.is(DamageTypes.DROWN) ||
                source.is(DamageTypes.ARROW) ||
                source.is(DamageTypes.CAMPFIRE) ||
                source.is(DamageTypes.DRAGON_BREATH) ||
                source.is(DamageTypes.EXPLOSION) ||
                source.is(DamageTypes.FALLING_ANVIL) ||
                source.is(DamageTypes.FALLING_BLOCK) ||
                source.is(DamageTypes.FALLING_STALACTITE) ||
                source.is(DamageTypes.FIREBALL) ||
                source.is(DamageTypes.FIREWORKS) ||
                source.is(DamageTypes.FLY_INTO_WALL) ||
                source.is(DamageTypes.FREEZE) ||
                source.is(DamageTypes.GENERIC) ||
                source.is(DamageTypes.HOT_FLOOR) ||
                source.is(DamageTypes.IN_FIRE) ||
                source.is(DamageTypes.IN_WALL) ||
                source.is(DamageTypes.INDIRECT_MAGIC) ||
                source.is(DamageTypes.LIGHTNING_BOLT) ||
                source.is(DamageTypes.MAGIC) ||
                source.is(DamageTypes.MOB_ATTACK) ||
                source.is(DamageTypes.MOB_ATTACK_NO_AGGRO) ||
                source.is(DamageTypes.MOB_PROJECTILE) ||
                source.is(DamageTypes.ON_FIRE) ||
                source.is(DamageTypes.PLAYER_EXPLOSION) ||
                source.is(DamageTypes.SONIC_BOOM) ||
                source.is(DamageTypes.SPIT) ||
                source.is(DamageTypes.STALAGMITE) ||
                source.is(DamageTypes.STARVE) ||
                source.is(DamageTypes.STING) ||
                source.is(DamageTypes.SWEET_BERRY_BUSH) ||
                source.is(DamageTypes.THORNS) ||
                source.is(DamageTypes.THROWN) ||
                source.is(DamageTypes.TRIDENT) ||
                source.is(DamageTypes.UNATTRIBUTED_FIREBALL) ||
                source.is(DamageTypes.WIND_CHARGE) ||
                source.is(DamageTypes.WITHER) ||
                source.is(DamageTypes.WITHER_SKULL) ||
                source.is(DamageTypes.LAVA) ||
                source.is(DamageTypes.CACTUS)) {
            return true;
        }
        return super.isInvulnerableTo(source);
    }

    @Override
    public boolean hurt(DamageSource source, float amount) {
        if (!this.level().isClientSide && source.getEntity() instanceof Player player) {
            Vec3 lookDirection = player.getLookAngle();
            double pushPower = 1.1D;
            Vec3 motion = new Vec3(lookDirection.x, 0.2D, lookDirection.z).normalize().scale(pushPower);

            this.setDeltaMovement(motion);
            this.hasImpulse = true;
            return true;
        }
        return super.hurt(source, amount);
    }

    @Override
    public void tick() {
        if (!this.level().isClientSide && this.level() instanceof ServerLevel serverLevel) {
            if (!serverLevel.isPositionEntityTicking(this.blockPosition())) {
                this.discard();
                return;
            }
        }

        super.tick();

        if (this.level().isClientSide && !this.loopSoundStarted) {
            this.startLoopSound();
        }
    }

    @Override
    public void checkDespawn() {
        if (!this.level().isClientSide && this.level() instanceof ServerLevel serverLevel) {
            if (!serverLevel.isPositionEntityTicking(this.blockPosition())) {
                this.discard();
                return;
            }
        }
        super.checkDespawn();
    }

    @Override
    public boolean removeWhenFarAway(double distanceToClosestPlayerSq) {
        return true;
    }

    private void startLoopSound() {
        ClientSoundHelper.playBombLoop(this);
        this.loopSoundStarted = true;
    }

    private static class ClientSoundHelper {
        @net.neoforged.api.distmarker.OnlyIn(Dist.CLIENT)
        public static void playBombLoop(RemoteBombCubedEntity bomb) {
            Minecraft.getInstance().getSoundManager().play(
                    new RemoteBombLoopSoundInstance(bomb)
            );
        }
    }

    public float getCurrentRollPitch() {
        return this.currentRollPitch;
    }

    @Override
    public boolean isPushable() {
        return true;
    }

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {}

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return this.cache;
    }

    public static void createCustomExplosion(Level level, Entity source, double x, double y, double z, float radius) {
        if (level.isClientSide) return;
        ServerLevel serverLevel = (ServerLevel) level;

        net.minecraft.world.level.ExplosionDamageCalculator peaceCalculator = new net.minecraft.world.level.ExplosionDamageCalculator() {
            @Override
            public float getEntityDamageAmount(Explosion explosion, Entity entity) {
                return 0.0F;
            }
        };

        Explosion explosion = new Explosion(
                serverLevel,
                source,
                null,
                peaceCalculator,
                x, y, z,
                radius,
                false,
                Explosion.BlockInteraction.DESTROY,
                ModParticles.REMOTE_BOMB_EXPLOSION_PARTICLE.get(),
                ModParticles.REMOTE_BOMB_EXPLOSION_PARTICLE.get(),
                BuiltInRegistries.SOUND_EVENT.wrapAsHolder(ModSounds.REMOTE_BOMB_EXPLODE.get())
        );

        explosion.explode();

        float f2 = radius * 2.0F;
        int k1 = Mth.floor(x - (double)f2 - 1.0);
        int l1 = Mth.floor(x + (double)f2 + 1.0);
        int i2 = Mth.floor(y - (double)f2 - 1.0);
        int i1 = Mth.floor(y + (double)f2 + 1.0);
        int j2 = Mth.floor(z - (double)f2 - 1.0);
        int j1 = Mth.floor(z + (double)f2 + 1.0);
        List<Entity> list = level.getEntities(source, new AABB((double)k1, (double)i2, (double)j2, (double)l1, (double)i1, (double)j1));

        net.neoforged.neoforge.event.EventHooks.onExplosionDetonate(level, explosion, list, f2);
        Vec3 vec3 = new Vec3(x, y, z);
        float maxDamage = 2.0F;

        for (Entity entity : list) {
            if (!entity.ignoreExplosion(explosion)) {
                double d11 = Math.sqrt(entity.distanceToSqr(vec3)) / (double)f2;
                if (d11 <= 1.0) {
                    float basisSchaden = (float)((1.0 - d11) * Explosion.getSeenPercent(vec3, entity));
                    float finalerSchaden = basisSchaden * maxDamage;

                    if (finalerSchaden > 0) {
                        entity.hurt(level.damageSources().explosion(explosion), finalerSchaden);
                    }

                    double d5 = entity.getX() - x;
                    double d7 = (entity instanceof PrimedTnt ? entity.getY() : entity.getEyeY()) - y;
                    double d9 = entity.getZ() - z;
                    double d12 = Math.sqrt(d5 * d5 + d7 * d7 + d9 * d9);
                    if (d12 != 0.0) {
                        d5 /= d12;
                        d7 /= d12;
                        d9 /= d12;
                        double d13 = (1.0 - d11) * (double)Explosion.getSeenPercent(vec3, entity);
                        double d10 = entity instanceof LivingEntity livingentity ? d13 * (1.0 - livingentity.getAttributeValue(Attributes.EXPLOSION_KNOCKBACK_RESISTANCE)) : d13;

                        d5 *= d10;
                        d7 *= d10;
                        d9 *= d10;
                        Vec3 knockback = new Vec3(d5, d7, d9);
                        knockback = net.neoforged.neoforge.event.EventHooks.getExplosionKnockback(level, explosion, entity, knockback);
                        entity.setDeltaMovement(entity.getDeltaMovement().add(knockback));

                        if (entity instanceof Player player && !player.isSpectator() && (!player.isCreative() || !player.getAbilities().flying)) {
                            explosion.getHitPlayers().put(player, knockback);
                        }
                        entity.onExplosionHit(source);
                    }
                }
            }
        }

        explosion.finalizeExplosion(true);

        for (ServerPlayer player : serverLevel.players()) {
            if (player.distanceToSqr(x, y, z) < 4096.0) {
                Vec3 playerKnockback = explosion.getHitPlayers().get(player);
                player.connection.send(new ClientboundExplodePacket(
                        x, y, z, radius, explosion.getToBlow(), playerKnockback,
                        explosion.getBlockInteraction(), explosion.getSmallExplosionParticles(),
                        explosion.getLargeExplosionParticles(), explosion.getExplosionSound()
                ));
            }
        }
    }
}