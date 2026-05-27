package net.nakumaerebos.shrines.client;

import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexFormat;
import com.mojang.math.Axis;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderStateShard;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceLocation;
import net.nakumaerebos.shrines.entity.GuardianScoutProjectileEntity;
import org.jetbrains.annotations.Nullable;
import software.bernie.geckolib.renderer.GeoEntityRenderer;

public class GuardianProjectileRenderer extends GeoEntityRenderer<GuardianScoutProjectileEntity> {
    public GuardianProjectileRenderer(EntityRendererProvider.Context renderManager) {
        super(renderManager, new GuardianProjectileModel());
    }

    // Dieser Teil kopiert den Effekt vom Holy Shimmer
    @Override
    public RenderType getRenderType(GuardianScoutProjectileEntity animatable, ResourceLocation texture,
                                    @Nullable MultiBufferSource bufferSource, float partialTick) {
        return RenderType.create(
                "projectile_shimmer",
                DefaultVertexFormat.NEW_ENTITY, // Wichtig: Für Entities NEW_ENTITY nutzen statt BLOCK
                VertexFormat.Mode.QUADS,
                1536,
                false,
                false,
                RenderType.CompositeState.builder()
                        .setShaderState(RenderStateShard.RENDERTYPE_BEACON_BEAM_SHADER)
                        .setTextureState(new RenderStateShard.TextureStateShard(texture, true, false))
                        .setTransparencyState(RenderStateShard.LIGHTNING_TRANSPARENCY)
                        .setCullState(RenderStateShard.NO_CULL)
                        .setLightmapState(RenderStateShard.LIGHTMAP)
                        .setOverlayState(RenderStateShard.OVERLAY) // Wichtig für Entities
                        .setWriteMaskState(RenderStateShard.COLOR_WRITE)
                        .createCompositeState(false)
        );
    }

    @Override
    public void render(GuardianScoutProjectileEntity entity, float entityYaw, float partialTick, PoseStack poseStack, MultiBufferSource bufferSource, int packedLight) {
        poseStack.pushPose();

        // Deine funktionierende Rotations-Logik
        float yaw = entity.getViewYRot(partialTick);
        poseStack.mulPose(Axis.YP.rotationDegrees(yaw + 180.0F));
        poseStack.mulPose(Axis.XP.rotationDegrees(entity.getViewXRot(partialTick)));

        // Wir rufen super.render auf, Geckolib nutzt nun automatisch unseren getRenderType
        super.render(entity, entityYaw, partialTick, poseStack, bufferSource, packedLight);

        poseStack.popPose();
    }
}