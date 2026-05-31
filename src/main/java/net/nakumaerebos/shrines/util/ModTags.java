package net.nakumaerebos.shrines.util;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.ItemTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.nakumaerebos.shrines.Shrines;

public class ModTags {
    public static class Blocks {

        private static TagKey<Block> createTag(String name) {
            return BlockTags.create(ResourceLocation.fromNamespaceAndPath(Shrines.MOD_ID, name));
        }
    }

    public static class Items {
        public static final TagKey<Item> IS_BLUE_FLAME_INFLAMMATORY = createTag("is_blue_flame_inflammatory");

        private static TagKey<Item> createTag(String name) {
            return ItemTags.create(ResourceLocation.fromNamespaceAndPath(Shrines.MOD_ID, name));
        }
    }
}