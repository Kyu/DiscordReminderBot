package com.preciouso.discordreminder.Util;

import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;

import java.util.HashMap;
import java.util.function.Function;

public class MessageInteractionCallbackStore {
    private final static HashMap<String, ModalWithAction> modalMap = new HashMap<>();
    private final static HashMap<String, StringSelectDropdownWithAction> stringSelectDropdownMap = new HashMap<>();
    private final static HashMap<String, Function<ButtonInteractionEvent, Void>> buttonMap = new HashMap<>();

    public static void registerModal(String id, ModalWithAction modal) {
        modalMap.put(id, modal);
    }

    public static ModalWithAction getModal(String id) {
        return modalMap.get(id);
    }

    public static void registerStringSelectDropDown(String id, StringSelectDropdownWithAction stringSelectDropDown) {
        stringSelectDropdownMap.put(id, stringSelectDropDown);
    }

    public static StringSelectDropdownWithAction getStringSelectDropdown(String id) {
        return stringSelectDropdownMap.get(id);
    }

    public static void registerButton(String id, Function<ButtonInteractionEvent, Void> buttonAction) {
        buttonMap.put(id, buttonAction);
    }

    public static Function<ButtonInteractionEvent, Void> getButtonAction(String id) {
        return buttonMap.get(id);
    }
}
