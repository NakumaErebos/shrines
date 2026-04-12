package net.nakumaerebos.shrines;

import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.client.resources.sounds.SoundInstance;
import net.nakumaerebos.shrines.block.entity.ModBlockEntities;
import net.nakumaerebos.shrines.client.HolyShimmerRenderer;
import net.nakumaerebos.shrines.client.SheikahLecternRenderer;
import net.nakumaerebos.shrines.client.ShrineDoorRenderer;
import net.nakumaerebos.shrines.client.ShrineItemRenderer;
import net.nakumaerebos.shrines.sound.ModSounds;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;
import net.neoforged.neoforge.client.gui.ConfigurationScreen;
import net.neoforged.neoforge.client.gui.IConfigScreenFactory;

// This class will not load on dedicated servers. Accessing client side code from here is safe.
@Mod(value = Shrines.MOD_ID, dist = Dist.CLIENT)
// You can use EventBusSubscriber to automatically register all static methods in the class annotated with @SubscribeEvent
@EventBusSubscriber(modid = Shrines.MOD_ID, value = Dist.CLIENT)
public class ShrinesClient {
    public ShrinesClient(ModContainer container) {
        // Allows NeoForge to create a config screen for this mod's configs.
        // The config screen is accessed by going to the Mods screen > clicking on your mod > clicking on config.
        // Do not forget to add translations for your config options to the en_us.json file.
        container.registerExtensionPoint(IConfigScreenFactory.class, ConfigurationScreen::new);
    }

    @SubscribeEvent
    static void onClientSetup(FMLClientSetupEvent event) {
        // Some client setup code
        Shrines.LOGGER.info("HELLO FROM CLIENT SETUP");
        Shrines.LOGGER.info("MINECRAFT NAME >> {}", Minecraft.getInstance().getUser().getName());
    }

    @SubscribeEvent
    public static void registerRenderers(EntityRenderersEvent.RegisterRenderers event) {
        event.registerBlockEntityRenderer(ModBlockEntities.SHRINE_DOOR_BE.get(), ShrineDoorRenderer::new);
        event.registerBlockEntityRenderer(ModBlockEntities.SHEIKAH_LECTERN_BE.get(), SheikahLecternRenderer::new);
        event.registerBlockEntityRenderer(ModBlockEntities.HOLY_SHIMMER.get(), HolyShimmerRenderer::new);

        event.registerEntityRenderer(net.nakumaerebos.shrines.entity.ModEntities.SHRINE_ITEM.get(), ShrineItemRenderer::new);
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
}