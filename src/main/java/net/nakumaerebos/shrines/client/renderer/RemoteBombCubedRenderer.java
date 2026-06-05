package net.nakumaerebos.shrines.client.renderer;

import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.nakumaerebos.shrines.client.models.RemoteBombCubedModel;
import net.nakumaerebos.shrines.entity.RemoteBombCubedEntity;
import software.bernie.geckolib.renderer.GeoEntityRenderer;

public class RemoteBombCubedRenderer extends GeoEntityRenderer<RemoteBombCubedEntity> {
    public RemoteBombCubedRenderer(EntityRendererProvider.Context renderManager) {
        super(renderManager, new RemoteBombCubedModel());
    }
}
