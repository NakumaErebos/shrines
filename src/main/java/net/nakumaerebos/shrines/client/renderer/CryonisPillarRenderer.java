package net.nakumaerebos.shrines.client.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.core.Direction;
import net.nakumaerebos.shrines.client.models.CryonisPillarModel;
import net.nakumaerebos.shrines.entity.CryonisPillarEntity;
import software.bernie.geckolib.renderer.GeoEntityRenderer;

public class CryonisPillarRenderer extends GeoEntityRenderer<CryonisPillarEntity> {

    public CryonisPillarRenderer(EntityRendererProvider.Context renderManager) {
        super(renderManager, new CryonisPillarModel());
    }

    @Override
    protected void applyRotations(CryonisPillarEntity animatable, PoseStack poseStack, float ageInTicks, float rotationYaw, float partialTick, float nativeScale) {
        // super.applyRotations(...) wird weiterhin ignoriert, um die MC-Standarddrehung auszuhebeln

        Direction dir = animatable.getOrientation();
        if (dir == null) dir = Direction.UP;

        switch (dir) {
            case NORTH:
                // Nach Norden kippen: Drehung um die globale X-Achse nach vorne (90 Grad)
                poseStack.mulPose(Axis.XP.rotationDegrees(-90));
                break;
            case SOUTH:
                // Nach Süden kippen: Drehung um die globale X-Achse nach hinten (-90 Grad)
                // Da wir uns nicht um die Y-Achse drehen, schaut die Oberseite jetzt nach Süden
                poseStack.mulPose(Axis.XP.rotationDegrees(90));
                break;
            case WEST:
                // Nach Westen kippen: Drehung um die globale Z-Achse nach links (-90 Grad)
                poseStack.mulPose(Axis.ZP.rotationDegrees(90));
                break;
            case EAST:
                // Nach Osten kippen: Drehung um die globale Z-Achse nach rechts (90 Grad)
                poseStack.mulPose(Axis.ZP.rotationDegrees(-90));
                break;
            case DOWN:
                // Komplett auf den Kopf stellen
                poseStack.mulPose(Axis.XP.rotationDegrees(-180));
                break;
            case UP:
            default:
                // Standard: Keine Rotation, Säule steht senkrecht nach oben
                break;
        }
    }
}