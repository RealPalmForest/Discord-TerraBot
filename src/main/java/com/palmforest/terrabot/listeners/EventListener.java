package com.palmforest.terrabot.listeners;

import net.dv8tion.jda.api.entities.emoji.Emoji;
import net.dv8tion.jda.api.events.message.react.MessageReactionAddEvent;
import net.dv8tion.jda.api.events.message.react.MessageReactionRemoveEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.jetbrains.annotations.NotNull;

import java.util.Locale;

public class EventListener extends ListenerAdapter {

    @Override
    public void onMessageReactionAdd(@NotNull MessageReactionAddEvent event) {
        String reaction = event.getReaction().getEmoji().getFormatted();
        String messageId = event.getMessageId();

        if(reaction.equals("\uD83C\uDF34")) {
            event.getChannel().addReactionById(messageId, Emoji.fromFormatted(reaction)).queue();
        }
    }

    @Override
    public void onMessageReactionRemove(@NotNull MessageReactionRemoveEvent event) {
        String reaction = event.getReaction().getEmoji().getFormatted();
        String messageId = event.getMessageId();

        if(reaction.equals("\uD83C\uDF34")) {
            event.getChannel().removeReactionById(messageId, Emoji.fromFormatted(reaction)).queue();
        }
    }
}
