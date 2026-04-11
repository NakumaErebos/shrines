package net.nakumaerebos.shrines.block.custom;

import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import com.mojang.serialization.MapCodec;
import org.jetbrains.annotations.NotNull;

public class SheikahStateBlock extends Block {
    public static final IntegerProperty STATE = IntegerProperty.create("state", 0, 3);

    public static final MapCodec<SheikahStateBlock> CODEC = simpleCodec(SheikahStateBlock::new);

    @Override
    public @NotNull MapCodec<? extends SheikahStateBlock> codec() {
        return CODEC;
    }

    public SheikahStateBlock(BlockBehaviour.Properties properties) {
        super(properties);
        this.registerDefaultState(this.stateDefinition.any().setValue(STATE, 0));
    }

    @Override
    public BlockState getStateForPlacement(@NotNull BlockPlaceContext context) {
        // Wird beim Platzieren immer mit State 0 gesetzt
        return this.defaultBlockState().setValue(STATE, 0);
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        // Registriert die Property beim Block
        builder.add(STATE);
    }
}