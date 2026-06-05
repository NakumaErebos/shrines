package net.nakumaerebos.shrines.entity;

import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.*;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.nakumaerebos.shrines.sound.ModSounds;
import software.bernie.geckolib.animatable.GeoEntity;
import software.bernie.geckolib.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.animation.AnimatableManager;
import software.bernie.geckolib.animation.RawAnimation;
import software.bernie.geckolib.util.GeckoLibUtil;

public class CryonisPillarEntity extends Entity implements GeoEntity {

    private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);

    private static final EntityDataAccessor<Boolean> IS_BREAKING =
            SynchedEntityData.defineId(CryonisPillarEntity.class, EntityDataSerializers.BOOLEAN);

    private static final EntityDataAccessor<Direction> ORIENTATION =
            SynchedEntityData.defineId(CryonisPillarEntity.class, EntityDataSerializers.DIRECTION);

    // FIX 1: Wir machen den growTimer synchronisiert, damit der Client ab Frame 0 Bescheid weiß!
    private static final EntityDataAccessor<Integer> GROW_TIMER =
            SynchedEntityData.defineId(CryonisPillarEntity.class, EntityDataSerializers.INT);

    private int breakTimer = 0;
    private static final int BREAK_DURATION = 20;
    private static final int GROW_DURATION = 15;

    public CryonisPillarEntity(EntityType<? extends Entity> entityType, Level level) {
        super(entityType, level);
        this.blocksBuilding = true;
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        builder.define(IS_BREAKING, false);
        builder.define(ORIENTATION, Direction.UP);
        builder.define(GROW_TIMER, 0); // Startet bei 0
    }

    public void setOrientation(Direction direction) {
        this.entityData.set(ORIENTATION, direction);
        this.refreshDimensions();
    }

    public Direction getOrientation() {
        return this.entityData.get(ORIENTATION);
    }

    @Override
    public void onSyncedDataUpdated(EntityDataAccessor<?> key) {
        super.onSyncedDataUpdated(key);
        if (ORIENTATION.equals(key)) {
            this.refreshDimensions();
        }
    }

    @Override
    public void tick() {
        super.tick();

        if (this.entityData.get(IS_BREAKING)) {
            this.breakTimer++;
            if (!this.level().isClientSide && this.breakTimer >= BREAK_DURATION) {
                this.discard();
            }
            return;
        }

        int currentGrow = this.entityData.get(GROW_TIMER);
        if (currentGrow < GROW_DURATION) {
            if (currentGrow == 0) {
                this.playGrowEffects();
            }
            this.entityData.set(GROW_TIMER, currentGrow + 1);
        }

        this.setDeltaMovement(Vec3.ZERO);
        this.move(MoverType.SELF, this.getDeltaMovement());

        if (!this.level().isClientSide && this.checkDespawnRules()) {
            return;
        }

        AABB touchBox = this.getBoundingBox().inflate(0.15);
        for (LivingEntity entity : this.level().getEntitiesOfClass(LivingEntity.class, touchBox)) {
            if (!entity.getUUID().equals(this.getUUID())) {
                handleClimbing(entity);
            }
        }
    }

    @Override
    protected AABB makeBoundingBox() {
        double x = this.getX();
        double y = this.getY();
        double z = this.getZ();
        Direction dir = getOrientation();
        if (dir == null) dir = Direction.UP;

        switch (dir) {
            case NORTH: return new AABB(x - 1.0, y - 1.0, z - 3.0, x + 1.0, y + 1.0, z);
            case SOUTH: return new AABB(x - 1.0, y - 1.0, z, x + 1.0, y + 1.0, z + 3.0);
            case WEST:  return new AABB(x - 3.0, y - 1.0, z - 1.0, x, y + 1.0, z + 1.0);
            case EAST:  return new AABB(x, y - 1.0, z - 1.0, x + 3.0, y + 1.0, z + 1.0);
            case DOWN:  return new AABB(x - 1.0, y - 3.0, z - 1.0, x + 1.0, y, z + 1.0);
            case UP:
            default:    return new AABB(x - 1.0, y, z - 1.0, x + 1.0, y + 3.0, z + 1.0);
        }
    }

    @Override
    public EntityDimensions getDimensions(Pose pose) {
        return getOrientation().getAxis() == Direction.Axis.Y ?
                EntityDimensions.scalable(2.0F, 3.0F) : EntityDimensions.scalable(3.0F, 2.0F);
    }

    private void handleClimbing(LivingEntity entity) {
        if (getOrientation() == Direction.UP) {
            double pillarTop = this.getY() + 3.0;
            if (entity.getY() >= pillarTop - 0.15) {
                return;
            }
        }

        if (entity.horizontalCollision || (entity.zza > 0 && isMovingTowardsCenter(entity))) {
            Vec3 motion = entity.getDeltaMovement();
            entity.resetFallDistance();

            if (entity.isShiftKeyDown()) {
                entity.setDeltaMovement(motion.x, 0.0, motion.z);
            } else {
                entity.setDeltaMovement(motion.x, 0.2, motion.z);
            }
        }
    }

    private boolean isMovingTowardsCenter(LivingEntity entity) {
        Vec3 toCenter = this.position().subtract(entity.position());
        Vec3 look = entity.getLookAngle();
        return look.dot(new Vec3(toCenter.x, 0, toCenter.z).normalize()) > 0.1;
    }

    private void playGrowEffects() {
        this.level().playSound(null, this.getX(), this.getY(), this.getZ(), ModSounds.GROW_CRYONIS_PILLAR.get(), SoundSource.BLOCKS, 1.5F, 1.0F);

        if (this.level() instanceof ServerLevel serverLevel) {
            serverLevel.sendParticles(ParticleTypes.SPLASH, this.getX(), this.getY(), this.getZ(), 100, 0.8, 0.8, 0.8, 0.1);
            serverLevel.sendParticles(ParticleTypes.SNOWFLAKE, this.getX(), this.getY(), this.getZ(), 60, 0.8, 0.8, 0.8, 0.05);
        }
    }

    @Override public AABB getBoundingBoxForCulling() { return this.getBoundingBox(); }
    @Override public void refreshDimensions() { this.setBoundingBox(this.makeBoundingBox()); }
    @Override public boolean canBeCollidedWith() { return !this.entityData.get(IS_BREAKING); }
    @Override public boolean canCollideWith(Entity other) { return !this.entityData.get(IS_BREAKING); }
    @Override public boolean isPickable() { return true; }
    @Override public boolean isPushable() { return false; }

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
        controllers.add(new software.bernie.geckolib.animation.AnimationController<>(this, "controller", 0, state -> {
            if (this.entityData.get(IS_BREAKING)) {
                return state.setAndContinue(RawAnimation.begin().thenPlayAndHold("animation.cryonis_pillar.break"));
            }

            // FIX 2: Nutzt das synchronisierte Datenfeld. Falls der Controller brandneu registriert wird,
            // erzwingen wir sofort die grow-Animation statt der idle-Animation.
            if (this.entityData.get(GROW_TIMER) < GROW_DURATION) {
                return state.setAndContinue(RawAnimation.begin().thenPlay("animation.cryonis_pillar.grow"));
            }
            return state.setAndContinue(RawAnimation.begin().thenLoop("animation.cryonis_pillar.idle"));
        }));
    }

    @Override public AnimatableInstanceCache getAnimatableInstanceCache() { return this.cache; }

    private boolean checkDespawnRules() {
        if (!this.level().hasChunkAt(this.blockPosition())) {
            this.discard();
            return true;
        }
        double maxDistance = 64.0;
        if (!this.level().hasNearbyAlivePlayer(this.getX(), this.getY(), this.getZ(), maxDistance)) {
            this.triggerCryonisBreak();
            return true;
        }
        return false;
    }

    public void triggerCryonisBreak() {
        if (this.entityData.get(IS_BREAKING)) return;
        this.entityData.set(IS_BREAKING, true);
        if (this.getPersistentData().contains("OwnerUUID")) {
            this.getPersistentData().remove("OwnerUUID");
        }
        if (!this.level().isClientSide) {
            this.level().playSound(null, this.getX(), this.getY(), this.getZ(), ModSounds.BREAK_CRYONIS_PILLAR.get(), SoundSource.BLOCKS, 2.0F, 1.0F);
            if (this.level() instanceof ServerLevel serverLevel) {
                serverLevel.sendParticles(ParticleTypes.SNOWFLAKE, this.getX(), this.getY(), this.getZ(), 80, 0.8, 0.8, 0.8, 0.1);
            }
        }
    }

    @Override protected void readAdditionalSaveData(net.minecraft.nbt.CompoundTag compound) {}
    @Override protected void addAdditionalSaveData(net.minecraft.nbt.CompoundTag compound) {}
}