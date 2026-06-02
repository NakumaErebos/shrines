package net.nakumaerebos.shrines.client;

import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.nakumaerebos.shrines.entity.CryonisPillarEntity;
import software.bernie.geckolib.renderer.GeoEntityRenderer;

public class CryonisPillarRenderer extends GeoEntityRenderer<CryonisPillarEntity> {
    public CryonisPillarRenderer(EntityRendererProvider.Context renderManager) {
        super(renderManager, new CryonisPillarModel());
    }
}