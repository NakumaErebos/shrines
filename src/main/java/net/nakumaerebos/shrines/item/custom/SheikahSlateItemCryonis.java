package net.nakumaerebos.shrines.item.custom;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.FluidTags;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
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

        if (tryBreakPillarViaRaycast(player, level)) {
            return InteractionResultHolder.sidedSuccess(itemstack, level.isClientSide);
        }

        BlockHitResult hitResult = getPlayerPOVHitResult(level, player, ClipContext.Fluid.ANY);

        if (hitResult.getType() == HitResult.Type.BLOCK) {
            BlockPos clickedPos = hitResult.getBlockPos();
            Direction face = hitResult.getDirection();
            Vec3 hitVec = hitResult.getLocation();

            // FIX: Nutzt jetzt FluidTags.WATER (erlaubt fließendes und stehendes Wasser)
            if (level.getFluidState(clickedPos).is(FluidTags.WATER)) {

                BlockPos bestSpawnPos = findBestSpawnBlock(level, clickedPos, hitVec, face);

                if (bestSpawnPos != null) {
                    if (!level.isClientSide && level instanceof ServerLevel serverLevel) {

                        managePillarLimit(serverLevel, player.getUUID());

                        CryonisPillarEntity pillar = ModEntities.CRYONIS_PILLAR.get().create(serverLevel);

                        if (pillar != null) {
                            pillar.setOrientation(face);

                            Direction.Axis axis = face.getAxis();

// 1. Alle Achsen standardmäßig perfekt auf die Mitte (+0.5) setzen
                            double spawnX = bestSpawnPos.getX() + 0.5;
                            double spawnY = bestSpawnPos.getY() + 0.5;
                            double spawnZ = bestSpawnPos.getZ() + 0.5;

// 2. Die aktive Achse exakt auf die Block-Kante setzen, aus der die Säule kommt
                            if (axis == Direction.Axis.X) {
                                // Wenn EAST (+1), starten wir bei der hinteren Kante (getX() + 1.0)
                                // Wenn WEST (-1), starten wir bei der vorderen Kante (getX())
                                spawnX = face.getStepX() > 0 ? bestSpawnPos.getX() + 1.0 : bestSpawnPos.getX();
                            } else if (axis == Direction.Axis.Y) {
                                // Wenn UP (+1), starten wir oben (getY() + 1.0)
                                // Wenn DOWN (-1), starten wir unten (getY())
                                spawnY = face.getStepY() > 0 ? bestSpawnPos.getY() + 1.0 : bestSpawnPos.getY();
                            } else {
                                // Wenn SOUTH (+1), starten wir bei getZ() + 1.0
                                // Wenn NORTH (-1), starten wir bei getZ()
                                spawnZ = face.getStepZ() > 0 ? bestSpawnPos.getZ() + 1.0 : bestSpawnPos.getZ();
                            }

                            double offset = -0.25;
                            spawnX += face.getStepX() * offset;
                            spawnY += face.getStepY() * offset;
                            spawnZ += face.getStepZ() * offset;

                            pillar.moveTo(spawnX, spawnY, spawnZ, 0.0F, 0.0F);

                            pillar.moveTo(spawnX, spawnY, spawnZ, 0.0F, 0.0F);

                            CompoundTag nbt = pillar.getPersistentData();
                            nbt.putUUID("OwnerUUID", player.getUUID());
                            nbt.putInt("PillarIndex", 3);

                            serverLevel.addFreshEntity(pillar);

                            AABB boostBox = pillar.getBoundingBox();
                            List<LivingEntity> entitiesToBoost = serverLevel.getEntitiesOfClass(LivingEntity.class, boostBox);

                            for (LivingEntity entity : entitiesToBoost) {
                                double boostPower = 0.5;
                                double xVel = entity.getDeltaMovement().x + (face.getStepX() * boostPower);
                                double yVel = face == Direction.UP ? 0.42 : (entity.getDeltaMovement().y + (face.getStepY() * boostPower));
                                double zVel = entity.getDeltaMovement().z + (face.getStepZ() * boostPower);

                                if (face == Direction.UP) {
                                    entity.teleportTo(entity.getX(), spawnY + 3.0, entity.getZ());
                                } else {
                                    entity.teleportTo(entity.getX() + face.getStepX() * 0.5, entity.getY() + 0.2, entity.getZ() + face.getStepZ() * 0.5);
                                }

                                entity.setDeltaMovement(xVel, yVel, zVel);
                                entity.hurtMarked = true;
                            }
                        }
                    }
                    return InteractionResultHolder.sidedSuccess(itemstack, level.isClientSide);
                }
            }
        }

        return InteractionResultHolder.pass(itemstack);
    }

    private BlockPos findBestSpawnBlock(Level level, BlockPos center, Vec3 hitVec, Direction face) {
        BlockPos bestPos = null;
        double shortestDistance = Double.MAX_VALUE;

        for (int a = -1; a <= 1; a++) {
            for (int b = -1; b <= 1; b++) {
                BlockPos checkPos;
                if (face.getAxis() == Direction.Axis.Y) checkPos = center.offset(a, 0, b);
                else if (face.getAxis() == Direction.Axis.Z) checkPos = center.offset(a, b, 0);
                else checkPos = center.offset(0, b, a);

                if (isValidPillarLocation(level, checkPos, face)) {
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

    private boolean isValidPillarLocation(Level level, BlockPos pos, Direction face) {
        // 1. Wasser-Check für das 3x3 Fundament
        for (int a = -1; a <= 1; a++) {
            for (int b = -1; b <= 1; b++) {
                BlockPos waterCheck;
                if (face.getAxis() == Direction.Axis.Y) waterCheck = pos.offset(a, 0, b);
                else if (face.getAxis() == Direction.Axis.Z) waterCheck = pos.offset(a, b, 0);
                else waterCheck = pos.offset(0, a, b);

                net.minecraft.world.level.material.FluidState fluidState = level.getFluidState(waterCheck);

                // Grundsätzlich muss es Wasser sein
                if (!fluidState.is(FluidTags.WATER)) {
                    return false;
                }

                // BEDINGUNG: Wenn von OBEN (UP) platziert wird, DARF ES KEIN fließendes Wasser sein (muss Source sein)
                if (face == Direction.UP && !fluidState.isSource()) {
                    return false;
                }
            }
        }

        // 2. 3x3x3 Freiraum-Check vor der Wand
        for (int offset = 1; offset <= 3; offset++) {
            for (int a = -1; a <= 1; a++) {
                for (int b = -1; b <= 1; b++) {
                    BlockPos airCheck;
                    if (face.getAxis() == Direction.Axis.Y) airCheck = pos.offset(a, offset * face.getStepY(), b);
                    else if (face.getAxis() == Direction.Axis.Z) airCheck = pos.offset(a, b, offset * face.getStepZ());
                    else airCheck = pos.offset(offset * face.getStepX(), a, b);

                    if (!level.getBlockState(airCheck).isAir()) {
                        return false;
                    }
                }
            }
        }
        return true;
    }

    private void managePillarLimit(ServerLevel level, UUID playerUUID) {
        List<? extends CryonisPillarEntity> playerPillars = level.getEntities(ModEntities.CRYONIS_PILLAR.get(), entity -> {
            CompoundTag nbt = entity.getPersistentData();
            return nbt.hasUUID("OwnerUUID") && nbt.getUUID("OwnerUUID").equals(playerUUID);
        });

        if (playerPillars.size() >= 3) {
            playerPillars.sort(Comparator.comparingInt(p -> p.getPersistentData().getInt("PillarIndex")));
            playerPillars.get(0).triggerCryonisBreak();
            playerPillars.remove(0);
        }

        int nextIndex = 1;
        for (CryonisPillarEntity remainingPillar : playerPillars) {
            remainingPillar.getPersistentData().putInt("PillarIndex", nextIndex);
            nextIndex++;
        }
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