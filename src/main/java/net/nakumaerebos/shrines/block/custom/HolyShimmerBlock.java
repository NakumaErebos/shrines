package net.nakumaerebos.shrines.block.custom;

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
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
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
import net.nakumaerebos.shrines.block.entity.HolyShimmerEntity;
import net.nakumaerebos.shrines.block.entity.ModBlockEntities;
import net.nakumaerebos.shrines.sound.ModSounds;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class HolyShimmerBlock extends Block implements EntityBlock {
    public static final DirectionProperty FACING = BlockStateProperties.HORIZONTAL_FACING;
    public static final BooleanProperty ACTIVATED = BooleanProperty.create("activated");
    public static final BooleanProperty IS_EDGE = BooleanProperty.create("is_edge");
    public static final BooleanProperty HAS_NO_VOXELSHAPE = BooleanProperty.create("has_no_voxelshape");

    // Identifiziert die Etage (0 = Basis mit BlockEntity, 1 = Mitte-Dummy, 2 = Oben-Dummy)
    public static final IntegerProperty HEIGHT_PART = IntegerProperty.create("height_part", 0, 2);

    // Die Boxen sind jetzt auf die normale Blockhöhe von 16 (1 Block) begrenzt, damit das Raycasting präzise bleibt
    protected static final VoxelShape SHAPE_NORTH_SOUTH = Block.box(7.0D, 0.0D, 0.0D, 9.0D, 16.0D, 16.0D);
    protected static final VoxelShape SHAPE_EAST_WEST = Block.box(0.0D, 0.0D, 7.0D, 16.0D, 16.0D, 9.0D);

    public HolyShimmerBlock(Properties properties) {
        super(properties);
        this.registerDefaultState(this.stateDefinition.any()
                .setValue(FACING, Direction.NORTH)
                .setValue(ACTIVATED, false)
                .setValue(IS_EDGE, false)
                .setValue(HAS_NO_VOXELSHAPE, false)
                .setValue(HEIGHT_PART, 0));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FACING, ACTIVATED, IS_EDGE, HAS_NO_VOXELSHAPE, HEIGHT_PART);
    }

    @Override
    public @NotNull VoxelShape getShape(BlockState state, @NotNull BlockGetter world, @NotNull BlockPos pos, @NotNull CollisionContext context) {
        if (state.getValue(HAS_NO_VOXELSHAPE)) {
            return Shapes.empty();
        }
        Direction dir = state.getValue(FACING);
        return (dir == Direction.NORTH || dir == Direction.SOUTH) ? SHAPE_EAST_WEST : SHAPE_NORTH_SOUTH;
    }

    @Override
    public @NotNull VoxelShape getInteractionShape(BlockState state, @NotNull BlockGetter world, @NotNull BlockPos pos) {
        return this.getShape(state, world, pos, CollisionContext.empty());
    }

    @Override
    public @NotNull VoxelShape getCollisionShape(BlockState state, @NotNull BlockGetter world, @NotNull BlockPos pos, @NotNull CollisionContext context) {
        return this.getShape(state, world, pos, context);
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        BlockPos pos = context.getClickedPos();
        Level level = context.getLevel();

        // Verhindert das Platzieren, wenn die 2 Blöcke darüber blockiert sind
        if (!level.getBlockState(pos.above()).canBeReplaced(context) || !level.getBlockState(pos.above(2)).canBeReplaced(context)) {
            return null;
        }

        boolean edge = level.getBlockState(pos.below()).isAir();

        return this.defaultBlockState()
                .setValue(FACING, context.getHorizontalDirection().getOpposite())
                .setValue(IS_EDGE, edge)
                .setValue(HEIGHT_PART, 0);
    }

    // Spawnt die beiden Dummy-Blöcke über dem Hauptblock, wenn dieser platziert wird
    @Override
    public void setPlacedBy(Level level, BlockPos pos, BlockState state, @Nullable LivingEntity placer, ItemStack stack) {
        super.setPlacedBy(level, pos, state, placer, stack);
        if (!level.isClientSide) {
            level.setBlock(pos.above(), state.setValue(HEIGHT_PART, 1), 3);
            level.setBlock(pos.above(2), state.setValue(HEIGHT_PART, 2), 3);
        }
    }

    // Wenn ein Teil der 3 Blöcke zerstört wird, fliegen die anderen automatisch mit weg
    @Override
    public void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState, boolean isMoving) {
        if (!state.is(newState.getBlock())) {
            int part = state.getValue(HEIGHT_PART);
            BlockPos basePos = pos.below(part);

            for (int i = 0; i < 3; i++) {
                BlockPos currentPos = basePos.above(i);
                if (level.getBlockState(currentPos).is(this)) {
                    level.setBlock(currentPos, Blocks.AIR.defaultBlockState(), 3);
                }
            }
            super.onRemove(state, level, pos, newState, isMoving);
        }
    }

    @Override
    protected @NotNull InteractionResult useWithoutItem(@NotNull BlockState state, Level level, @NotNull BlockPos pos, @NotNull Player player, @NotNull BlockHitResult hit) {
        if (!level.isClientSide) {
            // Ermittle die Position des echten Basis-Blocks (wo die Entity sitzt) anhand des aktuellen Etagen-Parts
            int part = state.getValue(HEIGHT_PART);
            BlockPos basePos = pos.below(part);
            BlockState baseState = level.getBlockState(basePos);

            if (baseState.is(this) && !baseState.getValue(ACTIVATED)) {
                // Aktiviere die Basis
                level.setBlock(basePos, baseState.setValue(ACTIVATED, true), 3);

                // Setze die Dummys darüber ebenfalls auf "activated" (falls für BlockStates/Visuals wichtig)
                for (int i = 1; i < 3; i++) {
                    BlockPos dummyPos = basePos.above(i);
                    BlockState dummyState = level.getBlockState(dummyPos);
                    if (dummyState.is(this)) {
                        level.setBlock(dummyPos, dummyState.setValue(ACTIVATED, true), 3);
                    }
                }

                // Animation & Sound auf der echten BlockEntity abspielen
                if (level.getBlockEntity(basePos) instanceof HolyShimmerEntity tile) {
                    level.playSound(null, basePos, ModSounds.HOLY_SHIMMER_SHATTER.get(),
                            SoundSource.BLOCKS, 1.0F, 1.0F);
                    triggerNearbyShimmers(level, basePos, 5);
                    tile.triggerShatter();
                }
            }
        }
        return InteractionResult.SUCCESS;
    }

    public static void triggerNearbyShimmers(Level level, BlockPos centerPos, int radius) {
        for (BlockPos pos : BlockPos.betweenClosed(
                centerPos.offset(-radius, -radius, -radius),
                centerPos.offset(radius, radius, radius))) {

            BlockState state = level.getBlockState(pos);

            // Wichtig: shimmers triggern läuft über die Basis-Blöcke (Teil 0)
            if (state.getBlock() instanceof HolyShimmerBlock && state.getValue(HEIGHT_PART) == 0 && !state.getValue(ACTIVATED)) {
                if (!level.isClientSide) {
                    level.setBlock(pos, state.setValue(ACTIVATED, true), 3);

                    // Auch hier die Dummys mitziehen
                    for (int i = 1; i < 3; i++) {
                        BlockPos dummyPos = pos.above(i);
                        BlockState dummyState = level.getBlockState(dummyPos);
                        if (dummyState.is(state.getBlock())) {
                            level.setBlock(dummyPos, dummyState.setValue(ACTIVATED, true), 3);
                        }
                    }

                    if (level.getBlockEntity(pos) instanceof HolyShimmerEntity tile) {
                        tile.triggerShatter();
                    }
                }
            }
        }
    }

    // Verhindert, dass Dummys eine eigene BlockEntity bekommen. Nur die Basis (part == 0) kriegt eine.
    @Nullable
    @Override
    public BlockEntity newBlockEntity(@NotNull BlockPos pos, @NotNull BlockState state) {
        if (state.getValue(HEIGHT_PART) == 0) {
            return new HolyShimmerEntity(pos, state);
        }
        return null;
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, @NotNull BlockState state, @NotNull BlockEntityType<T> type) {
        // Ticker läuft ebenfalls nur auf dem echten Basis-Block
        if (!level.isClientSide && state.getValue(ACTIVATED) && state.getValue(HEIGHT_PART) == 0) {
            if (type == ModBlockEntities.HOLY_SHIMMER.get()) {
                return (level1, pos, state1, blockEntity) -> {
                    if (blockEntity instanceof HolyShimmerEntity tile) {
                        HolyShimmerEntity.tick(level1, pos, tile);
                    }
                };
            }
        }
        return null;
    }

    @Override
    public @NotNull BlockState rotate(BlockState state, net.minecraft.world.level.block.Rotation rotation) {
        return state.setValue(FACING, rotation.rotate(state.getValue(FACING)));
    }

    @Override
    public @NotNull BlockState mirror(BlockState state, net.minecraft.world.level.block.Mirror mirror) {
        return state.rotate(mirror.getRotation(state.getValue(FACING)));
    }
}