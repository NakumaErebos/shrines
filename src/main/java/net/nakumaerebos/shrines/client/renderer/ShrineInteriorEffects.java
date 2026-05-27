package net.nakumaerebos.shrines.client.renderer;

import net.minecraft.client.renderer.DimensionSpecialEffects;
import net.minecraft.world.phys.Vec3;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import org.jetbrains.annotations.NotNull;
import javax.annotation.Nullable;

public class ShrineInteriorEffects extends DimensionSpecialEffects {

    @OnlyIn(Dist.CLIENT)
    public ShrineInteriorEffects() {
        super(Float.NaN, false, SkyType.END, true, false);
    }

    @Override
    public @NotNull Vec3 getBrightnessDependentFogColor(@NotNull Vec3 fogColor, float brightness) {
        return fogColor; // Verhindert das Abdunkeln
    }

    @Override
    public boolean isFoggyAt(int x, int y) {
        return false;
    }

    @Nullable
    @Override
    public float[] getSunriseColor(float timeOfDay, float partialTicks) {
        return null;
    }
}