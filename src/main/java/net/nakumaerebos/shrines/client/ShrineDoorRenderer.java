package net.nakumaerebos.shrines.client;

import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.nakumaerebos.shrines.block.entity.ShrineDoorBlockEntity;
import org.jetbrains.annotations.NotNull;
import software.bernie.geckolib.renderer.GeoBlockRenderer;

public class ShrineDoorRenderer extends GeoBlockRenderer<ShrineDoorBlockEntity> {
    public ShrineDoorRenderer(BlockEntityRendererProvider.Context context) {
        super(new ShrineDoorModel());
    }

    @Override
    public net.minecraft.world.phys.@NotNull AABB getRenderBoundingBox(ShrineDoorBlockEntity animatable) {
        // Wir nehmen die Position der BlockEntity und vergrößern sie massiv.
        // Da dein Tor 6 Blöcke breit und 3 hoch ist, ist inflate(4) ein sicherer Wert.
        return new net.minecraft.world.phys.AABB(animatable.getBlockPos()).inflate(4.0, 3.0, 4.0);
    }
}