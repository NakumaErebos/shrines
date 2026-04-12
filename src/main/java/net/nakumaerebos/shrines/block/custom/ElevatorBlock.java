package net.nakumaerebos.shrines.block.custom;

import com.mojang.logging.LogUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerChunkCache;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.level.TicketType;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.RandomState;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.minecraft.world.level.levelgen.structure.StructureStart;
import net.minecraft.world.phys.BlockHitResult;
import net.nakumaerebos.shrines.Shrines;
import net.nakumaerebos.shrines.block.ModBlocks;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;

import java.util.Optional;

public class ElevatorBlock extends Block {
    private static final Logger LOGGER = LogUtils.getLogger();

    public ElevatorBlock(Properties properties) {
        super(properties);
    }

    @Override
    protected @NotNull InteractionResult useWithoutItem(@NotNull BlockState state, Level level, @NotNull BlockPos pos, @NotNull Player player, @NotNull BlockHitResult hit) {
        if (!level.isClientSide && player instanceof ServerPlayer serverPlayer) {
            ServerLevel currentLevel = (ServerLevel) level;
            ResourceKey<Level> destKey = getDestination(currentLevel);
            ServerLevel destWorld = currentLevel.getServer().getLevel(destKey);

            if (destWorld == null) return InteractionResult.FAIL;

            Optional<BlockPos> targetPos = findNearestElevatorBlock(destWorld, pos);

            if (targetPos.isPresent()) {
                teleportPlayer(serverPlayer, destWorld, targetPos.get());
            } else if (destKey.location().getPath().equals("shrine_interior")) {
                // Chunks laden
                destWorld.getChunkSource().addRegionTicket(TicketType.PORTAL, new ChunkPos(pos), 5, pos);

                if (generateJigsawStructure(destWorld, pos)) {
                    findNearestElevatorBlock(destWorld, pos).ifPresentOrElse(
                            newPos -> teleportPlayer(serverPlayer, destWorld, newPos),
                            () -> LOGGER.warn("[Elevator] Struktur steht, aber Elevator nicht gefunden!")
                    );
                }
            }
        }
        return InteractionResult.SUCCESS;
    }

    private boolean generateJigsawStructure(ServerLevel world, BlockPos pos) {
        ResourceLocation structId = ResourceLocation.fromNamespaceAndPath(Shrines.MOD_ID, "shrine_inside");
        var registry = world.registryAccess().registryOrThrow(Registries.STRUCTURE);
        Optional<Holder.Reference<Structure>> holder = registry.getHolder(ResourceKey.create(Registries.STRUCTURE, structId));

        if (holder.isEmpty()) {
            LOGGER.error("[Elevator] Struktur {} nicht in Registry gefunden!", structId);
            return false;
        }

        Structure structure = holder.get().value();
        ChunkPos chunkPos = new ChunkPos(pos);

        // Zugriff über den ServerChunkCache (dein geschickter Code)
        ServerChunkCache chunkCache = world.getChunkSource();
        RandomState randomState = chunkCache.randomState();

        // Wir erstellen den Context für die 'generate' Methode deiner Structure-Klasse
        Structure.GenerationContext context = new Structure.GenerationContext(
                world.registryAccess(),
                chunkCache.getGenerator(), // Nutzt getGenerator() aus ServerChunkCache
                chunkCache.getGenerator().getBiomeSource(),
                randomState,
                world.getServer().getStructureManager(),
                world.getSeed(),
                chunkPos,
                world,
                (biomeHolder) -> true
        );

        // Aufruf der generate-Methode (aus deinem Structure-Code)
        StructureStart start = structure.generate(
                context.registryAccess(),
                context.chunkGenerator(),
                context.biomeSource(),
                context.randomState(),
                context.structureTemplateManager(),
                context.seed(),
                context.chunkPos(),
                20,
                context.heightAccessor(),
                context.validBiome()
        );

        if (start.isValid()) {
            // BoundingBox für 65x65 (großzügig bemessen)
            // Ersetze die alte Box durch diese hier:
            BoundingBox box = new BoundingBox(
                    pos.getX() - 96,            // Puffer nach "hinten"
                    world.getMinBuildHeight(),  // Gesamte Welt-Tiefe
                    pos.getZ() - 96,            // Puffer zur Seite
                    pos.getX() + 96,            // 65 + 31 Puffer nach vorne
                    world.getMaxBuildHeight(),  // Gesamte Welt-Höhe
                    pos.getZ() + 96             // 65 + 31 Puffer zur Seite
            );

            // Die Pieces (Räume) tatsächlich platzieren
            start.getPieces().forEach(piece -> piece.postProcess(
                    world,
                    world.structureManager(),
                    context.chunkGenerator(),
                    world.getRandom(),
                    box,
                    chunkPos,
                    pos // Pivot/Ankerpunkt an der Ecke
            ));
            return true;
        }

        LOGGER.error("[Elevator] Struktur-Start ist ungültig. Prüfe Jigsaw-Pools und Biome-Einstellungen!");
        return false;
    }

    // --- Hilfsmethoden ---

    private void teleportPlayer(ServerPlayer player, ServerLevel world, BlockPos pos) {
        player.teleportTo(world, pos.getX() + 0.5, pos.getY() + 1.1, pos.getZ() + 0.5, player.getYRot(), player.getXRot());
    }

    private ResourceKey<Level> getDestination(Level currentLevel) {
        if (currentLevel.dimension().location().equals(ResourceLocation.fromNamespaceAndPath(Shrines.MOD_ID, "shrine_interior"))) {
            return Level.OVERWORLD;
        }
        return ResourceKey.create(Registries.DIMENSION, ResourceLocation.fromNamespaceAndPath(Shrines.MOD_ID, "shrine_interior"));
    }

    private Optional<BlockPos> findNearestElevatorBlock(ServerLevel world, BlockPos origin) {
        BlockPos.MutableBlockPos mutablePos = new BlockPos.MutableBlockPos();
        // Wir prüfen primär die exakte Position (+/- 1 Block Toleranz für Rundungsfehler)
        for (int y = world.getMinBuildHeight(); y < world.getMaxBuildHeight(); y++) {
            for (int x = -64; x <= 64; x++) {
                for (int z = -64; z <= 64; z++) {
                    mutablePos.set(origin.getX() + x, y, origin.getZ() + z);
                    if (world.getBlockState(mutablePos).is(ModBlocks.ELEVATOR.get())) {
                        return Optional.of(mutablePos.immutable());
                    }
                }
            }
        }
        return Optional.empty();
    }
}