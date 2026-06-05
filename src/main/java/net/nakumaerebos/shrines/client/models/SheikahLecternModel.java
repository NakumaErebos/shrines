package net.nakumaerebos.shrines.client.models;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.state.BlockState;
import net.nakumaerebos.shrines.Shrines;
import net.nakumaerebos.shrines.block.custom.SheikahLecternBlock;
import net.nakumaerebos.shrines.block.entity.SheikahLecternBlockEntity;
import software.bernie.geckolib.model.GeoModel;
import software.bernie.geckolib.renderer.GeoRenderer;
import org.jetbrains.annotations.Nullable;

public class SheikahLecternModel extends GeoModel<SheikahLecternBlockEntity> {

    @Override
    public ResourceLocation getModelResource(SheikahLecternBlockEntity animatable, @Nullable GeoRenderer<SheikahLecternBlockEntity> renderer) {
        // Wir holen uns den BlockState direkt von der BlockEntity
        BlockState state = animatable.getBlockState();

        // Prüfung auf den ACTIVATED Property des Blocks
        if (state.getValue(SheikahLecternBlock.ACTIVATED)) {
            return ResourceLocation.fromNamespaceAndPath(Shrines.MOD_ID, "geo/sheikah_lectern_activated.geo.json");
        }

        return ResourceLocation.fromNamespaceAndPath(Shrines.MOD_ID, "geo/sheikah_lectern.geo.json");
    }

    @Override
    public ResourceLocation getTextureResource(SheikahLecternBlockEntity animatable, @Nullable GeoRenderer<SheikahLecternBlockEntity> renderer) {
        // Gleiche Textur für beide Zustände (oder hier ebenfalls if/else falls nötig)
        return ResourceLocation.fromNamespaceAndPath(Shrines.MOD_ID, "textures/block/sheikah_lectern.png");
    }

    @Override
    public ResourceLocation getAnimationResource(SheikahLecternBlockEntity animatable) {
        return ResourceLocation.fromNamespaceAndPath(Shrines.MOD_ID, "animations/sheikah_lectern.animation.json");
    }

    // --- Brücken-Methoden für die abstract Definitionen (deprecated in GeckoLib) ---

    @Deprecated
    @Override
    public ResourceLocation getModelResource(SheikahLecternBlockEntity animatable) {
        return getModelResource(animatable, null);
    }

    @Deprecated
    @Override
    public ResourceLocation getTextureResource(SheikahLecternBlockEntity animatable) {
        return getTextureResource(animatable, null);
    }
}