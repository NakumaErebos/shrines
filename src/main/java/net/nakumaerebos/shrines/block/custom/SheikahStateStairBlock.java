package net.nakumaerebos.shrines.block.custom;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.Direction;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.StairBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.Half;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.level.block.state.properties.StairsShape;
import org.jetbrains.annotations.NotNull;

public class SheikahStateStairBlock extends StairBlock {
    public static final IntegerProperty STATE = SheikahStateBlock.STATE;

    public static final MapCodec<SheikahStateStairBlock> CODEC = RecordCodecBuilder.mapCodec(
            inst -> inst.group(
                    BlockState.CODEC.fieldOf("base_state").forGetter(s -> s.baseState),
                    propertiesCodec()
            ).apply(inst, SheikahStateStairBlock::new)
    );

    @Override
    public @NotNull MapCodec<? extends SheikahStateStairBlock> codec() {
        return CODEC;
    }

    public SheikahStateStairBlock(BlockState baseState, Properties properties) {
        super(baseState, properties);
        this.registerDefaultState(this.stateDefinition.any()
                .setValue(FACING, Direction.NORTH)
                .setValue(HALF, Half.BOTTOM)
                .setValue(SHAPE, StairsShape.STRAIGHT)
                .setValue(STATE, 0));
    }

    @Override
    public @NotNull BlockState getStateForPlacement(@NotNull BlockPlaceContext context) {
        BlockState state = super.getStateForPlacement(context);
        return state.setValue(STATE, 0);
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.@NotNull Builder<Block, BlockState> builder) {
        super.createBlockStateDefinition(builder);
        builder.add(STATE);
    }
}