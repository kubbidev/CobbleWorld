package me.kubbidev.cobbleworld.commands;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import me.kubbidev.cobbleworld.CobbleWorldMod;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.Text;

public class MainCommand {

    public static void register(CobbleWorldMod mod, CommandDispatcher<ServerCommandSource> dispatcher) {
        dispatcher.register(CommandManager.literal("cobbleworld")
                .then(CommandManager.literal("reload")
                        .requires(source -> source.hasPermissionLevel(3))
                        .executes(context -> {
                            context.getSource().sendFeedback(() -> Text.literal("Reloaded!"), true);
                            mod.getConfiguration().reload();
                            return Command.SINGLE_SUCCESS;
                        }))
        );
    }
}
