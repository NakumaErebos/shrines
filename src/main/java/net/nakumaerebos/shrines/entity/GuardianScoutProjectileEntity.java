package net.nakumaerebos.shrines.entity;

import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.entity.projectile.ProjectileUtil;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import software.bernie.geckolib.animatable.GeoEntity;
import software.bernie.geckolib.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.animation.AnimatableManager;
import software.bernie.geckolib.animation.AnimationController;
import software.bernie.geckolib.animation.RawAnimation;
import software.bernie.geckolib.util.GeckoLibUtil;

public class GuardianScoutProjectileEntity extends Projectile implements GeoEntity {
    private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);
    private static final RawAnimation HIT_ANIM = RawAnimation.begin().thenPlay("animation.guardianscout_projectile.hit");

    private int hitTimer = -1;

    public GuardianScoutProjectileEntity(EntityType<? extends Projectile> type, Level level) {
        super(type, level);
        this.noPhysics = true; // Wir regeln Kollision manuell
    }

    // Hilfskonstruktor zum Schießen
    public GuardianScoutProjectileEntity(Level level, LivingEntity owner) {
        this(ModEntities.GUARDIANSCOUT_PROJECTILE.get(), level);
        this.setOwner(owner);
        this.setPos(owner.getX(), owner.getEyeY() - 0.1D, owner.getZ());
    }

    @Override
    protected void defineSynchedData(net.minecraft.network.syncher.SynchedEntityData.Builder builder) {
        // Notwendig für Entity (Basisklasse)
    }

    // Oben in der Klasse
    private float lockedYRot;
    private float lockedXRot;

    @Override
    public void tick() {
        super.tick();

        if (hitTimer == -1) {
            // FLUG: Normal bewegen und rotieren
            Vec3 delta = this.getDeltaMovement();
            this.setPos(this.getX() + delta.x, this.getY() + delta.y, this.getZ() + delta.z);

            // WICHTIG: updateRotation nur rufen, solange wir uns bewegen!
            if (delta.horizontalDistanceSqr() > 1.0E-6D) {
                this.updateRotation();
                // Speicher den Winkel ab
                this.lockedYRot = this.getYRot();
                this.lockedXRot = this.getXRot();
            }

            // Kollision prüfen
            HitResult hitresult = ProjectileUtil.getHitResultOnMoveVector(this, this::canHitEntity);
            if (hitresult.getType() != HitResult.Type.MISS) {
                this.onHit(hitresult);
            }
        } else {
            // HIT: Timer läuft
            hitTimer++;
            this.setDeltaMovement(Vec3.ZERO);

            // ZWINGE die Rotation auf den gespeicherten Wert
            this.setYRot(this.lockedYRot);
            this.setXRot(this.lockedXRot);
            // Setze auch die "Old" Werte, damit getViewYRot nicht interpoliert
            this.yRotO = this.lockedYRot;
            this.xRotO = this.lockedXRot;

            if (hitTimer >= 5) {
                this.discard();
            }
        }
    }

    @Override
    protected void onHitEntity(EntityHitResult result) {
        if (hitTimer == -1) {
            String className = result.getEntity().getClass().getSimpleName();
            if (className.equals("GuardianScoutIMobEntity") || className.equals("GuardianScoutIIMobEntity")) {
                return;
            }

            DamageSource source = this.damageSources().mobProjectile(this, (LivingEntity) this.getOwner());
            result.getEntity().hurt(source, 4.0F);
            startHitSequence();
        }
    }

    @Override
    protected void onHitBlock(BlockHitResult result) {
        if (hitTimer == -1) {
            super.onHitBlock(result);
            startHitSequence();
        }
    }

    private void startHitSequence() {
        this.hitTimer = 0;
        this.lockedYRot = this.getYRot();
        this.lockedXRot = this.getXRot();

        this.triggerAnim("controller", "hit");
        this.setDeltaMovement(Vec3.ZERO);

        this.level().playSound(null, this.getX(), this.getY(), this.getZ(),
                SoundEvents.COW_AMBIENT, SoundSource.NEUTRAL, 1.0F, 1.0F);
    }

    // --- Geckolib & Rendering ---

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
        controllers.add(new AnimationController<>(this, "controller", 0, state -> {
            if (hitTimer >= 0) {
                return state.setAndContinue(HIT_ANIM);
            }
            return state.setAndContinue(RawAnimation.begin().thenLoop("animation.guardianscout_projectile.idle"));
        }).triggerableAnim("hit", HIT_ANIM));
    }

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return this.cache;
    }

    @Override
    public boolean shouldRenderAtSqrDistance(double distance) {
        return distance < 1024.0D; // Weit sichtbar
    }
}