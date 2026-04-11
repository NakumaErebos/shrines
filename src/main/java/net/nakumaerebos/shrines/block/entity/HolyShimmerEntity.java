package net.nakumaerebos.shrines.block.entity;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.nakumaerebos.shrines.block.custom.HolyShimmerBlock;
import software.bernie.geckolib.animatable.GeoBlockEntity;
import software.bernie.geckolib.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.animation.AnimatableManager;
import software.bernie.geckolib.animation.AnimationController;
import software.bernie.geckolib.animation.PlayState;
import software.bernie.geckolib.animation.RawAnimation;
import software.bernie.geckolib.util.GeckoLibUtil;

public class HolyShimmerEntity extends BlockEntity implements GeoBlockEntity {
    private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);
    private int removalTimer = -1;

    public HolyShimmerEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.HOLY_SHIMMER.get(), pos, state);
    }

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
        controllers.add(new AnimationController<>(this, "controller", 0, state -> PlayState.STOP)
                .triggerableAnim("shatter", RawAnimation.begin().thenPlayAndHold("shatter"))
                .triggerableAnim("shatter_edge", RawAnimation.begin().thenPlayAndHold("shatter_edge"))
        );
    }

    public void triggerShatter() {
        boolean isEdge = this.getBlockState().getValue(HolyShimmerBlock.IS_EDGE);

        if (isEdge) {
            // Der Name muss exakt mit der ID in der .animation.json übereinstimmen
            triggerAnim("controller", "shatter_edge");
        } else {
            triggerAnim("controller", "shatter");
        }

        this.removalTimer = 16;
        this.setChanged();
    }

    // Tick-Methode muss im Block registriert oder via NeoForge Event aufgerufen werden
    public static void tick(Level level, BlockPos pos, BlockState state, HolyShimmerEntity tile) {
        if (tile.removalTimer > 0) {
            tile.removalTimer--;
            if (tile.removalTimer == 0) {
                level.setBlock(pos, Blocks.AIR.defaultBlockState(), 3);
            }
        }
    }

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return cache;
    }
}