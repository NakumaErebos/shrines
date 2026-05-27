package net.nakumaerebos.shrines.client;

import net.minecraft.resources.ResourceLocation;
import net.nakumaerebos.shrines.Shrines;
import net.nakumaerebos.shrines.entity.GuardianScoutProjectileEntity;
import software.bernie.geckolib.model.GeoModel;

public class GuardianProjectileModel extends GeoModel<GuardianScoutProjectileEntity> {
    @Override
    public ResourceLocation getModelResource(GuardianScoutProjectileEntity animatable) {
        return ResourceLocation.fromNamespaceAndPath(Shrines.MOD_ID, "geo/guardianscout_projectile.geo.json");
    }

    @Override
    public ResourceLocation getTextureResource(GuardianScoutProjectileEntity animatable) {
        return ResourceLocation.fromNamespaceAndPath(Shrines.MOD_ID, "textures/entity/guradianscout_projectile.png");
    }

    @Override
    public ResourceLocation getAnimationResource(GuardianScoutProjectileEntity animatable) {
        return ResourceLocation.fromNamespaceAndPath(Shrines.MOD_ID, "animations/guardianscout_projectile.animation.json");
    }
}