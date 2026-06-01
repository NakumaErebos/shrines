package net.nakumaerebos.shrines.attachments;

import com.mojang.serialization.Codec;
import net.nakumaerebos.shrines.Shrines;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.attachment.AttachmentType;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.NeoForgeRegistries;

import java.util.function.Supplier;

public class ModAttachments {
    public static final DeferredRegister<AttachmentType<?>> ATTACHMENT_TYPES =
            DeferredRegister.create(NeoForgeRegistries.Keys.ATTACHMENT_TYPES, Shrines.MOD_ID);

    public static final Supplier<AttachmentType<Integer>> FREEZE_TICKS =
            ATTACHMENT_TYPES.register("freeze_ticks", () -> AttachmentType.builder(() -> 0)
                    .serialize(Codec.INT) // Und hier wird der Codec für das Speichern übergeben
                    .build());

    public static final Supplier<AttachmentType<Float>> ACCUMULATED_DAMAGE =
            ATTACHMENT_TYPES.register("accumulated_damage", () -> AttachmentType.builder(() -> 0.0F).serialize(Codec.FLOAT).build());

    public static final Supplier<AttachmentType<Float>> ACCUMULATED_KNOCKBACK =
            ATTACHMENT_TYPES.register("accumulated_knockback", () -> AttachmentType.builder(() -> 0.0F).serialize(Codec.FLOAT).build());

    public static final Supplier<AttachmentType<Float>> LAST_ATTACK_YAW =
            ATTACHMENT_TYPES.register("last_attack_yaw", () -> AttachmentType.builder(() -> 0.0F).serialize(Codec.FLOAT).build());

    public static void register(IEventBus eventBus) {
        ATTACHMENT_TYPES.register(eventBus);
    }
}