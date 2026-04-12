package net.nakumaerebos.shrines.client;

import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexFormat;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderStateShard;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.resources.ResourceLocation;
import net.nakumaerebos.shrines.block.custom.HolyShimmerBlock;
import net.nakumaerebos.shrines.block.entity.HolyShimmerEntity;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import software.bernie.geckolib.renderer.GeoBlockRenderer;

public class HolyShimmerRenderer extends GeoBlockRenderer<HolyShimmerEntity> {
    public HolyShimmerRenderer(BlockEntityRendererProvider.Context context) {
        super(new HolyShimmerModel());
    }

    @Override
    public RenderType getRenderType(HolyShimmerEntity animatable, ResourceLocation texture,
                                    @Nullable MultiBufferSource bufferSource, float partialTick) {
        return RenderType.create(
                "holy_shimmer_additive",
                DefaultVertexFormat.BLOCK, // Oder POSITION_COLOR_TEX
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
                        .setWriteMaskState(RenderStateShard.COLOR_WRITE)
                        .createCompositeState(false)
        );
    }

    @Override
    public void render(HolyShimmerEntity animatable, float partialTick, PoseStack poseStack,
                       @NotNull MultiBufferSource bufferSource, int packedLight, int packedOverlay) {

        animatable.getBlockState().getValue(HolyShimmerBlock.FACING);
        poseStack.pushPose();
        super.render(animatable, partialTick, poseStack, bufferSource, packedLight, packedOverlay);
        poseStack.popPose();
    }

    @Override
    public net.minecraft.world.phys.@NotNull AABB getRenderBoundingBox(HolyShimmerEntity animatable) {
        // Wir nehmen die Position der BlockEntity und vergrößern sie massiv.
        // Da dein Tor 6 Blöcke breit und 3 hoch ist, ist inflate(4) ein sicherer Wert.
        return new net.minecraft.world.phys.AABB(animatable.getBlockPos()).inflate(1, 3.0, 1);
    }
}