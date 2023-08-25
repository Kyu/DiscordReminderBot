package com.preciouso.discordreminder.EventHandlers;

import com.preciouso.discordreminder.Util.MessageInteractionCallbackStore;
import com.preciouso.discordreminder.Util.StringSelectDropdownWithAction;
import net.dv8tion.jda.api.events.interaction.component.StringSelectInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

public class SelectStringDropdownHandler extends ListenerAdapter {
    @Override
    public void onStringSelectInteraction(StringSelectInteractionEvent event) {
        StringSelectDropdownWithAction modal = MessageInteractionCallbackStore.getStringSelectDropdown(event.getComponentId());

        if (modal != null) {
            modal.interact(event);
        }
    }
}
