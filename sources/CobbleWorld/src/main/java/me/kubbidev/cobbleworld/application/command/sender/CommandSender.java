package me.kubbidev.cobbleworld.application.command.sender;

import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageEmbed;

import java.util.concurrent.CompletableFuture;

/**
 * Represents a functional interface used for sending messages.
 * <p>
 * This interface can be implemented for various messaging systems to provide a consistent way of
 * sending messages, where the {@link #sendMessage(String)} method serves as the primary entry
 * point for dispatching messages to a recipient.
 */
public interface CommandSender {

    /**
     * Sends a message represented by a {@link String} to the intended recipient.
     *
     * @param message the message to be sent
     * @return a {@code CompletableFuture} that completes with the resulting {@code Message} object
     *         once the message has been successfully sent
     */
    CompletableFuture<Message> sendMessage(String message);

    /**
     * Sends a message in the form of a {@link MessageEmbed} to the intended recipient.
     *
     * @param embed the embed containing the structured message details such as title, content,
     *              fields, and other visual elements
     * @return a {@code CompletableFuture} that completes with the resulting {@code Message} object
     *         once the message has been successfully sent
     */
    CompletableFuture<Message> sendMessage(MessageEmbed embed);
}