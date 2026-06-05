package net.nakumaerebos.shrines.client.keys;

import net.minecraft.client.Minecraft;
import net.nakumaerebos.shrines.network.MagnesisKeyEvent;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.network.PacketDistributor;

public class ClientInputHandler {

    private static boolean lastPullState = false;
    private static boolean lastPushState = false;

    public static void onClientTick(ClientTickEvent.Post event) {
        var mc = Minecraft.getInstance();
        if (mc.player == null) return;

        // Prüfen, ob die Tasten gedrückt sind
        boolean isPullPressed = ModKeyMappings.pullKey.isDown();
        boolean isPushPressed = ModKeyMappings.pushKey.isDown();

        // Nur senden, wenn sich der Zustand geändert hat (schont das Netzwerk)
        if (isPullPressed != lastPullState || isPushPressed != lastPushState) {
            lastPullState = isPullPressed;
            lastPushState = isPushPressed;

            // Paket an den Server senden
            PacketDistributor.sendToServer(new MagnesisKeyEvent(isPullPressed, isPushPressed));
        }
    }
}