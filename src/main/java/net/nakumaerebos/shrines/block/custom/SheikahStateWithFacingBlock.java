package net.nakumaerebos.shrines.block.custom;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.Direction;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import org.jetbrains.annotations.NotNull;

public class SheikahStateWithFacingBlock extends HorizontalDirectionalBlock {
    // Übernimmt die State-Property von deiner Basis-Klasse
    public static final IntegerProperty STATE = SheikahStateBlock.STATE;
    // Standard Horizontal Facing (North, South, East, West)
    public static final DirectionProperty FACING = BlockStateProperties.HORIZONTAL_FACING;

    public static final MapCodec<SheikahStateWithFacingBlock> CODEC = simpleCodec(SheikahStateWithFacingBlock::new);

    @Override
    public @NotNull MapCodec<? extends SheikahStateWithFacingBlock> codec() {
        return CODEC;
    }

    public SheikahStateWithFacingBlock(BlockBehaviour.Properties properties) {
        super(properties);
        this.registerDefaultState(this.stateDefinition.any()
                .setValue(FACING, Direction.NORTH)
                .setValue(STATE, 0));
    }

    @Override
    public BlockState getStateForPlacement(@NotNull BlockPlaceContext context) {
        // Setzt die Blickrichtung basierend auf der Spieler-Ausrichtung
        return this.defaultBlockState()
                .setValue(FACING, context.getHorizontalDirection().getOpposite())
                .setValue(STATE, 0);
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        // Registriert beide Properties
        builder.add(FACING, STATE);
    }

    // Hilfsmethoden für Standard-Block-Interaktionen (Rotation/Spiegeln)
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