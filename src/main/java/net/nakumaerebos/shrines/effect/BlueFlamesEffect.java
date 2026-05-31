package net.nakumaerebos.shrines.effect;

import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.nakumaerebos.shrines.util.ModTags;

public class BlueFlamesEffect extends MobEffect {

    public BlueFlamesEffect(MobEffectCategory category, int color) {
        super(category, color);
    }

    @Override
    public boolean applyEffectTick(LivingEntity entity, int amplifier) {
        if (entity instanceof Player player && !player.level().isClientSide()) {
            ServerLevel serverLevel = (ServerLevel) player.level();
            ServerPlayer serverPlayer = (ServerPlayer) player;

            // Wir holen uns die aktuelle Restdauer des Effekts auf dem Spieler
            int duration = 0;
            var effectInstance = player.getEffect(ModEffects.BLUE_FLAMES);
            if (effectInstance != null) {
                duration = effectInstance.getDuration();
            }

            // Schaden wird alle 20 Ticks (1 Sekunde) zugefügt
            boolean shouldDamage = (duration % 20 == 0);
            // Partikel spawnen alle 5 Ticks für einen flüssigeren Effekt
            boolean shouldSpawnParticles = (duration % 5 == 0);

            int damage = amplifier + 1;
            int validItemsHeld = 0;

            for (InteractionHand hand : InteractionHand.values()) {
                ItemStack stack = player.getItemInHand(hand);

                if (!stack.isEmpty() && stack.is(ModTags.Items.IS_BLUE_FLAME_INFLAMMATORY)) {
                    validItemsHeld++; // Item in der Hand gefunden!

                    if (shouldDamage) {
                        stack.hurtAndBreak(damage, serverPlayer, player.getEquipmentSlotForItem(stack));
                    }

                    if (shouldSpawnParticles) {
                        spawnToolFlameParticles(serverLevel, player, hand);
                    }
                }
            }

            // Sofortiges Löschen, wenn kein brennbares Item gehalten wird
            if (validItemsHeld == 0) {
                player.removeEffect(ModEffects.BLUE_FLAMES);
                return false;
            }
        }
        return true;
    }

    private void spawnToolFlameParticles(ServerLevel level, Player player, InteractionHand hand) {
        double x = player.getX();
        double y = player.getY() + player.getEyeHeight() - 0.25;
        double z = player.getZ();

        double bodyYaw = Math.toRadians(player.yBodyRot);

        double forwardOffset = 0.5;
        x += Math.sin(-bodyYaw) * forwardOffset;
        z += Math.cos(bodyYaw) * forwardOffset;

        boolean isMainHand = hand == InteractionHand.MAIN_HAND;
        boolean isRightHanded = player.getMainArm() == net.minecraft.world.entity.HumanoidArm.RIGHT;
        double sideOffset = (isMainHand == isRightHanded) ? 0.5 : -0.5;

        double sideYaw = bodyYaw + (Math.PI / 2.0);
        x += Math.sin(-sideYaw) * sideOffset;
        z += Math.cos(sideYaw) * sideOffset;

        level.sendParticles(ParticleTypes.SOUL_FIRE_FLAME, x, y, z, 6, 0.05, 0.05, 0.05, 0.02);
        level.sendParticles(ParticleTypes.SOUL_FIRE_FLAME, x, y, z, 4, 0.02, 0.02, 0.02, 0.01);
    }

    @Override
    public boolean shouldApplyEffectTickThisTick(int duration, int amplifier) {
        // WICHTIG: Muss jeden Tick true zurückgeben, damit die Item-Prüfung
        // sofort beim Rechtsklick reagiert und nicht fälschlicherweise verpufft!
        return true;
    }
}