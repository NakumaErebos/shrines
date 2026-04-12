package net.nakumaerebos.shrines.client;

import net.minecraft.resources.ResourceLocation;
import net.nakumaerebos.shrines.Shrines;
import net.nakumaerebos.shrines.block.entity.ShrineDoorBlockEntity;
import software.bernie.geckolib.model.GeoModel;
import software.bernie.geckolib.renderer.GeoRenderer;
import org.jetbrains.annotations.Nullable;

public class ShrineDoorModel extends GeoModel<ShrineDoorBlockEntity> {

    @Override
    public ResourceLocation getModelResource(ShrineDoorBlockEntity animatable, @Nullable GeoRenderer<ShrineDoorBlockEntity> renderer) {
        return ResourceLocation.fromNamespaceAndPath(Shrines.MOD_ID, "geo/shrine_door.geo.json");
    }

    @Override
    public ResourceLocation getTextureResource(ShrineDoorBlockEntity animatable, @Nullable GeoRenderer<ShrineDoorBlockEntity> renderer) {
        return ResourceLocation.fromNamespaceAndPath(Shrines.MOD_ID, "textures/block/shrine_door.png");
    }

    @Override
    public ResourceLocation getAnimationResource(ShrineDoorBlockEntity animatable) {
        return ResourceLocation.fromNamespaceAndPath(Shrines.MOD_ID, "animations/shrine_door.animation.json");
    }

    // --- Brücken-Methoden zur Erfüllung der abstrakten (deprecated) Basis-Methoden ---

    @Deprecated
    @Override
    public ResourceLocation getModelResource(ShrineDoorBlockEntity animatable) {
        return getModelResource(animatable, null);
    }

    @Deprecated
    @Override
    public ResourceLocation getTextureResource(ShrineDoorBlockEntity animatable) {
        return getTextureResource(animatable, null);
    }
}