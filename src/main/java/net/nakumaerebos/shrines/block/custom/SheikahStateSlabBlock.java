package net.nakumaerebos.shrines.block.custom;

import com.mojang.serialization.MapCodec;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SlabBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.level.block.state.properties.SlabType;
import org.jetbrains.annotations.NotNull;

public class SheikahStateSlabBlock extends SlabBlock {
    public static final IntegerProperty STATE = SheikahStateBlock.STATE;

    // Slabs brauchen keinen base_state im Codec, simpleCodec reicht hier völlig aus
    public static final MapCodec<SheikahStateSlabBlock> CODEC = simpleCodec(SheikahStateSlabBlock::new);

    @Override
    public @NotNull MapCodec<? extends SheikahStateSlabBlock> codec() {
        return CODEC;
    }

    public SheikahStateSlabBlock(Properties properties) {
        super(properties);
        this.registerDefaultState(this.stateDefinition.any()
                .setValue(TYPE, SlabType.BOTTOM)
                .setValue(WATERLOGGED, false)
                .setValue(STATE, 0));
    }

    @Override
    public BlockState getStateForPlacement(@NotNull BlockPlaceContext context) {
        // SlabBlock berechnet hier automatisch, ob TOP, BOTTOM oder DOUBLE
        BlockState state = super.getStateForPlacement(context);
        return state != null ? state.setValue(STATE, 0) : null;
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.@NotNull Builder<Block, BlockState> builder) {
        super.createBlockStateDefinition(builder); // Registriert TYPE und WATERLOGGED
        builder.add(STATE);
    }
}