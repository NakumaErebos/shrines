package net.nakumaerebos.shrines.block.custom;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.nakumaerebos.shrines.block.entity.SheikahLecternBlockEntity;
import net.nakumaerebos.shrines.sound.ModSounds;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class SheikahLecternBlock extends Block implements EntityBlock {
    public static final DirectionProperty FACING = HorizontalDirectionalBlock.FACING;
    public static final BooleanProperty ACTIVATED = BooleanProperty.create("activated");

    // Die VoxelShape entspricht in etwa der eines Lecterns
    protected static final VoxelShape SHAPE = Block.box(0.0D, 0.0D, 0.0D, 16.0D, 12.0D, 16.0D);

    public SheikahLecternBlock(BlockBehaviour.Properties properties) {
        super(properties);
        this.registerDefaultState(this.stateDefinition.any()
                .setValue(FACING, Direction.NORTH)
                .setValue(ACTIVATED, false));
    }

    @Override
    public @NotNull VoxelShape getShape(@NotNull BlockState state, @NotNull BlockGetter world, @NotNull BlockPos pos, @NotNull CollisionContext context) {
        return SHAPE;
    }

    @Override
    protected @NotNull ItemInteractionResult useItemOn(@NotNull ItemStack stack, BlockState state, @NotNull Level level, @NotNull BlockPos pos, @NotNull Player player, @NotNull InteractionHand hand, @NotNull BlockHitResult hitResult) {
        if (state.getValue(ACTIVATED)) {
            return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
        }

        if (stack.getItem().getDescriptionId().contains("sheikahslate")) {
            if (level.getBlockEntity(pos) instanceof SheikahLecternBlockEntity tile) {
                if (!level.isClientSide) {
                    // 1. Sofort die Animation starten
                    tile.triggerBlingAnimation();
                    level.playSound(null, pos, ModSounds.AUTHENTICATION_BING.get(),
                            SoundSource.BLOCKS, 1.0F, 1.0F);
                    // 2. Einen Tick in 0.75 Sek (15 Ticks) planen
                    level.scheduleTick(pos, this, 15);
                }
                return ItemInteractionResult.sidedSuccess(level.isClientSide);
            }
        }
        return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
    }

    @Override
    protected void tick(BlockState state, @NotNull ServerLevel level, @NotNull BlockPos pos, @NotNull RandomSource random) {
        if (!state.getValue(ACTIVATED)) {
            level.setBlock(pos, state.setValue(ACTIVATED, true), 3);
            level.scheduleTick(pos, this, 10);
        } else {
            // Hier rufen wir die neue kombinierte Aktivierungs-Logik auf
            activateNearbyShrineMechanisms(level, pos);
        }
    }

    private void activateNearbyShrineMechanisms(Level level, BlockPos pos) {
        int radius = 12;
        double radiusSq = radius * radius; // Für den euklidischen Vergleich (Distanz im Quadrat ist performanter)

        // Wir suchen in einem 8x8x8 Bereich
        for (BlockPos targetPos : BlockPos.betweenClosed(pos.offset(-radius, -radius, -radius), pos.offset(radius, radius, radius))) {

            // Euklidische Distanz prüfen: (x2-x1)^2 + (y2-y1)^2 + (z2-z1)^2 <= r^2
            if (targetPos.distSqr(pos) <= radiusSq) {
                BlockState targetState = level.getBlockState(targetPos);

                // 1. Logik für die Shrine-Tür (radius war vorher 5, jetzt 8)
                if (targetState.getBlock() instanceof ShrineDoorBlock) {
                    if (targetState.hasProperty(BlockStateProperties.OPEN) && !targetState.getValue(BlockStateProperties.OPEN)) {
                        level.setBlock(targetPos, targetState.setValue(BlockStateProperties.OPEN, true), 3);
                    }
                }

                // 2. Logik für die SheikahState Blöcke
                if (targetState.getBlock() instanceof SheikahStateBlock) {
                    int currentState = targetState.getValue(SheikahStateBlock.STATE);
                    if (currentState == 2) {
                        // Von State 2 (blau/aktivierend) auf State 3 (aktiviert) setzen
                        level.setBlock(targetPos, targetState.setValue(SheikahStateBlock.STATE, 3), 3);
                    }
                }

                if (targetState.getBlock() instanceof SheikahStateSlabBlock) {
                    int currentState = targetState.getValue(SheikahStateSlabBlock.STATE);
                    if (currentState == 2) {
                        level.setBlock(targetPos, targetState.setValue(SheikahStateSlabBlock.STATE, 3), 3);
                    }
                }
            }
        }
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(@NotNull BlockPos pos, @NotNull BlockState state) {
        return new SheikahLecternBlockEntity(pos, state);
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FACING, ACTIVATED);
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        return this.defaultBlockState().setValue(FACING, context.getHorizontalDirection());
    }

    @Override
    protected @NotNull BlockState mirror(BlockState state, net.minecraft.world.level.block.@NotNull Mirror mirror) {
        // In 1.21.1 nutzt man die Methode des States,
        // die intern die korrekte Rotation basierend auf dem Mirror berechnet.
        return state.mirror(mirror);
    }

    @Override
    protected @NotNull BlockState rotate(BlockState state, net.minecraft.world.level.block.Rotation rotation) {
        // Auch für die Rotation selbst nutzt man den State
        return state.setValue(FACING, rotation.rotate(state.getValue(FACING)));
    }
}