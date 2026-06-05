package net.nakumaerebos.shrines.item.custom;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.nakumaerebos.shrines.entity.ModEntities;
import net.nakumaerebos.shrines.entity.RemoteBombCubedEntity;
import net.nakumaerebos.shrines.sound.ModSounds;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public class SheikahSlateItemRemoteBombSquare extends Item {

    public SheikahSlateItemRemoteBombSquare(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack itemStack = player.getItemInHand(hand);

        if (!level.isClientSide && level instanceof ServerLevel serverLevel) {
            UUID playerUUID = player.getUUID();

            // 1. Umgebung nach eigener Bombe scannen
            double searchRadius = 64.0D;
            AABB searchBox = player.getBoundingBox().inflate(searchRadius);
            List<RemoteBombCubedEntity> nearbyBombs = level.getEntitiesOfClass(
                    RemoteBombCubedEntity.class,
                    searchBox
            );

            RemoteBombCubedEntity ownedBomb = null;
            for (RemoteBombCubedEntity bomb : nearbyBombs) {
                Optional<UUID> ownerUUID = bomb.getOwnerUUID();
                if (ownerUUID.isPresent() && ownerUUID.get().equals(playerUUID)) {
                    ownedBomb = bomb;
                    break;
                }
            }

            if (ownedBomb != null) {
                // Fall 1: Bombe existiert -> Explodieren lassen
                ownedBomb.explode();
            } else {
                // Fall 2: Keine Bombe da -> Spawnposition per Raycast berechnen
                Vec3 eyePosition = player.getEyePosition();
                Vec3 lookDirection = player.getLookAngle();
                double maxDistance = 3.5D; // Maximaler Radius zum Erstellen

                // Berechne das theoretische Ende des Strahls
                Vec3 endPosition = eyePosition.add(lookDirection.scale(maxDistance));

                // Raycast-Kontext: Wir suchen nach kollidierbaren Blöcken (COLLIDER)
                ClipContext clipContext = new ClipContext(
                        eyePosition,
                        endPosition,
                        ClipContext.Block.COLLIDER,
                        ClipContext.Fluid.NONE,
                        player
                );

                BlockHitResult hitResult = level.clip(clipContext);
                Vec3 spawnPosition;

                if (hitResult.getType() == HitResult.Type.BLOCK) {
                    // Strahl hat eine Wand oder den Boden getroffen!
                    // hitResult.getLocation() gibt den exakten Schnittpunkt auf der Block-Oberfläche.
                    spawnPosition = hitResult.getLocation();

                    // Optionaler Komfort-Fix für den Boden:
                    // Da die Entität ihren Pivot-Punkt (Nullpunkt) unten hat, würde sie bei Wänden perfekt
                    // anliegen, aber beim Boden leicht einsinken, wenn die Hitbox groß ist.
                    // Falls du merkst, sie steckt im Boden, kann man sie minimal anheben:
                    // if (hitResult.getDirection() == net.minecraft.core.Direction.UP) {
                    //     spawnPosition = spawnPosition.add(0, 0.1, 0);
                    // }
                } else {
                    // Strahl hat nichts getroffen (Blick in den freien Himmel) -> Spawne 5 Blöcke entfernt
                    spawnPosition = endPosition;
                }

                // Bombe spawnen
                level.playSound(null, spawnPosition.x, spawnPosition.y, spawnPosition.z, ModSounds.REMOTE_BOMB_APPEAR.get(), SoundSource.AMBIENT, 1.0F, 1.0F);
                RemoteBombCubedEntity newBomb = ModEntities.REMOTE_BOMB_CUBED.get().create(serverLevel);
                if (newBomb != null) {
                    newBomb.moveTo(spawnPosition.x, spawnPosition.y, spawnPosition.z, player.getYRot(), 0.0F);
                    newBomb.setOwnerUUID(playerUUID);
                    serverLevel.addFreshEntity(newBomb);
                }
            }
        }

        return InteractionResultHolder.sidedSuccess(itemStack, level.isClientSide());
    }
}