package net.nakumaerebos.shrines.client;

import net.minecraft.resources.ResourceLocation;
import net.nakumaerebos.shrines.Shrines;
import net.nakumaerebos.shrines.entity.StasisArrowEffectEntity;
import software.bernie.geckolib.model.GeoModel;

public class StasisArrowEffectModel extends GeoModel<StasisArrowEffectEntity> {

    @Override
    public ResourceLocation getModelResource(StasisArrowEffectEntity animatable) {
        // Du kannst hier dasselbe Basis-Modell für den Pfeil nutzen, da sich nur die Textur ändert
        return ResourceLocation.fromNamespaceAndPath(Shrines.MOD_ID, "geo/stasis_arrow_effect.geo.json");
    }

    @Override
    public ResourceLocation getTextureResource(StasisArrowEffectEntity animatable) {
        int stage = animatable.getArrowStage();

        String textureName = switch (stage) {
            case 1 -> "stasis_arrow_weak";
            case 2 -> "stasis_arrow_middle";
            case 3 -> "stasis_arrow_strong";
            default -> "empty"; // Eine komplett leere, transparente 2x2 PNG im Ordner, falls KB = 0
        };

        return ResourceLocation.fromNamespaceAndPath(Shrines.MOD_ID, "textures/entity/" + textureName + ".png");
    }

    @Override
    public ResourceLocation getAnimationResource(StasisArrowEffectEntity animatable) {
        return null;
    }
}