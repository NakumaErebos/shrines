package net.nakumaerebos.shrines.client;

import net.minecraft.resources.ResourceLocation;
import net.nakumaerebos.shrines.Shrines;
import net.nakumaerebos.shrines.entity.StasisEffectEntity;
import software.bernie.geckolib.model.GeoModel;

public class StasisEffectModel extends GeoModel<StasisEffectEntity> {

    @Override
    public ResourceLocation getModelResource(StasisEffectEntity animatable) {
        return ResourceLocation.fromNamespaceAndPath(Shrines.MOD_ID, "geo/stasis_effect.geo.json");
    }

    @Override
    public ResourceLocation getTextureResource(StasisEffectEntity animatable) {
        return ResourceLocation.fromNamespaceAndPath(Shrines.MOD_ID, "textures/entity/stasis_effect.png");
    }

    @Override
    public ResourceLocation getAnimationResource(StasisEffectEntity animatable) {
        return ResourceLocation.fromNamespaceAndPath(Shrines.MOD_ID, "animations/stasis_effect.animation.json");
    }
}