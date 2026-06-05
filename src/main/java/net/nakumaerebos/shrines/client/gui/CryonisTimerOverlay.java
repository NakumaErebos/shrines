package net.nakumaerebos.shrines.client.gui;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.nakumaerebos.shrines.item.custom.SheikahSlateItemCryonis;
import net.neoforged.neoforge.client.event.RenderGuiLayerEvent;
import net.neoforged.neoforge.client.gui.VanillaGuiLayers;

public class CryonisTimerOverlay {

    private static final ResourceLocation[] TIMER_TEXTURES = new ResourceLocation[49];

    static {
        for (int i = 0; i < 49; i++) {
            TIMER_TEXTURES[i] = ResourceLocation.fromNamespaceAndPath("shrines", "textures/timer/cryonis_timer/cryonis_timer_" + i + ".png");
        }
    }

    public static void onRenderGui(RenderGuiLayerEvent.Post event) {
        if (event.getName().equals(VanillaGuiLayers.HOTBAR)) {
            Minecraft mc = Minecraft.getInstance();
            Player player = mc.player;
            if (player == null) return;

            ItemStack mainHand = player.getMainHandItem();
            ItemStack offHand = player.getOffhandItem();
            ItemStack cryonisStack = null;

            if (mainHand.getItem() instanceof SheikahSlateItemCryonis) cryonisStack = mainHand;
            else if (offHand.getItem() instanceof SheikahSlateItemCryonis) cryonisStack = offHand;

            if (cryonisStack == null) return;

            int ticksLeft = SheikahSlateItemCryonis.getTicksRemaining(cryonisStack, player.level());
            if (ticksLeft <= 0) return;

            int maxDuration = SheikahSlateItemCryonis.getMaxDuration(cryonisStack);

            // Prozentualer Fortschritt (0.0 bis 1.0)
            float progress = (float) ticksLeft / (float) maxDuration;

            // FIX: "1.0f - progress" invertiert die Abspielrichtung der Bilder
            float invertedProgress = 1.0f - progress;

            // Index berechnen (Mth.clamp nutzen, da Math.clamp in manchen Java-Kontexten Probleme macht)
            int frameIndex = Mth.clamp((int) (invertedProgress * TIMER_TEXTURES.length), 0, TIMER_TEXTURES.length - 1);

            GuiGraphics guiGraphics = event.getGuiGraphics();
            int screenHeight = guiGraphics.guiHeight();

            // POSITIONS-FIX: Unten links platziert mit 10 Pixel Abstand vom Rand
            int x = 10;

            // GRÖSSEN-FIX: Doppelt so klein -> 94 / 2 = 47 Pixel Höhe und Breite
            int targetSize = 47;
            int y = screenHeight - targetSize - 10;

            RenderSystem.enableBlend();
            RenderSystem.defaultBlendFunc();

            // Die letzten beiden Parameter (47, 47) zwingen Minecraft dazu, die 94x94 Textur auf 47x47 herunterzuskalieren
            guiGraphics.blit(TIMER_TEXTURES[frameIndex], x, y, 0, 0, targetSize, targetSize, targetSize, targetSize);

            RenderSystem.disableBlend();
        }
    }
}