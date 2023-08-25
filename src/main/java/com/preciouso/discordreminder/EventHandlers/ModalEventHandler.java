package com.preciouso.discordreminder.EventHandlers;

import com.preciouso.discordreminder.Util.ModalWithAction;
import com.preciouso.discordreminder.Util.MessageInteractionCallbackStore;
import net.dv8tion.jda.api.events.interaction.ModalInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.jetbrains.annotations.NotNull;

public class ModalEventHandler extends ListenerAdapter {
    @Override
    public void onModalInteraction(@NotNull ModalInteractionEvent event) {
        ModalWithAction modal = MessageInteractionCallbackStore.getModal(event.getModalId());

        if (modal != null) {
            modal.interact(event);
        }
    }
}