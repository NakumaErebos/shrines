package net.nakumaerebos.shrines.client.models;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Player;
import net.nakumaerebos.shrines.Shrines;
import net.nakumaerebos.shrines.entity.GuardianScoutIIMobEntity;
import software.bernie.geckolib.animation.AnimationState;
import software.bernie.geckolib.cache.object.GeoBone;
import software.bernie.geckolib.model.GeoModel;

public class GuardianScoutIIMobModel extends GeoModel<GuardianScoutIIMobEntity> {

    @Override
    public ResourceLocation getModelResource(GuardianScoutIIMobEntity animatable) {
        return ResourceLocation.fromNamespaceAndPath(Shrines.MOD_ID, "geo/guardian_scout_ii.geo.json");
    }

    @Override
    public ResourceLocation getTextureResource(GuardianScoutIIMobEntity animatable) {
        return ResourceLocation.fromNamespaceAndPath(Shrines.MOD_ID, "textures/entity/guardian_scout_ii.png");
    }

    @Override
    public ResourceLocation getAnimationResource(GuardianScoutIIMobEntity animatable) {
        // Da die Animationen im Prefix alle "animation.guardianscout..." heißen,
        // gehe ich davon aus, dass sie in derselben Datei liegen.
        return ResourceLocation.fromNamespaceAndPath(Shrines.MOD_ID, "animations/guardianscout.animation.json");
    }

    @Override
    public void setCustomAnimations(GuardianScoutIIMobEntity animatable, long instanceId, AnimationState<GuardianScoutIIMobEntity> animationState) {
        GeoBone head = getAnimationProcessor().getBone("bone18");

        if (head != null) {
            // Kopf soll sich nur drehen, wenn er aktiv (entfaltet) ist. Status 2 = ACTIVE
            if (animatable.getFoldState() == 2) {
                Player closestPlayer = animatable.level().getNearestPlayer(
                        animatable.getX(), animatable.getY(), animatable.getZ(),
                        24.0D, false);

                if (closestPlayer != null) {
                    double dx = closestPlayer.getX() - animatable.getX();
                    double dz = closestPlayer.getZ() - animatable.getZ();

                    float targetAngle = (float) Math.atan2(dz, dx);
                    float bodyYaw = animatable.yBodyRot * Mth.DEG_TO_RAD;
                    float finalYaw = -(targetAngle - bodyYaw) + (float)Math.PI - (float)(Math.PI / 2);

                    head.setRotY(Mth.wrapDegrees(finalYaw * Mth.RAD_TO_DEG) * Mth.DEG_TO_RAD);
                } else {
                    head.setRotY(Mth.lerp(0.05f, head.getRotY(), 0));
                }
            } else {
                head.setRotY(Mth.lerp(0.1f, head.getRotY(), 0));
            }
        }
    }
}