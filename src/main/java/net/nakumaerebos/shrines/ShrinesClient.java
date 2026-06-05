package net.nakumaerebos.shrines;

import com.mojang.blaze3d.platform.InputConstants;
import com.mojang.blaze3d.shaders.FogShape;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.*;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.particle.HugeExplosionParticle;
import net.minecraft.client.renderer.FogRenderer;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.client.resources.sounds.SoundInstance;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;
import net.nakumaerebos.shrines.block.entity.ModBlockEntities;
import net.nakumaerebos.shrines.client.ShrineInteriorEffects;
import net.nakumaerebos.shrines.client.gui.CryonisTimerOverlay;
import net.nakumaerebos.shrines.client.keys.ClientInputHandler;
import net.nakumaerebos.shrines.client.keys.ModKeyMappings;
import net.nakumaerebos.shrines.client.renderer.*;
import net.nakumaerebos.shrines.effect.ModEffects;
import net.nakumaerebos.shrines.entity.ModEntities;
import net.nakumaerebos.shrines.particles.ModParticles;
import net.nakumaerebos.shrines.sound.ModSounds;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.neoforge.client.event.*;
import net.neoforged.neoforge.client.gui.ConfigurationScreen;
import net.neoforged.neoforge.client.gui.IConfigScreenFactory;
import net.neoforged.neoforge.network.PacketDistributor;
import org.joml.Matrix4f;
import org.lwjgl.glfw.GLFW;

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
        event.registerBlockEntityRenderer(ModBlockEntities.DUNGEON_DOOR_BE.get(), DungeonDoorRenderer::new);
        event.registerBlockEntityRenderer(ModBlockEntities.SHEIKAH_LECTERN_BE.get(), SheikahLecternRenderer::new);
        event.registerBlockEntityRenderer(ModBlockEntities.SHRINE_CHEST_BE.get(), ShrineChestRenderer::new);
        event.registerBlockEntityRenderer(ModBlockEntities.HOLY_SHIMMER.get(), HolyShimmerRenderer::new);
        event.registerBlockEntityRenderer(ModBlockEntities.SHEIKAH_TORCH_BE.get(), SheikahTorchRenderer::new);

        event.registerEntityRenderer(ModEntities.SHRINE_ITEM.get(), ShrineItemRenderer::new);
        event.registerEntityRenderer(ModEntities.STASIS_EFFECT.get(), StasisEffectRenderer::new);
        event.registerEntityRenderer(ModEntities.CRYONIS_PILLAR.get(), CryonisPillarRenderer::new);
        event.registerEntityRenderer(ModEntities.STASIS_ARROW_EFFECT.get(), StasisArrowEffectRenderer::new);
        event.registerEntityRenderer(ModEntities.GUARDIAN_SCOUT_I.get(), GuardianScoutIMobRenderer::new);
        event.registerEntityRenderer(ModEntities.GUARDIAN_SCOUT_II.get(), GuardianScoutIIMobRenderer::new);
        event.registerEntityRenderer(ModEntities.GUARDIANSCOUT_PROJECTILE.get(), GuardianProjectileRenderer::new);
        event.registerEntityRenderer(ModEntities.REMOTE_BOMB_ROUND.get(), RemoteBombRoundRenderer::new);
        event.registerEntityRenderer(ModEntities.REMOTE_BOMB_CUBED.get(), RemoteBombCubedRenderer::new);
    }

    @SubscribeEvent
    public static void registerKeyMappings(RegisterKeyMappingsEvent event) {
        ModKeyMappings.registerKeyMappings(event);
    }

    @SubscribeEvent
    public static void onRenderGui(RenderGuiLayerEvent.Post event){
        CryonisTimerOverlay.onRenderGui(event);
    }

    @SubscribeEvent
    public static void onClientTick(ClientTickEvent.Post event) {
        ClientInputHandler.onClientTick(event);
    }

    @SubscribeEvent
    public static void registerParticles(RegisterParticleProvidersEvent event) {
        // Statt dem Standard-Provider nutzen wir eine eigene Lambda-Funktion
        event.registerSpriteSet(ModParticles.REMOTE_BOMB_EXPLOSION_PARTICLE.get(), spriteSet -> {
            // Wir erstellen zuerst den originalen Minecraft-Explosions-Provider
            HugeExplosionParticle.Provider standardProvider = new HugeExplosionParticle.Provider(spriteSet);

            // Wir geben ein modifiziertes Verhalten zurück
            return (type, level, x, y, z, xSpeed, ySpeed, zSpeed) -> {
                // Erstelle das originale Partikel-Objekt
                net.minecraft.client.particle.Particle particle = standardProvider.createParticle(
                        type, level, x, y, z, xSpeed, ySpeed, zSpeed
                );

                if (particle != null) {
                    float groessenFaktor = 3.00F;

                    particle.scale(groessenFaktor);
                }

                return particle;
            };
        });
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
            if (mc.level == null || mc.player == null) return;

            // Prüfen, ob wir uns in der Shrine-Dimension befinden
            if (mc.level.dimension().location().getPath().equals("shrine_interior")) {

                // 1. Blend-Faktor berechnen
                float blendFactor = 0.0F;
                if (mc.player.hasEffect(ModEffects.DEEP_DARKNESS)) {
                    var effect = mc.player.getEffect(ModEffects.DEEP_DARKNESS);
                    if (effect != null) {
                        float partialTick = event.getPartialTick().getGameTimeDeltaTicks();
                        blendFactor = effect.getBlendFactor((LivingEntity) mc.player, partialTick);
                    }
                }

                // 2. RGB-Kanäle basierend auf dem Blend-Faktor abdunkeln
                // Ohne Effekt: 255, 255, 255 (Weiß, entspricht Vanilla -1)
                // Mit vollem Effekt: 40, 40, 40 (Das exakte, extrem dunkle Grau aus dem End-Sky, entspricht -14145496)
                // Wenn du es komplett pechschwarz haben willst, ersetze die 40 durch 0!
                int r = (int) Mth.lerp(blendFactor, 255.0F, 0.0F);
                int g = (int) Mth.lerp(blendFactor, 255.0F, 0.0F);
                int b = (int) Mth.lerp(blendFactor, 255.0F, 0.0F);
                int a = 255; // Alpha bleibt voll da, damit die Box blickdicht ist

                // Packt die RGBA-Kanäle in einen einzelnen Integer, so wie der End-Sky es macht
                int finalColor = net.minecraft.util.FastColor.ARGB32.color(a, r, g, b);

                RenderSystem.enableBlend();
                RenderSystem.depthMask(false);
                RenderSystem.setShader(GameRenderer::getPositionTexColorShader);
                RenderSystem.setShaderTexture(0, SKY_LOCATION);

                Tesselator tesselator = Tesselator.getInstance();
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

                    // Hier übergeben wir jetzt die gepackte Farbe als einzelnen Integer per .setColor(int)
                    bufferbuilder.addVertex(matrix4f, -100.0F, -100.0F, -100.0F).setUv(0.0F, 0.0F).setColor(finalColor);
                    bufferbuilder.addVertex(matrix4f, -100.0F, -100.0F, 100.0F).setUv(0.0F, 4.0F).setColor(finalColor);
                    bufferbuilder.addVertex(matrix4f, 100.0F, -100.0F, 100.0F).setUv(4.0F, 4.0F).setColor(finalColor);
                    bufferbuilder.addVertex(matrix4f, 100.0F, -100.0F, -100.0F).setUv(4.0F, 0.0F).setColor(finalColor);

                    MeshData meshdata = bufferbuilder.build();
                    if (meshdata != null) {
                        BufferUploader.drawWithShader(meshdata);
                    }
                }

                RenderSystem.depthMask(true);
                RenderSystem.disableBlend();
            }
        }
    }

    @SubscribeEvent
    public static void onRenderFog(ViewportEvent.RenderFog event) {
        Minecraft mc = Minecraft.getInstance();
        LivingEntity entity = mc.player;

        if (entity != null && entity.hasEffect(ModEffects.DEEP_DARKNESS)) {
            MobEffectInstance effect = entity.getEffect(ModEffects.DEEP_DARKNESS);
            if (effect == null) return;

            float blendFactor = effect.getBlendFactor(entity, (float) event.getPartialTick());

            if (blendFactor > 0.0F) {
                // Hier übergeben wir jetzt das 'effect' Objekt mit
                float targetFarPlane = getTargetFarPlane(event, entity, blendFactor, effect);

                event.setNearPlaneDistance(0.0F);
                event.setFarPlaneDistance(targetFarPlane);
                event.setFogShape(FogShape.CYLINDER);

                event.setCanceled(true);
            }
        }
    }

    private static float getTargetFarPlane(ViewportEvent.RenderFog event, LivingEntity entity, float blendFactor, MobEffectInstance effect) {
        float darknessFactor = 0.95F * blendFactor;
        float originalFarPlane = event.getFarPlaneDistance();

        if (event.getMode() == FogRenderer.FogMode.FOG_SKY) {
            return 0.0F;
        } else {
            // 1. Hole das Level des Effekts (0 für Stufe I, 1 für Stufe II, etc.)
            int amplifier = effect.getAmplifier();

            // 2. Berechne die maximale Nebel-Distanz basierend auf der Stufe
            // Stufe 0 = 15, Stufe 1 = 14... nach unten hin deckeln wir es bei 1.0 Block ab
            float maxDistanceBasedOnLevel = Math.max(1.0F, 255 - amplifier);

            // 3. Blende den Nebel sanft ein (Nutzt den berechneten dynamischen Maximalwert statt stur 15.0F)
            return Mth.lerp(darknessFactor, originalFarPlane, Math.min(maxDistanceBasedOnLevel, originalFarPlane));
        }
    }

    @SubscribeEvent
    public static void onComputeFogColor(ViewportEvent.ComputeFogColor event) {
        Minecraft mc = Minecraft.getInstance();
        LivingEntity entity = mc.player;

        if (entity != null && entity.hasEffect(ModEffects.DEEP_DARKNESS)) {
            MobEffectInstance effect = entity.getEffect(ModEffects.DEEP_DARKNESS);
            if (effect == null) return;

            // Wir holen uns wieder den Blend-Faktor für sanftes Abdunkeln beim Start
            float blendFactor = effect.getBlendFactor(entity, (float) event.getPartialTick());

            if (blendFactor > 0.0F) {
                // Aktuelle Nebelfarbe holen (z.B. vom Tageslicht oder Biom)
                float currentRed = event.getRed();
                float currentGreen = event.getGreen();
                float currentBlue = event.getBlue();

                // Wir blenden die Originalfarbe basierend auf dem blendFactor zu Schwarz (0.0) über
                event.setRed(Mth.lerp(blendFactor, currentRed, 0.0F));
                event.setGreen(Mth.lerp(blendFactor, currentGreen, 0.0F));
                event.setBlue(Mth.lerp(blendFactor, currentBlue, 0.0F));
            }
        }
    }
}