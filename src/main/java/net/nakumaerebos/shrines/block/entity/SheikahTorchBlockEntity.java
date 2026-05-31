package net.nakumaerebos.shrines.block.entity;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.nakumaerebos.shrines.block.custom.SheikahTorchBlock;
import software.bernie.geckolib.animatable.GeoBlockEntity;
import software.bernie.geckolib.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.animation.*;
import software.bernie.geckolib.util.GeckoLibUtil;

public class SheikahTorchBlockEntity extends BlockEntity implements GeoBlockEntity {
    private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);

    private static final RawAnimation IDLE_UNIGNITED = RawAnimation.begin().thenLoop("animation.sheikah_torch.idle");
    private static final RawAnimation IGNITE = RawAnimation.begin().thenPlay("animation.sheikah_torch.ignite").thenLoop("animation.sheikah_torch.idle_ignited");
    private static final RawAnimation IDLE_IGNITED = RawAnimation.begin().thenLoop("animation.sheikah_torch.idle_ignited");
    private static final RawAnimation UNIGNITE = RawAnimation.begin().thenPlay("animation.sheikah_torch.unignite").thenLoop("animation.sheikah_torch.idle");

    public SheikahTorchBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.SHEIKAH_TORCH_BE.get(), pos, state);
    }

    // Übergibt nun explizit den newState (Target State)
    public void toggleIgnition(boolean newState) {
        if (this.level == null) return;

        BlockState state = this.getBlockState();

        // BlockState updaten
        this.level.setBlock(this.worldPosition, state.setValue(SheikahTorchBlock.IGNITED, newState), 3);

        // Passenden Animations-Trigger feuern
        if (newState) {
            triggerAnim("torches", "ignite_trigger");
        } else {
            triggerAnim("torches", "unignite_trigger");
        }
    }

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
        controllers.add(new AnimationController<>(this, "torches", 4, this::deployAnimationController)
                .triggerableAnim("ignite_trigger", IGNITE)
                .triggerableAnim("unignite_trigger", UNIGNITE)
        );
    }

    private PlayState deployAnimationController(AnimationState<SheikahTorchBlockEntity> state) {
        if (state.getController().getCurrentAnimation() == null) {
            if (this.getBlockState().getValue(SheikahTorchBlock.IGNITED)) {
                return state.setAndContinue(IDLE_IGNITED);
            } else {
                return state.setAndContinue(IDLE_UNIGNITED);
            }
        }
        return PlayState.CONTINUE;
    }

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return this.cache;
    }
}