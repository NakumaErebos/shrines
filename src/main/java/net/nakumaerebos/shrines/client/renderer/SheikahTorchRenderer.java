package net.nakumaerebos.shrines.client.renderer;

import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.nakumaerebos.shrines.block.entity.SheikahTorchBlockEntity;
import net.nakumaerebos.shrines.client.models.SheikahTorchModel;
import org.jetbrains.annotations.NotNull;
import software.bernie.geckolib.renderer.GeoBlockRenderer;

public class SheikahTorchRenderer extends GeoBlockRenderer<SheikahTorchBlockEntity> {
    public SheikahTorchRenderer(BlockEntityRendererProvider.Context context) {
        super(new SheikahTorchModel());
    }

    @Override
    public net.minecraft.world.phys.@NotNull AABB getRenderBoundingBox(SheikahTorchBlockEntity animatable) {
        return new net.minecraft.world.phys.AABB(animatable.getBlockPos()).inflate(2);
    }
}