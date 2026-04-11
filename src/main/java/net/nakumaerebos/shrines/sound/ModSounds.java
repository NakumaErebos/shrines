package net.nakumaerebos.shrines.sound;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.nakumaerebos.shrines.Shrines;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.function.Supplier;

public class ModSounds {
    public static final DeferredRegister<SoundEvent> SOUND_EVENTS =
            DeferredRegister.create(BuiltInRegistries.SOUND_EVENT, Shrines.MOD_ID);

    public static final Supplier<SoundEvent> SHRINE_DOOR_OPEN = registerSoundEvent("shrine_door_open");
    public static final Supplier<SoundEvent> AUTHENTICATION_BING = registerSoundEvent("authentication_bing");
    public static final Supplier<SoundEvent> HOLY_SHIMMER_SHATTER = registerSoundEvent("holy_shimmer_shatter");


    private static Supplier<SoundEvent> registerSoundEvent(String name) {
        ResourceLocation id = ResourceLocation.fromNamespaceAndPath(Shrines.MOD_ID, name);
        return SOUND_EVENTS.register(name, () -> SoundEvent.createVariableRangeEvent(id));
    }

    public static void register(IEventBus eventBus) {
        SOUND_EVENTS.register(eventBus);
    }
}