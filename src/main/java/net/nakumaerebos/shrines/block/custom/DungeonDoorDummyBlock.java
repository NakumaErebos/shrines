package net.nakumaerebos.shrines.block.custom;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.NotNull;

public class DungeonDoorDummyBlock extends Block {

    // Da die Tür 2x2 Blöcke groß ist, reichen die Offsets von 0 bis 1
    public static final IntegerProperty OFFSET_X = IntegerProperty.create("offset_x", 0, 1);
    public static final IntegerProperty OFFSET_Y = IntegerProperty.create("offset_y", 0, 1);
    public static final DirectionProperty FACING = BlockStateProperties.HORIZONTAL_FACING;
    public static final BooleanProperty OPEN = BlockStateProperties.OPEN;

    public DungeonDoorDummyBlock(Properties properties) {
        super(properties);
        this.registerDefaultState(this.stateDefinition.any()
                .setValue(OFFSET_X, 0)
                .setValue(OFFSET_Y, 0)
                .setValue(FACING, Direction.NORTH)
                .setValue(OPEN, false));
    }

    @Override
    public @NotNull VoxelShape getShape(BlockState state, @NotNull BlockGetter level, @NotNull BlockPos pos, @NotNull CollisionContext context) {
        // Wenn die Tür offen ist, fährt sie komplett aus dem 2x2 Rahmen heraus -> Keine Hitbox
        if (state.getValue(OPEN)) {
            return Shapes.empty();
        }
        // Wenn geschlossen, blockiert der Dummy komplett wie ein normaler Block
        return Shapes.block();
    }

    private BlockPos findMainBlock(BlockState state, BlockPos pos) {
        if (!state.hasProperty(OFFSET_X) || !state.hasProperty(FACING)) return pos;

        int r = state.getValue(OFFSET_X);
        int y = state.getValue(OFFSET_Y);
        Direction facing = state.getValue(FACING);

        // Da der Dummy mit CounterClockWise platziert wurde,
        // müssen wir jetzt mit ClockWise zurück zum Hauptblock gehen.
        Direction backToMain = facing.getClockWise();

        // Gehe r-Schritte zurück zum Hauptblock und y-Schritte nach unten
        return pos.relative(backToMain, r).below(y);
    }

    @Override
    protected @NotNull InteractionResult useWithoutItem(@NotNull BlockState state, Level level, @NotNull BlockPos pos, @NotNull Player player, @NotNull BlockHitResult hitResult) {
        BlockPos mainPos = findMainBlock(state, pos);
        BlockState mainState = level.getBlockState(mainPos);

        // Leitet den Rechtsklick direkt an den Hauptblock weiter
        if (mainState.getBlock() instanceof DungeonDoorBlock) {
            return mainState.useWithoutItem(level, player, hitResult.withPosition(mainPos));
        }

        return InteractionResult.PASS;
    }

    @Override
    public void onRemove(BlockState state, @NotNull Level level, @NotNull BlockPos pos, BlockState newState, boolean isMoving) {
        if (state.is(newState.getBlock())) {
            super.onRemove(state, level, pos, newState, isMoving);
            return;
        }

        // Wenn ein Spieler den Dummy abbaut, wird auch der Hauptblock zerstört
        if (!level.isClientSide && !isMoving) {
            BlockPos mainPos = findMainBlock(state, pos);
            BlockState mainState = level.getBlockState(mainPos);

            if (mainState.getBlock() instanceof DungeonDoorBlock) {
                level.removeBlock(mainPos, false);
            }
        }

        super.onRemove(state, level, pos, newState, isMoving);
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(OFFSET_X, OFFSET_Y, FACING, OPEN);
    }

    @Override
    public @NotNull RenderShape getRenderShape(@NotNull BlockState state) {
        // Der Dummy bleibt vollkommen unsichtbar, da Geckolib auf dem Hauptblock rendert
        return RenderShape.INVISIBLE;
    }

    @Override
    public @NotNull VoxelShape getCollisionShape(@NotNull BlockState state, @NotNull BlockGetter level, @NotNull BlockPos pos, @NotNull CollisionContext context) {
        return this.getShape(state, level, pos, context);
    }

    @Override
    public BlockState rotate(BlockState state, net.minecraft.world.level.block.Rotation rotation) {
        return state.setValue(FACING, rotation.rotate(state.getValue(FACING)));
    }

    @Override
    public BlockState mirror(BlockState state, net.minecraft.world.level.block.Mirror mirror) {
        return state.rotate(mirror.getRotation(state.getValue(FACING)));
    }
}