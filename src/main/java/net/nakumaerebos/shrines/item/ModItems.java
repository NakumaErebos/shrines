package net.nakumaerebos.shrines.item;

import net.minecraft.world.item.Item;
import net.nakumaerebos.shrines.Shrines;
import net.nakumaerebos.shrines.entity.ModEntities;
import net.nakumaerebos.shrines.item.custom.*;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.common.DeferredSpawnEggItem;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;

public class ModItems {
    public static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(Shrines.MOD_ID);

    public static final DeferredItem<SheikahSlateItemRemoteBombSquare> SHEIKAHSLATE = ITEMS.register("sheikahslate", () -> new SheikahSlateItemRemoteBombSquare(new Item.Properties()));
    public static final DeferredItem<SheikahSlateItemRemoteBombRound> SHEIKAHSLATE_ROUND = ITEMS.register("sheikahslate_round", () -> new SheikahSlateItemRemoteBombRound(new Item.Properties()));
    public static final DeferredItem<SheikahSlateItemStasis> SHEIKAHSLATE_STASIS = ITEMS.register("sheikahslate_stasis", () -> new SheikahSlateItemStasis(new Item.Properties()));
    public static final DeferredItem<SheikahSlateItemCryonis> SHEIKAHSLATE_CRYONIS = ITEMS.register("sheikahslate_cryonis", () -> new SheikahSlateItemCryonis(new Item.Properties()));
    public static final DeferredItem<SheikahSlateItemMagnesis> SHEIKAHSLATE_MAGNESIS = ITEMS.register("sheikahslate_magnesis", () -> new SheikahSlateItemMagnesis(new Item.Properties()));
    public static final DeferredItem<Item> DUNGEON_KEY = ITEMS.register("dungeon_key", () -> new Item(new Item.Properties()));

    public static final DeferredItem<Item> GUARDIAN_SCOUT_SPAWN_EGG =
            ITEMS.register("guardian_scout_i_spawn_egg", () -> new DeferredSpawnEggItem(
                    ModEntities.GUARDIAN_SCOUT_I,
                    0x4A5A6A,
                    0xCC3333,
                    new Item.Properties()
            ));

    public static final DeferredItem<Item> GUARDIAN_SCOUT_II_SPAWN_EGG =
            ITEMS.register("guardian_scout_ii_spawn_egg", () -> new DeferredSpawnEggItem(
                    ModEntities.GUARDIAN_SCOUT_II,
                    0x4A5A6A,
                    0xCC3333,
                    new Item.Properties()
            ));

    public static void register(IEventBus eventBus) {
        ITEMS.register(eventBus);
    }
}