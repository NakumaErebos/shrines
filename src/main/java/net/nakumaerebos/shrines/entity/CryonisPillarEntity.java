package net.nakumaerebos.shrines.entity;

import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.*;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import software.bernie.geckolib.animatable.GeoEntity;
import software.bernie.geckolib.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.animation.AnimatableManager;
import software.bernie.geckolib.animation.RawAnimation;
import software.bernie.geckolib.util.GeckoLibUtil;

public class CryonisPillarEntity extends Entity implements GeoEntity {

    private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);

    private static final EntityDataAccessor<Boolean> IS_BREAKING =
            SynchedEntityData.defineId(CryonisPillarEntity.class, EntityDataSerializers.BOOLEAN);

    private int breakTimer = 0;
    private static final int BREAK_DURATION = 20;

    // Neues Tracking für die Entstehung
    private int growTimer = 0;
    private static final int GROW_DURATION = 15; // 15 Ticks wächst die Säule optisch

    public CryonisPillarEntity(EntityType<? extends Entity> entityType, Level level) {
        super(entityType, level);
        this.blocksBuilding = true;
        this.refreshDimensions();
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        builder.define(IS_BREAKING, false);
    }

    @Override
    protected void readAdditionalSaveData(net.minecraft.nbt.CompoundTag compound) {
    }

    @Override
    protected void addAdditionalSaveData(net.minecraft.nbt.CompoundTag compound) {
    }

    @Override
    public void tick() {
        super.tick();

        // 1. Logik für das Zerbrechen
        if (this.entityData.get(IS_BREAKING)) {
            this.breakTimer++;
            if (!this.level().isClientSide && this.breakTimer >= BREAK_DURATION) {
                this.discard();
            }
            return;
        }

        // 2. Logik für das Spawnen / Erscheinen (Grow-Effekte)
        if (this.growTimer < GROW_DURATION) {
            if (this.growTimer == 0) {
                this.playGrowEffects();
            }
            this.growTimer++;
        }

        this.setDeltaMovement(Vec3.ZERO);
        this.move(MoverType.SELF, this.getDeltaMovement());

        if (!this.level().isClientSide && this.checkDespawnRules()) {
            return;
        }

        AABB touchBox = this.getBoundingBox().inflate(0.15, 0.0, 0.15);
        for (LivingEntity entity : this.level().getEntitiesOfClass(LivingEntity.class, touchBox)) {
            if (!entity.getUUID().equals(this.getUUID())) {
                handleClimbing(entity);
            }
        }
    }

    /**
     * Erzeugt Sound und Partikel beim Erschaffen der Säule
     */
    private void playGrowEffects() {
        this.level().playSound(
                null,
                this.getX(), this.getY(), this.getZ(),
                SoundEvents.BUCKET_EMPTY_POWDER_SNOW,
                SoundSource.BLOCKS,
                1.5F,
                0.6F // Tiefer Pitch für Wucht
        );

        this.level().playSound(
                null,
                this.getX(), this.getY(), this.getZ(),
                SoundEvents.BUCKET_FILL_POWDER_SNOW,
                SoundSource.BLOCKS,
                1.0F,
                1.0F
        );

        if (this.level() instanceof ServerLevel serverLevel) {
            // Spritzende Wasser- und Eispartikel an der Oberfläche
            serverLevel.sendParticles(
                    ParticleTypes.SPLASH,
                    this.getX(), this.getY() + 0.5, this.getZ(),
                    50, 0.8, 0.2, 0.8, 0.1
            );
            serverLevel.sendParticles(
                    ParticleTypes.SNOWFLAKE,
                    this.getX(), this.getY() + 1.0, this.getZ(),
                    30, 0.5, 1.0, 0.5, 0.05
            );
        }
    }

    private void handleClimbing(LivingEntity entity) {
        if (entity.horizontalCollision || (entity.zza > 0 && isMovingTowardsCenter(entity))) {
            Vec3 motion = entity.getDeltaMovement();
            double climbSpeed = 0.2;

            entity.resetFallDistance();

            if (entity.isShiftKeyDown()) {
                entity.setDeltaMovement(motion.x, 0.0, motion.z);
            } else {
                entity.setDeltaMovement(motion.x, climbSpeed, motion.z);
            }
        }
    }

    private boolean isMovingTowardsCenter(LivingEntity entity) {
        Vec3 toCenter = this.position().subtract(entity.position());
        Vec3 look = entity.getLookAngle();
        return look.dot(new Vec3(toCenter.x, 0, toCenter.z).normalize()) > 0.2;
    }

    @Override
    public EntityDimensions getDimensions(Pose pose) {
        return EntityDimensions.scalable(2.0F, 3.0F);
    }

    @Override
    public AABB getBoundingBoxForCulling() {
        return this.getBoundingBox();
    }

    @Override
    protected AABB makeBoundingBox() {
        double x = this.getX();
        double y = this.getY();
        double z = this.getZ();
        return new AABB(x - 1.0, y, z - 1.0, x + 1.0, y + 3.0, z + 1.0);
    }

    @Override
    public void refreshDimensions() {
        this.setBoundingBox(this.makeBoundingBox());
    }

    @Override
    public boolean canBeCollidedWith() {
        return !this.entityData.get(IS_BREAKING);
    }

    @Override
    public boolean canCollideWith(Entity other) {
        return !this.entityData.get(IS_BREAKING);
    }

    @Override
    public boolean isPickable() {
        return true;
    }

    @Override
    public boolean isPushable() {
        return false;
    }

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
        controllers.add(new software.bernie.geckolib.animation.AnimationController<>(this, "controller", 2, state -> {
            if (this.entityData.get(IS_BREAKING)) {
                return state.setAndContinue(RawAnimation.begin().thenPlayAndHold("animation.cryonis_pillar.break"));
            }

            // Wenn der growTimer läuft, erzwingen wir die Grow-Animation
            if (this.growTimer < GROW_DURATION) {
                return state.setAndContinue(RawAnimation.begin().thenPlay("animation.cryonis_pillar.grow"));
            }

            return state.setAndContinue(RawAnimation.begin().thenLoop("animation.cryonis_pillar.idle"));
        }));
    }

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return this.cache;
    }

    private boolean checkDespawnRules() {
        if (!this.level().hasChunkAt(this.blockPosition())) {
            this.discard();
            return true;
        }

        double maxDistance = 64.0;
        boolean playerNearby = this.level().hasNearbyAlivePlayer(this.getX(), this.getY(), this.getZ(), maxDistance);

        if (!playerNearby) {
            this.triggerCryonisBreak();
            return true;
        }

        return false;
    }

    public void triggerCryonisBreak() {
        if (this.entityData.get(IS_BREAKING)) return;

        this.entityData.set(IS_BREAKING, true);

        // WICHTIG: UUID-Tag beim Zerbrechen löschen, damit das Item sie im Limit-Check
        // ab sofort ignoriert, selbst wenn die Animation noch läuft!
        if (this.getPersistentData().contains("OwnerUUID")) {
            this.getPersistentData().remove("OwnerUUID");
        }

        if (!this.level().isClientSide) {
            this.level().playSound(
                    null,
                    this.getX(), this.getY(), this.getZ(),
                    SoundEvents.GLASS_BREAK,
                    SoundSource.BLOCKS,
                    1.0F,
                    0.8F
            );

            if (this.level() instanceof ServerLevel serverLevel) {
                serverLevel.sendParticles(
                        ParticleTypes.SNOWFLAKE,
                        this.getX(), this.getY() + 1.5, this.getZ(),
                        40, 0.5, 1.0, 0.5, 0.1
                );
            }
        }
    }
}