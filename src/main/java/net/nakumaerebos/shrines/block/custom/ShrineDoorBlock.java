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
    public void setPlacedBy(Level level, BlockPos pos, BlockState state, @Nullable LivingEntity placer, ItemStack stack) {
        if (!level.isClientSide) {
            Direction facing = state.getValue(FACING);
            Direction right = facing.getClockWise();

            for (int r = -1; r <= 1; r++) {
                for (int y = 0; y <= 2; y++) {

                    if (r == 0 && y == 0) continue;

                    BlockPos targetPos = pos.relative(right, r).above(y);

                    BlockState dummyState = ModBlocks.SHRINE_DOOR_DUMMY.get().defaultBlockState()
                            .setValue(ShrineDoorDummyBlock.OFFSET_X, r + 1)
                            .setValue(ShrineDoorDummyBlock.OFFSET_Y, y)
                            .setValue(ShrineDoorDummyBlock.FACING, facing);

                    level.setBlock(targetPos, dummyState, 3);
                }
            }
        }
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
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hitResult) {
        if (!level.isClientSide && player.hasInfiniteMaterials()) {
            state = state.cycle(OPEN);
            level.setBlock(pos, state, 10);
            return InteractionResult.SUCCESS;
        } else {
            return InteractionResult.PASS;
        }
    }

    @Override
    public void onPlace(BlockState state, Level level, BlockPos pos, BlockState oldState, boolean isMoving) {
        if (!oldState.is(state.getBlock())) {
            return;
        }
        if (state.getValue(OPEN) && !oldState.getValue(OPEN)) {
            level.playSound(null, pos, ModSounds.SHRINE_DOOR_OPEN.get(),
                    SoundSource.BLOCKS, 1.0F, 1.0F);
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
}