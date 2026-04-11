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

public class ShrineDoorDummyBlock extends Block {

    public static final IntegerProperty OFFSET_X = IntegerProperty.create("offset_x", 0, 2);
    public static final IntegerProperty OFFSET_Y = IntegerProperty.create("offset_y", 0, 2);
    public static final DirectionProperty FACING = BlockStateProperties.HORIZONTAL_FACING;
    public static final BooleanProperty OPEN = BlockStateProperties.OPEN;

    public ShrineDoorDummyBlock(Properties properties) {
        super(properties);
        this.registerDefaultState(this.stateDefinition.any()
                .setValue(OFFSET_X, 0)
                .setValue(OFFSET_Y, 0)
                .setValue(FACING, Direction.NORTH)
                .setValue(OPEN, false)); // Standardmäßig zu
    }

    @Override
    public @NotNull VoxelShape getShape(BlockState state, @NotNull BlockGetter level, @NotNull BlockPos pos, @NotNull CollisionContext context) {
        // KEIN getBlockState mehr! Nur noch der eigene Status zählt.
        if (state.getValue(OPEN)) {
            int offsetX = state.getValue(OFFSET_X) - 1;
            Direction facing = state.getValue(FACING);

            if (offsetX == 0) return Shapes.empty();

            return switch (facing) {
                case NORTH -> offsetX == -1 ? Block.box(0, 0, 0, 8, 16, 16) : Block.box(8, 0, 0, 16, 16, 16);
                case SOUTH -> offsetX == -1 ? Block.box(8, 0, 0, 16, 16, 16) : Block.box(0, 0, 0, 8, 16, 16);
                case WEST -> offsetX == -1 ? Block.box(0, 0, 8, 16, 16, 16) : Block.box(0, 0, 0, 16, 16, 8);
                case EAST -> offsetX == -1 ? Block.box(0, 0, 0, 16, 16, 8) : Block.box(0, 0, 8, 16, 16, 16);
                default -> Shapes.block();
            };
        }
        return Shapes.block();
    }

    private BlockPos findMainBlock(BlockState state, BlockPos pos) {
        // Falls aus irgendeinem Grund die Properties fehlen (sollte nicht sein, aber sicher ist sicher)
        if (!state.hasProperty(OFFSET_X) || !state.hasProperty(FACING)) return pos;

        int r = state.getValue(OFFSET_X) - 1;
        int y = state.getValue(OFFSET_Y);
        Direction facing = state.getValue(FACING);
        Direction left = facing.getCounterClockWise();

        return pos.relative(left, r).below(y);
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hitResult) {
        BlockPos mainPos = findMainBlock(state, pos);

        if (level.getBlockState(mainPos).getBlock() instanceof ShrineDoorBlock) {
            return level.getBlockState(mainPos).useWithoutItem(level, player, hitResult.withPosition(mainPos));
        }

        return InteractionResult.PASS;
    }

    @Override
    public void onRemove(BlockState state, @NotNull Level level, @NotNull BlockPos pos, BlockState newState, boolean isMoving) {
        // Wenn der Block-Typ gleich bleibt (nur State-Änderung), Finger weg!
        if (state.is(newState.getBlock())) {
            super.onRemove(state, level, pos, newState, isMoving);
            return;
        }

        // isMoving ist bei Strukturen oft true. Wir wollen den Main-Block NUR löschen,
        // wenn ein Spieler den Dummy wirklich im Spiel abbricht.
        if (!level.isClientSide && !isMoving) {
            BlockPos mainPos = findMainBlock(state, pos);
            BlockState mainState = level.getBlockState(mainPos);

            if (mainState.getBlock() instanceof ShrineDoorBlock) {
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
    public RenderShape getRenderShape(BlockState state) {
        return RenderShape.INVISIBLE;
    }

    @Override
    public @NotNull VoxelShape getCollisionShape(@NotNull BlockState state, @NotNull BlockGetter level, @NotNull BlockPos pos, @NotNull CollisionContext context) {
        return this.getShape(state, level, pos, context);
    }

    @Override
    public BlockState rotate(BlockState state, net.minecraft.world.level.block.Rotation rotation) {
        // Dreht das FACING basierend auf der Rotation der Struktur/Welt
        return state.setValue(FACING, rotation.rotate(state.getValue(FACING)));
    }

    @Override
    public BlockState mirror(BlockState state, net.minecraft.world.level.block.Mirror mirror) {
        // Spiegelt den Block (wichtig für symmetrische Strukturen)
        return state.rotate(mirror.getRotation(state.getValue(FACING)));
    }
}