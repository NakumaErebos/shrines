package net.nakumaerebos.shrines.network;

import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;

public class ModNetworking {

    public static void registerPayloads(final RegisterPayloadHandlersEvent event) {
        final PayloadRegistrar registrar = event.registrar("1.0.0");

        registrar.playToServer(
                MagnesisKeyEvent.TYPE,
                MagnesisKeyEvent.CODEC,
                MagnesisKeyEvent::handle
        );

        // In deiner ModNetworking.java hinzufügen:
        registrar.playToClient(
                MagnesisSoundPacket.TYPE,
                MagnesisSoundPacket.CODEC,
                MagnesisSoundPacket::handle
        );
    }
}