package net.nakumaerebos.shrines.client;

import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.nakumaerebos.shrines.entity.RemoteBombRoundEntity;
import software.bernie.geckolib.renderer.GeoEntityRenderer;

public class RemoteBombRoundRenderer extends GeoEntityRenderer<RemoteBombRoundEntity> {
    public RemoteBombRoundRenderer(EntityRendererProvider.Context renderManager) {
        super(renderManager, new RemoteBombRoundModel());
    }
}
