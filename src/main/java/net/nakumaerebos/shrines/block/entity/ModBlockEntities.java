package net.nakumaerebos.shrines.block.entity;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.nakumaerebos.shrines.Shrines;
import net.nakumaerebos.shrines.block.ModBlocks;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.function.Supplier;

public class ModBlockEntities {
    public static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITIES =
            DeferredRegister.create(BuiltInRegistries.BLOCK_ENTITY_TYPE, Shrines.MOD_ID);

    public static final Supplier<BlockEntityType<ShrineDoorBlockEntity>> SHRINE_DOOR_BE =
            BLOCK_ENTITIES.register("shrine_door_be", () -> BlockEntityType.Builder.of(
                    ShrineDoorBlockEntity::new, ModBlocks.SHRINE_DOOR.get()).build(null));

    public static final Supplier<BlockEntityType<DungeonDoorBlockEntity>> DUNGEON_DOOR_BE =
            BLOCK_ENTITIES.register("dungeon_door_be", () -> BlockEntityType.Builder.of(
                    DungeonDoorBlockEntity::new, ModBlocks.DUNGEON_DOOR.get()).build(null));

    public static final Supplier<BlockEntityType<SheikahLecternBlockEntity>> SHEIKAH_LECTERN_BE =
            BLOCK_ENTITIES.register("sheikah_lectern_be", () -> BlockEntityType.Builder.of(
                    SheikahLecternBlockEntity::new, ModBlocks.SHEIKAH_LECTERN.get()).build(null));

    public static final Supplier<BlockEntityType<SheikahTorchBlockEntity>> SHEIKAH_TORCH_BE =
            BLOCK_ENTITIES.register("sheikah_torch_be", () -> BlockEntityType.Builder.of(
                    SheikahTorchBlockEntity::new, ModBlocks.SHEIKAH_TORCH.get()).build(null));

    public static final Supplier<BlockEntityType<ShrineChestBlockEntity>> SHRINE_CHEST_BE =
            BLOCK_ENTITIES.register("shrine_chest_be", () -> BlockEntityType.Builder.of(
                    ShrineChestBlockEntity::new, ModBlocks.SHRINE_CHEST.get()).build(null));

    public static final Supplier<BlockEntityType<HolyShimmerEntity>> HOLY_SHIMMER =
            BLOCK_ENTITIES.register("holy_shimmer",
                    () -> BlockEntityType.Builder.of(HolyShimmerEntity::new, ModBlocks.HOLY_SHIMMER.get()).build(null));

    public static void register(IEventBus eventBus) {
        BLOCK_ENTITIES.register(eventBus);
    }
}
