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

    public static void register(IEventBus eventBus) {
        BLOCK_ENTITIES.register(eventBus);
    }
}
