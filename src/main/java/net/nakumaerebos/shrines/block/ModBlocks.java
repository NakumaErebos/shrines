package net.nakumaerebos.shrines.block;

import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.MapColor;
import net.nakumaerebos.shrines.Shrines;
import net.nakumaerebos.shrines.block.custom.*;
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

    public static final DeferredBlock<SheikahStateSlabBlock> SHEIKAHSTONE_SLAB = registerBlock("sheikahstone_slab",
            () -> new SheikahStateSlabBlock(BlockBehaviour.Properties.of()
                    .lightLevel(state -> switch (state.getValue(SheikahStateBlock.STATE)) {
                        case 1 -> 5;
                        case 2 -> 6;
                        case 3 -> 7;
                        default -> 0;
                    }).noLootTable().noOcclusion()
            ));

    public static final DeferredBlock<SheikahStateStairBlock> SHEIKAHSTONE_STAIRS = registerBlock("sheikahstone_stairs",
            () -> new SheikahStateStairBlock(ModBlocks.SHEIKAHSTONE.get().defaultBlockState(),
                    BlockBehaviour.Properties.of()
                            .lightLevel(state -> switch (state.getValue(SheikahStateBlock.STATE)) {
                                case 1 -> 5;
                                case 2 -> 6;
                                case 3 -> 7;
                                default -> 0;
                            }).noLootTable().noOcclusion()
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
                        case 3 -> 9;
                        default -> 0;
                    }).noLootTable()
            ));

    public static final DeferredBlock<WallBlock> SHEIKAHSTONE_SWIRLS_WALL = registerBlock("sheikahstone_swirls_wall",
            () -> new WallBlock(BlockBehaviour.Properties.ofFullCopy(Blocks.BEDROCK)));

    public static final DeferredBlock<SheikahStateBlock> STARRY_DARKSTONE = registerBlock("starry_darkstone",
            () -> new SheikahStateBlock(BlockBehaviour.Properties.of()
                    .lightLevel(state -> switch (state.getValue(SheikahStateBlock.STATE)) {
                        case 1 -> 5;
                        case 2 -> 6;
                        case 3 -> 7;
                        default -> 0;
                    }).noLootTable()
            ));

    public static final DeferredBlock<SheikahStateWithFacingBlock> SHRINE_SYMBOL_BLOCK = registerBlock("shrine_symbol_block",
            () -> new SheikahStateWithFacingBlock(BlockBehaviour.Properties.of()
                    .lightLevel(state -> switch (state.getValue(SheikahStateBlock.STATE)) {
                        case 1 -> 5;
                        case 2 -> 6;
                        case 3 -> 7;
                        default -> 0;
                    }).noLootTable()
            ));

    public static final DeferredBlock<SheikahStateSlabBlock> SHEIKAHSTONE_SWIRLS_SLAB = registerBlock("sheikahstone_swirls_slab",
            () -> new SheikahStateSlabBlock(BlockBehaviour.Properties.of()
                            .lightLevel(state -> switch (state.getValue(SheikahStateBlock.STATE)) {
                                case 1 -> 5;
                                case 2 -> 6;
                                case 3 -> 7;
                                default -> 0;
                            }).noLootTable().noOcclusion()
            ));

    public static final DeferredBlock<SheikahStateStairBlock> SHEIKAHSTONE_SWIRLS_STAIRS = registerBlock("sheikahstone_swirls_stairs",
            () -> new SheikahStateStairBlock(ModBlocks.SHEIKAHSTONE_SWIRLS.get().defaultBlockState(),
            BlockBehaviour.Properties.of()
                    .lightLevel(state -> switch (state.getValue(SheikahStateBlock.STATE)) {
                        case 1 -> 5;
                        case 2 -> 6;
                        case 3 -> 7;
                        default -> 0;
                    }).noLootTable().noOcclusion()
            ));

    public static final DeferredBlock<VerticalSlabBlock> INVISIBLE_VERTICAL_SLAB = registerBlock("invisible_vertical_slab",
            () -> new VerticalSlabBlock(BlockBehaviour.Properties.ofFullCopy(Blocks.BARRIER)));

    public static final DeferredBlock<Block> SHRINE_DOOR = registerBlock("shrine_door",
            () -> new ShrineDoorBlock(BlockBehaviour.Properties.of().noOcclusion().noLootTable()));

    public static final DeferredBlock<Block> SHEIKAH_LECTERN = registerBlock("sheikah_lectern",
            () -> new SheikahLecternBlock(BlockBehaviour.Properties.of().noOcclusion().noLootTable()));

    public static final DeferredBlock<Block> SHRINE_DOOR_DUMMY = registerBlock("shrine_door_dummy",
            () -> new ShrineDoorDummyBlock(BlockBehaviour.Properties.of().noLootTable().noOcclusion().noCollission().dynamicShape()));

    public static final DeferredBlock<Block> DARKSTONE = registerBlock("darkstone",
            () -> new Block(BlockBehaviour.Properties.ofFullCopy(Blocks.BEDROCK)));

    public static final DeferredBlock<ElevatorPlatformBlock> ELEVATOR_PLATFORM = registerBlock("elevator_platform",
            () -> new ElevatorPlatformBlock(BlockBehaviour.Properties.of().noOcclusion().noLootTable()));

    public static final DeferredBlock<ElevatorPlatformBlock> ELEVATOR_PLATFORM_MOVED = registerBlock("elevator_platform_moved",
            () -> new ElevatorPlatformBlock(BlockBehaviour.Properties.of().noOcclusion().noLootTable()));

    public static final DeferredBlock<HolyShimmerBlock> HOLY_SHIMMER = registerBlock("holy_shimmer",
            () -> new HolyShimmerBlock(BlockBehaviour.Properties.of().noOcclusion().noLootTable()));

    public static final DeferredBlock<ElevatorBlock> ELEVATOR = registerBlock("elevator",
            () -> new ElevatorBlock(BlockBehaviour.Properties.ofFullCopy(Blocks.BEDROCK)));

    public static final DeferredBlock<ElevatorBlock> ELEVATOR_EXIT = registerBlock("elevator_exit",
            () -> new ElevatorBlock(BlockBehaviour.Properties.ofFullCopy(Blocks.BEDROCK)));

    public static final DeferredBlock<StairBlock> DARKSTONE_STAIRS = registerBlock("darkstone_stairs",
            () -> new StairBlock(ModBlocks.DARKSTONE.get().defaultBlockState(),BlockBehaviour.Properties.ofFullCopy(Blocks.BEDROCK)));

    public static final DeferredBlock<SlabBlock> DARKSTONE_SLAB = registerBlock("darkstone_slab",
            () -> new SlabBlock(BlockBehaviour.Properties.ofFullCopy(Blocks.BEDROCK)));

    public static final DeferredBlock<Block> CHISELED_DARKSTONE = registerBlock("chiseled_darkstone",
            () -> new Block(BlockBehaviour.Properties.ofFullCopy(Blocks.BEDROCK)));

    public static final DeferredBlock<StairBlock> CHISELED_DARKSTONE_STAIRS = registerBlock("chiseled_darkstone_stairs",
            () -> new StairBlock(CHISELED_DARKSTONE.get().defaultBlockState(),BlockBehaviour.Properties.ofFullCopy(Blocks.BEDROCK)));

    public static final DeferredBlock<SlabBlock> CHISELED_DARKSTONE_SLAB = registerBlock("chiseled_darkstone_slab",
            () -> new SlabBlock(BlockBehaviour.Properties.ofFullCopy(Blocks.BEDROCK)));

    public static final DeferredBlock<RotatedPillarBlock> TRIMMED_SHEIKAHSTONE = registerBlock("trimmed_sheikahstone",
            () -> new RotatedPillarBlock(BlockBehaviour.Properties.ofFullCopy(Blocks.BEDROCK)));

    public static final DeferredBlock<RotatedPillarBlock> GLOWTRIMMED_SHEIKAHSTONE = registerBlock("glowtrimmed_sheikahstone",
            () -> new RotatedPillarBlock(BlockBehaviour.Properties.ofFullCopy(Blocks.BEDROCK)));

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

    public static final DeferredBlock<Block> DARKSTONE_LAMP = registerBlock("darkstone_lamp",
            () -> new Block(BlockBehaviour.Properties.ofFullCopy(Blocks.BEDROCK).noOcclusion().noCollission().lightLevel(p_50872_ -> 15)));

    public static final DeferredBlock<Block> SHEIKAHSTONE_LAMP = registerBlock("sheikahstone_lamp",
            () -> new Block(BlockBehaviour.Properties.ofFullCopy(Blocks.BEDROCK).noOcclusion().noCollission().lightLevel(p_50872_ -> 15)));


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
