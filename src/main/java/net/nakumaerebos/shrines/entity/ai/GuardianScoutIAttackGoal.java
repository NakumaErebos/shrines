package net.nakumaerebos.shrines.entity.ai;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.util.DefaultRandomPos;
import net.minecraft.world.phys.Vec3;
import net.nakumaerebos.shrines.entity.GuardianScoutIMobEntity;
import net.nakumaerebos.shrines.entity.GuardianScoutProjectileEntity;
import java.util.EnumSet;

public class GuardianScoutIAttackGoal extends Goal {
    private final GuardianScoutIMobEntity mob;
    private int attackCooldown = 40;
    private int shotsFired = 0;
    private int burstTimer = 0;

    public GuardianScoutIAttackGoal(GuardianScoutIMobEntity mob) {
        this.mob = mob;
        this.setFlags(EnumSet.of(Goal.Flag.MOVE, Goal.Flag.LOOK));
    }

    @Override
    public boolean canUse() {
        return this.mob.getTarget() != null;
    }

    @Override
    public void tick() {
        LivingEntity target = this.mob.getTarget();
        if (target == null) return;

        double distSq = this.mob.distanceToSqr(target);
        boolean canSee = this.mob.getSensing().hasLineOfSight(target);
        boolean isFiring = (this.shotsFired > 0);

        // --- LOGIK: WEGLAUFEN (KITING) ---
        if (distSq < 100.0D && !isFiring) {
            Vec3 fleeVec = DefaultRandomPos.getPosAway(this.mob, 16, 7, target.position());
            if (fleeVec != null) {
                this.mob.getNavigation().moveTo(fleeVec.x, fleeVec.y, fleeVec.z, 1.2D);
            }
        }
        else if (distSq > 225.0D) {
            this.mob.getNavigation().moveTo(target, 1.0D);
        } else {
            this.mob.getNavigation().stop();
        }

        this.mob.getLookControl().setLookAt(target, 30.0F, 30.0F);

        // --- ANGRIFFS-LOGIK ---
        if (canSee) {
            if (this.shotsFired < 3) {
                if (this.attackCooldown <= 0) {
                    if (this.burstTimer <= 0) {
                        this.fireProjectile(target);
                        this.shotsFired++;
                        this.burstTimer = 10; // 0.5s Intervall
                    } else {
                        this.burstTimer--;
                    }
                } else {
                    this.attackCooldown--;
                }
            } else {
                this.shotsFired = 0;
                this.attackCooldown = 50;
            }
        }
    }

    private void fireProjectile(LivingEntity target) {
        // Trigger die Animation im Controller
        this.mob.triggerAnim("controller", "shoot_tick");

        GuardianScoutProjectileEntity projectile = new GuardianScoutProjectileEntity(this.mob.level(), this.mob);
        projectile.setPos(this.mob.getX(), this.mob.getY() + 0.5D, this.mob.getZ());

        double d0 = target.getX() - this.mob.getX();
        double d1 = target.getY(0.5D) - projectile.getY();
        double d2 = target.getZ() - this.mob.getZ();

        projectile.shoot(d0, d1, d2, 1.6F, 1.0F);
        this.mob.level().addFreshEntity(projectile);
    }
}