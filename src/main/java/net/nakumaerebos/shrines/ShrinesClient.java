package net.nakumaerebos.shrines;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.*;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.client.resources.sounds.SoundInstance;
import net.minecraft.resources.ResourceLocation;
import net.nakumaerebos.shrines.block.entity.ModBlockEntities;
import net.nakumaerebos.shrines.client.*;
import net.nakumaerebos.shrines.client.renderer.ShrineInteriorEffects;
import net.nakumaerebos.shrines.entity.ModEntities;
import net.nakumaerebos.shrines.sound.ModSounds;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;
import net.neoforged.neoforge.client.event.RegisterDimensionSpecialEffectsEvent;
import net.neoforged.neoforge.client.event.RenderLevelStageEvent;
import net.neoforged.neoforge.client.gui.ConfigurationScreen;
import net.neoforged.neoforge.client.gui.IConfigScreenFactory;
import org.joml.Matrix4f;

// This class will not load on dedicated servers. Accessing client side code from here is safe.
@Mod(value = Shrines.MOD_ID, dist = Dist.CLIENT)
// You can use EventBusSubscriber to automatically register all static methods in the class annotated with @SubscribeEvent
@EventBusSubscriber(modid = Shrines.MOD_ID, value = Dist.CLIENT)
public class ShrinesClient {

    public ShrinesClient(ModContainer container) {
        container.registerExtensionPoint(IConfigScreenFactory.class, ConfigurationScreen::new);
    }

    @SubscribeEvent
    static void onClientSetup(FMLClientSetupEvent event) {
        Shrines.LOGGER.info("HELLO FROM CLIENT SETUP");
    }

    @SubscribeEvent
    public static void registerRenderers(EntityRenderersEvent.RegisterRenderers event) {
        event.registerBlockEntityRenderer(ModBlockEntities.SHRINE_DOOR_BE.get(), ShrineDoorRenderer::new);
        event.registerBlockEntityRenderer(ModBlockEntities.SHEIKAH_LECTERN_BE.get(), SheikahLecternRenderer::new);
        event.registerBlockEntityRenderer(ModBlockEntities.HOLY_SHIMMER.get(), HolyShimmerRenderer::new);

        event.registerEntityRenderer(ModEntities.SHRINE_ITEM.get(), ShrineItemRenderer::new);
        event.registerEntityRenderer(ModEntities.GUARDIAN_SCOUT.get(), GuardianScoutMobRenderer::new);
        event.registerEntityRenderer(ModEntities.GUARDIAN_SCOUT_II.get(), GuardianScoutIIMobRenderer::new);
        event.registerEntityRenderer(ModEntities.GUARDIANSCOUT_PROJECTILE.get(), GuardianProjectileRenderer::new);
    }

    @SubscribeEvent
    public static void onRegisterDimensionSpecialEffects(RegisterDimensionSpecialEffectsEvent event) {
        event.register(ResourceLocation.fromNamespaceAndPath(Shrines.MOD_ID, "shrine_interior"), new ShrineInteriorEffects());
    }

    @EventBusSubscriber(modid = Shrines.MOD_ID, value = Dist.CLIENT)
    public static class ClientAmbienceHandler {

        private static SoundInstance currentMusic = null;

        @SubscribeEvent
        public static void onClientTick(ClientTickEvent.Post event) {
            Minecraft mc = Minecraft.getInstance();
            if (mc.level == null || mc.player == null) return;

            boolean inShrine = mc.level.dimension().location().getPath().equals("shrine_interior");

            // Wenn wir im Shrine sind und noch nichts spielt
            if (inShrine) {
                if (currentMusic == null || !mc.getSoundManager().isActive(currentMusic)) {
                    // Wir erstellen eine Loop-SoundInstance
                    currentMusic = SimpleSoundInstance.forMusic(ModSounds.SHRINE_MUSIC.get());
                    mc.getSoundManager().play(currentMusic);
                }
            } else {
                // Wenn wir die Dimension verlassen, Musik stoppen
                if (currentMusic != null) {
                    mc.getSoundManager().stop(currentMusic);
                    currentMusic = null;
                }
            }
        }
    }

    private static final ResourceLocation SKY_LOCATION = ResourceLocation.fromNamespaceAndPath(Shrines.MOD_ID, "textures/environment/shrine_interior_sky.png");

    @SubscribeEvent
    public static void onRenderLevelStage(RenderLevelStageEvent event) {
        // Wir fangen das Event exakt in der Sky-Render-Phase ab
        if (event.getStage() == RenderLevelStageEvent.Stage.AFTER_SKY) {
            Minecraft mc = Minecraft.getInstance();
            if (mc.level == null) return;

            // Prüfen, ob wir uns in der Shrine-Dimension befinden
            if (mc.level.dimension().location().getPath().equals("shrine_interior")) {

                RenderSystem.enableBlend();
                RenderSystem.depthMask(false);
                RenderSystem.setShader(GameRenderer::getPositionTexColorShader);
                RenderSystem.setShaderTexture(0, SKY_LOCATION);

                Tesselator tesselator = Tesselator.getInstance();

                // Holt die aktuelle Model-View-Matrix aus dem Event-Kontext
                Matrix4f modelViewMatrix = event.getModelViewMatrix();

                for (int i = 0; i < 6; ++i) {
                    PoseStack posestack = new PoseStack();
                    posestack.mulPose(modelViewMatrix);

                    if (i == 1) posestack.mulPose(com.mojang.math.Axis.XP.rotationDegrees(90.0F));
                    if (i == 2) posestack.mulPose(com.mojang.math.Axis.XP.rotationDegrees(-90.0F));
                    if (i == 3) posestack.mulPose(com.mojang.math.Axis.XP.rotationDegrees(180.0F));
                    if (i == 4) posestack.mulPose(com.mojang.math.Axis.ZP.rotationDegrees(90.0F));
                    if (i == 5) posestack.mulPose(com.mojang.math.Axis.ZP.rotationDegrees(-90.0F));

                    Matrix4f matrix4f = posestack.last().pose();

                    BufferBuilder bufferbuilder = tesselator.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_TEX_COLOR);

                    int r = 255, g = 255, b = 255, a = 255;

                    bufferbuilder.addVertex(matrix4f, -100.0F, -100.0F, -100.0F).setUv(0.0F, 0.0F).setColor(r, g, b, a);
                    bufferbuilder.addVertex(matrix4f, -100.0F, -100.0F, 100.0F).setUv(0.0F, 16.0F).setColor(r, g, b, a);
                    bufferbuilder.addVertex(matrix4f, 100.0F, -100.0F, 100.0F).setUv(16.0F, 16.0F).setColor(r, g, b, a);
                    bufferbuilder.addVertex(matrix4f, 100.0F, -100.0F, -100.0F).setUv(16.0F, 0.0F).setColor(r, g, b, a);

                    MeshData meshdata = bufferbuilder.build();
                    if (meshdata != null) {
                        BufferUploader.drawWithShader(meshdata);
                    }
                }

                RenderSystem.depthMask(true);
                RenderSystem.disableBlend();

                // Da RenderLevelStageEvent nicht gecancelt werden kann, reicht unser eigener DrawCall hier aus.
                // Weil in der ShrineInteriorEffects "SkyType.NONE" eingestellt ist, zeichnet Vanilla
                // von sich aus danach absolut nichts mehr drüber.
            }
        }
    }
}