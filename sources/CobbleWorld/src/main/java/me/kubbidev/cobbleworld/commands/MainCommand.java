package me.kubbidev.cobbleworld.commands;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import me.kubbidev.cobbleworld.CobbleWorldMod;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.Text;

@SuppressWarnings("SpellCheckingInspection")
public class MainCommand {
    private static final int COMMAND_ADMIN = 2;

    public static void register(CobbleWorldMod mod, CommandDispatcher<ServerCommandSource> dispatcher) {
        dispatcher.register(CommandManager.literal("cobbleworld")
                .then(CommandManager.literal("forceupdate")
                        .requires(source -> source.hasPermissionLevel(COMMAND_ADMIN))
                        .executes(context -> {
                            mod.getCaughtPokemonManager().triggerUpdate();
                            context.getSource().sendFeedback(() -> Text.literal("Successfully force update leaderboard(s)!"), true);
                            return Command.SINGLE_SUCCESS;
                        }))
        );
    }
}
