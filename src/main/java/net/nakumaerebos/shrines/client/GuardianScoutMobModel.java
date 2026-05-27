package net.nakumaerebos.shrines.client;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Player;
import net.nakumaerebos.shrines.Shrines;
import net.nakumaerebos.shrines.entity.GuardianScoutMobEntity;
import software.bernie.geckolib.animation.AnimationState;
import software.bernie.geckolib.cache.object.GeoBone;
import software.bernie.geckolib.constant.DataTickets;
import software.bernie.geckolib.model.GeoModel;
import software.bernie.geckolib.model.data.EntityModelData;

public class GuardianScoutMobModel extends GeoModel<GuardianScoutMobEntity> {

    @Override
    public ResourceLocation getModelResource(GuardianScoutMobEntity animatable) {
        return ResourceLocation.fromNamespaceAndPath(Shrines.MOD_ID, "geo/guardianscout.geo.json");
    }

    @Override
    public ResourceLocation getTextureResource(GuardianScoutMobEntity animatable) {
        return ResourceLocation.fromNamespaceAndPath(Shrines.MOD_ID, "textures/entity/guardianscout.png");
    }

    @Override
    public ResourceLocation getAnimationResource(GuardianScoutMobEntity animatable) {
        return ResourceLocation.fromNamespaceAndPath(Shrines.MOD_ID, "animations/guardianscout.animation.json");
    }

    @Override
    public void setCustomAnimations(GuardianScoutMobEntity animatable, long instanceId, AnimationState<GuardianScoutMobEntity> animationState) {
        GeoBone head = getAnimationProcessor().getBone("bone18");

        if (head != null) {
            Player closestPlayer = animatable.level().getNearestPlayer(
                    animatable.getX(), animatable.getY(), animatable.getZ(),
                    24.0D,
                    false);

            if (closestPlayer != null) {
                double dx = closestPlayer.getX() - animatable.getX();
                double dz = closestPlayer.getZ() - animatable.getZ();

                // 1. Berechne den Zielwinkel
                float targetAngle = (float) Math.atan2(dz, dx);

                // 2. Hol die Körperrotation
                float bodyYaw = animatable.yBodyRot * Mth.DEG_TO_RAD;

                // 3. Die korrigierte Formel:
                // Wir addieren Math.PI (180 Grad), um den "Hinterkopf-Effekt" zu beheben.
                // Das Minuszeichen davor sorgt für die korrekte Links/Rechts-Richtung.
                float finalYaw = -(targetAngle - bodyYaw) + (float)Math.PI - (float)(Math.PI / 2);

                // 4. Wrap Degrees sorgt dafür, dass er den kürzesten Weg zum Drehen nimmt
                head.setRotY(Mth.wrapDegrees(finalYaw * Mth.RAD_TO_DEG) * Mth.DEG_TO_RAD);
            } else {
                // Langsames Zurückdrehen zur Mitte
                head.setRotY(Mth.lerp(0.05f, head.getRotY(), 0));
            }
        }
    }
}