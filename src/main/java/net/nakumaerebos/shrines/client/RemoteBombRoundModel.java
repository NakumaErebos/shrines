package net.nakumaerebos.shrines.client;

import net.minecraft.resources.ResourceLocation;
import net.nakumaerebos.shrines.Shrines;
import net.nakumaerebos.shrines.entity.RemoteBombRoundEntity;
import software.bernie.geckolib.model.GeoModel;

public class RemoteBombRoundModel extends GeoModel<RemoteBombRoundEntity> {

    @Override
    public ResourceLocation getModelResource(RemoteBombRoundEntity animatable) {
        return ResourceLocation.fromNamespaceAndPath(Shrines.MOD_ID, "geo/remote_bomb_round.geo.json");
    }

    @Override
    public ResourceLocation getTextureResource(RemoteBombRoundEntity animatable) {
        return ResourceLocation.fromNamespaceAndPath(Shrines.MOD_ID, "textures/entity/remote_bomb_round.png");
    }

    @Override
    public ResourceLocation getAnimationResource(RemoteBombRoundEntity animatable) {
        return ResourceLocation.fromNamespaceAndPath(Shrines.MOD_ID, "animations/remote_bomb_round.animation.json");
    }

    @Override
    public void setCustomAnimations(RemoteBombRoundEntity animatable, long instanceId, software.bernie.geckolib.animation.AnimationState<RemoteBombRoundEntity> animationState) {
        super.setCustomAnimations(animatable, instanceId, animationState);

        // Holt sich den Hauptknochen aus deiner GeoModel-Datei (.geo.json)
        // Wenn dein Hauptknochen in Blockbench nicht "root" heißt, passe den Namen hier an!
        software.bernie.geckolib.cache.object.GeoBone rootBone = this.getAnimationProcessor().getBone("root");

        if (rootBone != null) {
            // Holt den berechneten Pitch aus der Entity und rechnet ihn in Bogenmaß um
            float pitchInRadians = (float) Math.toRadians(animatable.getCurrentRollPitch());

            // Weist dem Knochen direkt die Rotation für diesen Frame zu
            rootBone.setRotX(pitchInRadians);
        }
    }
}