package com.ethanrobins.chatbridge_v2.utils;

import com.ethanrobins.chatbridge_v2.ChatBridge;
import com.ethanrobins.chatbridge_v2.drivers.TranslateEmbedBuilder;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.interactions.DiscordLocale;
import net.dv8tion.jda.api.interactions.commands.Command;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

public class Messages {
    /**
     * <b>Unregistered Message Cache.</b>
     * <br>This data represents messages that have been received in the bot's DM but have not
     * yet been translated due to the user being unregistered in the database. To automatically
     * translate messages in the bot's DM, the user must have a stored locale value in the
     * ChatBridge database.
     */
    private static final List<MessageReceivedEvent> URM = new ArrayList<>();

    /**
     * Gets all current MessageReceivedEvent cache
     * @return {@link List}{@code <}{@link MessageReceivedEvent}{@code >}
     */
    public static List<MessageReceivedEvent> getUnregisteredMessages() {
        return URM;
    }

    /**
     * Gets the MessageReceivedEvent
     * @param event The event to get
     * @return {@link MessageReceivedEvent}
     */
    public static MessageReceivedEvent getUnregisteredMessage(MessageReceivedEvent event) {
        for (MessageReceivedEvent e : URM) {
            if (e.getMessageIdLong() == event.getMessageIdLong()) {
                return e;
            }
        }

        return null;
    }

    /**
     * Gets all {@link MessageReceivedEvent}s by userId
     * @param userId The user the message belongs to
     * @return {@link List}{@code <}{@link MessageReceivedEvent}{@code >} belonging to the user
     */
    public static List<MessageReceivedEvent> getUnregisteredMessages(String userId) {
        List<MessageReceivedEvent> messages = new ArrayList<>();

        for (MessageReceivedEvent e : URM) {
            if (e.getAuthor().getId().equals(userId)) {
                messages.add(e);
            }
        }

        return messages;
    }

    /**
     * Removes the MessageReceivedEvent
     * @param event The event to remove
     */
    public static void removeUnregisteredMessage(MessageReceivedEvent event) {
        URM.removeIf(e -> e.getMessageIdLong() == event.getMessageIdLong());
    }

    /**
     * Removes the MessageReceivedEvent by userId
     * @param userId The user the message belongs to
     */
    public static void removeUnregisteredMessage(String userId) {
        URM.removeIf(e -> e.getAuthor().getId().equals(userId));
    }

    /**
     * Gets and removes the MessageReceivedEvent by userId
     * @param userId The user the message belongs to
     * @return {@link MessageReceivedEvent} belonging to the user
     */
    public static MessageReceivedEvent retrieve(String userId) {
        for (MessageReceivedEvent e : URM) {
            if (e.getAuthor().getId().equals(userId)) {
                URM.remove(e);
                return e;
            }
        }

        return null;
    }

    /**
     * The message to be sent in the event of a user's first interaction using ChatBridge, after registering their user with the database
     * @param locale The locale the message should be sent in
     * @return {@link MessageEmbed}
     * @throws ExecutionException
     * @throws InterruptedException
     */
    public static MessageEmbed firstInteraction(DiscordLocale locale) throws ExecutionException, InterruptedException {
        TranslateEmbedBuilder builder = new TranslateEmbedBuilder();
        builder.setAuthor("(" + locale.getLocale() + ") " + locale.getNativeName(), null, null, false);
        builder.setTitle("Welcome to ChatBridge!", null, true);
        builder.setDescription("Thank you for using ChatBridge!\nYour language has been set to " + locale.getLocale() + ".\n\nWhenever you wish to translate a message, you can use this direct messaging channel.\nAll you have to do is copy and paste the message here and I will translate the message in the language you have set.\n\n**Happy Translating!**", true);
        builder.setFooter("ChatBridge", null, false);
        builder.setColor(Color.decode("#55ccff"));

        CompletableFuture<TranslateEmbedBuilder> embedBuilderFuture =  builder.translateAsync(locale.getLocale());
        return embedBuilderFuture.get().build();
    }

    /**
     * The message to be sent in the event a user DMs the bot before they have used an interaction.
     * @param event The {@link MessageReceivedEvent} they originally sent (for cache)
     * @return {@link MessageEmbed}
     */
    public static MessageEmbed firstPrivateMessageUnregistered(MessageReceivedEvent event) {
        URM.add(event);

        Command privateTranslationCommand = ChatBridge.getCommand("Private Translation");
        Command publicTranslationCommand = ChatBridge.getCommand("Public Translation");
        String privateTranslationCommandAsMention = privateTranslationCommand != null ? privateTranslationCommand.getAsMention() : "`Private Translation` message interaction command";
        String publicTranslationCommandAsMention = publicTranslationCommand != null ? publicTranslationCommand.getAsMention() : "`Public Translation` message interaction command";

        StringBuilder desc = new StringBuilder();
        desc.append("You have not used ChatBridge before. Please use ").append(privateTranslationCommandAsMention).append(" or ").append(publicTranslationCommandAsMention).append(" to register your locale with ChatBridge.").append("\n\n");

        EmbedBuilder builder = new EmbedBuilder();
        builder.setTitle("Unable to translate message.", null);
        builder.setDescription(desc);
        builder.setFooter("ChatBridge", null);
//        builder.addField("Private Translation", privateTranslationCommandAsMention, false);
//        builder.addField("Public Translation", publicTranslationCommandAsMention, false);

        return builder.build();
    }
}
