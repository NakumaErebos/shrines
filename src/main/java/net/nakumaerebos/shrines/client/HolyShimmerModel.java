package net.nakumaerebos.shrines.client;

import net.minecraft.resources.ResourceLocation;
import net.nakumaerebos.shrines.block.custom.HolyShimmerBlock;
import net.nakumaerebos.shrines.block.entity.HolyShimmerEntity;
import software.bernie.geckolib.model.GeoModel;

public class HolyShimmerModel extends GeoModel<HolyShimmerEntity> {
    @Override
    public ResourceLocation getModelResource(HolyShimmerEntity animatable) {
        if (animatable.getBlockState().getValue(HolyShimmerBlock.IS_EDGE)) {
            return ResourceLocation.fromNamespaceAndPath("shrines", "geo/holy_shimmer_edge.geo.json");
        }
        return ResourceLocation.fromNamespaceAndPath("shrines", "geo/holy_shimmer.geo.json");
    }

    @Override
    public ResourceLocation getTextureResource(HolyShimmerEntity animatable) {
        // Falls das Edge-Modell eine eigene Textur hat, hier ebenfalls prüfen
        return ResourceLocation.fromNamespaceAndPath("shrines", "textures/block/holy_shimmer.png");
    }

    @Override
    public ResourceLocation getAnimationResource(HolyShimmerEntity animatable) {
        return ResourceLocation.fromNamespaceAndPath("shrines", "animations/holy_shimmer.animation.json");
    }
}