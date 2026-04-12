package net.nakumaerebos.shrines.client;

import net.minecraft.resources.ResourceLocation;
import net.nakumaerebos.shrines.block.custom.HolyShimmerBlock;
import net.nakumaerebos.shrines.block.entity.HolyShimmerEntity;
import software.bernie.geckolib.model.GeoModel;
import software.bernie.geckolib.renderer.GeoRenderer;
import org.jetbrains.annotations.Nullable;

public class HolyShimmerModel extends GeoModel<HolyShimmerEntity> {

    @Override
    public ResourceLocation getModelResource(HolyShimmerEntity animatable, @Nullable GeoRenderer<HolyShimmerEntity> renderer) {
        if (animatable.getBlockState().getValue(HolyShimmerBlock.IS_EDGE)) {
            return ResourceLocation.fromNamespaceAndPath("shrines", "geo/holy_shimmer_edge.geo.json");
        }
        return ResourceLocation.fromNamespaceAndPath("shrines", "geo/holy_shimmer.geo.json");
    }

    @Override
    public ResourceLocation getTextureResource(HolyShimmerEntity animatable, @Nullable GeoRenderer<HolyShimmerEntity> renderer) {
        return ResourceLocation.fromNamespaceAndPath("shrines", "textures/block/holy_shimmer.png");
    }

    @Override
    public ResourceLocation getAnimationResource(HolyShimmerEntity animatable) {
        return ResourceLocation.fromNamespaceAndPath("shrines", "animations/holy_shimmer.animation.json");
    }

    // Diese alten Methoden müssen wir trotzdem behalten, da sie im Interface abstract sind,
    // aber wir können sie einfach auf die neuen Methoden leiten oder leer lassen,
    // falls der Compiler sonst meckert.
    // Da sie aber oben bereits überschrieben wurden (durch die Signatur-Anpassung),
    // sollte das hier reichen:

    @Deprecated
    @Override
    public ResourceLocation getModelResource(HolyShimmerEntity animatable) {
        return getModelResource(animatable, null);
    }

    @Deprecated
    @Override
    public ResourceLocation getTextureResource(HolyShimmerEntity animatable) {
        return getTextureResource(animatable, null);
    }
}