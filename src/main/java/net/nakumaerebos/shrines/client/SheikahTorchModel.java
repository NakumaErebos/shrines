package net.nakumaerebos.shrines.client;

import net.minecraft.resources.ResourceLocation;
import net.nakumaerebos.shrines.Shrines;
import net.nakumaerebos.shrines.block.entity.SheikahTorchBlockEntity;
import org.jetbrains.annotations.Nullable;
import software.bernie.geckolib.model.GeoModel;
import software.bernie.geckolib.renderer.GeoRenderer;

public class SheikahTorchModel extends GeoModel<SheikahTorchBlockEntity> {

    @Override
    public ResourceLocation getModelResource(SheikahTorchBlockEntity animatable, @Nullable GeoRenderer<SheikahTorchBlockEntity> renderer) {
        return ResourceLocation.fromNamespaceAndPath(Shrines.MOD_ID, "geo/sheikah_torch.geo.json");
    }

    @Override
    public ResourceLocation getTextureResource(SheikahTorchBlockEntity animatable, @Nullable GeoRenderer<SheikahTorchBlockEntity> renderer) {
        return ResourceLocation.fromNamespaceAndPath(Shrines.MOD_ID, "textures/block/sheikah_torch.png");
    }

    @Override
    public ResourceLocation getAnimationResource(SheikahTorchBlockEntity animatable) {
        return ResourceLocation.fromNamespaceAndPath(Shrines.MOD_ID, "animations/sheikah_torch.animation.json");
    }

    @Deprecated
    @Override
    public ResourceLocation getModelResource(SheikahTorchBlockEntity animatable) {
        return getModelResource(animatable, null);
    }

    @Deprecated
    @Override
    public ResourceLocation getTextureResource(SheikahTorchBlockEntity animatable) {
        return getTextureResource(animatable, null);
    }
}