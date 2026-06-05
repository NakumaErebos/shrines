package net.nakumaerebos.shrines.item.custom;

import net.minecraft.network.protocol.game.ClientboundStopSoundPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.ProjectileUtil;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.Vec3;
import net.nakumaerebos.shrines.attachments.ModAttachments;
import net.nakumaerebos.shrines.datagen.ModEntityTypeTagProvider;
import net.nakumaerebos.shrines.event.ShortStasisEffectHandler;
import net.nakumaerebos.shrines.event.StasisEffectHandler;
import net.nakumaerebos.shrines.sound.ModSounds;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;

public class SheikahSlateItemStasis extends Item {

    public SheikahSlateItemStasis(Properties properties) {
        super(properties);
    }

    @Override
    public @NotNull InteractionResult interactLivingEntity(@NotNull ItemStack stack, Player player, @NotNull LivingEntity target, @NotNull InteractionHand hand) {
        if (player.level() instanceof ServerLevel serverLevel) {
            if (target.hasData(ModAttachments.STASIS_TICKS) && target.getData(ModAttachments.STASIS_TICKS) > 0) {
                target.setData(ModAttachments.STASIS_TICKS, 0);
                stopStasisTimerSound(serverLevel);
                return InteractionResult.SUCCESS;
            }
            if (target.hasData(ModAttachments.SHORT_STASIS_TICKS) && target.getData(ModAttachments.SHORT_STASIS_TICKS) > 0) {
                target.setData(ModAttachments.SHORT_STASIS_TICKS, 0);
                stopStasisTimerSound(serverLevel);
                return InteractionResult.SUCCESS;
            }

            if (hasActiveStasisTarget(serverLevel, player)) {
                return InteractionResult.FAIL;
            }

            // Nur einfrieren, wenn das Entity im Tag registriert ist
            if (target.getType().is(ModEntityTypeTagProvider.STASISALBE)) {
                StasisEffectHandler.applyFreeze(target, 180, player);
                return InteractionResult.SUCCESS;
            }
            if (target.getType().is(ModEntityTypeTagProvider.SHORT_STASISABLE)) {
                ShortStasisEffectHandler.applyFreeze(target, 100, player, false);
                return InteractionResult.SUCCESS;
            }
            if (target.getType().is(ModEntityTypeTagProvider.VERY_SHORT_STASISABLE)) {
                ShortStasisEffectHandler.applyFreeze(target, 40, player, true);
                return InteractionResult.SUCCESS;
            }
        }
        return InteractionResult.sidedSuccess(player.level().isClientSide());
    }

    @Override
    public @NotNull InteractionResultHolder<ItemStack> use(@NotNull Level level, Player player, @NotNull InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        Entity targetEntity = getTargetEntity(player, 5.0);

        if (targetEntity != null && level instanceof ServerLevel serverLevel) {
            // Wenn das Entity bereits eingefroren ist -> IMMER ABBRECHEN ERLAUBEN
            if (targetEntity.hasData(ModAttachments.STASIS_TICKS) && targetEntity.getData(ModAttachments.STASIS_TICKS) > 0) {
                targetEntity.setData(ModAttachments.STASIS_TICKS, 0);
                stopStasisTimerSound(serverLevel);
                return InteractionResultHolder.success(stack);
            }
            if (targetEntity.hasData(ModAttachments.SHORT_STASIS_TICKS) && targetEntity.getData(ModAttachments.SHORT_STASIS_TICKS) > 0) {
                targetEntity.setData(ModAttachments.SHORT_STASIS_TICKS, 0);
                stopStasisTimerSound(serverLevel);
                return InteractionResultHolder.success(stack);
            }

            // NEU: Checken, ob der Spieler bereits IRGENDEIN Entity in Stasis hat
            if (hasActiveStasisTarget(serverLevel, player)) {
                return InteractionResultHolder.fail(stack); // Blockiert das Einfrieren
            }

            // Nur einfrieren, wenn das Entity im Tag registriert ist
            if (targetEntity.getType().is(ModEntityTypeTagProvider.STASISALBE)) {
                if (!(targetEntity instanceof LivingEntity)) {
                    StasisEffectHandler.applyFreeze(targetEntity, 180, player);
                    return InteractionResultHolder.success(stack);
                }
            } else if (targetEntity.getType().is(ModEntityTypeTagProvider.SHORT_STASISABLE)) {
                if (!(targetEntity instanceof LivingEntity)) {
                    ShortStasisEffectHandler.applyFreeze(targetEntity, 100, player, false);
                    return InteractionResultHolder.success(stack);
                }
            } else if (targetEntity.getType().is(ModEntityTypeTagProvider.VERY_SHORT_STASISABLE)) {
                if (!(targetEntity instanceof LivingEntity)) {
                    ShortStasisEffectHandler.applyFreeze(targetEntity, 40, player, true);
                    return InteractionResultHolder.success(stack);
                }
            }
        }

        return InteractionResultHolder.pass(stack);
    }

    /**
     * NEU: Überprüft, ob dieser spezifische Spieler bereits ein Entity in der Welt eingefroren hat.
     */
    private boolean hasActiveStasisTarget(ServerLevel level, Player player) {
        UUID playerUUID = player.getUUID();

        for (Entity entity : level.getAllEntities()) {
            if (entity.hasData(ModAttachments.STASIS_CASTER)) {
                UUID casterUUID = entity.getData(ModAttachments.STASIS_CASTER);

                if (playerUUID.equals(casterUUID)) {
                    // Prüfen, ob die Stasis auf diesem Entity noch aktiv ist
                    boolean normalStasisActive = entity.hasData(ModAttachments.STASIS_TICKS) && entity.getData(ModAttachments.STASIS_TICKS) > 0;
                    boolean shortStasisActive = entity.hasData(ModAttachments.SHORT_STASIS_TICKS) && entity.getData(ModAttachments.SHORT_STASIS_TICKS) > 0;

                    if (normalStasisActive || shortStasisActive) {
                        return true; // Gefunden! Spieler hat bereits ein aktives Ziel.
                    }
                }
            }
        }
        return false;
    }

    private void stopStasisTimerSound(ServerLevel level) {
        ClientboundStopSoundPacket stopPacket = new ClientboundStopSoundPacket(ModSounds.STASIS_TIMER.get().getLocation(), SoundSource.NEUTRAL);
        ClientboundStopSoundPacket stopShortPacket = new ClientboundStopSoundPacket(ModSounds.STASIS_TIMER_SHORT.get().getLocation(), SoundSource.NEUTRAL);
        for (ServerPlayer player : level.players()) {
            player.connection.send(stopPacket);
            player.connection.send(stopShortPacket);
        }
    }

    private Entity getTargetEntity(Player player, double range) {
        Vec3 eyePosition = player.getEyePosition(1.0F);
        Vec3 lookVector = player.getViewVector(1.0F);
        Vec3 reachVector = eyePosition.add(lookVector.x * range, lookVector.y * range, lookVector.z * range);

        EntityHitResult hitResult = ProjectileUtil.getEntityHitResult(
                player.level(), player, eyePosition, reachVector,
                player.getBoundingBox().expandTowards(lookVector.scale(range)).inflate(1.0D),
                entity -> !entity.isSpectator() && (entity.isPickable() ||
                        (entity.hasData(ModAttachments.STASIS_TICKS) && entity.getData(ModAttachments.STASIS_TICKS) > 0) ||
                        (entity.hasData(ModAttachments.SHORT_STASIS_TICKS) && entity.getData(ModAttachments.SHORT_STASIS_TICKS) > 0)));
        return hitResult != null ? hitResult.getEntity() : null;
    }
}