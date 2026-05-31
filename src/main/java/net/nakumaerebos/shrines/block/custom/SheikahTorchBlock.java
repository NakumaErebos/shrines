package net.nakumaerebos.shrines.block.custom;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.player.Player;
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
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.nakumaerebos.shrines.block.ModBlocks;
import net.nakumaerebos.shrines.block.entity.SheikahTorchBlockEntity;
import net.nakumaerebos.shrines.effect.ModEffects;
import org.jetbrains.annotations.Nullable;

public class SheikahTorchBlock extends BaseEntityBlock {
    public static final MapCodec<SheikahTorchBlock> CODEC = simpleCodec(SheikahTorchBlock::new);

    public static final BooleanProperty IGNITED = BooleanProperty.create("ignited");

    public static final IntegerProperty DURATION = IntegerProperty.create("duration", 0, 30);
    public static final IntegerProperty AMPLIFIER = IntegerProperty.create("amplifier", 0, 5);

    private static final VoxelShape SHAPE = Block.box(6.0, 0.0, 6.0, 10.0, 13.0, 10.0);

    @Override
    protected MapCodec<? extends BaseEntityBlock> codec() {
        return CODEC;
    }

    public SheikahTorchBlock(Properties properties) {
        // Hier modifizieren wir die übergebenen Properties so, dass die Lichtstufe dynamisch ermittelt wird
        super(properties.lightLevel(state -> state.getValue(IGNITED) ? 8 : 0));

        this.registerDefaultState(this.stateDefinition.any()
                .setValue(IGNITED, false)
                .setValue(DURATION, 5)
                .setValue(AMPLIFIER, 0));
    }

    @Override
    protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return SHAPE;
    }

    @Override
    protected RenderShape getRenderShape(BlockState state) {
        return RenderShape.MODEL;
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hitResult) {
        if (level.getBlockEntity(pos) instanceof SheikahTorchBlockEntity torchTile) {
            boolean isIgnited = state.getValue(IGNITED);

            if (!isIgnited) {
                if (player.isCreative() || player.hasEffect(ModEffects.BLUE_FLAMES)) {
                    if (!level.isClientSide) {
                        torchTile.toggleIgnition(true);
                        openDungeonDoorsInRadius(level, pos, 3);
                    }
                    return InteractionResult.SUCCESS;
                }
            } else {
                if (player.isCreative()) {
                    if (!level.isClientSide) {
                        torchTile.toggleIgnition(false);
                    }
                    return InteractionResult.SUCCESS;
                } else {
                    if (!level.isClientSide) {
                        int durationInTicks = state.getValue(DURATION) * 20;
                        int amp = state.getValue(AMPLIFIER);

                        if (durationInTicks > 0) {
                            player.addEffect(new MobEffectInstance(ModEffects.BLUE_FLAMES, durationInTicks, amp));
                        }
                    }
                    return InteractionResult.SUCCESS;
                }
            }
        }
        return InteractionResult.PASS;
    }

    private void openDungeonDoorsInRadius(Level level, BlockPos torchPos, int radius) {
        for (int x = -radius; x <= radius; x++) {
            for (int y = -radius; y <= radius; y++) {
                for (int z = -radius; z <= radius; z++) {
                    BlockPos checkPos = torchPos.offset(x, y, z);
                    BlockState targetState = level.getBlockState(checkPos);

                    if (targetState.is(ModBlocks.DUNGEON_DOOR.get())) {
                        if (targetState.hasProperty(BlockStateProperties.OPEN) && !targetState.getValue(BlockStateProperties.OPEN)) {
                            level.setBlock(checkPos, targetState.setValue(BlockStateProperties.OPEN, true), 3);
                        }
                    }
                }
            }
        }
    }

    @Nullable
    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        return this.defaultBlockState().setValue(IGNITED, false).setValue(DURATION, 5).setValue(AMPLIFIER, 0);
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(IGNITED, DURATION, AMPLIFIER);
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new SheikahTorchBlockEntity(pos, state);
    }
}