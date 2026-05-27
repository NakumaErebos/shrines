package net.nakumaerebos.shrines.entity;

import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.*;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.nakumaerebos.shrines.entity.ai.GuardianScoutIIAttackGoal;
import software.bernie.geckolib.animatable.GeoEntity;
import software.bernie.geckolib.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.animation.AnimatableManager;
import software.bernie.geckolib.animation.AnimationController;
import software.bernie.geckolib.animation.RawAnimation;
import software.bernie.geckolib.util.GeckoLibUtil;

public class GuardianScoutIIMobEntity extends PathfinderMob implements GeoEntity {
    private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);

    private static final EntityDataAccessor<Integer> FOLD_STATE = SynchedEntityData.defineId(GuardianScoutIIMobEntity.class, EntityDataSerializers.INT);
    public int foldTimer = 0;

    public GuardianScoutIIMobEntity(EntityType<? extends PathfinderMob> type, Level level) {
        super(type, level);
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(FOLD_STATE, 0);
    }

    public int getFoldState() {
        return this.entityData.get(FOLD_STATE);
    }

    public void setFoldState(int state) {
        this.entityData.set(FOLD_STATE, state);
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(0, new FloatGoal(this));
        this.goalSelector.addGoal(1, new GuardianScoutIIAttackGoal(this));
        this.goalSelector.addGoal(2, new LookAtPlayerGoal(this, Player.class, 12.0F));
        this.goalSelector.addGoal(3, new WaterAvoidingRandomStrollGoal(this, 1.0D));
        this.goalSelector.addGoal(4, new RandomLookAroundGoal(this));

        this.targetSelector.addGoal(1, new NearestAttackableTargetGoal<>(this, Player.class, true));
    }

    @Override
    public void customServerAiStep() {
        super.customServerAiStep();
        LivingEntity target = this.getTarget();
        int state = this.getFoldState();
        boolean hasValidTarget = target != null && target.isAlive() && this.getSensing().hasLineOfSight(target);

        if (hasValidTarget) {
            if (state == 0) {
                this.setFoldState(1);
                this.foldTimer = 30; // 1.5s
            } else if (state == 1) {
                this.foldTimer--;
                if (this.foldTimer <= 0) this.setFoldState(2);
            } else if (state == 3) {
                this.setFoldState(1);
                this.foldTimer = 30;
            }
        } else {
            if (state == 2) {
                this.setFoldState(3);
                this.foldTimer = 30; // 1.5s
            } else if (state == 3) {
                this.foldTimer--;
                if (this.foldTimer <= 0) {
                    this.setFoldState(0);
                    this.setHealth(this.getMaxHealth());
                }
            }
        }
    }

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
        controllers.add(new AnimationController<>(this, "controller", 2, state -> {
            int foldState = this.getFoldState();
            if (foldState == 0) {
                return state.setAndContinue(state.isMoving() ?
                        RawAnimation.begin().thenLoop("animation.guardianscout.walk_fold_in") :
                        RawAnimation.begin().thenLoop("animation.guardianscout.idle_fold_in"));
            } else if (foldState == 1) {
                return state.setAndContinue(RawAnimation.begin().thenPlayAndHold("animation.guardianscout.fold_out"));
            } else if (foldState == 3) {
                return state.setAndContinue(RawAnimation.begin().thenPlayAndHold("animation.guardianscout.fold_in"));
            }

            if (state.isMoving()) {
                return state.setAndContinue(RawAnimation.begin().thenLoop("animation.guardianscout.walk"));
            }
            return state.setAndContinue(RawAnimation.begin().thenLoop("animation.guardianscout.idle"));
        }).triggerableAnim("shoot", RawAnimation.begin().thenPlay("animation.guardianscout.shoot"))
                .triggerableAnim("sword_attack", RawAnimation.begin().thenPlay("animation.guardianscout.sword_attack")));
    }

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return this.cache;
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Mob.createMobAttributes()
                .add(Attributes.MAX_HEALTH, 40.0D)
                .add(Attributes.MOVEMENT_SPEED, 0.28D)
                .add(Attributes.ATTACK_DAMAGE, 6.0D)
                .add(Attributes.FOLLOW_RANGE, 35.0D);
    }
}