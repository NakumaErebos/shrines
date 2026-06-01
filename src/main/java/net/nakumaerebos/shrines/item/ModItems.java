package net.nakumaerebos.shrines.item;

import net.minecraft.world.item.Item;
import net.nakumaerebos.shrines.Shrines;
import net.nakumaerebos.shrines.entity.ModEntities;
import net.nakumaerebos.shrines.item.custom.FreezeWandItem;
import net.nakumaerebos.shrines.item.custom.SheikahSlateItem;
import net.nakumaerebos.shrines.item.custom.SheikahSlateItemRound;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.common.DeferredSpawnEggItem;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;

public class ModItems {
    public static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(Shrines.MOD_ID);

    public static final DeferredItem<SheikahSlateItem> SHEIKAHSLATE = ITEMS.register("sheikahslate", () -> new SheikahSlateItem(new Item.Properties()));
    public static final DeferredItem<SheikahSlateItemRound> SHEIKAHSLATE_ROUND = ITEMS.register("sheikahslate_round", () -> new SheikahSlateItemRound(new Item.Properties()));
    public static final DeferredItem<FreezeWandItem> SHEIKAHSLATE_STASIS = ITEMS.register("sheikahslate_stasis", () -> new FreezeWandItem(new Item.Properties()));
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