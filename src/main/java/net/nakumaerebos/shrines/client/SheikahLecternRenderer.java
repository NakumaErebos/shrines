package net.nakumaerebos.shrines.client;

import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.nakumaerebos.shrines.block.entity.SheikahLecternBlockEntity;
import net.nakumaerebos.shrines.block.entity.ShrineDoorBlockEntity;
import org.jetbrains.annotations.NotNull;
import software.bernie.geckolib.renderer.GeoBlockRenderer;

public class SheikahLecternRenderer extends GeoBlockRenderer<SheikahLecternBlockEntity> {
    public SheikahLecternRenderer(BlockEntityRendererProvider.Context context) {
        super(new SheikahLecternModel());
    }

    @Override
    public net.minecraft.world.phys.@NotNull AABB getRenderBoundingBox(SheikahLecternBlockEntity animatable) {
        // Ein Pult ist meist nur 1 Block hoch, aber falls Teile der Animation
        // leicht überstehen (z.B. das Slate beim Einstecken), nehmen wir ein kleines Padding.
        return new net.minecraft.world.phys.AABB(animatable.getBlockPos()).inflate(1.5);
    }
}