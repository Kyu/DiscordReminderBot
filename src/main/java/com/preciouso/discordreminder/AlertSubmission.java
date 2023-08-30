package com.preciouso.discordreminder;

import net.dv8tion.jda.api.events.interaction.ModalInteractionEvent;
import net.dv8tion.jda.api.events.interaction.component.StringSelectInteractionEvent;

import java.util.ArrayList;
import java.util.Map;
import java.util.Objects;

import static java.util.Map.entry;

public class AlertSubmission {
    public static final Map<String, Long> defaultTimeOffsetValues = Map.ofEntries(
            entry("on-time", 0L),
            entry("12h-before", -12*3600L),
            entry("1d-before", -24*3600L),
            entry("7d-before", -24*7*3600L)
    );

    public static final Map<String, Long> validTimeUnits = Map.ofEntries(
            entry("minutes", -60L),
            entry("minute", -60L),
            entry("mins", -60L),
            entry("m", -60L),

            entry("hours", -3600L),
            entry("hour", -3600L),
            entry("hr", -3600L),
            entry("h", -3600L),

            entry("days", -24*3600L),
            entry("day", -24*3600L),
            entry("d", -24*3600L)
    );

    private boolean isValid = false;
    private final String alerterId;
    private final ArrayList<Long> timeOffsets = new ArrayList<>();
    private final String messageId;

    public AlertSubmission(StringSelectInteractionEvent event) {
        alerterId = Objects.requireNonNull(event.getMember()).getId();
        messageId = Objects.requireNonNull(event.getMessage()).getId();

        for (String selection: event.getValues()) {
            long offset = defaultTimeOffsetValues.get(selection) * 1000L;
            timeOffsets.add(offset);

            isValid = true;
        }
    }

    public AlertSubmission(ModalInteractionEvent event) {
        isValid = false;
        alerterId = Objects.requireNonNull(event.getMember()).getId();
        messageId = Objects.requireNonNull(event.getMessage()).getId();

        if (event.getValues().size() >= 2) {
            Long timeOffset = validTimeUnits.get(event.getValues().get(0).getAsString());
            if (timeOffset != null) {
                timeOffset *= 1000L;
            } else {
                timeOffset = 1000L; // this makes sure its cancelled
            }

            int amountOfTime = -1;
            try {
                amountOfTime = Integer.parseInt(event.getValues().get(1).getAsString());
            } catch (NumberFormatException ignored) {
            }

            if (amountOfTime >= 0 && timeOffset <= 0) {
                long finalTimeOffset = amountOfTime * timeOffset;
                timeOffsets.add(finalTimeOffset);
                isValid = true;
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
