package net.nakumaerebos.shrines.item.custom;

import com.mojang.logging.LogUtils;
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
import net.nakumaerebos.shrines.event.StasisEffectHandler;
import net.nakumaerebos.shrines.sound.ModSounds; // Passe den Importpfad an, falls nötig
import org.slf4j.Logger;

public class SheikahSlateItemStasis extends Item {
    private static final Logger LOGGER = LogUtils.getLogger();

    public SheikahSlateItemStasis(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResult interactLivingEntity(ItemStack stack, Player player, LivingEntity target, InteractionHand hand) {
        if (player.level() instanceof ServerLevel serverLevel) {
            // Wenn das Entity bereits eingefroren ist -> ABBRECHEN
            if (target.hasData(ModAttachments.STASIS_TICKS) && target.getData(ModAttachments.STASIS_TICKS) > 0) {
                LOGGER.info("[FreezeWand] Breche Freeze vorzeitig ab für: {}", target.getType().getDescriptionId());
                target.setData(ModAttachments.STASIS_TICKS, 0);
                stopStasisTimerSound(serverLevel);
                return InteractionResult.SUCCESS;
            }

            // Ansonsten: Normal einfrieren (Kein Cooldown mehr!)
            LOGGER.info("[FreezeWand] Sende Freeze-Befehl an Handler für: {}", target.getType().getDescriptionId());
            StasisEffectHandler.applyFreeze(target, 180);
        }
        return InteractionResult.sidedSuccess(player.level().isClientSide());
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        Entity targetEntity = getTargetEntity(player, 5.0);

        if (targetEntity != null && level instanceof ServerLevel serverLevel) {
            // Wenn das Entity (z.B. Boot/Minecart) bereits eingefroren ist -> ABBRECHEN
            if (targetEntity.hasData(ModAttachments.STASIS_TICKS) && targetEntity.getData(ModAttachments.STASIS_TICKS) > 0) {
                LOGGER.info("[FreezeWand] Breche Freeze vorzeitig ab für Nicht-Living: {}", targetEntity.getType().getDescriptionId());
                targetEntity.setData(ModAttachments.STASIS_TICKS, 0);
                stopStasisTimerSound(serverLevel);
                return InteractionResultHolder.success(stack);
            }

            // Ansonsten: Normal einfrieren, falls es kein LivingEntity ist (da LivingEntity oben abgefangen wird)
            if (!(targetEntity instanceof LivingEntity)) {
                LOGGER.info("[FreezeWand] Sende Freeze-Befehl für Nicht-Living (z.B. Boot): {}", targetEntity.getType().getDescriptionId());
                StasisEffectHandler.applyFreeze(targetEntity, 180);
                return InteractionResultHolder.success(stack);
            }
        }

        return InteractionResultHolder.pass(stack);
    }

    /**
     * Stoppt den STASIS_TIMER Sound für alle Spieler in der Nähe auf dem Server.
     */
    private void stopStasisTimerSound(ServerLevel level) {
        ClientboundStopSoundPacket stopPacket = new ClientboundStopSoundPacket(ModSounds.STASIS_TIMER.get().getLocation(), SoundSource.NEUTRAL);
        for (ServerPlayer player : level.players()) {
            player.connection.send(stopPacket);
        }
    }

    private Entity getTargetEntity(Player player, double range) {
        Vec3 eyePosition = player.getEyePosition(1.0F);
        Vec3 lookVector = player.getViewVector(1.0F);
        Vec3 reachVector = eyePosition.add(lookVector.x * range, lookVector.y * range, lookVector.z * range);

        EntityHitResult hitResult = ProjectileUtil.getEntityHitResult(
                player.level(), player, eyePosition, reachVector,
                player.getBoundingBox().expandTowards(lookVector.scale(range)).inflate(1.0D),
                entity -> !entity.isSpectator() && entity.isPickable()
        );

        return hitResult != null ? hitResult.getEntity() : null;
    }
}