package me.kubbidev.cobbleworld.application.command;

import me.kubbidev.cobbleworld.application.command.sender.CommandSender;
import net.dv8tion.jda.api.entities.*;
import net.dv8tion.jda.api.entities.channel.middleman.MessageChannel;
import net.dv8tion.jda.api.interactions.InteractionHook;
import net.dv8tion.jda.api.interactions.commands.CommandInteraction;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import net.dv8tion.jda.api.utils.messages.MessageCreateBuilder;
import org.jetbrains.annotations.Nullable;

import java.util.concurrent.CompletableFuture;
import java.util.function.Function;

/**
 * Event class used to parse and provide Information about a command execution.
 */
public class InteractionContext implements CommandSender {
    protected final CommandInteraction interaction;
    private boolean deferred = false;

    @Nullable
    protected InteractionHook cachedHook = null;

    public static InteractionContext wrap(CommandInteraction interaction) {
        return new InteractionContext(interaction);
    }

    /**
     * Constructs an {@link InteractionContext} that provides context for a command interaction.
     *
     * @param interaction the {@link CommandInteraction} instance associated with this context
     */
    protected InteractionContext(CommandInteraction interaction) {
        this.interaction = interaction;
    }

    public void setDeferred(boolean deferred) {
        this.deferred = deferred;
    }

    private InteractionHook assertHookIsCached() {
        if (this.cachedHook == null) {
            this.cachedHook = this.interaction.deferReply(this.deferred).complete();
        }
        return this.cachedHook;
    }

    public InteractionHook getInteraction() {
        return assertHookIsCached();
    }

    @Override
    public CompletableFuture<Message> sendMessage(String message) {
        return getInteraction().sendMessage(message).submit();
    }

    @Override
    public CompletableFuture<Message> sendMessage(MessageEmbed embed) {
        return getInteraction().sendMessage(new MessageCreateBuilder()
                .setEmbeds(embed).build()).submit();
    }

    public User getUser() {
        return this.interaction.getUser();
    }

    public @Nullable Guild getGuild() {
        return this.interaction.getGuild();
    }

    public @Nullable Member getMember() {
        return this.interaction.getMember();
    }

    public MessageChannel getChannel() {
        return this.interaction.getMessageChannel();
    }

    public @Nullable <T> T get(String arg, Function<OptionMapping, T> mapping) {
        return this.interaction.getOption(arg, mapping);
    }
}