package net.nakumaerebos.shrines.block;

import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.nakumaerebos.shrines.Shrines;
import net.nakumaerebos.shrines.block.custom.SheikahStateBlock;
import net.nakumaerebos.shrines.block.custom.ShrineDoorBlock;
import net.nakumaerebos.shrines.block.custom.ShrineDoorDummyBlock;
import net.nakumaerebos.shrines.item.ModItems;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.function.Supplier;

public class ModBlocks {
    public static final DeferredRegister.Blocks BLOCKS = DeferredRegister.createBlocks(Shrines.MOD_ID);



    public static final DeferredBlock<SheikahStateBlock> SHEIKAHSTONE = registerBlock("sheikahstone",
            () -> new SheikahStateBlock(BlockBehaviour.Properties.of()
                    .lightLevel(state -> switch (state.getValue(SheikahStateBlock.STATE)) {
                        case 1 -> 5;
                        case 2 -> 6;
                        case 3 -> 7;
                        default -> 0;
                    }).noLootTable()
            ));

    public static final DeferredBlock<SheikahStateBlock> DARK_SHEIKAHSTONE = registerBlock("dark_sheikahstone",
            () -> new SheikahStateBlock(BlockBehaviour.Properties.of()
                    .lightLevel(state -> switch (state.getValue(SheikahStateBlock.STATE)) {
                        case 1 -> 5;
                        case 2 -> 6;
                        case 3 -> 7;
                        default -> 0;
                    }).noLootTable()
            ));

    public static final DeferredBlock<SheikahStateBlock> SHEIKAHSTONE_SWIRLS = registerBlock("sheikahstone_swirls",
            () -> new SheikahStateBlock(BlockBehaviour.Properties.of()
                    .lightLevel(state -> switch (state.getValue(SheikahStateBlock.STATE)) {
                        case 1 -> 5;
                        case 2 -> 9;
                        case 3 -> 15;
                        default -> 0;
                    }).noLootTable()
            ));

    public static final DeferredBlock<SheikahStateBlock> STARRY_DARKSTONE = registerBlock("starry_darkstone",
            () -> new SheikahStateBlock(BlockBehaviour.Properties.of()
                    .lightLevel(state -> switch (state.getValue(SheikahStateBlock.STATE)) {
                        case 1 -> 5;
                        case 2 -> 6;
                        case 3 -> 7;
                        default -> 0;
                    }).noLootTable()
            ));

    public static final DeferredBlock<Block> SHRINE_DOOR = registerBlock("shrine_door",
            () -> new ShrineDoorBlock(BlockBehaviour.Properties.of().noOcclusion()));

    public static final DeferredBlock<Block> SHRINE_DOOR_DUMMY = registerBlock("shrine_door_dummy",
            () -> new ShrineDoorDummyBlock(BlockBehaviour.Properties.of().noOcclusion().noCollission().dynamicShape()));

    public static final DeferredBlock<Block> DARKSTONE = registerBlock("darkstone",
            () -> new Block(BlockBehaviour.Properties.ofFullCopy(Blocks.BEDROCK)));

    public static final DeferredBlock<Block> CHISELED_DARKSTONE = registerBlock("chiseled_darkstone",
            () -> new Block(BlockBehaviour.Properties.ofFullCopy(Blocks.BEDROCK)));

    public static final DeferredBlock<RotatedPillarBlock> PILLARED_DARKSTONE = registerBlock("pillared_darkstone",
            () -> new RotatedPillarBlock(BlockBehaviour.Properties.ofFullCopy(Blocks.BEDROCK)));

    public static final DeferredBlock<RotatedPillarBlock> GLOWTRIMMED_DARKSTONE = registerBlock("glowtrimmed_darkstone",
            () -> new RotatedPillarBlock(BlockBehaviour.Properties.ofFullCopy(Blocks.BEDROCK)));

    public static final DeferredBlock<RotatedPillarBlock> PILLARED_GLOWTRIMMED_DARKSTONE = registerBlock("pillared_glowtrimmed_darkstone",
            () -> new RotatedPillarBlock(BlockBehaviour.Properties.ofFullCopy(Blocks.BEDROCK)));

    public static final DeferredBlock<RotatedPillarBlock> TRIMMED_DARKSTONE = registerBlock("trimmed_darkstone",
            () -> new RotatedPillarBlock(BlockBehaviour.Properties.ofFullCopy(Blocks.BEDROCK)));

    public static final DeferredBlock<RotatedPillarBlock> PILLARED_TRIMMED_DARKSTONE = registerBlock("pillared_trimmed_darkstone",
            () -> new RotatedPillarBlock(BlockBehaviour.Properties.ofFullCopy(Blocks.BEDROCK)));


    private static <T extends Block> DeferredBlock<T> registerBlock(String name, Supplier<T> block) {
        DeferredBlock<T> toReturn = BLOCKS.register(name, block);
        registerBlockItem(name, toReturn);
        return toReturn;
    }

    private static <T extends Block> void registerBlockItem(String name, DeferredBlock<T> block){
        ModItems.ITEMS.register(name, () -> new BlockItem(block.get(), new Item.Properties()));
    }

    public static void register(IEventBus eventBus) {
        BLOCKS.register(eventBus);
    }
}
