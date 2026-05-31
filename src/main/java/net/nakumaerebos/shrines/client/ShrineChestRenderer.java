package net.nakumaerebos.shrines.client;

import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.nakumaerebos.shrines.block.entity.ShrineChestBlockEntity;
import software.bernie.geckolib.renderer.GeoBlockRenderer;

public class ShrineChestRenderer extends GeoBlockRenderer<ShrineChestBlockEntity> {
    public ShrineChestRenderer(BlockEntityRendererProvider.Context context) {
        super(new ShrineChestModel());
    }
}