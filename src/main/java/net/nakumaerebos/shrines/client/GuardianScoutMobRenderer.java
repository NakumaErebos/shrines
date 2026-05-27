package net.nakumaerebos.shrines.client;

import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.nakumaerebos.shrines.entity.GuardianScoutMobEntity;
import software.bernie.geckolib.renderer.GeoEntityRenderer;

public class GuardianScoutMobRenderer extends GeoEntityRenderer<GuardianScoutMobEntity> {
    public GuardianScoutMobRenderer(EntityRendererProvider.Context renderManager) {
        super(renderManager, new GuardianScoutMobModel());
    }
}
