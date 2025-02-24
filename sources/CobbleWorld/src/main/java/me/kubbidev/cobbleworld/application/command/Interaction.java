package me.kubbidev.cobbleworld.application.command;

import me.kubbidev.cobbleworld.CobbleWorldMod;
import me.kubbidev.cobbleworld.application.command.sender.CommandSender;
import net.dv8tion.jda.api.interactions.commands.build.SlashCommandData;

/**
 * The Interaction interface represents a contract that defines the structure
 * of an executable interaction.
 */
public interface Interaction {

    /**
     * Retrieves the {@link SlashCommandData} associated with the interaction's slash command.
     * <p>
     * This method is used to provide metadata about the command, such as its name, description,
     * and options, for registration with Discord's slash command system.
     *
     * @return an instance of {@link SlashCommandData} representing the metadata for the command
     */
    SlashCommandData getMetadata();

    /**
     * Executes the {@link Interaction} asynchronously with the provided mod, channel, and context.
     * <p>
     * This method should contain the main logic for processing the interaction.
     *
     * @param mod the instance of the {@link CobbleWorldMod} executing this interaction
     * @param channel the {@link CommandSender} representing the channel where the interaction was initiated
     * @param context the {@link InteractionContext} providing details about the interaction
     */
    void execute(CobbleWorldMod mod, CommandSender channel, InteractionContext context);
}