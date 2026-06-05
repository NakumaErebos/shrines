package net.nakumaerebos.shrines.client.renderer;

import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.nakumaerebos.shrines.block.entity.SheikahLecternBlockEntity;
import net.nakumaerebos.shrines.client.models.SheikahLecternModel;
import org.jetbrains.annotations.NotNull;
import software.bernie.geckolib.renderer.GeoBlockRenderer;

public class SheikahLecternRenderer extends GeoBlockRenderer<SheikahLecternBlockEntity> {
    public SheikahLecternRenderer(BlockEntityRendererProvider.Context context) {
        super(new SheikahLecternModel());
    }

    @Override
    public net.minecraft.world.phys.@NotNull AABB getRenderBoundingBox(SheikahLecternBlockEntity animatable) {
        return new net.minecraft.world.phys.AABB(animatable.getBlockPos()).inflate(1.5);
    }
}