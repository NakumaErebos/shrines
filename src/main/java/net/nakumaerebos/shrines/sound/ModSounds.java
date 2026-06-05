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
    public static final Supplier<SoundEvent> REMOTE_BOMB_HOLD = registerSoundEvent("remote_bomb_hold");
    public static final Supplier<SoundEvent> REMOTE_BOMB_EXPLODE = registerSoundEvent("remote_bomb_explode");
    public static final Supplier<SoundEvent> REMOTE_BOMB_APPEAR = registerSoundEvent("remote_bomb_appear");
    public static final Supplier<SoundEvent> SHRINE_MUSIC = registerSoundEvent("shrine_music");
    public static final Supplier<SoundEvent> STASIS_END_1 = registerSoundEvent("stasis_end_1");
    public static final Supplier<SoundEvent> STASIS_END_2 = registerSoundEvent("stasis_end_2");
    public static final Supplier<SoundEvent> STASIS_END_3 = registerSoundEvent("stasis_end_3");
    public static final Supplier<SoundEvent> STASIS_HIT = registerSoundEvent("stasis_hit");
    public static final Supplier<SoundEvent> STASIS_START = registerSoundEvent("stasis_start");
    public static final Supplier<SoundEvent> STASIS_TIMER = registerSoundEvent("stasis_timer");
    public static final Supplier<SoundEvent> STASIS_TIMER_SHORT = registerSoundEvent("stasis_timer_short");
    public static final Supplier<SoundEvent> STASIS_TIMER_VERY_SHORT = registerSoundEvent("stasis_timer_very_short");
    public static final Supplier<SoundEvent> BREAK_CRYONIS_PILLAR = registerSoundEvent("break_cryonis_pillar");
    public static final Supplier<SoundEvent> GROW_CRYONIS_PILLAR = registerSoundEvent("grow_cryonis_pillar");
    public static final Supplier<SoundEvent> MAGNESIS_CATCH = registerSoundEvent("magnesis_catch");
    public static final Supplier<SoundEvent> MAGNESIS_LOOP = registerSoundEvent("magnesis_loop");
    public static final Supplier<SoundEvent> MAGNESIS_RELEASE = registerSoundEvent("magnesis_release");


    private static Supplier<SoundEvent> registerSoundEvent(String name) {
        ResourceLocation id = ResourceLocation.fromNamespaceAndPath(Shrines.MOD_ID, name);
        return SOUND_EVENTS.register(name, () -> SoundEvent.createVariableRangeEvent(id));
    }

    public static void register(IEventBus eventBus) {
        SOUND_EVENTS.register(eventBus);
    }
}