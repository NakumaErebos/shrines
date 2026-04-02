package net.nakumaerebos.shrines.block.custom;

import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class ShrineDoorDummyBlock extends Block {
    public ShrineDoorDummyBlock(Properties properties) {
        super(properties);
    }

    @Override
    protected @NotNull InteractionResult useWithoutItem(@NotNull BlockState state, @NotNull Level level, @NotNull BlockPos pos, @NotNull Player player, @NotNull BlockHitResult hitResult) {
        // Wir suchen den Hauptblock. Das erfordert eine Logik, um die "Mutter" zu finden.
        // Ein einfacher Weg: Wir speichern die Position des Hauptblocks in einer BlockEntity oder
        // wir suchen in einem Radius von 2 Blöcken nach dem ShrineDoorBlock.
        BlockPos mainPos = findMainBlock(level, pos);
        if (mainPos != null) {
            return level.getBlockState(mainPos).useWithoutItem(level, player, hitResult.withPosition(mainPos));
        }
        return InteractionResult.PASS;
    }

    @Override
    public void onRemove(@NotNull BlockState state, @NotNull Level level, @NotNull BlockPos pos, @NotNull BlockState newState, boolean isMoving) {
        // Wenn ein Dummy zerstört wird, sollte das ganze Tor verschwinden
        BlockPos mainPos = findMainBlock(level, pos);
        if (mainPos != null && !newState.is(state.getBlock())) {
            level.destroyBlock(mainPos, false);
        }
        super.onRemove(state, level, pos, newState, isMoving);
    }

    @Nullable
    private BlockPos findMainBlock(Level level, BlockPos pos) {
        // Suche im 3x3x3 Bereich nach dem ShrineDoorBlock
        for (BlockPos p : BlockPos.betweenClosed(pos.offset(-2, -2, -2), pos.offset(2, 2, 2))) {
            if (level.getBlockState(p).getBlock() instanceof ShrineDoorBlock) return p.immutable();
        }
        return null;
    }

    // Im ShrineDoorDummyBlock
    @Override
    public @NotNull VoxelShape getShape(@NotNull BlockState state, @NotNull BlockGetter level, @NotNull BlockPos pos, @NotNull CollisionContext context) {
        // 1. Finde den Hauptblock über das BlockGetter-Interface
        BlockPos mainPos = findMainBlock(level, pos);
        if (mainPos == null) return Shapes.block();

        // BlockGetter erlaubt uns getBlockState aufzurufen
        BlockState mainState = level.getBlockState(mainPos);

        // Sicherheitshalber prüfen, ob der gefundene Block wirklich unser Tor ist
        if (!(mainState.getBlock() instanceof ShrineDoorBlock)) return Shapes.block();

        boolean isOpen = mainState.getValue(ShrineDoorBlock.OPEN);

        if (!isOpen) return Shapes.block(); // Tor zu → überall volle Kollision

        // 2. Wenn offen: Nur die äußeren Pfosten (8 Pixel) haben Kollision
        int xOffset = pos.getX() - mainPos.getX();

        if (xOffset == -1) { // Linke Spalte des 3x3 Verbunds
            return Block.box(0, 0, 0, 8, 16, 16);
        } else if (xOffset == 1) { // Rechte Spalte des 3x3 Verbunds
            return Block.box(8, 0, 0, 16, 16, 16);
        }

        // Mittlere Spalte (xOffset == 0) ist frei begehbar
        return Shapes.empty();
    }

    /**
     * Geänderte Hilfsmethode: Nutzt jetzt BlockGetter statt Level.
     */
    @Nullable
    private BlockPos findMainBlock(BlockGetter level, BlockPos pos) {
        // Wir suchen im 3x3x3 Bereich nach dem ShrineDoorBlock
        for (BlockPos p : BlockPos.betweenClosed(pos.offset(-2, -2, -2), pos.offset(2, 2, 2))) {
            if (level.getBlockState(p).getBlock() instanceof ShrineDoorBlock) {
                return p.immutable();
            }
        }
        return null;
    }

    @Override
    protected @NotNull RenderShape getRenderShape(@NotNull BlockState state) {
        return RenderShape.INVISIBLE; // Der Dummy soll nicht gerendert werden
    }

    @Override
    public @NotNull VoxelShape getCollisionShape(@NotNull BlockState state, @NotNull BlockGetter level, @NotNull BlockPos pos, @NotNull CollisionContext context) {
        // Leitet die physische Kollision direkt an deine getShape-Logik weiter
        return this.getShape(state, level, pos, context);
    }
}