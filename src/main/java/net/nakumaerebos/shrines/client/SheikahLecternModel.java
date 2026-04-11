package net.nakumaerebos.shrines.client;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.state.BlockState;
import net.nakumaerebos.shrines.Shrines;
import net.nakumaerebos.shrines.block.custom.SheikahLecternBlock;
import net.nakumaerebos.shrines.block.entity.SheikahLecternBlockEntity;
import software.bernie.geckolib.model.GeoModel;

public class SheikahLecternModel extends GeoModel<SheikahLecternBlockEntity> {
    @Override
    public ResourceLocation getModelResource(SheikahLecternBlockEntity animatable) {
        // Wir holen uns den BlockState direkt von der BlockEntity
        BlockState state = animatable.getBlockState();

        if (state.getValue(SheikahLecternBlock.ACTIVATED)) {
            return ResourceLocation.fromNamespaceAndPath(Shrines.MOD_ID, "geo/sheikah_lectern_activated.geo.json");
        }

        return ResourceLocation.fromNamespaceAndPath(Shrines.MOD_ID, "geo/sheikah_lectern.geo.json");
    }

    @Override
    public ResourceLocation getTextureResource(SheikahLecternBlockEntity animatable) {
        // Falls das aktivierte Modell eine andere Textur braucht, hier ebenfalls if/else nutzen
        return ResourceLocation.fromNamespaceAndPath(Shrines.MOD_ID, "textures/block/sheikah_lectern.png");
    }

    @Override
    public ResourceLocation getAnimationResource(SheikahLecternBlockEntity animatable) {
        return ResourceLocation.fromNamespaceAndPath(Shrines.MOD_ID, "animations/sheikah_lectern.animation.json");
    }
}