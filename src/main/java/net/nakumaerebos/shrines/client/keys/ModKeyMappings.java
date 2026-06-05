package net.nakumaerebos.shrines.client.keys;

import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.KeyMapping;
import net.neoforged.neoforge.client.event.RegisterKeyMappingsEvent;
import org.lwjgl.glfw.GLFW;

public class ModKeyMappings {

    public static KeyMapping pullKey;
    public static KeyMapping pushKey;

    public static void registerKeyMappings(RegisterKeyMappingsEvent event) {
        pullKey = new KeyMapping(
                "key.shrines.magnesis_pull", // Übersetzungs-Schlüssel
                InputConstants.Type.KEYSYM,
                GLFW.GLFW_KEY_V,          // Standard: V
                "key.categories.shrines"    // Eigene Kategorie im Menü
        );

        pushKey = new KeyMapping(
                "key.shrines.magnesis_push",
                InputConstants.Type.KEYSYM,
                GLFW.GLFW_KEY_G,          // Standard: G
                "key.categories.shrines"
        );

        event.register(pullKey);
        event.register(pushKey);
    }
}