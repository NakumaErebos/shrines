package net.nakumaerebos.shrines.entity; // Passe das Paket ggf. an net.nakumaerebos.shrines.entity an

import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.Level;
import net.nakumaerebos.shrines.attachments.ModAttachments;
import software.bernie.geckolib.animatable.GeoEntity;
import software.bernie.geckolib.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.animation.*;
import software.bernie.geckolib.util.GeckoLibUtil;

import java.util.UUID;

public class StasisEffectEntity extends Entity implements GeoEntity {

    // Cache für die GeckoLib-Animationen
    private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);

    // Referenzen auf das eingefrorene Ziel
    private UUID targetUUID;
    private Entity cachedTarget;

    // Status, ob wir uns gerade in der Zerspring-Phase befinden
    private boolean isUnfreezing = false;
    private int unfreezeTimer = 0;
    private static final int UNFREEZE_DURATION_TICKS = 20; // 1 Sekunde für die Unfreeze-Animation (anpassen falls nötig!)

    public StasisEffectEntity(EntityType<? extends Entity> entityType, Level level) {
        super(entityType, level);
        this.noPhysics = true; // Keine Kollision

        // NEU: Entity leuchtet standardmäßig ab dem ersten Tick
        this.setGlowingTag(true);
    }

    /**
     * Verknüpft den Effekt mit dem Opfer
     */
    public void setTarget(Entity target) {
        this.cachedTarget = target;
        this.targetUUID = target.getUUID();
    }

    @Override
    public void tick() {
        super.tick();

        // 1. Ziel auf dem Server auflösen, falls die UUID geladen wurde
        if (this.cachedTarget == null && this.targetUUID != null && !this.level().isClientSide) {
            this.cachedTarget = ((net.minecraft.server.level.ServerLevel) this.level()).getEntity(this.targetUUID);
        }

        // 2. Sicherheitscheck: Wenn das Ziel stirbt oder gelöscht wird, verschwindet auch der Effekt
        if (this.cachedTarget == null || !this.cachedTarget.isAlive()) {
            if (!this.level().isClientSide && !this.isUnfreezing) {
                this.discard();
            }
            return;
        }

        // 3. POSITION ANPASSEN (FIX FÜR DIE MITTE)
        // cachedTarget.getY(0.5) berechnet automatisch: Boden-Y + (Höhe der Entity * 0.5)
        // Wenn dein Blockbench-Modell seinen Dreh- und Angelpunkt (Pivot Point) auf 0,0,0 im Center hat,
        // musst du die eigene Höhe der Stasis-Entity abziehen (this.getBbHeight() * 0.5F),
        // damit die Mitten der beiden Entities perfekt aufeinanderliegen.
        double targetCenterY = this.cachedTarget.getY(0.5) - (this.getBbHeight() * 0.5F);

        this.setPos(this.cachedTarget.getX(), targetCenterY, this.cachedTarget.getZ());
        this.setYRot(this.cachedTarget.getYRot());
        this.setXRot(this.cachedTarget.getXRot());

        // 4. Logik für das Umschalten der Animationen
        if (!this.level().isClientSide) {
            if (this.cachedTarget.hasData(ModAttachments.FREEZE_TICKS)) {
                int remainingTicks = this.cachedTarget.getData(ModAttachments.FREEZE_TICKS);

                // Wenn der Freeze vorbei ist, leiten wir das Unfreezing ein
                if (remainingTicks <= 0 && !this.isUnfreezing) {
                    this.isUnfreezing = true;
                    this.unfreezeTimer = UNFREEZE_DURATION_TICKS;
                    // Erzwinge ein Netzwerk-Update, damit der Client bescheid weiß
                    this.level().broadcastEntityEvent(this, (byte) 60);
                }
            }

            // Wenn wir in der Unfreeze-Animation sind, ticken wir den Zähler runter
            if (this.isUnfreezing) {
                this.unfreezeTimer--;
                if (this.unfreezeTimer <= 0) {
                    this.discard(); // Effekt komplett löschen, wenn die Animation durch ist
                }
            }
        }
    }

    /**
     * Empfängt Netzwerk-Events vom Server, um den Animationswechsel auf dem Client zu triggern
     */
    @Override
    public void handleEntityEvent(byte id) {
        if (id == 60) {
            this.isUnfreezing = true;
        } else {
            super.handleEntityEvent(id);
        }
    }

    // --- GECKOLIB ANIMATIONS-STEUERUNG ---

    private static final RawAnimation FREEZE_ANIM = RawAnimation.begin().thenPlayAndHold("animation.stasis_effect.freeze");
    private static final RawAnimation UNFREEZE_ANIM = RawAnimation.begin().thenPlay("animation.stasis_effect.unfreeze");

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
        controllers.add(new AnimationController<>(this, "stasis_controller", 5, event -> {
            if (this.isUnfreezing) {
                return event.setAndContinue(UNFREEZE_ANIM);
            }
            return event.setAndContinue(FREEZE_ANIM);
        }));
    }

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return this.cache;
    }

    /**
     * Wird aufgerufen, wenn die Entity aus der Welt entfernt wird (z.B. durch discard() oder /kill).
     * Perfekter Ort, um den gelben Leuchteffekt beim Ziel aufzuräumen.
     */
    @Override
    public void remove(Entity.RemovalReason reason) {
        // Wenn wir auf dem Server sind, entfernen wir die Effekte vom Ziel
        if (!this.level().isClientSide && this.level() instanceof net.minecraft.server.level.ServerLevel serverLevel) {
            Entity target = this.cachedTarget;

            // Falls cachedTarget noch nicht aufgelöst wurde, versuchen wir es über die UUID
            if (target == null && this.targetUUID != null) {
                target = serverLevel.getEntity(this.targetUUID);
            }

            if (target != null) {
                // 1. Glowing beim Ziel ausschalten
                target.setGlowingTag(false);

                // 2. Ziel aus dem stasis_yellow Team entfernen
                net.minecraft.world.scores.Scoreboard scoreboard = serverLevel.getScoreboard();
                net.minecraft.world.scores.PlayerTeam team = scoreboard.getPlayerTeam("stasis_yellow");

                if (team != null && scoreboard.getPlayersTeam(target.getScoreboardName()) == team) {
                    scoreboard.removePlayerFromTeam(target.getScoreboardName(), team);
                }
            }
        }

        // Ruft die originale Vanilla-Logik für das Entfernen auf
        super.remove(reason);
    }

    // --- NBT-DATA (Wichtig für Server-Restarts) ---

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        // Keine synchronisierten Vanilla-DataWatcher zwingend nötig
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