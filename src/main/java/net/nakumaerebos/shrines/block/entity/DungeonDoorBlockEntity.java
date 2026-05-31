package net.nakumaerebos.shrines.block.entity;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.nakumaerebos.shrines.block.custom.DungeonDoorBlock; // Pfad ggf. anpassen
import software.bernie.geckolib.animatable.GeoBlockEntity;
import software.bernie.geckolib.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.animation.*;
import software.bernie.geckolib.util.GeckoLibUtil;

public class DungeonDoorBlockEntity extends BlockEntity implements GeoBlockEntity {
    private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);

    public DungeonDoorBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.DUNGEON_DOOR_BE.get(), pos, state);
    }

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
        controllers.add(new AnimationController<>(this, "door_controller", 0, this::deployAnimationController));
    }

    private PlayState deployAnimationController(AnimationState<DungeonDoorBlockEntity> state) {
        // Wir prüfen den aktuellen BlockState, um zu wissen, ob die Tür offen sein soll
        if (this.getBlockState().getValue(DungeonDoorBlock.OPEN)) {
            // Spielt die Open-Animation ab. Wenn sie vorbei ist, bleibt sie im letzten Frame (HoldOnLastFrame)
            return state.setAndContinue(RawAnimation.begin().then("dungeon_door.animation.open", Animation.LoopType.HOLD_ON_LAST_FRAME));
        } else {
            // Wenn geschlossen, setzen wir die Animation zurück (oder spielen im Idle, falls vorhanden)
            return state.setAndContinue(RawAnimation.begin().then("dungeon_door.animation.idle", Animation.LoopType.HOLD_ON_LAST_FRAME));
        }
    }

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return this.cache;
    }
}