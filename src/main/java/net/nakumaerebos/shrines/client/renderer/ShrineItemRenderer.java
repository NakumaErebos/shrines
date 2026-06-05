package net.nakumaerebos.shrines.client.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.nakumaerebos.shrines.entity.ShrineItemEntity;
import org.jetbrains.annotations.NotNull;

public class ShrineItemRenderer extends EntityRenderer<ShrineItemEntity> {

    public ShrineItemRenderer(EntityRendererProvider.Context context) {
        super(context);
    }

// In deinem ShrineItemRenderer in der render-Methode:

    @Override
    public void render(ShrineItemEntity entity, float entityYaw, float partialTicks, @NotNull PoseStack poseStack, @NotNull MultiBufferSource buffer, int packedLight) {
        ItemStack stack = entity.getItem();
        if (stack.isEmpty()) return;

        poseStack.pushPose();

        // 1. Positionierung im Block
        poseStack.translate(0.0, 0.5, 0.0);
        poseStack.scale(0.7f, 0.7f, 0.7f);

        // 2. Der Systemzeit-Ansatz
        // System.currentTimeMillis() liefert Millisekunden.
        // Wir teilen durch einen Wert, um die Geschwindigkeit zu steuern.
        // 1000ms = 1 Sekunde für eine volle Umdrehung (360 Grad).
        float rotationSpeed = 10000.0f; // Höherer Wert = Langsamer
        float degrees = (System.currentTimeMillis() % (long)rotationSpeed) / rotationSpeed * 360.0f;

        poseStack.mulPose(Axis.YP.rotationDegrees(degrees));

        // 3. Item rendern
        Minecraft.getInstance().getItemRenderer().renderStatic(
                stack,
                ItemDisplayContext.FIXED,
                packedLight,
                OverlayTexture.NO_OVERLAY,
                poseStack,
                buffer,
                entity.level(),
                entity.getId()
        );

        poseStack.popPose();
        super.render(entity, entityYaw, partialTicks, poseStack, buffer, packedLight);
    }

    @Override
    public @NotNull ResourceLocation getTextureLocation(@NotNull ShrineItemEntity entity) {
        // Da wir ein Item rendern, brauchen wir keine direkte Entity-Textur
        return null;
    }
}