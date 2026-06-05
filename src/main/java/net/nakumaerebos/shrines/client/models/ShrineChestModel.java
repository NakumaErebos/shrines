package net.nakumaerebos.shrines.client.models;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.state.BlockState;
import net.nakumaerebos.shrines.Shrines;
import net.nakumaerebos.shrines.block.custom.ShrineChestBlock;
import net.nakumaerebos.shrines.block.entity.ShrineChestBlockEntity;
import org.jetbrains.annotations.Nullable;
import software.bernie.geckolib.model.GeoModel;
import software.bernie.geckolib.renderer.GeoRenderer;

public class ShrineChestModel extends GeoModel<ShrineChestBlockEntity> {

    @Override
    public ResourceLocation getModelResource(ShrineChestBlockEntity animatable, @Nullable GeoRenderer<ShrineChestBlockEntity> renderer) {
        return ResourceLocation.fromNamespaceAndPath(Shrines.MOD_ID, "geo/shrine_chest.geo.json");
    }

    @Override
    public ResourceLocation getTextureResource(ShrineChestBlockEntity animatable, @Nullable GeoRenderer<ShrineChestBlockEntity> renderer) {

        BlockState state = animatable.getBlockState();

        if (state.getValue(ShrineChestBlock.FILLED)) {
            return ResourceLocation.fromNamespaceAndPath(Shrines.MOD_ID, "textures/block/shrine_chest_active.png");
        }

        return ResourceLocation.fromNamespaceAndPath(Shrines.MOD_ID, "textures/block/shrine_chest_inactive.png");
    }

    @Override
    public ResourceLocation getAnimationResource(ShrineChestBlockEntity animatable) {
        return ResourceLocation.fromNamespaceAndPath(Shrines.MOD_ID, "animations/shrine_chest.animation.json");
    }

    // --- Brücken-Methoden für die abstract Definitionen (deprecated in GeckoLib) ---

    @Deprecated
    @Override
    public ResourceLocation getModelResource(ShrineChestBlockEntity animatable) {
        return getModelResource(animatable, null);
    }

    @Deprecated
    @Override
    public ResourceLocation getTextureResource(ShrineChestBlockEntity animatable) {
        return getTextureResource(animatable, null);
    }
}