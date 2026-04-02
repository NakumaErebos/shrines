package net.nakumaerebos.shrines.block.entity;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.nakumaerebos.shrines.block.custom.ShrineDoorBlock;
import software.bernie.geckolib.animatable.GeoBlockEntity;
import software.bernie.geckolib.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.animation.AnimatableManager;
import software.bernie.geckolib.animation.AnimationController;
import software.bernie.geckolib.animation.RawAnimation;
import software.bernie.geckolib.util.GeckoLibUtil;

public class ShrineDoorBlockEntity extends BlockEntity implements GeoBlockEntity {
    // Der Cache für GeckoLib, um die Animations-Instanz zu speichern
    private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);

    // Definition der Animationen (Namen müssen exakt wie in Blockbench sein)
    protected static final RawAnimation OPEN_ANIM = RawAnimation.begin().thenPlayAndHold("animation.shrine_door.open");
    protected static final RawAnimation CLOSE_ANIM = RawAnimation.begin().thenPlayAndHold("animation.gate.close");

    public ShrineDoorBlockEntity(BlockPos pos, BlockState state) {
        // Hier musst du deinen registrierten BlockEntityType einfügen
        super(ModBlockEntities.SHRINE_DOOR_BE.get(), pos, state);
    }

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
        controllers.add(new AnimationController<>(this, "gateController", 5, state -> {
            // Wir prüfen den BlockState "OPEN" aus der ShrineDoorBlock Klasse
            boolean isOpen = getBlockState().getValue(ShrineDoorBlock.OPEN);

            // Spiele die entsprechende Animation ab
            return state.setAndContinue(isOpen ? OPEN_ANIM : CLOSE_ANIM);
        }));
    }

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return this.cache;
    }
}