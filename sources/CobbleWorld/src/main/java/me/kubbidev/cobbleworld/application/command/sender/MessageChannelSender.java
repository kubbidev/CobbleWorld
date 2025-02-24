package me.kubbidev.cobbleworld.application.command.sender;

import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.channel.middleman.MessageChannel;

import java.util.concurrent.CompletableFuture;

public class MessageChannelSender implements CommandSender {
    private final MessageChannel channel;

    public static CommandSender wrap(MessageChannel channel) {
        return new MessageChannelSender(channel);
    }

    protected MessageChannelSender(MessageChannel channel) {
        this.channel = channel;
    }

    @Override
    public CompletableFuture<Message> sendMessage(String message) {
        return this.channel.sendMessage(message).submit();
    }

    @Override
    public CompletableFuture<Message> sendMessage(MessageEmbed embed) {
        return this.channel.sendMessageEmbeds(embed).submit();
    }
}
