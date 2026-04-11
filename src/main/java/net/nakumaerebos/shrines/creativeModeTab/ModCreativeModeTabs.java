package net.nakumaerebos.shrines.creativeModeTab;

import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.nakumaerebos.shrines.Shrines;
import net.nakumaerebos.shrines.block.ModBlocks;
import net.nakumaerebos.shrines.item.ModItems;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public class ModCreativeModeTabs {
    public static final DeferredRegister<CreativeModeTab> CREATIVE_MODE_TABS = DeferredRegister.create(Registries.CREATIVE_MODE_TAB, Shrines.MOD_ID);

    public static final DeferredHolder<CreativeModeTab, CreativeModeTab> SHRINES_TAB = CREATIVE_MODE_TABS.register("shrines", () -> CreativeModeTab.builder()
            .title(Component.translatable("itemGroup.shrines"))
            .withTabsBefore(net.minecraft.world.item.CreativeModeTabs.COMBAT)
            .icon(ModItems.SHEIKAHSLATE::toStack)
            .displayItems((parameters, output) -> {
                ModItems.ITEMS.getEntries().forEach(item -> {
                    output.accept(item.get());
                });
                ModBlocks.BLOCKS.getEntries().stream()
                        .filter(block -> block != ModBlocks.SHRINE_DOOR_DUMMY)
                        .forEach(block -> output.accept(block.get()));
            }).build());

    public static void register(IEventBus eventBus) {
        CREATIVE_MODE_TABS.register(eventBus);
    }
}
