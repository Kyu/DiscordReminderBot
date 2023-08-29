package com.preciouso.discordreminder;

import com.preciouso.discordreminder.Database.Entities.Reminder;
import com.preciouso.discordreminder.Util.DateTimeParser;
import net.dv8tion.jda.api.events.interaction.ModalInteractionEvent;
import net.dv8tion.jda.api.interactions.modals.ModalMapping;

import java.time.LocalDateTime;
import java.util.Date;
import java.util.Objects;
import java.util.TimeZone;

public class ReminderSubmission {
    private final String subject;
    private final String description;
    private final LocalDateTime localDateTime;
    private Date actualDate;
    private TimeZone timezone;
    private final String creatorId;

    public ReminderSubmission(ModalInteractionEvent event, DateTimeParser dateTimeParser) {
        ModalMapping subject = event.getValue("subject");
        String subjectString = subject == null ? "" : subject.getAsString();

        ModalMapping description = event.getValue("description");
        String descriptionString = description == null ? "" : description.getAsString();

        this.subject = subjectString;
        this.description = descriptionString;
        this.localDateTime = dateTimeParser.getDateTime();
        this.creatorId = Objects.requireNonNull(event.getMember()).getId();
    }

    public TimeZone getTimezone() {
        return timezone;
    }

    public void setTimeZone(String timezone) {
        timezone = timezone.replace("UTC", "GMT").strip();
        this.timezone = TimeZone.getTimeZone(timezone);

        actualDate = Date.from(localDateTime.atZone(this.timezone.toZoneId()).toInstant());
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

        return "<t:" + actualDate.getTime() / 1000L + ">";
    }

    public Reminder toReminderEntity(String originalMessageId) {
        return new Reminder(subject, description, actualDate, originalMessageId, creatorId);
    }
}
