package me.kubbidev.cobbleworld.application.commands;

import me.kubbidev.cobbleworld.CobbleWorldMod;
import me.kubbidev.cobbleworld.application.command.Interaction;
import me.kubbidev.cobbleworld.application.command.InteractionContext;
import me.kubbidev.cobbleworld.application.command.sender.CommandSender;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.interactions.commands.build.SlashCommandData;

public class PingCommand implements Interaction {

    @Override
    public SlashCommandData getMetadata() {
        return Commands.slash("ping", "Pong!");
    }

    @Override
    public void execute(CobbleWorldMod mod, CommandSender channel, InteractionContext context) {
        context.setDeferred(true);
        context.sendMessage("Pong!");
    }
}