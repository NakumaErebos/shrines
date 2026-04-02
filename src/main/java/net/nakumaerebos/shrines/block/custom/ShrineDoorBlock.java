package net.nakumaerebos.shrines.block.custom;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
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
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.nakumaerebos.shrines.block.ModBlocks;
import net.nakumaerebos.shrines.block.entity.ShrineDoorBlockEntity;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class ShrineDoorBlock extends BaseEntityBlock {
    // Der State für Offen/Zu
    public static final BooleanProperty OPEN = BlockStateProperties.OPEN;

    public ShrineDoorBlock(Properties properties) {
        super(properties);
        this.registerDefaultState(this.stateDefinition.any().setValue(OPEN, Boolean.FALSE));
    }

    @Override
    protected @NotNull MapCodec<? extends BaseEntityBlock> codec() {
        // Normalerweise registrierst du hier deinen Codec für NeoForge
        return simpleCodec(ShrineDoorBlock::new);
    }

    // Im ShrineDoorBlock
    @Override
    public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        // Wenn zu: Voller Block. Wenn offen: Leer (man kann durchlaufen)
        return state.getValue(OPEN) ? Shapes.empty() : Shapes.block();
    }

    @Override
    protected @NotNull InteractionResult useWithoutItem(@NotNull BlockState state, Level level, @NotNull BlockPos pos, @NotNull Player player, @NotNull BlockHitResult hitResult) {
        if (!level.isClientSide) {
            // Zustand umschalten
            state = state.cycle(OPEN);
            level.setBlock(pos, state, 10); // 10 = Block Update + Send to Clients

            // Optional: Sound abspielen
            // float pitch = state.getValue(OPEN) ? 0.6f : 0.5f;
            // level.playSound(null, pos, SoundEvents.IRON_GATE_OPEN, SoundSource.BLOCKS, 1.0f, pitch);
        }
        return InteractionResult.SUCCESS;
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(OPEN);
    }

    @Override
    public @NotNull RenderShape getRenderShape(@NotNull BlockState state) {
        // Da wir GeckoLib nutzen (BlockEntityRenderer), muss der Block selbst "invisible" sein
        return RenderShape.ENTITYBLOCK_ANIMATED;
    }

    @Override
    public @Nullable BlockEntity newBlockEntity(@NotNull BlockPos pos, @NotNull BlockState state) {
        return new ShrineDoorBlockEntity(pos, state);
    }

    @Override
    public void setPlacedBy(Level level, BlockPos pos, BlockState state, @Nullable LivingEntity placer, ItemStack stack) {
        super.setPlacedBy(level, pos, state, placer, stack);
        if (!level.isClientSide) {
            // Wir platzieren Dummies in einem 3x3 Raster (Mitte unten ist der Hauptblock)
            // x: -1, 0, 1 | y: 0, 1, 2
            for (int x = -1; x <= 1; x++) {
                for (int y = 0; y <= 2; y++) {
                    BlockPos targetPos = pos.offset(x, y, 0);
                    if (targetPos.equals(pos)) continue; // Hauptblock nicht überschreiben

                    level.setBlock(targetPos, ModBlocks.SHRINE_DOOR_DUMMY.get().defaultBlockState(), 3);
                }
            }
        }
    }

    @Override
    public void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState, boolean isMoving) {
        if (!state.is(newState.getBlock())) {
            // Wenn der Hauptblock zerstört wird, entferne alle Dummies drumherum
            for (int x = -1; x <= 1; x++) {
                for (int y = 0; y <= 2; y++) {
                    BlockPos targetPos = pos.offset(x, y, 0);
                    if (level.getBlockState(targetPos).getBlock() instanceof ShrineDoorDummyBlock) {
                        level.removeBlock(targetPos, false);
                    }
                }
            }
        }
        super.onRemove(state, level, pos, newState, isMoving);
    }
}