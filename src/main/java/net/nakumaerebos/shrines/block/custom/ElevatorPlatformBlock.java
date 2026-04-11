package net.nakumaerebos.shrines.block.custom;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.NotNull;

public class ElevatorPlatformBlock extends HorizontalDirectionalBlock {

    public ElevatorPlatformBlock(BlockBehaviour.Properties properties) {
        super(properties);
        // Standardmäßig nach Norden ausrichten
        this.registerDefaultState(this.stateDefinition.any().setValue(FACING, Direction.NORTH));
    }

    @Override
    protected @NotNull MapCodec<? extends HorizontalDirectionalBlock> codec() {
        return null;
    }

    /**
     * WICHTIG: getShape definiert, was das Fadenkreuz (Raytrace) sieht.
     * Durch Shapes.empty() geht jeder Klick "durch" den Block auf das Objekt dahinter.
     */
    @Override
    public VoxelShape getShape(BlockState state, BlockGetter world, BlockPos pos, CollisionContext context) {
        return Shapes.empty();
    }

    /**
     * Definiert die physische Kollision.
     * Hier geben wir eine Form zurück, damit der Spieler auf der Plattform stehen kann.
     */
    @Override
    public VoxelShape getCollisionShape(BlockState state, BlockGetter world, BlockPos pos, CollisionContext context) {
        return Shapes.empty();
    }

    /**
     * Sorgt dafür, dass das Fadenkreuz auch bei Interaktionen (Rechtsklick)
     * den Block ignoriert.
     */
    @Override
    public VoxelShape getInteractionShape(BlockState state, BlockGetter world, BlockPos pos) {
        return Shapes.empty();
    }

    // -- Licht- und Render-Einstellungen --

    @Override
    public float getShadeBrightness(BlockState state, BlockGetter world, BlockPos pos) {
        return 1.0F;
    }

    @Override
    public boolean propagatesSkylightDown(BlockState state, BlockGetter world, BlockPos pos) {
        return true;
    }

    // -- Rotations-Logik --

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        // Richtet den Block beim Platzieren gegenüber vom Spieler aus
        return this.defaultBlockState().setValue(FACING, context.getHorizontalDirection().getOpposite());
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FACING);
    }
}