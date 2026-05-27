package net.nakumaerebos.shrines.entity.ai;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.util.DefaultRandomPos;
import net.minecraft.world.phys.Vec3;
import net.nakumaerebos.shrines.entity.GuardianScoutIIMobEntity;
import net.nakumaerebos.shrines.entity.GuardianScoutProjectileEntity;

import java.util.EnumSet;

public class GuardianScoutIIAttackGoal extends Goal {
    private final GuardianScoutIIMobEntity mob;

    // --- STATE MACHINE ---
    private enum CombatState { APPROACH, MELEE_COMBO, EVADE, SHOOTING }
    private CombatState currentState = CombatState.APPROACH;

    // Aktions-Variablen
    private int actionDelay = 0;
    private int comboStrikesLeft = 0;
    private int shotsLeft = 0;
    private int evadeTimer = 0;
    private int evadeGracePeriod = 0; // Verhindert sofortigen Abbruch der Flucht

    // Animations-Blockaden
    private int damageDelayTimer = -1;
    private int animationFreezeTimer = -1;

    // Gesundheits-Tracker für Flucht-Mechanik
    private float lastHealth;

    public GuardianScoutIIAttackGoal(GuardianScoutIIMobEntity mob) {
        this.mob = mob;
        this.setFlags(EnumSet.of(Goal.Flag.MOVE, Goal.Flag.LOOK));
    }

    @Override
    public boolean canUse() {
        return this.mob.getTarget() != null && this.mob.getFoldState() == 2;
    }

    @Override
    public void start() {
        this.currentState = CombatState.APPROACH;
        this.actionDelay = 0;
        this.lastHealth = this.mob.getHealth(); // Leben beim Start merken
    }

    @Override
    public void tick() {
        LivingEntity target = this.mob.getTarget();
        if (target == null) return;

        // --- SCHADENS-ERKENNUNG (Sofortige Flucht) ---
        float currentHealth = this.mob.getHealth();
        if (currentHealth < this.lastHealth) {
            this.lastHealth = currentHealth;
            this.currentState = CombatState.EVADE;
            this.evadeTimer = 80;
            this.evadeGracePeriod = 15;
            this.comboStrikesLeft = 0;
            this.shotsLeft = 0;
            this.animationFreezeTimer = -1;
            this.damageDelayTimer = -1;

            Vec3 fleeVec = DefaultRandomPos.getPosAway(this.mob, 16, 8, target.position());
            if (fleeVec != null) {
                this.mob.getNavigation().moveTo(fleeVec.x, fleeVec.y, fleeVec.z, 1.6D);
            }
            return;
        }

        // --- 1. ABSOLUTE PRIORITÄT: ANIMATIONS-FREEZE ---
        if (this.animationFreezeTimer > 0) {
            this.animationFreezeTimer--;
            this.mob.getNavigation().stop();

            if (this.damageDelayTimer > 0) {
                this.damageDelayTimer--;
                if (this.damageDelayTimer == 0) {
                    applySweepingDamage(target);
                    this.damageDelayTimer = -1;
                }
            }
            return; // Blockiert den Rest der KI, solange die Animation läuft
        }

        // Blick immer auf den Spieler richten
        this.mob.getLookControl().setLookAt(target, 30.0F, 30.0F);

        // Universeller Cooldown (wird jetzt korrekt nach dem Freeze verarbeitet)
        if (this.actionDelay > 0) {
            this.actionDelay--;
            return;
        }

        double distSq = this.mob.distanceToSqr(target);

        // --- 2. STATE MACHINE LOGIK ---
        switch (this.currentState) {

            case APPROACH:
                if (distSq > 256.0D) { // > 16 Blöcke
                    this.mob.getNavigation().stop();
                    this.shotsLeft = 3;
                    this.currentState = CombatState.SHOOTING;
                    break;
                }

                if (distSq < 8.0D) { // Nah genug für Combo
                    this.mob.getNavigation().stop();
                    this.comboStrikesLeft = 1 + this.mob.getRandom().nextInt(3);
                    this.currentState = CombatState.MELEE_COMBO;
                } else {
                    this.mob.getNavigation().moveTo(target, 1.4D);
                }
                break;

            case MELEE_COMBO:
                // Wenn der Spieler während der Combo wegrennt, bricht der Mob NICHT mehr sofort ab,
                // sondern zieht seine Schläge ins Leere durch (wichtig für deine Änderung!)
                if (this.comboStrikesLeft > 0) {
                    startMeleeAnimation();
                    this.comboStrikesLeft--;

                    // WICHTIG: Der Cooldown MUSS länger sein als der animationFreezeTimer (15 Ticks),
                    // sonst überschneiden sich die Angriffe und die State-Machine glitched.
                    this.actionDelay = 18;
                } else {
                    // Combo fertig -> Flucht einleiten
                    this.currentState = CombatState.EVADE;
                    this.evadeTimer = 60;
                    Vec3 fleeVec = DefaultRandomPos.getPosAway(this.mob, 15, 7, target.position());
                    if (fleeVec != null) {
                        this.mob.getNavigation().moveTo(fleeVec.x, fleeVec.y, fleeVec.z, 1.5D);
                    } else {
                        this.evadeTimer = 0;
                    }
                }
                break;

            case EVADE:
                this.evadeTimer--;
                if (this.evadeGracePeriod > 0) this.evadeGracePeriod--;

                boolean amZiel = this.mob.getNavigation().isDone();
                boolean zuWeitWeg = distSq >= 225.0D;

                if (this.evadeGracePeriod <= 0) {
                    if (zuWeitWeg || amZiel || this.evadeTimer <= 0) {
                        this.mob.getNavigation().stop();
                        this.shotsLeft = 3;
                        this.currentState = CombatState.SHOOTING;
                    }
                }
                break;

            case SHOOTING:
                this.mob.getNavigation().stop();
                if (this.shotsLeft > 0) {
                    fireProjectile(target);
                    this.shotsLeft--;
                    this.actionDelay = 8;
                } else {
                    this.currentState = CombatState.APPROACH;
                    this.actionDelay = 10;
                }
                break;
        }
    }
    private void startMeleeAnimation() {
        this.mob.triggerAnim("controller", "sword_attack");
        this.damageDelayTimer = 5;
        this.animationFreezeTimer = 15;
    }

    private void applySweepingDamage(LivingEntity target) {
        // Richtungsvektor zum Ziel
        Vec3 dirToTarget = target.position().subtract(this.mob.position()).normalize();

        // Reichweite der Box vergrößert (inflate auf 4.5D für mehr "Wumms")
        // Die Box ist nun 9 Blöcke breit (4.5 in jede Richtung vom Zentrum)
        for (LivingEntity entity : this.mob.level().getEntitiesOfClass(LivingEntity.class,
                this.mob.getBoundingBox().inflate(2.0D, 1.5D, 2.0D))) {

            if (entity != this.mob && !this.mob.isAlliedTo(entity)) {
                Vec3 toEntity = entity.position().subtract(this.mob.position()).normalize();

                // Der Trefferkegel (Dot-Product):
                // 0.3D ist ein recht breiter Winkel (ca. 145 Grad vor dem Mob)
                if (toEntity.dot(dirToTarget) > 0.3D) {
                    entity.hurt(this.mob.damageSources().mobAttack(this.mob), 8.0F);
                    // Rückstoß weg vom Guardian
                    entity.knockback(0.6D, -dirToTarget.x, -dirToTarget.z);
                }
            }
        }

        // Partikel-Logik entfernt, nur der Sound bleibt für das Treffer-Feedback
        if (this.mob.level() instanceof ServerLevel) {
            this.mob.playSound(SoundEvents.PLAYER_ATTACK_SWEEP, 1.0F, 0.7F);
            // Falls du den Partikel später mal brauchst, einfach hier wieder einfügen.
        }
    }

    private void fireProjectile(LivingEntity target) {
        this.mob.triggerAnim("controller", "shoot");
        GuardianScoutProjectileEntity projectile = new GuardianScoutProjectileEntity(this.mob.level(), this.mob);
        projectile.setPos(this.mob.getX(), this.mob.getY() + 1.2D, this.mob.getZ());
        Vec3 dir = target.getEyePosition().subtract(projectile.position());
        projectile.shoot(dir.x, dir.y, dir.z, 1.6F, 1.0F);
        this.mob.level().addFreshEntity(projectile);
    }
}