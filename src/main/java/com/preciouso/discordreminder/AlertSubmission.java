package com.preciouso.discordreminder;

import com.preciouso.discordreminder.Database.Entities.Alert;
import net.dv8tion.jda.api.events.interaction.ModalInteractionEvent;
import net.dv8tion.jda.api.events.interaction.component.StringSelectInteractionEvent;
import net.dv8tion.jda.api.interactions.components.selections.StringSelectInteraction;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class AlertSubmission {
    public static final Map<String, Long> defaultTimeOffsetValues = Map.of(
            "on-time", 0L,
            "12h-before", -12*3600L,
            "1d-before", -24*3600L,
            "7d-before", -24*7*3600L
    );

    public static final Map<String, Long> validTimeUnits = Map.of(
            "Minutes", -60L,
            "Hours", -3600L,
            "Days", -24*3600L
            );

    private boolean isValid = false;
    private String alerterId;
    private final ArrayList<Long> timeOffsets = new ArrayList<>();
    private String messageId;
    public AlertSubmission(StringSelectInteractionEvent event) {
        alerterId = Objects.requireNonNull(event.getMember()).getId();
        messageId = Objects.requireNonNull(event.getMessage()).getId();

        for (String selection: event.getValues()) {
            Long offset = defaultTimeOffsetValues.get(selection);
            if (offset != null) {
                timeOffsets.add(offset);
            }

            isValid = true;
        }
    }

    public AlertSubmission(ModalInteractionEvent event) {
        isValid = false;
        alerterId = Objects.requireNonNull(event.getMember()).getId();
        messageId = Objects.requireNonNull(event.getMessage()).getId();

        if (event.getValues().size() >= 2) {
            Long timeOffset = validTimeUnits.get(event.getValues().get(0).getAsString());
            int amountOfTime = -1;
            if (timeOffset != null) {
                try {
                    amountOfTime = Integer.parseInt(event.getValues().get(1).getAsString());
                } catch (NumberFormatException ignored) {}

                if (amountOfTime > 0) {
                    long finalTimeOffset = amountOfTime * timeOffset;
                    timeOffsets.add(finalTimeOffset);
                    isValid = true;
                }
            }
        }
    }

    public boolean isValid() {
        return isValid;
    }

    public String getAlerterId() {
        return alerterId;
    }

    public ArrayList<Long> getTimeOffsets() {
        return timeOffsets;
    }

    public String getMessageId() {
        return messageId;
    }
}
