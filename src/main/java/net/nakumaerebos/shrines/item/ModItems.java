package net.nakumaerebos.shrines.item;

import net.minecraft.world.item.Item;
import net.nakumaerebos.shrines.Shrines;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;

public class ModItems {
    public static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(Shrines.MOD_ID);

    public static final DeferredItem<Item> EXAMPLE = ITEMS.register("example", () -> new Item(new Item.Properties()));

    public static void register(IEventBus eventBus) {
        ITEMS.register(eventBus);
    }
}
