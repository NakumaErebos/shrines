package net.nakumaerebos.shrines.client.renderer;

import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.nakumaerebos.shrines.client.models.StasisArrowEffectModel;
import net.nakumaerebos.shrines.entity.StasisArrowEffectEntity;
import software.bernie.geckolib.renderer.GeoEntityRenderer;

public class StasisArrowEffectRenderer extends GeoEntityRenderer<StasisArrowEffectEntity> {
    public StasisArrowEffectRenderer(EntityRendererProvider.Context renderManager) {
        super(renderManager, new StasisArrowEffectModel());
        this.shadowRadius = 0.0F; // Verhindert Schatten unter dem Pfeil
    }

    @Override
    public void preRender(com.mojang.blaze3d.vertex.PoseStack poseStack,
                          StasisArrowEffectEntity animatable,
                          software.bernie.geckolib.cache.object.BakedGeoModel model,
                          @org.jetbrains.annotations.Nullable net.minecraft.client.renderer.MultiBufferSource bufferSource,
                          @org.jetbrains.annotations.Nullable com.mojang.blaze3d.vertex.VertexConsumer buffer,
                          boolean isReRender, float partialTick, int packedLight, int packedOverlay, int colour) {

        super.preRender(poseStack, animatable, model, bufferSource, buffer,
                isReRender, partialTick, packedLight, packedOverlay, colour);

        float yaw = animatable.getArrowYaw();

        poseStack.mulPose(com.mojang.math.Axis.YP.rotationDegrees(-yaw));
    }
}