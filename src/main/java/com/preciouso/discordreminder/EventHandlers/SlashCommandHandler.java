package com.preciouso.discordreminder.EventHandlers;

import com.preciouso.discordreminder.Util.ModalWithAction;
import com.preciouso.discordreminder.Util.MessageInteractionCallbackStore;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.jetbrains.annotations.NotNull;

public class SlashCommandHandler extends ListenerAdapter {

    @Override
    public void onSlashCommandInteraction(@NotNull SlashCommandInteractionEvent event) {
        switch (event.getName()) {
            case "ping":
                long time1 = event.getTimeCreated().getNano();
                long time = System.currentTimeMillis();
                event.reply("Pong!").setEphemeral(true) // reply or acknowledge
                        .flatMap(v ->
                                event.getHook().editOriginalFormat("Pong: %d ms", time - time1) // then edit original
                        ).queue(); // Queue both reply and edit;
            case "reminder":
                ModalWithAction modal = MessageInteractionCallbackStore.getModal("reminderForm");

                if (modal != null) {
                    event.replyModal(modal).queue();
                } else {
                    event.reply("A problem occurred while loading NewReminder form").setEphemeral(true).queue();
                }
                break;
        }
    }
}
