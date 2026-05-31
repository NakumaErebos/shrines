package net.nakumaerebos.shrines.block.custom;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionResult;
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
import net.nakumaerebos.shrines.block.entity.DungeonDoorBlockEntity;
import net.nakumaerebos.shrines.item.ModItems;
import net.nakumaerebos.shrines.sound.ModSounds;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class DungeonDoorBlock extends BaseEntityBlock {
    public static final BooleanProperty OPEN = BlockStateProperties.OPEN;
    public static final DirectionProperty FACING = BlockStateProperties.HORIZONTAL_FACING;

    public static final MapCodec<DungeonDoorBlock> CODEC = simpleCodec(DungeonDoorBlock::new);

    @Override
    protected @NotNull MapCodec<? extends BaseEntityBlock> codec() {
        return CODEC;
    }

    public DungeonDoorBlock(Properties properties) {
        super(properties);
        this.registerDefaultState(this.stateDefinition.any()
                .setValue(OPEN, false)
                .setValue(FACING, Direction.NORTH));
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        // Richtet die Tür zum Spieler aus
        return this.defaultBlockState().setValue(FACING, context.getHorizontalDirection().getOpposite());
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(OPEN, FACING);
    }

    @Override
    public @NotNull VoxelShape getShape(BlockState state, @NotNull BlockGetter level, @NotNull BlockPos pos, @NotNull CollisionContext context) {
        // Hauptblock verliert seine Hitbox, wenn geöffnet
        return state.getValue(OPEN) ? Shapes.empty() : Shapes.block();
    }

    @Override
    protected @NotNull InteractionResult useWithoutItem(@NotNull BlockState state, Level level, @NotNull BlockPos pos, @NotNull Player player, @NotNull BlockHitResult hitResult) {
        // Wir holen uns das Item aus der Hand, mit der der Spieler geklickt hat
        ItemStack heldItem = player.getItemInHand(player.getUsedItemHand());

        // Bedingung: Spieler ist im Creative-Modus ODER hält den richtigen Schlüssel
        if (player.isCreative() || heldItem.is(ModItems.DUNGEON_KEY.get())) {

            if (!level.isClientSide) {
                boolean newState = !state.getValue(OPEN);

                // 1. Hauptblock umschalten
                level.setBlock(pos, state.setValue(OPEN, newState), 10);

                // 2. Dummies umschalten
                updateDummies(level, pos, state, newState);

                // Wenn der Spieler NICHT im Creative ist, wird ein Schlüssel verbraucht
                if (!player.isCreative()) {
                    heldItem.shrink(1);
                }
            }
            return InteractionResult.SUCCESS;
        }

        // Wenn der Spieler keinen Schlüssel hat und nicht im Creative ist, passiert nichts
        return InteractionResult.PASS;
    }

    @Override
    public void onPlace(BlockState state, @NotNull Level level, @NotNull BlockPos pos, BlockState oldState, boolean isMoving) {
        if (!level.isClientSide) {
            if (!state.is(oldState.getBlock())) {
                Direction facing = state.getValue(FACING);
                // Wechsel zu CounterClockWise, damit es aus Spielersicht rechts platziert wird
                Direction right = facing.getCounterClockWise();

                // Platziere die Dummies (1 nach rechts, 1 nach oben)
                for (int r = 0; r <= 1; r++) {
                    for (int y = 0; y <= 1; y++) {
                        if (r == 0 && y == 0) continue; // Hauptblock überspringen

                        BlockPos dummyPos = pos.relative(right, r).above(y);

                        BlockState dummyState = ModBlocks.DUNGEON_DOOR_DUMMY.get().defaultBlockState()
                                .setValue(DungeonDoorDummyBlock.OFFSET_X, r)
                                .setValue(DungeonDoorDummyBlock.OFFSET_Y, y)
                                .setValue(DungeonDoorDummyBlock.FACING, facing)
                                .setValue(DungeonDoorDummyBlock.OPEN, state.getValue(OPEN));

                        level.setBlock(dummyPos, dummyState, 3);
                    }
                }
            } else if (state.getValue(OPEN) != oldState.getValue(OPEN)) {
                updateDummies(level, pos, state, state.getValue(OPEN));
            }
        }
        super.onPlace(state, level, pos, oldState, isMoving);
    }

    @Override
    public void onRemove(BlockState state, @NotNull Level level, @NotNull BlockPos pos, BlockState newState, boolean isMoving) {
        // Nur ausführen, wenn der Block wirklich zerstört/ersetzt wird (nicht bei State-Änderungen wie OPEN)
        if (!state.is(newState.getBlock())) {
            // HIER GEÄNDERT: Von getClockWise() zu getCounterClockWise() wechseln,
            // damit beim Abbauen auf derselben Seite gesucht wird, auf der sie platziert wurden.
            Direction right = state.getValue(FACING).getCounterClockWise();

            // Alle potenziellen Dummy-Positionen (1 nach rechts, 1 nach oben) ablaufen
            for (int r = 0; r <= 1; r++) {
                for (int y = 0; y <= 1; y++) {
                    // Den Ursprung (Hauptblock) überspringen wir, da Minecraft ihn ohnehin löscht
                    if (r == 0 && y == 0) continue;

                    BlockPos targetPos = pos.relative(right, r).above(y);

                    // Wenn dort ein Dummy steht, entfernen wir ihn
                    if (level.getBlockState(targetPos).getBlock() instanceof DungeonDoorDummyBlock) {
                        level.removeBlock(targetPos, false);
                    }
                }
            }
        }
        super.onRemove(state, level, pos, newState, isMoving);
    }

    private void updateDummies(Level level, BlockPos pos, BlockState state, boolean open) {
        level.playSound(null, pos, ModSounds.SHRINE_DOOR_OPEN.get(),
                SoundSource.BLOCKS, 1.0F, 1.0F);
        Direction facing = state.getValue(FACING);
        Direction right = facing.getCounterClockWise(); // Auch hier anpassen!

        for (int r = 0; r <= 1; r++) {
            for (int y = 0; y <= 1; y++) {
                if (r == 0 && y == 0) continue;

                BlockPos targetPos = pos.relative(right, r).above(y);
                BlockState targetState = level.getBlockState(targetPos);

                if (targetState.getBlock() instanceof DungeonDoorDummyBlock) {
                    level.setBlock(targetPos, targetState.setValue(DungeonDoorDummyBlock.OPEN, open), 3);
                }
            }
        }
    }

    @Override
    public @NotNull RenderShape getRenderShape(@NotNull BlockState state) {
        return RenderShape.ENTITYBLOCK_ANIMATED;
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(@NotNull BlockPos pos, @NotNull BlockState state) {
        return new DungeonDoorBlockEntity(pos, state);
    }

    @Override
    public BlockState rotate(BlockState state, net.minecraft.world.level.block.Rotation rotation) {
        return state.setValue(FACING, rotation.rotate(state.getValue(FACING)));
    }

    @Override
    public BlockState mirror(BlockState state, net.minecraft.world.level.block.Mirror mirror) {
        return state.rotate(mirror.getRotation(state.getValue(FACING)));
    }
}