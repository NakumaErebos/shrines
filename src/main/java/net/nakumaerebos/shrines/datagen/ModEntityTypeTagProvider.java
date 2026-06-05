package net.nakumaerebos.shrines.datagen;

import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.PackOutput;
import net.minecraft.data.tags.EntityTypeTagsProvider;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.EntityType;
import net.nakumaerebos.shrines.Shrines;
import net.neoforged.neoforge.common.data.ExistingFileHelper;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.concurrent.CompletableFuture;

public class ModEntityTypeTagProvider extends EntityTypeTagsProvider {
    public ModEntityTypeTagProvider(PackOutput output, CompletableFuture<HolderLookup.Provider> provider, @Nullable ExistingFileHelper existingFileHelper) {
        super(output, provider, Shrines.MOD_ID, existingFileHelper);
    }

    public static final TagKey<EntityType<?>> MAGNESIS_GRABBABLE = TagKey.create(
            Registries.ENTITY_TYPE,
            ResourceLocation.fromNamespaceAndPath(Shrines.MOD_ID, "magnesis_grabbable")
    );

    public static final TagKey<EntityType<?>> SHORT_STASISABLE = TagKey.create(
            Registries.ENTITY_TYPE,
            ResourceLocation.fromNamespaceAndPath(Shrines.MOD_ID, "short_stasisable")
    );

    public static final TagKey<EntityType<?>> VERY_SHORT_STASISABLE = TagKey.create(
            Registries.ENTITY_TYPE,
            ResourceLocation.fromNamespaceAndPath(Shrines.MOD_ID, "very_short_stasisable")
    );

    public static final TagKey<EntityType<?>> STASISALBE = TagKey.create(
            Registries.ENTITY_TYPE,
            ResourceLocation.fromNamespaceAndPath(Shrines.MOD_ID, "stasisable")
    );

    @Override
    protected void addTags(HolderLookup.@NotNull Provider provider) {
        // Hier tragen wir Schwein und Schaf in das Tag ein
        tag(MAGNESIS_GRABBABLE)
                .add(EntityType.IRON_GOLEM);

        tag(STASISALBE)
                .add(EntityType.PIG);

        tag(SHORT_STASISABLE)
                .add(EntityType.HUSK);

        tag(VERY_SHORT_STASISABLE)
                .add(EntityType.WITHER);
    }
}