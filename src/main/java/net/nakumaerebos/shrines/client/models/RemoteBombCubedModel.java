package net.nakumaerebos.shrines.client.models;

import net.minecraft.resources.ResourceLocation;
import net.nakumaerebos.shrines.Shrines;
import net.nakumaerebos.shrines.entity.RemoteBombCubedEntity;
import software.bernie.geckolib.model.GeoModel;

public class RemoteBombCubedModel extends GeoModel<RemoteBombCubedEntity> {

    @Override
    public ResourceLocation getModelResource(RemoteBombCubedEntity animatable) {
        return ResourceLocation.fromNamespaceAndPath(Shrines.MOD_ID, "geo/remote_bomb_cubed.geo.json");
    }

    @Override
    public ResourceLocation getTextureResource(RemoteBombCubedEntity animatable) {
        return ResourceLocation.fromNamespaceAndPath(Shrines.MOD_ID, "textures/entity/remote_bomb_cubed.png");
    }

    @Override
    public ResourceLocation getAnimationResource(RemoteBombCubedEntity animatable) {
        return null;
    }
}