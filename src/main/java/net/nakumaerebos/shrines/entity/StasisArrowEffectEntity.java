package net.nakumaerebos.shrines.entity;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.Level;
import net.nakumaerebos.shrines.attachments.ModAttachments;
import software.bernie.geckolib.animatable.GeoEntity;
import software.bernie.geckolib.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.animation.AnimatableManager;
import software.bernie.geckolib.util.GeckoLibUtil;

import java.util.UUID;

public class StasisArrowEffectEntity extends Entity implements GeoEntity {

    private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);

    private UUID targetUUID;
    private Entity cachedTarget;

    // Datenparameter für die Textur-Stufe
    private static final EntityDataAccessor<Integer> ARROW_STAGE = SynchedEntityData.defineId(StasisArrowEffectEntity.class, EntityDataSerializers.INT);
    // NEU: Synchronisierter Parameter für die exakte Rotation (Yaw) am Client
    private static final EntityDataAccessor<Float> ARROW_YAW = SynchedEntityData.defineId(StasisArrowEffectEntity.class, EntityDataSerializers.FLOAT);

    public StasisArrowEffectEntity(EntityType<? extends Entity> entityType, Level level) {
        super(entityType, level);
        this.noPhysics = true;
    }

    public void setTarget(Entity target) {
        this.cachedTarget = target;
        this.targetUUID = target.getUUID();
    }

    @Override
    public void tick() {
        super.tick();

        // 1. Ziel auflösen (nur Server)
        if (this.cachedTarget == null && this.targetUUID != null && !this.level().isClientSide) {
            this.cachedTarget = ((ServerLevel) this.level()).getEntity(this.targetUUID);
        }

        // 2. Sicherheitscheck
        if (this.cachedTarget == null || !this.cachedTarget.isAlive() ||
                !this.cachedTarget.hasData(ModAttachments.FREEZE_TICKS) || this.cachedTarget.getData(ModAttachments.FREEZE_TICKS) <= 0) {
            if (!this.level().isClientSide) {
                this.discard();
            }
            return;
        }

        // 3. POSITIONIERUNG: Mittig am Ziel verankern
        double targetCenterY = this.cachedTarget.getY() + (this.cachedTarget.getBbHeight() * 0.5D);
        this.setPos(this.cachedTarget.getX(), targetCenterY, this.cachedTarget.getZ());

        // 4. ROTATION (Flugrichtung)
        if (this.cachedTarget.hasData(ModAttachments.LAST_ATTACK_YAW)) {
            if (!this.level().isClientSide) {
                // Server berechnet die Rotation und schickt sie über das SynchedEntityData-Feld an die Clients
                float attackYaw = this.cachedTarget.getData(ModAttachments.LAST_ATTACK_YAW);
                float flightYaw = attackYaw + 180.0F;
                this.setArrowYaw(flightYaw);
            }
        }

        // 4b. CLIENT-FIX: Synchronisierte Rotation lokal auf die Entity-Variablen anwenden
        // Das sorgt dafür, dass GeckoLib die korrekten Render-Winkel auf dem Client abgreift!
        float syncedYaw = this.getArrowYaw();
        this.yRotO = this.getYRot();
        this.setYRot(syncedYaw);
        this.setXRot(0.0F);

        // 5. TEXTUR-STUFEN (Nur Server)
        if (!this.level().isClientSide) {
            int stage = 0;
            if (this.cachedTarget.hasData(ModAttachments.ACCUMULATED_KNOCKBACK)) {
                float kb = this.cachedTarget.getData(ModAttachments.ACCUMULATED_KNOCKBACK);

                if (kb > 0.0F) {
                    if (kb <= 1.2F) {
                        stage = 1;
                    } else if (kb <= 3.5F) {
                        stage = 2;
                    } else {
                        stage = 3;
                    }
                }
            }
            this.setArrowStage(stage);
        }
    }

    // --- SYNCHRONISIERTE VARIABLEN-STEUERUNG ---

    public void setArrowStage(int stage) {
        this.entityData.set(ARROW_STAGE, stage);
    }

    public int getArrowStage() {
        return this.entityData.get(ARROW_STAGE);
    }

    public void setArrowYaw(float yaw) {
        this.entityData.set(ARROW_YAW, yaw);
    }

    public float getArrowYaw() {
        return this.entityData.get(ARROW_YAW);
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        builder.define(ARROW_STAGE, 0);
        builder.define(ARROW_YAW, 0.0F); // Initialisiert die Rotation standardmäßig auf 0 Grad
    }

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
    }

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return this.cache;
    }

    @Override
    protected void readAdditionalSaveData(CompoundTag tag) {
        if (tag.hasUUID("TargetUUID")) {
            this.targetUUID = tag.getUUID("TargetUUID");
        }
    }

    @Override
    protected void addAdditionalSaveData(CompoundTag tag) {
        if (this.targetUUID != null) {
            tag.putUUID("TargetUUID", this.targetUUID);
        }
    }
}