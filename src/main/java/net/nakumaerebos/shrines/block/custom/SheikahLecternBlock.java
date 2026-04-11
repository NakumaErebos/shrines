package net.nakumaerebos.shrines.block.custom;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
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
    public VoxelShape getShape(BlockState state, BlockGetter world, BlockPos pos, CollisionContext context) {
        return SHAPE;
    }

    @Override
    protected ItemInteractionResult useItemOn(ItemStack stack, BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hitResult) {
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
    protected void tick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
        // 1. Schritt: Modell wechseln (nach den ersten 0.75s)
        if (!state.getValue(ACTIVATED)) {
            level.setBlock(pos, state.setValue(ACTIVATED, true), 3);
            // 2. Schritt: Zweiten Tick für die Tür in 0.5s (10 Ticks) planen
            level.scheduleTick(pos, this, 10);
        }
        // 3. Schritt: Nach der Aktivierung (die weiteren 0.5s sind um)
        else {
            openNearbyShrineDoors(level, pos);
        }
    }

    private void openNearbyShrineDoors(Level level, BlockPos pos) {
        int radius = 5;
        // Wir iterieren durch einen Bereich von 5 Blöcken in jede Richtung
        for (BlockPos targetPos : BlockPos.betweenClosed(pos.offset(-radius, -radius, -radius), pos.offset(radius, radius, radius))) {
            BlockState targetState = level.getBlockState(targetPos);

            // Prüfen, ob der Block eine Shrine-Tür ist
            // Ich nutze hier den Klassennamen deiner Tür-Klasse (bitte ggf. anpassen)
            if (targetState.getBlock() instanceof ShrineDoorBlock) {
                if (targetState.hasProperty(BlockStateProperties.OPEN) && !targetState.getValue(BlockStateProperties.OPEN)) {
                    // Tür auf OPEN setzen
                    level.setBlock(targetPos, targetState.setValue(BlockStateProperties.OPEN, true), 3);
                }
            }
        }
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
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