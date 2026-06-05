package net.nakumaerebos.shrines.client.renderer;

import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.nakumaerebos.shrines.client.models.GuardianScoutIMobModel;
import net.nakumaerebos.shrines.entity.GuardianScoutIMobEntity;
import software.bernie.geckolib.renderer.GeoEntityRenderer;

public class GuardianScoutIMobRenderer extends GeoEntityRenderer<GuardianScoutIMobEntity> {
    public GuardianScoutIMobRenderer(EntityRendererProvider.Context renderManager) {
        super(renderManager, new GuardianScoutIMobModel());
    }
}
