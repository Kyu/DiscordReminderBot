package com.preciouso.discordreminder.Util;

import net.dv8tion.jda.api.events.interaction.ModalInteractionEvent;
import net.dv8tion.jda.api.interactions.components.LayoutComponent;
import net.dv8tion.jda.api.utils.data.DataObject;
import net.dv8tion.jda.internal.interactions.modal.ModalImpl;

import java.util.List;
import java.util.function.Function;

public class ModalWithAction extends ModalImpl {
    private final Function<ModalInteractionEvent, Void> actionAfter;
    public ModalWithAction(DataObject object, Function<ModalInteractionEvent, Void> actionAfter) {
        super(object);
        this.actionAfter = actionAfter;
    }

    public ModalWithAction(String id, String title, List<LayoutComponent> components, Function<ModalInteractionEvent, Void> actionAfter) {
        super(id, title, components);
        this.actionAfter = actionAfter;
    }

    public void interact(ModalInteractionEvent event) {
        actionAfter.apply(event);
    }
}
