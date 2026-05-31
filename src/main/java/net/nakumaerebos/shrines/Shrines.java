package net.nakumaerebos.shrines;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.GameType;
import net.nakumaerebos.shrines.block.ModBlocks;
import net.nakumaerebos.shrines.block.entity.ModBlockEntities;
import net.nakumaerebos.shrines.creativeModeTab.ModCreativeModeTabs;
import net.nakumaerebos.shrines.effect.ModEffects;
import net.nakumaerebos.shrines.entity.ModEntities;
import net.nakumaerebos.shrines.item.ModItems;
import net.nakumaerebos.shrines.sound.ModSounds;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.event.server.ServerStartingEvent;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;
import org.slf4j.Logger;
import com.mojang.logging.LogUtils;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.level.block.Blocks;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.neoforge.common.NeoForge;

@Mod(Shrines.MOD_ID)
public class Shrines {
    public static final String MOD_ID = "shrines";
    public static final Logger LOGGER = LogUtils.getLogger();

    public Shrines(IEventBus modEventBus, ModContainer modContainer) {
        modEventBus.addListener(this::commonSetup);

        ModEffects.register(modEventBus);
        ModItems.register(modEventBus);
        ModBlocks.register(modEventBus);
        ModBlockEntities.register(modEventBus);
        ModEntities.register(modEventBus);
        ModSounds.register(modEventBus);
        ModCreativeModeTabs.register(modEventBus);

        NeoForge.EVENT_BUS.register(this);
        modContainer.registerConfig(ModConfig.Type.COMMON, Config.SPEC);
    }

    private void commonSetup(FMLCommonSetupEvent event) {
        // Some common setup code
        LOGGER.info("HELLO FROM COMMON SETUP");

        if (Config.LOG_DIRT_BLOCK.getAsBoolean()) {
            LOGGER.info("DIRT BLOCK >> {}", BuiltInRegistries.BLOCK.getKey(Blocks.DIRT));
        }

        LOGGER.info("{}{}", Config.MAGIC_NUMBER_INTRODUCTION.get(), Config.MAGIC_NUMBER.getAsInt());

        Config.ITEM_STRINGS.get().forEach((item) -> LOGGER.info("ITEM >> {}", item));
    }

    @SubscribeEvent
    public void onServerStarting(ServerStartingEvent event) {
        // Do something when the server starts
        LOGGER.info("HELLO from server starting");
    }

    @SubscribeEvent
    public void onPlayerTick(PlayerTickEvent.Post event) {

        Player player = event.getEntity();

        if (player instanceof ServerPlayer serverPlayer) {
            ResourceLocation dimLocation = serverPlayer.level().dimension().location();
            GameType currentMode = serverPlayer.gameMode.getGameModeForPlayer();

            // Prüfung auf deine Dimension
            if (dimLocation.getPath().equals("shrine_interior")) {

                // Wechsel von Survival zu Adventure
                if (currentMode == GameType.SURVIVAL) {
                    serverPlayer.setGameMode(GameType.ADVENTURE);
                }

                // Elytren-Stopp
                if (serverPlayer.isFallFlying()) {
                    serverPlayer.stopFallFlying();
                }
            }
            // Rückwechsel beim Verlassen
            else if (currentMode == GameType.ADVENTURE) {
                serverPlayer.setGameMode(GameType.SURVIVAL);
            }
        }
    }
}
