package net.nakumaerebos.shrines;

import net.minecraft.client.Minecraft;
import net.nakumaerebos.shrines.block.entity.ModBlockEntities;
import net.nakumaerebos.shrines.client.HolyShimmerRenderer;
import net.nakumaerebos.shrines.client.SheikahLecternRenderer;
import net.nakumaerebos.shrines.client.ShrineDoorRenderer;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
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
    }
}