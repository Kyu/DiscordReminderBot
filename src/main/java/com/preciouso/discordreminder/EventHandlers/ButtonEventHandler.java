package com.preciouso.discordreminder.EventHandlers;

import com.preciouso.discordreminder.Util.MessageInteractionCallbackStore;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.jetbrains.annotations.NotNull;

import java.util.function.Function;

public class ButtonEventHandler extends ListenerAdapter {
    @Override
    public void onButtonInteraction(@NotNull ButtonInteractionEvent event) {
        Function<ButtonInteractionEvent, Void> actionFunction = MessageInteractionCallbackStore.getButtonAction(event.getComponentId());

        if (actionFunction != null) {
            actionFunction.apply(event);
        }
    }
}
