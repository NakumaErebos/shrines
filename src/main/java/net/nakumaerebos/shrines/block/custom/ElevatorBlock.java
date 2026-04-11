package net.nakumaerebos.shrines.block.custom;

import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.nakumaerebos.shrines.Shrines;
import net.nakumaerebos.shrines.block.ModBlocks;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;

public class ElevatorBlock extends Block {
    public ElevatorBlock(Properties properties) {
        super(properties);
    }

    // In 1.21.1 heißt die Methode "useWithoutItem" (für Rechtsklick ohne Item-Interaktion)
    // oder "useItemOn" (für Rechtsklick mit Item). Wir nehmen die Basis-Interaktion:
    @Override
    protected @NotNull InteractionResult useWithoutItem(@NotNull BlockState state, Level level, @NotNull BlockPos pos, @NotNull Player player, @NotNull BlockHitResult hit) {
        if (!level.isClientSide && player instanceof ServerPlayer serverPlayer) {
            ServerLevel currentLevel = (ServerLevel) level;

            ResourceKey<Level> destinationKey = getDestination(currentLevel);
            ServerLevel destinationWorld = currentLevel.getServer().getLevel(destinationKey);

            if (destinationWorld != null) {
                Optional<BlockPos> targetPos = findNearestElevatorBlock(destinationWorld, pos);

                if (targetPos.isPresent()) {
                    BlockPos dest = targetPos.get();

                    // In 1.21.1 nutzt man für ServerPlayer 'teleportTo' mit ServerLevel
                    serverPlayer.teleportTo(
                            destinationWorld,
                            dest.getX() + 0.5,
                            dest.getY() + 1.0,
                            dest.getZ() + 0.5,
                            player.getYRot(),
                            player.getXRot()
                    );
                    return InteractionResult.SUCCESS;
                }
            }
        }
        return InteractionResult.SUCCESS;
    }

    private ResourceKey<Level> getDestination(Level currentLevel) {
        // ResourceLocation.fromNamespaceAndPath ist der neue Standard in 1.21
        if (currentLevel.dimension().location().equals(ResourceLocation.fromNamespaceAndPath(Shrines.MOD_ID, "shrine_interior"))) {
            return Level.OVERWORLD;
        }
        return ResourceKey.create(net.minecraft.core.registries.Registries.DIMENSION,
                ResourceLocation.fromNamespaceAndPath(Shrines.MOD_ID, "shrine_interior"));
    }

    private Optional<BlockPos> findNearestElevatorBlock(ServerLevel world, BlockPos origin) {
        BlockPos.MutableBlockPos mutablePos = new BlockPos.MutableBlockPos();
        int minHeight = world.getMinBuildHeight();
        int maxHeight = world.getMaxBuildHeight();

        for (int x = -32; x <= 32; x++) {
            for (int z = -32; z <= 32; z++) {
                for (int y = minHeight; y < maxHeight; y++) {
                    mutablePos.set(origin.getX() + x, y, origin.getZ() + z);

                    // Hier prüfen wir jetzt explizit auf deinen Mod-Block
                    if (world.getBlockState(mutablePos).is(ModBlocks.ELEVATOR)) {
                        return Optional.of(mutablePos.immutable());
                    }
                }
            }
        }
        return Optional.empty();
    }
}