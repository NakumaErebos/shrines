package net.nakumaerebos.shrines.client;

import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.nakumaerebos.shrines.entity.GuardianScoutIIMobEntity;
import software.bernie.geckolib.renderer.GeoEntityRenderer;

public class GuardianScoutIIMobRenderer extends GeoEntityRenderer<GuardianScoutIIMobEntity> {
    public GuardianScoutIIMobRenderer(EntityRendererProvider.Context renderManager) {
        super(renderManager, new GuardianScoutIIMobModel());
    }
}