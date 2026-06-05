package net.nakumaerebos.shrines.network;

import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.sounds.AbstractTickableSoundInstance;
import net.minecraft.client.resources.sounds.SoundInstance;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundSource;
import net.nakumaerebos.shrines.sound.ModSounds;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import org.jetbrains.annotations.NotNull;

public record MagnesisSoundPacket(boolean start) implements CustomPacketPayload {

    public static final Type<MagnesisSoundPacket> TYPE = new Type<>(ResourceLocation.fromNamespaceAndPath("shrines", "magnesis_sound_packet"));

    public static final StreamCodec<FriendlyByteBuf, MagnesisSoundPacket> CODEC = StreamCodec.composite(
            ByteBufCodecs.BOOL, MagnesisSoundPacket::start,
            MagnesisSoundPacket::new
    );

    @Override
    public @NotNull Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handle(final MagnesisSoundPacket payload, final IPayloadContext context) {
        context.enqueueWork(() -> {
            Minecraft mc = Minecraft.getInstance();
            if (mc.level == null || mc.player == null) return;

            ResourceLocation soundLocation = ModSounds.MAGNESIS_LOOP.get().getLocation();

            if (payload.start()) {
                // Erst alle eventuell noch hängenden alten Loops dieses Sounds hart killen
                mc.getSoundManager().stop(soundLocation, SoundSource.PLAYERS);

                // Ein Tickable-Sound verfolgt den Spieler dynamisch durch die Welt!
                SoundInstance dynamicSound = new AbstractTickableSoundInstance(ModSounds.MAGNESIS_LOOP.get(), SoundSource.PLAYERS, SoundInstance.createUnseededRandom()) {
                    {
                        this.looping = true;
                        this.delay = 0;
                        this.volume = 0.6F;
                        this.pitch = 0.8F;
                        // An den Spieler binden
                        this.x = mc.player.getX();
                        this.y = mc.player.getY();
                        this.z = mc.player.getZ();
                    }

                    @Override
                    public void tick() {
                        if (mc.player == null || !mc.player.isAlive()) {
                            this.stop(); // Sich selbst zerstören, falls der Spieler stirbt
                        } else {
                            // Sound-Position wandert flüssig mit dem Spieler mit
                            this.x = mc.player.getX();
                            this.y = mc.player.getY();
                            this.z = mc.player.getZ();
                        }
                    }
                };

                mc.getSoundManager().play(dynamicSound);
            } else {
                // STOP-Signal: Stoppt gezielt jede Instanz dieses spezifischen Loops
                mc.getSoundManager().stop(soundLocation, SoundSource.PLAYERS);
            }
        });
    }
}