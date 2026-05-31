package net.nakumaerebos.shrines.block.custom;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.ChestBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.ChestType;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.nakumaerebos.shrines.block.entity.ModBlockEntities;
import net.nakumaerebos.shrines.block.entity.ShrineChestBlockEntity;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class ShrineChestBlock extends ChestBlock {

    protected static final VoxelShape NORTH_AABB = Block.box(1.0, 0.0, 2.5, 15.0, 9.5, 13.5);
    protected static final VoxelShape WEST_AABB = Block.box(2.5, 0.0, 1.0, 13.5, 9.5, 15.0);

    public static final BooleanProperty FILLED = BooleanProperty.create("filled");

    public static final MapCodec<ShrineChestBlock> CODEC = simpleCodec(properties ->
            new ShrineChestBlock(properties, ModBlockEntities.SHRINE_CHEST_BE::get));

    public ShrineChestBlock(Properties properties, java.util.function.Supplier<BlockEntityType<? extends net.minecraft.world.level.block.entity.ChestBlockEntity>> blockEntityType) {
        super(properties, blockEntityType);
        this.registerDefaultState(this.stateDefinition.any()
                .setValue(FACING, Direction.NORTH)
                .setValue(TYPE, ChestType.SINGLE)
                .setValue(WATERLOGGED, false)
                .setValue(FILLED, true));
    }

    @Override
    public MapCodec<? extends ChestBlock> codec() {
        return CODEC;
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        FluidState fluidstate = context.getLevel().getFluidState(context.getClickedPos());
        return this.defaultBlockState()
                .setValue(FACING, context.getHorizontalDirection())
                .setValue(TYPE, ChestType.SINGLE)
                .setValue(WATERLOGGED, fluidstate.getType() == Fluids.WATER)
                .setValue(FILLED, true);
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        super.createBlockStateDefinition(builder);
        builder.add(FILLED);
    }

    // WICHTIG: Überschreibt das Vanilla-System, das nach einer zweiten Kistenhälfte sucht.
    // Wir geben direkt unsere eigene BlockEntity als MenuProvider zurück.
    @Nullable
    @Override
    protected MenuProvider getMenuProvider(@NotNull BlockState state, @NotNull Level level, @NotNull BlockPos pos) {
        BlockEntity blockEntity = level.getBlockEntity(pos);
        if (blockEntity instanceof MenuProvider menuProvider) {
            return menuProvider;
        }
        return null;
    }

    @Override
    protected @NotNull InteractionResult useWithoutItem(BlockState state, @NotNull Level level, @NotNull BlockPos pos, @NotNull Player player, @NotNull BlockHitResult hitResult) {
        // Interaktions-Schutz: Nur öffnen, wenn FILLED=true oder im Creative-Modus
        if (state.getValue(FILLED) || player.isCreative()) {
            return super.useWithoutItem(state, level, pos, player, hitResult);
        }
        return InteractionResult.PASS;
    }

    @Override
    public BlockEntity newBlockEntity(@NotNull BlockPos pos, @NotNull BlockState state) {
        return new ShrineChestBlockEntity(pos, state);
    }

    @Override
    protected @NotNull VoxelShape getShape(BlockState state, @NotNull BlockGetter level, @NotNull BlockPos pos, @NotNull CollisionContext context) {
        // Holt die aktuelle Himmelsrichtung des Blocks aus dem BlockState
        Direction facing = state.getValue(FACING);

        return switch (facing) {
            case NORTH -> NORTH_AABB;
            case SOUTH -> NORTH_AABB;
            case WEST  -> WEST_AABB;
            case EAST  -> WEST_AABB;
            default    -> NORTH_AABB;
        };
    }
}