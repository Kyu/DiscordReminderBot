package com.preciouso.discordreminder;

import com.preciouso.discordreminder.Util.DateTimeParser;
import net.dv8tion.jda.api.events.interaction.ModalInteractionEvent;
import net.dv8tion.jda.api.interactions.modals.ModalMapping;

import java.time.LocalDateTime;
import java.util.Date;
import java.util.TimeZone;

public class ReminderSubmission {
    private final String subject;
    private final String description;
    private final LocalDateTime localDateTime;
    private TimeZone timezone;

    public ReminderSubmission(ModalInteractionEvent event, DateTimeParser dateTimeParser) {
        ModalMapping subject = event.getValue("subject");
        String subjectString = subject == null ? "" : subject.getAsString();

        ModalMapping description = event.getValue("description");
        String descriptionString = description == null ? "" : description.getAsString();

        this.subject = subjectString;
        this.description = descriptionString;
        this.localDateTime = dateTimeParser.getDateTime();
    }

    public void setTimeZone(String timezone) {
        timezone = timezone.replace("UTC", "GMT").strip();
        this.timezone = TimeZone.getTimeZone(timezone);
    }

    public String getSubject() {
        return subject;
    }

    public String getDescription() {
        return description;
    }

    public LocalDateTime getLocalDateTime() {
        return localDateTime;
    }

    public String getFormattedTime() {
        if (timezone == null) {
            return "";
        }

        Date date = Date.from(localDateTime.atZone(timezone.toZoneId()).toInstant());
        return "<t:" + date.getTime() / 1000L + ">";
    }
}
