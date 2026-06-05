package net.nakumaerebos.shrines.client.models;

import net.minecraft.resources.ResourceLocation;
import net.nakumaerebos.shrines.Shrines;
import net.nakumaerebos.shrines.entity.CryonisPillarEntity;
import org.jetbrains.annotations.Nullable;
import software.bernie.geckolib.animation.AnimationState;
import software.bernie.geckolib.cache.object.GeoBone;
import software.bernie.geckolib.model.GeoModel;
import software.bernie.geckolib.renderer.GeoRenderer;

public class CryonisPillarModel extends GeoModel<CryonisPillarEntity> {

    @Override
    public ResourceLocation getModelResource(CryonisPillarEntity animatable, @Nullable GeoRenderer<CryonisPillarEntity> renderer) {
        return ResourceLocation.fromNamespaceAndPath(Shrines.MOD_ID, "geo/cryonis_pillar.geo.json");
    }

    @Override
    public ResourceLocation getTextureResource(CryonisPillarEntity animatable, @Nullable GeoRenderer<CryonisPillarEntity> renderer) {
        return ResourceLocation.fromNamespaceAndPath(Shrines.MOD_ID, "textures/entity/cryonis_pillar.png");
    }

    @Override
    public ResourceLocation getAnimationResource(CryonisPillarEntity animatable) {
        return ResourceLocation.fromNamespaceAndPath(Shrines.MOD_ID, "animations/cryonis_pillar.animation.json");
    }

    @Override
    public void setCustomAnimations(CryonisPillarEntity animatable, long instanceId, AnimationState<CryonisPillarEntity> animationState) {
        super.setCustomAnimations(animatable, instanceId, animationState);

        GeoBone root = getAnimationProcessor().getBone("root");
        if (root != null) {
            // FIX: Keine doppelten Rotationen mehr! Der Renderer übernimmt das ab jetzt komplett.
            root.setRotX(0);
            root.setRotY(0);
            root.setRotZ(0);
        }
    }

    @Deprecated
    @Override
    public ResourceLocation getModelResource(CryonisPillarEntity animatable) { return getModelResource(animatable, null); }
    @Deprecated
    @Override
    public ResourceLocation getTextureResource(CryonisPillarEntity animatable) { return getTextureResource(animatable, null); }
}