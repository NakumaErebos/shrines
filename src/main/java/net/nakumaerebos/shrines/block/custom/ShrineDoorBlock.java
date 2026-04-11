package net.nakumaerebos.shrines.block.custom;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.nakumaerebos.shrines.block.ModBlocks;
import net.nakumaerebos.shrines.block.entity.ShrineDoorBlockEntity;
import net.nakumaerebos.shrines.sound.ModSounds;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class ShrineDoorBlock extends BaseEntityBlock {

    public static final BooleanProperty OPEN = BlockStateProperties.OPEN;
    public static final DirectionProperty FACING = BlockStateProperties.HORIZONTAL_FACING;

    public ShrineDoorBlock(Properties properties) {
        super(properties);
        this.registerDefaultState(this.stateDefinition.any()
                .setValue(OPEN, Boolean.FALSE)
                .setValue(FACING, Direction.NORTH));
    }

    @Override
    protected @NotNull MapCodec<? extends BaseEntityBlock> codec() {
        return null;
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        return this.defaultBlockState().setValue(FACING, context.getHorizontalDirection().getOpposite());
    }

    @Override
    public void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState, boolean isMoving) {
        if (!state.is(newState.getBlock())) {
            Direction right = state.getValue(FACING).getClockWise();

            for (int r = -1; r <= 1; r++) {
                for (int y = 0; y <= 2; y++) {

                    BlockPos targetPos = pos.relative(right, r).above(y);

                    if (level.getBlockState(targetPos).getBlock() instanceof ShrineDoorDummyBlock) {
                        level.removeBlock(targetPos, false);
                    }
                }
            }
        }
        super.onRemove(state, level, pos, newState, isMoving);
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(OPEN, FACING);
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return state.getValue(OPEN) ? Shapes.empty() : Shapes.block();
    }

    @Override
    protected @NotNull InteractionResult useWithoutItem(@NotNull BlockState state, Level level, @NotNull BlockPos pos, @NotNull Player player, @NotNull BlockHitResult hitResult) {
        if (!level.isClientSide && player.hasInfiniteMaterials()) {
            boolean newState = !state.getValue(OPEN);

            // 1. Hauptblock umschalten
            level.setBlock(pos, state.setValue(OPEN, newState), 10);

            // 2. Dummies umschalten
            updateDummies(level, pos, state, newState);

            return InteractionResult.SUCCESS;
        }
        return InteractionResult.PASS;
    }

    @Override
    public void onPlace(BlockState state, Level level, BlockPos pos, BlockState oldState, boolean isMoving) {
        // 1. Prüfen: Ist es derselbe Block? (Dann hat sich nur ein State wie OPEN geändert)
        if (state.is(oldState.getBlock())) {
            boolean wasOpen = oldState.getValue(OPEN);
            boolean isOpen = state.getValue(OPEN);

            // 2. Wenn sich der OPEN-Status geändert hat, Dummies benachrichtigen
            if (wasOpen != isOpen && !level.isClientSide) {
                updateDummies(level, pos, state, isOpen);
            }
        }
        super.onPlace(state, level, pos, oldState, isMoving);
    }

    // Hilfsmethode zur Synchronisation der Dummies
    private void updateDummies(Level level, BlockPos pos, BlockState state, boolean open) {
        Direction facing = state.getValue(FACING);
        Direction right = facing.getClockWise();

        for (int r = -1; r <= 1; r++) {
            for (int y = 0; y <= 2; y++) {
                // Den Hauptblock selbst überspringen
                if (r == 0 && y == 0) continue;

                BlockPos targetPos = pos.relative(right, r).above(y);
                BlockState targetState = level.getBlockState(targetPos);

                // Wenn dort ein Dummy steht, setzen wir seinen OPEN-Status auf den der Tür
                if (targetState.getBlock() instanceof ShrineDoorDummyBlock) {
                    level.setBlock(targetPos, targetState.setValue(ShrineDoorDummyBlock.OPEN, open), 3);
                }
            }
        }
    }

    @Override
    public RenderShape getRenderShape(BlockState state) {
        return RenderShape.ENTITYBLOCK_ANIMATED;
    }

    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new ShrineDoorBlockEntity(pos, state);
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