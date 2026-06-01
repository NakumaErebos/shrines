package net.nakumaerebos.shrines.event;

import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.protocol.game.ClientboundStopSoundPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.phys.Vec3;
import net.nakumaerebos.shrines.Shrines;
import net.nakumaerebos.shrines.attachments.ModAttachments;
import net.nakumaerebos.shrines.entity.ModEntities;
import net.nakumaerebos.shrines.entity.StasisArrowEffectEntity;
import net.nakumaerebos.shrines.entity.StasisEffectEntity;
import net.nakumaerebos.shrines.item.custom.FreezeWandItem; // Import für dein Item
import net.nakumaerebos.shrines.sound.ModSounds;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.LivingIncomingDamageEvent;
import net.neoforged.neoforge.event.entity.player.AttackEntityEvent;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;
import net.neoforged.neoforge.event.tick.EntityTickEvent;

@EventBusSubscriber(modid = Shrines.MOD_ID)
public class FreezeEffectHandler {

    @SubscribeEvent
    public static void onEntityTick(EntityTickEvent.Pre event) {
        Entity entity = event.getEntity();

        if (!entity.hasData(ModAttachments.FREEZE_TICKS)) return;

        int freezeTicks = entity.getData(ModAttachments.FREEZE_TICKS);

        if (freezeTicks > 0) {
            if (!entity.level().isClientSide) {
                entity.setPos(entity.xo, entity.yo, entity.zo);
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

            if (entity.level() instanceof ServerLevel) {
                entity.setData(ModAttachments.FREEZE_TICKS, freezeTicks - 1);
            }

            if (entity.level().isClientSide && entity.tickCount % 5 == 0) {
                entity.level().addParticle(
                        ParticleTypes.INSTANT_EFFECT,
                        entity.getRandomX(0.5), entity.getRandomY(), entity.getRandomZ(0.5),
                        0, 0, 0
                );
            }
        } else {
            if (entity.hasData(ModAttachments.ACCUMULATED_DAMAGE)) {
                float totalDamage = entity.getData(ModAttachments.ACCUMULATED_DAMAGE);

                if (!entity.level().isClientSide) {
                    entity.setGlowingTag(false);
                    net.minecraft.world.scores.Scoreboard scoreboard = entity.level().getScoreboard();
                    net.minecraft.world.scores.PlayerTeam team = scoreboard.getPlayerTeam("stasis_yellow");
                    if (team != null && scoreboard.getPlayersTeam(entity.getScoreboardName()) == team) {
                        scoreboard.removePlayerFromTeam(entity.getScoreboardName(), team);
                    }

                    if (entity.level() instanceof ServerLevel serverLevel) {
                        ClientboundStopSoundPacket stopPacket = new ClientboundStopSoundPacket(ModSounds.STASIS_TIMER.get().getLocation(), SoundSource.NEUTRAL);
                        for (ServerPlayer player : serverLevel.players()) {
                            player.connection.send(stopPacket);
                        }
                    }
                }

                if (totalDamage > 0.0F) {
                    if (!entity.level().isClientSide) {
                        entity.hurt(entity.damageSources().generic(), totalDamage);

                        entity.level().playSound(null, entity.getX(), entity.getY(), entity.getZ(),
                                ModSounds.STASIS_END.get(), SoundSource.NEUTRAL, 1.0F, 1.2F);

                        if (entity.hasData(ModAttachments.ACCUMULATED_KNOCKBACK) && entity.hasData(ModAttachments.LAST_ATTACK_YAW)) {
                            float kbStrength = entity.getData(ModAttachments.ACCUMULATED_KNOCKBACK);
                            float attackYaw = entity.getData(ModAttachments.LAST_ATTACK_YAW);

                            if (kbStrength > 0.0F) {
                                double motionX = -Mth.sin(attackYaw * (float)Math.PI / 180.0F);
                                double motionZ = Mth.cos(attackYaw * (float)Math.PI / 180.0F);

                                Vec3 lookVector = new Vec3(motionX, 0, motionZ).normalize().scale(kbStrength);
                                double upWardForce = (entity instanceof LivingEntity) ? (0.4F + (kbStrength * 0.1F)) : 0.2F;

                                entity.setDeltaMovement(new Vec3(lookVector.x, upWardForce, lookVector.z));
                                entity.hasImpulse = true;
                            }
                        }
                    }

                    entity.setData(ModAttachments.ACCUMULATED_DAMAGE, 0.0F);
                    entity.setData(ModAttachments.ACCUMULATED_KNOCKBACK, 0.0F);
                    entity.setData(ModAttachments.LAST_ATTACK_YAW, 0.0F);
                }
            }

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
        Entity attacker = event.getEntity();

        if (target instanceof LivingEntity) return;

        if (target.hasData(ModAttachments.FREEZE_TICKS) && target.getData(ModAttachments.FREEZE_TICKS) > 0) {
            float currentAccumulated = target.hasData(ModAttachments.ACCUMULATED_DAMAGE) ? target.getData(ModAttachments.ACCUMULATED_DAMAGE) : 0.0F;
            target.setData(ModAttachments.ACCUMULATED_DAMAGE, currentAccumulated + 1.0F);

            if (attacker != null) {
                target.setData(ModAttachments.LAST_ATTACK_YAW, attacker.getYRot());

                float additionalKb = 0.4F;
                if (attacker instanceof LivingEntity livingAttacker) {
                    if (livingAttacker.isSprinting()) {
                        additionalKb += 0.3F;
                    }

                    if (target.level() instanceof ServerLevel serverLevel) {
                        float enchantmentKnockbackBonus = net.minecraft.world.item.enchantment.EnchantmentHelper.modifyKnockback(
                                serverLevel, livingAttacker.getMainHandItem(), target, target.damageSources().playerAttack(event.getEntity()), 0.0F
                        );
                        additionalKb += enchantmentKnockbackBonus;
                    }
                }

                float currentKb = target.hasData(ModAttachments.ACCUMULATED_KNOCKBACK) ? target.getData(ModAttachments.ACCUMULATED_KNOCKBACK) : 0.0F;
                target.setData(ModAttachments.ACCUMULATED_KNOCKBACK, currentKb + additionalKb);
            }

            if (target.level() instanceof ServerLevel serverLevel) {
                serverLevel.sendParticles(ParticleTypes.CRIT, target.getX(), target.getY() + 1, target.getZ(), 5, 0.2, 0.2, 0.2, 0.1);

                float currentKb = target.hasData(ModAttachments.ACCUMULATED_KNOCKBACK) ? target.getData(ModAttachments.ACCUMULATED_KNOCKBACK) : 0.0F;
                float dynamicPitch = 0.5F + (currentKb * 0.3F);
                if (dynamicPitch > 2.0F) dynamicPitch = 2.0F;

                target.level().playSound(null, target.getX(), target.getY(), target.getZ(),
                        ModSounds.STASIS_HIT.get(), SoundSource.NEUTRAL, 0.4F, dynamicPitch);
            }

            event.setCanceled(true);
        }
    }

    @SubscribeEvent
    public static void onIncomingDamage(LivingIncomingDamageEvent event) {
        LivingEntity target = event.getEntity();

        if (target.hasData(ModAttachments.FREEZE_TICKS) && target.getData(ModAttachments.FREEZE_TICKS) > 0) {
            float incomingDamage = event.getAmount();
            float currentAccumulated = target.hasData(ModAttachments.ACCUMULATED_DAMAGE) ? target.getData(ModAttachments.ACCUMULATED_DAMAGE) : 0.0F;
            target.setData(ModAttachments.ACCUMULATED_DAMAGE, currentAccumulated + incomingDamage);

            Entity attacker = event.getSource().getEntity();
            if (attacker != null) {
                target.setData(ModAttachments.LAST_ATTACK_YAW, attacker.getYRot());
                float additionalKb = 0.4F;

                if (attacker instanceof LivingEntity livingAttacker && target.level() instanceof ServerLevel serverLevel) {
                    if (livingAttacker.isSprinting()) {
                        additionalKb += 0.3F;
                    }

                    float enchantmentKnockbackBonus = net.minecraft.world.item.enchantment.EnchantmentHelper.modifyKnockback(
                            serverLevel, livingAttacker.getMainHandItem(), target, event.getSource(), 0.0F
                    );
                    additionalKb += enchantmentKnockbackBonus;
                }

                float currentKb = target.hasData(ModAttachments.ACCUMULATED_KNOCKBACK) ? target.getData(ModAttachments.ACCUMULATED_KNOCKBACK) : 0.0F;
                target.setData(ModAttachments.ACCUMULATED_KNOCKBACK, currentKb + additionalKb);
            }

            if (target.level() instanceof ServerLevel serverLevel) {
                serverLevel.sendParticles(ParticleTypes.CRIT, target.getX(), target.getY() + 1, target.getZ(), 5, 0.2, 0.2, 0.2, 0.1);

                float currentKb = target.hasData(ModAttachments.ACCUMULATED_KNOCKBACK) ? target.getData(ModAttachments.ACCUMULATED_KNOCKBACK) : 0.0F;
                float dynamicPitch = 0.5F + (currentKb * 0.3F);
                if (dynamicPitch > 2.0F) dynamicPitch = 2.0F;

                target.level().playSound(null, target.getX(), target.getY(), target.getZ(),
                        ModSounds.STASIS_HIT.get(), SoundSource.NEUTRAL, 0.6F, dynamicPitch);
            }

            event.setCanceled(true);
        }
    }

    @SubscribeEvent
    public static void onEntityInteract(PlayerInteractEvent.EntityInteract event) {
        Entity target = event.getTarget();

        if (target.hasData(ModAttachments.FREEZE_TICKS) && target.getData(ModAttachments.FREEZE_TICKS) > 0) {
            // FIX: Wenn der Spieler den FreezeWand in der Hand hält, brechen wir das Event NICHT ab,
            // damit interactLivingEntity() im Item gefeuert werden kann!
            if (event.getItemStack().getItem() instanceof FreezeWandItem) {
                return;
            }
            event.setCanceled(true);
        }
    }

    public static void applyFreeze(Entity target, int durationInTicks) {
        int finalDuration = 180;
        target.setData(ModAttachments.FREEZE_TICKS, finalDuration);

        if (target.hasData(ModAttachments.ACCUMULATED_DAMAGE)) target.setData(ModAttachments.ACCUMULATED_DAMAGE, 0.0F);
        if (target.hasData(ModAttachments.ACCUMULATED_KNOCKBACK)) target.setData(ModAttachments.ACCUMULATED_KNOCKBACK, 0.0F);
        if (target.hasData(ModAttachments.LAST_ATTACK_YAW)) target.setData(ModAttachments.LAST_ATTACK_YAW, 0.0F);

        target.ejectPassengers();

        target.level().playSound(null, target.getX(), target.getY(), target.getZ(),
                ModSounds.STASIS_START.get(), SoundSource.NEUTRAL, 1.0F, 1.0F);
        target.level().playSound(null, target.getX(), target.getY(), target.getZ(),
                ModSounds.STASIS_TIMER.get(), SoundSource.NEUTRAL, 1.0F, 1.0F);

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

            StasisArrowEffectEntity arrowEffect = ModEntities.STASIS_ARROW_EFFECT.get().create(serverLevel);
            if (arrowEffect != null) {
                arrowEffect.setTarget(target);
                serverLevel.addFreshEntity(arrowEffect);

                double targetCenterY = target.getY() + (target.getBbHeight() * 0.5D);
                arrowEffect.moveTo(target.getX(), targetCenterY, target.getZ(), target.getYRot() + 180.0F, 0.0F);
            }
        }
    }
}