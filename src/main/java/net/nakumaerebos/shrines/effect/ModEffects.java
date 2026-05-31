package net.nakumaerebos.shrines.effect;

import net.minecraft.core.registries.Registries;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.nakumaerebos.shrines.Shrines;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public class ModEffects {
    public static final DeferredRegister<MobEffect> EFFECTS =
            DeferredRegister.create(Registries.MOB_EFFECT, Shrines.MOD_ID);

    // Dein bestehender Effekt
    public static final DeferredHolder<MobEffect, MobEffect> DEEP_DARKNESS = EFFECTS.register("deep_darkness",
            () -> new DeepDarknessEffect(MobEffectCategory.HARMFUL, 0x000000).setBlendDuration(22));

    // DEIN NEUER EFFEKT: Hier einfach die neue Klasse instanziieren
    public static final DeferredHolder<MobEffect, MobEffect> BLUE_FLAMES = EFFECTS.register("blue_flames",
            () -> new BlueFlamesEffect(MobEffectCategory.NEUTRAL, 0x60F5FA));

    public static void register(IEventBus eventBus) {
        EFFECTS.register(eventBus);
    }
}