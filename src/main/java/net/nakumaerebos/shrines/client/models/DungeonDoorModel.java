package net.nakumaerebos.shrines.client.models;

import net.minecraft.resources.ResourceLocation;
import net.nakumaerebos.shrines.Shrines;
import net.nakumaerebos.shrines.block.entity.DungeonDoorBlockEntity;
import org.jetbrains.annotations.Nullable;
import software.bernie.geckolib.model.GeoModel;
import software.bernie.geckolib.renderer.GeoRenderer;

public class DungeonDoorModel extends GeoModel<DungeonDoorBlockEntity> {

    @Override
    public ResourceLocation getModelResource(DungeonDoorBlockEntity animatable, @Nullable GeoRenderer<DungeonDoorBlockEntity> renderer) {
        return ResourceLocation.fromNamespaceAndPath(Shrines.MOD_ID, "geo/dungeon_door.geo.json");
    }

    @Override
    public ResourceLocation getTextureResource(DungeonDoorBlockEntity animatable, @Nullable GeoRenderer<DungeonDoorBlockEntity> renderer) {
        return ResourceLocation.fromNamespaceAndPath(Shrines.MOD_ID, "textures/block/dungeon_door.png");
    }

    @Override
    public ResourceLocation getAnimationResource(DungeonDoorBlockEntity animatable) {
        return ResourceLocation.fromNamespaceAndPath(Shrines.MOD_ID, "animations/dungeon_door.animation.json");
    }

    // --- Brücken-Methoden zur Erfüllung der abstrakten (deprecated) Basis-Methoden ---

    @Deprecated
    @Override
    public ResourceLocation getModelResource(DungeonDoorBlockEntity animatable) {
        return getModelResource(animatable, null);
    }

    @Deprecated
    @Override
    public ResourceLocation getTextureResource(DungeonDoorBlockEntity animatable) {
        return getTextureResource(animatable, null);
    }
}