package net.nakumaerebos.shrines.block.custom;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
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
    public static final BooleanProperty IS_EDGE = BooleanProperty.create("is_edge"); // Neu
    public static final BooleanProperty HAS_NO_VOXELSHAPE = BooleanProperty.create("has_no_voxelshape"); // Neu

    // Hitbox ähnlich wie ein schmales Gitter (X/Z Achse je nach Rotation)
    protected static final VoxelShape SHAPE_NORTH_SOUTH = Block.box(7.0D, 0.0D, 0.0D, 9.0D, 48.0D, 16.0D);
    protected static final VoxelShape SHAPE_EAST_WEST = Block.box(0.0D, 0.0D, 7.0D, 16.0D, 48.0D, 9.0D);

    public HolyShimmerBlock(Properties properties) {
        super(properties);
        this.registerDefaultState(this.stateDefinition.any()
                .setValue(FACING, Direction.NORTH)
                .setValue(ACTIVATED, false)
                .setValue(IS_EDGE, false)
                .setValue(HAS_NO_VOXELSHAPE, false));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FACING, ACTIVATED, IS_EDGE, HAS_NO_VOXELSHAPE);
    }

    @Override
    public @NotNull VoxelShape getShape(BlockState state, @NotNull BlockGetter world, @NotNull BlockPos pos, @NotNull CollisionContext context) {
        Boolean hvs = state.getValue(HAS_NO_VOXELSHAPE);
        if (hvs){
            return Shapes.empty();
        }
        Direction dir = state.getValue(FACING);
        return (dir == Direction.NORTH || dir == Direction.SOUTH) ? SHAPE_EAST_WEST : SHAPE_NORTH_SOUTH;
    }

    @Override
    public @NotNull VoxelShape getInteractionShape(BlockState state, @NotNull BlockGetter world, @NotNull BlockPos pos) {
        Boolean hvs = state.getValue(HAS_NO_VOXELSHAPE);
        if (hvs){
            return Shapes.empty();
        }
        Direction dir = state.getValue(FACING);
        return (dir == Direction.NORTH || dir == Direction.SOUTH) ? SHAPE_EAST_WEST : SHAPE_NORTH_SOUTH;
    }

    @Override
    public @NotNull VoxelShape getCollisionShape(BlockState state, @NotNull BlockGetter world, @NotNull BlockPos pos, @NotNull CollisionContext context) {
        Boolean hvs = state.getValue(HAS_NO_VOXELSHAPE);
        if (hvs){
            return Shapes.empty();
        }
        Direction dir = state.getValue(FACING);
        return (dir == Direction.NORTH || dir == Direction.SOUTH) ? SHAPE_EAST_WEST : SHAPE_NORTH_SOUTH;
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        BlockPos pos = context.getClickedPos();
        Level level = context.getLevel();
        // Beispiel: Wenn kein Block unter ihm ist, ist es eine Kante
        boolean edge = level.getBlockState(pos.below()).isAir();

        return this.defaultBlockState()
                .setValue(FACING, context.getHorizontalDirection().getOpposite())
                .setValue(IS_EDGE, edge);
    }

    @Override
    protected @NotNull InteractionResult useWithoutItem(@NotNull BlockState state, Level level, @NotNull BlockPos pos, @NotNull Player player, @NotNull BlockHitResult hit) {
        if (!level.isClientSide) {
            // Auf dem Server den State ändern
            level.setBlock(pos, state.setValue(ACTIVATED, true), 3);

            // Animation triggern
            if (level.getBlockEntity(pos) instanceof HolyShimmerEntity tile) {
                level.playSound(null, pos, ModSounds.HOLY_SHIMMER_SHATTER.get(),
                        SoundSource.BLOCKS, 1.0F, 1.0F);
                // Das hier triggert die Animation für alle Clients in der Nähe
                triggerNearbyShimmers(level, pos, 5);
                tile.triggerShatter();
            }
        }
        return InteractionResult.SUCCESS;
    }

    public static void triggerNearbyShimmers(Level level, BlockPos centerPos, int radius) {
        // BlockPos.betweenClosed erstellt einen quaderförmigen Bereich
        for (BlockPos pos : BlockPos.betweenClosed(
                centerPos.offset(-radius, -radius, -radius),
                centerPos.offset(radius, radius, radius))) {

            // Wir prüfen JEDEN Block innerhalb der Box (ohne kreisförmige Distanzprüfung)
            BlockState state = level.getBlockState(pos);

            if (state.getBlock() instanceof HolyShimmerBlock && !state.getValue(HolyShimmerBlock.ACTIVATED)) {
                if (!level.isClientSide) {
                    level.setBlock(pos, state.setValue(HolyShimmerBlock.ACTIVATED, true), 3);

                    if (level.getBlockEntity(pos) instanceof HolyShimmerEntity tile) {
                        tile.triggerShatter();
                    }
                }
            }
        }
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(@NotNull BlockPos pos, @NotNull BlockState state) {
        return new HolyShimmerEntity(pos, state);
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, @NotNull BlockState state, @NotNull BlockEntityType<T> type) {
        // 1. Prüfen, ob wir auf dem Server sind und der Block aktiviert ist
        if (!level.isClientSide && state.getValue(ACTIVATED)) {
            // 2. Prüfen, ob der BlockEntityType der richtige für unsere Tile ist
            if (type == ModBlockEntities.HOLY_SHIMMER.get()) {
                // 3. Den Cast sicher durchführen und den Ticker zurückgeben
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