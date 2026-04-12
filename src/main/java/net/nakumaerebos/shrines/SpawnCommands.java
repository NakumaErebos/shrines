package net.nakumaerebos.shrines;

import com.mojang.brigadier.CommandDispatcher;
import net.minecraft.commands.CommandBuildContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.item.ItemArgument;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;
import net.nakumaerebos.shrines.entity.ModEntities;
import net.nakumaerebos.shrines.entity.ShrineItemEntity;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.RegisterCommandsEvent;

@EventBusSubscriber(modid = Shrines.MOD_ID)
public class SpawnCommands {

    @SubscribeEvent
    public static void onRegisterCommands(RegisterCommandsEvent event) {
        // Wir übergeben den Dispatcher UND den BuildContext vom Event
        register(event.getDispatcher(), event.getBuildContext());
    }

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher, CommandBuildContext context) {
        dispatcher.register(Commands.literal("spawnshrineitem")
                .requires(source -> source.hasPermission(2))
                .then(Commands.argument("item", ItemArgument.item(context)) // Hier wird der context nun korrekt genutzt
                        .executes(cmdContext -> {
                            CommandSourceStack source = cmdContext.getSource();
                            Vec3 pos = source.getPosition();

                            // Item aus dem Argument parsen
                            ItemStack stack = ItemArgument.getItem(cmdContext, "item").createItemStack(1, false);

                            // Entity erstellen
                            ShrineItemEntity shrineItem = new ShrineItemEntity(ModEntities.SHRINE_ITEM.get(), source.getLevel());

                            // Position setzen (Y + 1.0 für einen Block höher)
                            shrineItem.setPos(pos.x, pos.y + 1.0, pos.z);

                            // WICHTIG: Schwerkraft ausschalten, damit es in der Luft bleibt
                            shrineItem.setNoGravity(true);

                            // Item zuweisen
                            shrineItem.setItem(stack);

                            // In der Welt spawnen
                            source.getLevel().addFreshEntity(shrineItem);

                            source.sendSuccess(() -> Component.literal("Shrine Entity mit " + stack.getHoverName().getString() + " wurde platziert!"), true);
                            return 1;
                        }))
        );
    }
}