package net.nakumaerebos.shrines.event;

import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.protocol.game.ClientboundStopSoundPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;
import net.nakumaerebos.shrines.Shrines;
import net.nakumaerebos.shrines.attachments.ModAttachments;
import net.nakumaerebos.shrines.entity.ModEntities;
import net.nakumaerebos.shrines.entity.StasisEffectEntity;
import net.nakumaerebos.shrines.item.custom.SheikahSlateItemStasis;
import net.nakumaerebos.shrines.sound.ModSounds;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.LivingIncomingDamageEvent;
import net.neoforged.neoforge.event.entity.player.AttackEntityEvent;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;
import net.neoforged.neoforge.event.tick.EntityTickEvent;

import java.util.Collections;

@EventBusSubscriber(modid = Shrines.MOD_ID)
public class ShortStasisEffectHandler {

    @SubscribeEvent
    public static void onEntityTick(EntityTickEvent.Pre event) {
        Entity entity = event.getEntity();

        if (!entity.hasData(ModAttachments.SHORT_STASIS_TICKS)) return;
        int freezeTicks = entity.getData(ModAttachments.SHORT_STASIS_TICKS);
        if (freezeTicks < 0) return;

        if (freezeTicks > 0) {
            if (!entity.level().isClientSide) {
                if (entity.hasData(ModAttachments.STASIS_X) && entity.hasData(ModAttachments.STASIS_Y) && entity.hasData(ModAttachments.STASIS_Z)) {
                    double targetX = entity.getData(ModAttachments.STASIS_X);
                    double targetY = entity.getData(ModAttachments.STASIS_Y);
                    double targetZ = entity.getData(ModAttachments.STASIS_Z);

                    if (entity instanceof ServerPlayer serverPlayer) {
                        serverPlayer.connection.teleport(targetX, targetY, targetZ, entity.getYRot(), entity.getXRot(), Collections.emptySet());
                    } else {
                        entity.teleportTo(targetX, targetY, targetZ);
                    }
                }
                entity.setDeltaMovement(Vec3.ZERO);
            }

            entity.setYRot(entity.yRotO);
            entity.setXRot(entity.xRotO);

            if (entity instanceof LivingEntity living) {
                living.setYHeadRot(living.yHeadRotO);
                living.yBodyRot = living.yBodyRotO;
            }

            entity.fallDistance = 0;

            if (entity instanceof Mob mob && !mob.isNoAi()) {
                mob.setNoAi(true);
            }

            if (entity.level() instanceof ServerLevel serverLevel) {
                entity.setData(ModAttachments.SHORT_STASIS_TICKS, freezeTicks - 1);
            }

            if (entity.level().isClientSide && entity.tickCount % 5 == 0) {
                entity.level().addParticle(
                        ParticleTypes.INSTANT_EFFECT,
                        entity.getRandomX(0.5), entity.getRandomY(), entity.getRandomZ(0.5),
                        0, 0, 0
                );
            }
        } else {
            // Effekt beenden (Verhindert Loop)
            entity.setData(ModAttachments.SHORT_STASIS_TICKS, -1);

            // Schaden auslesen (Fallback auf 0.0F falls unbeschädigt)
            float totalDamage = entity.hasData(ModAttachments.ACCUMULATED_DAMAGE) ? entity.getData(ModAttachments.ACCUMULATED_DAMAGE) : 0.0F;

            if (!entity.level().isClientSide) {
                entity.setGlowingTag(false);
                net.minecraft.world.scores.Scoreboard scoreboard = entity.level().getScoreboard();
                net.minecraft.world.scores.PlayerTeam team = scoreboard.getPlayerTeam("stasis_yellow");
                if (team != null && scoreboard.getPlayersTeam(entity.getScoreboardName()) == team) {
                    scoreboard.removePlayerFromTeam(entity.getScoreboardName(), team);
                }

                if (entity.level() instanceof ServerLevel serverLevel) {
                    ClientboundStopSoundPacket stopPacket = new ClientboundStopSoundPacket(ModSounds.STASIS_TIMER_SHORT.get().getLocation(), SoundSource.NEUTRAL);
                    for (ServerPlayer player : serverLevel.players()) {
                        player.connection.send(stopPacket);
                    }

                    // Schadensabhängige End-Sounds
                    if (totalDamage <= 0.0F) {
                        serverLevel.playSound(null, entity.getX(), entity.getY(), entity.getZ(),
                                ModSounds.STASIS_END_1.get(), SoundSource.NEUTRAL, 1.0F, 1.0F);
                    } else if (totalDamage <= 20.0F) {
                        serverLevel.playSound(null, entity.getX(), entity.getY(), entity.getZ(),
                                ModSounds.STASIS_END_2.get(), SoundSource.NEUTRAL, 1.0F, 1.0F);
                    } else {
                        serverLevel.playSound(null, entity.getX(), entity.getY(), entity.getZ(),
                                ModSounds.STASIS_END_3.get(), SoundSource.NEUTRAL, 1.0F, 1.0F);
                    }
                }
            }

            if (totalDamage > 0.0F) {
                if (!entity.level().isClientSide) {
                    entity.hurt(entity.damageSources().generic(), totalDamage);
                }
            }

            // Sicheres Zurücksetzen des Schadenswertes
            entity.setData(ModAttachments.ACCUMULATED_DAMAGE, 0.0F);

            if (entity instanceof LivingEntity living) {
                if (living instanceof Mob mob && mob.isNoAi()) {
                    mob.setNoAi(false);
                }
                if (living.isSilent()) {
                    living.setSilent(false);
                }
            }
        }
    }

    @SubscribeEvent
    public static void onAttackEntity(AttackEntityEvent event) {
        Entity target = event.getTarget();

        if (target instanceof LivingEntity) return;

        if (target.hasData(ModAttachments.SHORT_STASIS_TICKS) && target.getData(ModAttachments.SHORT_STASIS_TICKS) > 0) {
            float currentAccumulated = target.hasData(ModAttachments.ACCUMULATED_DAMAGE) ? target.getData(ModAttachments.ACCUMULATED_DAMAGE) : 0.0F;
            target.setData(ModAttachments.ACCUMULATED_DAMAGE, currentAccumulated + 1.0F);

            if (target.level() instanceof ServerLevel serverLevel) {
                serverLevel.sendParticles(ParticleTypes.CRIT, target.getX(), target.getY() + 1, target.getZ(), 5, 0.2, 0.2, 0.2, 0.1);

                // Sound mit statischer Pitch abspielen
                serverLevel.playSound(null, target.getX(), target.getY(), target.getZ(),
                        ModSounds.STASIS_HIT.get(), SoundSource.NEUTRAL, 0.4F, 1.0F);
            }

            event.setCanceled(true);
        }
    }

    @SubscribeEvent
    public static void onIncomingDamage(LivingIncomingDamageEvent event) {
        LivingEntity target = event.getEntity();

        if (target.hasData(ModAttachments.SHORT_STASIS_TICKS) && target.getData(ModAttachments.SHORT_STASIS_TICKS) > 0) {
            float incomingDamage = event.getAmount();
            float currentAccumulated = target.hasData(ModAttachments.ACCUMULATED_DAMAGE) ? target.getData(ModAttachments.ACCUMULATED_DAMAGE) : 0.0F;
            target.setData(ModAttachments.ACCUMULATED_DAMAGE, currentAccumulated + incomingDamage);

            if (target.level() instanceof ServerLevel serverLevel) {
                serverLevel.sendParticles(ParticleTypes.CRIT, target.getX(), target.getY() + 1, target.getZ(), 5, 0.2, 0.2, 0.2, 0.1);

                // Sound mit statischer Pitch abspielen
                serverLevel.playSound(null, target.getX(), target.getY(), target.getZ(),
                        ModSounds.STASIS_HIT.get(), SoundSource.NEUTRAL, 0.6F, 1.0F);
            }

            event.setCanceled(true);
        }
    }

    @SubscribeEvent
    public static void onEntityInteract(PlayerInteractEvent.EntityInteract event) {
        Entity target = event.getTarget();

        if (target.hasData(ModAttachments.SHORT_STASIS_TICKS) && target.getData(ModAttachments.SHORT_STASIS_TICKS) > 0) {
            if (event.getItemStack().getItem() instanceof SheikahSlateItemStasis) {
                return;
            }
            event.setCanceled(true);
        }
    }

    public static void applyFreeze(Entity target, int durationInTicks, Player player, boolean veryShort) {
        target.setData(ModAttachments.ACCUMULATED_DAMAGE, 0.0F);
        target.setData(ModAttachments.SHORT_STASIS_TICKS, durationInTicks);
        target.setData(ModAttachments.STASIS_CASTER, player.getUUID());

        target.setData(ModAttachments.STASIS_X, target.getX());
        target.setData(ModAttachments.STASIS_Y, target.getY());
        target.setData(ModAttachments.STASIS_Z, target.getZ());

        target.ejectPassengers();

        target.playSound(ModSounds.STASIS_START.get(), 1.0F, 1.0F);
        if (veryShort){
            target.playSound(ModSounds.STASIS_TIMER_VERY_SHORT.get(), 1.0F, 1.0F);
        }else{
            target.playSound(ModSounds.STASIS_TIMER_SHORT.get(), 1.0F, 1.0F);
        }

        if (target instanceof LivingEntity living) {
            living.setSilent(true);
        }

        if (!target.level().isClientSide && target.level() instanceof ServerLevel serverLevel) {
            StasisEffectEntity effect = ModEntities.STASIS_EFFECT.get().create(serverLevel);
            if (effect != null) {
                effect.setTarget(target);

                effect.setGlowingTag(true);
                target.setGlowingTag(true);

                net.minecraft.world.scores.Scoreboard scoreboard = serverLevel.getScoreboard();
                String teamName = "stasis_yellow";
                net.minecraft.world.scores.PlayerTeam team = scoreboard.getPlayerTeam(teamName);

                if (team == null) {
                    team = scoreboard.addPlayerTeam(teamName);
                    team.setColor(net.minecraft.ChatFormatting.YELLOW);
                }

                scoreboard.addPlayerToTeam(effect.getScoreboardName(), team);
                scoreboard.addPlayerToTeam(target.getScoreboardName(), team);

                serverLevel.addFreshEntity(effect);

                double targetCenterY = target.getY() + (target.getBbHeight() * 0.5D);
                effect.moveTo(target.getX(), targetCenterY - 1.0D, target.getZ(), target.getYRot(), target.getXRot());
            }
        }
    }
}