package net.nakumaerebos.shrines.item.custom;


import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.UseAnim;
import net.minecraft.world.level.Level;
import net.minecraft.world.scores.PlayerTeam;
import net.minecraft.world.scores.Scoreboard;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.nakumaerebos.shrines.datagen.ModEntityTypeTagProvider;
import net.nakumaerebos.shrines.network.MagnesisSoundPacket;
import net.nakumaerebos.shrines.sound.ModSounds;

import java.util.List;

public class SheikahSlateItemMagnesis extends Item {

    // Maximale Reichweite, um ein Entity zu greifen
    private static final double REACH_DISTANCE = 10.0;
    // Name des temporären Teams für die rote Umrandung
    private static final String GLOW_TEAM_NAME = "magnesis_red";

    public SheikahSlateItemMagnesis(Properties properties) {
        super(properties.stacksTo(1));
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);

        if (!level.isClientSide) {
            // Strahl-Berechnung (Raycast) für Entities
            Vec3 eyePosition = player.getEyePosition();
            Vec3 lookVector = player.getLookAngle();
            Vec3 targetPosition = eyePosition.add(lookVector.scale(REACH_DISTANCE));

            AABB searchBox = player.getBoundingBox().expandTowards(lookVector.scale(REACH_DISTANCE)).inflate(1.0);
            List<Entity> entities = level.getEntities(player, searchBox, entity -> entity instanceof LivingEntity);

            Entity targetEntity = null;
            double closestDistance = REACH_DISTANCE;

            // Finde das Entity, das am nächsten am Fadenkreuz ist
            for (Entity entity : entities) {
                // ÄNDERUNG 1: Überprüfen, ob das Entity im registrierten Tag enthalten ist
                if (!entity.getType().is(ModEntityTypeTagProvider.MAGNESIS_GRABBABLE)) {
                    continue;
                }

                AABB aabb = entity.getBoundingBox().inflate(entity.getPickRadius());
                var intersection = aabb.clip(eyePosition, targetPosition);
                if (intersection.isPresent()) {
                    double distance = eyePosition.distanceTo(intersection.get());
                    if (distance < closestDistance) {
                        targetEntity = entity;
                        closestDistance = distance;
                    }
                }
            }

            if (targetEntity != null) {
                player.startUsingItem(hand);

                targetEntity.setGlowingTag(true);
                registerEntityToRedTeam(level, targetEntity);

                // Greif-Sound (Einzelsound)
                level.playSound(null, player.getX(), player.getY(), player.getZ(),
                        ModSounds.MAGNESIS_CATCH.get(), SoundSource.PLAYERS, 1.0F, 1.0F);

                // NEU: Dem Client sagen, er soll den Loop-Sound starten
                if (player instanceof net.minecraft.server.level.ServerPlayer serverPlayer) {
                    net.neoforged.neoforge.network.PacketDistributor.sendToPlayer(serverPlayer, new MagnesisSoundPacket(true));
                }

                Entity finalTargetEntity = targetEntity;
                double finalClosestDistance = closestDistance;
                stack.set(net.minecraft.core.component.DataComponents.CUSTOM_DATA, net.minecraft.world.item.component.CustomData.of(
                        new net.minecraft.nbt.CompoundTag() {{
                            putInt("GrabbedEntityId", finalTargetEntity.getId());
                            putDouble("GrabDistance", finalClosestDistance);
                        }}
                ));
                return InteractionResultHolder.success(stack);
            }
        }

        return InteractionResultHolder.pass(stack);
    }

    @Override
    public void onUseTick(Level level, LivingEntity livingEntity, ItemStack stack, int count) {
        if (!level.isClientSide && livingEntity instanceof Player player) {
            var customData = stack.get(net.minecraft.core.component.DataComponents.CUSTOM_DATA);
            if (customData != null) {
                var tag = customData.copyTag();
                if (tag.contains("GrabbedEntityId")) {
                    int entityId = tag.getInt("GrabbedEntityId");
                    double distance = tag.getDouble("GrabDistance");

                    // Tastenabfrage aus den Spieler-Daten
                    boolean isPulling = player.getPersistentData().getBoolean("MagnesisPulling");
                    boolean isPushing = player.getPersistentData().getBoolean("MagnesisPushing");

                    // Distanz ändern (0.2 Blöcke pro Tick)
                    if (isPulling) {
                        distance = Math.max(2.0, distance - 0.2); // Nicht näher als 2 Blöcke
                    }
                    if (isPushing) {
                        distance = Math.min(20.0, distance + 0.2); // Nicht weiter als 20 Blöcke
                    }

                    // Neuen Distanzwert im Item speichern
                    tag.putDouble("GrabDistance", distance);
                    stack.set(net.minecraft.core.component.DataComponents.CUSTOM_DATA, net.minecraft.world.item.component.CustomData.of(tag));

                    Entity grabbedEntity = level.getEntity(entityId);
                    if (grabbedEntity != null) {
                        Vec3 eyePosition = player.getEyePosition();
                        Vec3 lookVector = player.getLookAngle();
                        Vec3 targetPos = eyePosition.add(lookVector.scale(distance));

                        Vec3 motion = targetPos.subtract(grabbedEntity.position()).scale(0.3);
                        grabbedEntity.setDeltaMovement(motion);
                        grabbedEntity.hurtMarked = true;

                    }
                }
            }
        }
    }

    @Override
    public void releaseUsing(ItemStack stack, Level level, LivingEntity livingEntity, int timeCharged) {
        if (!level.isClientSide && livingEntity instanceof Player player) {
            var customData = stack.get(net.minecraft.core.component.DataComponents.CUSTOM_DATA);
            if (customData != null) {
                var tag = customData.copyTag();
                if (tag.contains("GrabbedEntityId")) {
                    int entityId = tag.getInt("GrabbedEntityId");
                    Entity grabbedEntity = level.getEntity(entityId);

                    if (grabbedEntity != null) {
                        grabbedEntity.setGlowingTag(false);
                        removeEntityFromRedTeam(level, grabbedEntity);
                    }
                }
            }

            // Loslass-Sound (Einzelsound)
            level.playSound(null, player.getX(), player.getY(), player.getZ(),
                    ModSounds.MAGNESIS_RELEASE.get(), SoundSource.PLAYERS, 1.0F, 1.0F);

            // NEU: Dem Client sagen, er MUSS den Loop-Sound sofort abbrechen
            if (player instanceof net.minecraft.server.level.ServerPlayer serverPlayer) {
                net.neoforged.neoforge.network.PacketDistributor.sendToPlayer(serverPlayer, new MagnesisSoundPacket(false));
            }

            stack.remove(net.minecraft.core.component.DataComponents.CUSTOM_DATA);
        }
    }

    @Override
    public void inventoryTick(ItemStack stack, Level level, net.minecraft.world.entity.Entity entity, int slotId, boolean isSelected) {
        if (!level.isClientSide && entity instanceof Player player) {
            // Wenn das Item NBT-Daten hat, aber der Spieler es nicht mehr aktiv "benutzt" (Rechtsklick losgelassen oder Slot gewechselt)
            if (player.getUseItem() != stack) {
                var customData = stack.get(net.minecraft.core.component.DataComponents.CUSTOM_DATA);
                if (customData != null && customData.copyTag().contains("GrabbedEntityId")) {

                    // Sicherheits-Cleanup ausführen!
                    int entityId = customData.copyTag().getInt("GrabbedEntityId");
                    Entity grabbedEntity = level.getEntity(entityId);
                    if (grabbedEntity != null) {
                        grabbedEntity.setGlowingTag(false);
                        removeEntityFromRedTeam(level, grabbedEntity);
                    }

                    // Sound-Stop erzwingen
                    if (player instanceof net.minecraft.server.level.ServerPlayer serverPlayer) {
                        net.neoforged.neoforge.network.PacketDistributor.sendToPlayer(serverPlayer, new MagnesisSoundPacket(false));
                    }

                    // Daten löschen
                    stack.remove(net.minecraft.core.component.DataComponents.CUSTOM_DATA);
                }
            }
        }
    }

    private void registerEntityToRedTeam(Level level, Entity entity) {
        Scoreboard scoreboard = level.getScoreboard();
        PlayerTeam team = scoreboard.getPlayerTeam(GLOW_TEAM_NAME);

        if (team == null) {
            team = scoreboard.addPlayerTeam(GLOW_TEAM_NAME);
            team.setColor(net.minecraft.ChatFormatting.RED); // Macht die "Glowing"-Outline rot!
        }

        scoreboard.addPlayerToTeam(entity.getScoreboardName(), team);
    }

    /**
     * Hilfsmethode: Entfernt das Entity wieder aus dem roten Team.
     */
    private void removeEntityFromRedTeam(Level level, Entity entity) {
        Scoreboard scoreboard = level.getScoreboard();
        PlayerTeam team = scoreboard.getPlayerTeam(GLOW_TEAM_NAME);
        if (team != null) {
            scoreboard.removePlayerFromTeam(entity.getScoreboardName(), team);
        }
    }

    @Override
    public UseAnim getUseAnimation(ItemStack stack) {
        return UseAnim.BOW;
    }

    @Override
    public int getUseDuration(ItemStack stack, LivingEntity entity) {
        return 72000;
    }
}