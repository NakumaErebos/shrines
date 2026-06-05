package net.nakumaerebos.shrines.network;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.nakumaerebos.shrines.Shrines;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record MagnesisKeyEvent(boolean pullPressed, boolean pushPressed) implements CustomPacketPayload {

    public static final Type<MagnesisKeyEvent> TYPE = new Type<>(ResourceLocation.fromNamespaceAndPath(Shrines.MOD_ID, "magnesis_key_event"));

    public static final StreamCodec<FriendlyByteBuf, MagnesisKeyEvent> CODEC = StreamCodec.composite(
            ByteBufCodecs.BOOL, MagnesisKeyEvent::pullPressed,
            ByteBufCodecs.BOOL, MagnesisKeyEvent::pushPressed,
            MagnesisKeyEvent::new
    );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    // Wird auf dem Server ausgeführt
    public static void handle(final MagnesisKeyEvent payload, final IPayloadContext context) {
        context.enqueueWork(() -> {
            Player player = context.player();
            player.getPersistentData().putBoolean("MagnesisPulling", payload.pullPressed());
            player.getPersistentData().putBoolean("MagnesisPushing", payload.pushPressed());
        });
    }
}