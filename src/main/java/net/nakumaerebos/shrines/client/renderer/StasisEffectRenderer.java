package net.nakumaerebos.shrines.client.renderer;

import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.VertexFormat;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderStateShard;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceLocation;
import net.nakumaerebos.shrines.client.models.StasisEffectModel;
import net.nakumaerebos.shrines.entity.StasisEffectEntity;
import org.jetbrains.annotations.Nullable;
import software.bernie.geckolib.renderer.GeoEntityRenderer;

public class StasisEffectRenderer extends GeoEntityRenderer<StasisEffectEntity> {
    public StasisEffectRenderer(EntityRendererProvider.Context renderManager) {
        super(renderManager, new StasisEffectModel());
    }

    @Override
    public RenderType getRenderType(StasisEffectEntity animatable, ResourceLocation texture,
                                    @Nullable MultiBufferSource bufferSource, float partialTick) {
        return RenderType.create(
                "stasis_additive",
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
}
