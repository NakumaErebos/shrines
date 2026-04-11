package net.nakumaerebos.shrines.client;

import net.minecraft.resources.ResourceLocation;
import net.nakumaerebos.shrines.Shrines;
import net.nakumaerebos.shrines.block.entity.ShrineDoorBlockEntity;
import software.bernie.geckolib.model.GeoModel;

public class ShrineDoorModel extends GeoModel<ShrineDoorBlockEntity> {

    @Override
    public ResourceLocation getModelResource(ShrineDoorBlockEntity animatable) {
        return ResourceLocation.fromNamespaceAndPath(Shrines.MOD_ID, "geo/shrine_door.geo.json");
    }

    @Override
    public ResourceLocation getTextureResource(ShrineDoorBlockEntity animatable) {
        return ResourceLocation.fromNamespaceAndPath(Shrines.MOD_ID, "textures/block/shrine_door.png");
    }

    @Override
    public ResourceLocation getAnimationResource(ShrineDoorBlockEntity animatable) {
        return ResourceLocation.fromNamespaceAndPath(Shrines.MOD_ID, "animations/shrine_door.animation.json");
    }
}