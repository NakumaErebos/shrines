package net.nakumaerebos.shrines.item.custom;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.nakumaerebos.shrines.entity.CryonisPillarEntity;
import net.nakumaerebos.shrines.entity.ModEntities;

import java.util.Comparator;
import java.util.List;
import java.util.UUID;

public class SheikahSlateItemCryonis extends Item {

    public SheikahSlateItemCryonis(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack itemstack = player.getItemInHand(hand);

        // 1. Bestehende Säulen per Raycast abbauen
        if (tryBreakPillarViaRaycast(player, level)) {
            return InteractionResultHolder.sidedSuccess(itemstack, level.isClientSide);
        }

        // 2. Flüssigkeits-Raycast für die Wasseroberfläche
        BlockHitResult hitResult = getPlayerPOVHitResult(level, player, ClipContext.Fluid.SOURCE_ONLY);

        if (hitResult.getType() == HitResult.Type.BLOCK) {
            BlockPos clickedPos = hitResult.getBlockPos();
            Vec3 hitVec = hitResult.getLocation();

            if (level.getFluidState(clickedPos).getType() == Fluids.WATER) {
                BlockPos bestSpawnPos = findBestSpawnBlock(level, clickedPos, hitVec);

                if (bestSpawnPos != null) {
                    if (!level.isClientSide && level instanceof ServerLevel serverLevel) {

                        // --- 3-SÄULEN-MANAGEMENT ---
                        managePillarLimit(serverLevel, player.getUUID());

                        // Neue Säule erstellen
                        CryonisPillarEntity pillar = ModEntities.CRYONIS_PILLAR.get().create(serverLevel);

                        if (pillar != null) {
                            double spawnX = bestSpawnPos.getX() + 0.5;
                            double spawnY = bestSpawnPos.getY() + 0.875;
                            double spawnZ = bestSpawnPos.getZ() + 0.5;

                            pillar.moveTo(spawnX, spawnY, spawnZ, 0.0F, 0.0F);

                            // NBT-Tags für Besitzer und Säulen-Nummer setzen (Nummer 3 ist immer die neueste)
                            CompoundTag nbt = pillar.getPersistentData();
                            nbt.putUUID("OwnerUUID", player.getUUID());
                            nbt.putInt("PillarIndex", 3);

                            serverLevel.addFreshEntity(pillar);

                            // --- HOCHBOOST-LOGIK ---
                            // Suchbox entspricht der echten Hitbox der Säule (Breite 2, Höhe 3)
                            AABB boostBox = new AABB(spawnX - 1.0, spawnY, spawnZ - 1.0, spawnX + 1.0, spawnY + 3.0, spawnZ + 1.0);
                            List<LivingEntity> entitiesToBoost = serverLevel.getEntitiesOfClass(LivingEntity.class, boostBox);

                            for (LivingEntity entity : entitiesToBoost) {
                                // Teleportiert das Entity exakt auf die Spitze der Säule (+3 Blöcke von der Basis)
                                entity.teleportTo(entity.getX(), spawnY + 3.0, entity.getZ());
                                // Ein kleiner physikalischer Impuls nach oben (4 Blöcke hoch boosten)
                                entity.setDeltaMovement(entity.getDeltaMovement().x, 0.42, entity.getDeltaMovement().z);
                                entity.hurtMarked = true; // Zwingt den Client, die Bewegungsänderung sofort zu übernehmen
                            }
                        }
                    }
                    return InteractionResultHolder.sidedSuccess(itemstack, level.isClientSide);
                }
            }
        }

        return InteractionResultHolder.pass(itemstack);
    }

    /**
     * Findet alle Säulen des Spielers in der Welt. Wenn es bereits 3 sind, wird die älteste zerstört
     * und die verbleibenden werden herabgestuft (Index 3 -> 2, Index 2 -> 1).
     */
    private void managePillarLimit(ServerLevel level, UUID playerUUID) {
        // Wir suchen global im geladenen Bereich nach allen Cryonis-Säulen des Spielers
        // Verwende "List<? extends CryonisPillarEntity>" statt "List<CryonisPillarEntity>"
        List<? extends CryonisPillarEntity> playerPillars = level.getEntities(ModEntities.CRYONIS_PILLAR.get(), entity -> {
            CompoundTag nbt = entity.getPersistentData();
            return nbt.hasUUID("OwnerUUID") && nbt.getUUID("OwnerUUID").equals(playerUUID);
        });

        // Wenn schon 3 oder mehr Säulen existieren, müssen wir aufräumen
        if (playerPillars.size() >= 3) {
            // Sortiert die Säulen aufsteigend nach ihrem Index (niedrigster Index zuerst)
            playerPillars.sort(Comparator.comparingInt(p -> p.getPersistentData().getInt("PillarIndex")));

            // Die älteste Säule (Index am niedrigsten, meistens 1) herausholen und zerstören
            CryonisPillarEntity oldestPillar = playerPillars.get(0);
            oldestPillar.triggerCryonisBreak();
            playerPillars.remove(0);
        }

        // Die verbleibenden Säulen im Index herunterstufen, damit die neue Säule immer die "3" beanspruchen kann
        int nextIndex = 1;
        for (CryonisPillarEntity remainingPillar : playerPillars) {
            remainingPillar.getPersistentData().putInt("PillarIndex", nextIndex);
            nextIndex++;
        }
    }

    private BlockPos findBestSpawnBlock(Level level, BlockPos center, Vec3 hitVec) {
        BlockPos bestPos = null;
        double shortestDistance = Double.MAX_VALUE;

        for (int x = -1; x <= 1; x++) {
            for (int z = -1; z <= 1; z++) {
                BlockPos checkPos = center.offset(x, 0, z);
                if (isValidPillarLocation(level, checkPos)) {
                    Vec3 blockCenterVec = new Vec3(checkPos.getX() + 0.5, checkPos.getY() + 0.5, checkPos.getZ() + 0.5);
                    double distance = hitVec.distanceToSqr(blockCenterVec);

                    if (distance < shortestDistance) {
                        shortestDistance = distance;
                        bestPos = checkPos;
                    }
                }
            }
        }
        return bestPos;
    }

    private boolean isValidPillarLocation(Level level, BlockPos pos) {
        for (int x = -1; x <= 1; x++) {
            for (int z = -1; z <= 1; z++) {
                if (level.getFluidState(pos.offset(x, 0, z)).getType() != Fluids.WATER) {
                    return false;
                }
            }
        }
        for (int yOffset = 1; yOffset <= 3; yOffset++) {
            for (int x = -1; x <= 1; x++) {
                for (int z = -1; z <= 1; z++) {
                    if (!level.getBlockState(pos.offset(x, yOffset, z)).isAir()) {
                        return false;
                    }
                }
            }
        }
        return true;
    }

    private boolean tryBreakPillarViaRaycast(Player player, Level level) {
        double reach = player.getAttributeValue(net.minecraft.world.entity.ai.attributes.Attributes.BLOCK_INTERACTION_RANGE) + 1.0;
        Vec3 eyePos = player.getEyePosition();
        Vec3 lookVec = player.getLookAngle().scale(reach);
        Vec3 targetVec = eyePos.add(lookVec);
        AABB searchBox = player.getBoundingBox().expandTowards(lookVec).inflate(2.0);

        List<Entity> entities = level.getEntities(player, searchBox, entity -> entity instanceof CryonisPillarEntity);
        for (Entity entity : entities) {
            AABB entityBox = entity.getBoundingBox().inflate(0.2);
            if (entityBox.clip(eyePos, targetVec).isPresent()) {
                if (entity instanceof CryonisPillarEntity pillar) {
                    if (!level.isClientSide) {
                        pillar.triggerCryonisBreak();
                    }
                    return true;
                }
            }
        }
        return false;
    }
}