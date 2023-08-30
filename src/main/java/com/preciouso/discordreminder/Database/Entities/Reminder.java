package com.preciouso.discordreminder.Database.Entities;


import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

import java.util.Date;
import java.util.UUID;


@DatabaseTable(tableName = "reminders")
public class Reminder {
    public static final String ORIGINAL_MESSAGE_ID_FIELD_NAME = "originalMessageId";
    public static final String ID_FIELD_NAME = "id";
    @DatabaseField(id = true, columnName = ID_FIELD_NAME)
    private UUID id;

    @DatabaseField
    private String subject;

    @DatabaseField
    private String description;

    @DatabaseField
    private Date reminderTime;

    @DatabaseField(columnName = ORIGINAL_MESSAGE_ID_FIELD_NAME)
    private String originalMessageId;

    @DatabaseField
    private String creatorId;

    public Reminder() {}

    public Reminder(String subject, String description, Date reminderTime, String originalMessageId, String creatorId) {
        this.id = UUID.randomUUID();
        this.subject = subject;
        this.description = description;
        this.reminderTime = reminderTime;
        this.originalMessageId = originalMessageId;
        this.creatorId = creatorId;
    }

    public Reminder(UUID uuid, String subject, String description, Date reminderTime, String originalMessageId, String creatorId) {
        this.id = uuid;
        this.subject = subject;
        this.description = description;
        this.reminderTime = reminderTime;
        this.originalMessageId = originalMessageId;
        this.creatorId = creatorId;
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public String getSubject() {
        return subject;
    }

    public void setSubject(String subject) {
        this.subject = subject;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Date getReminderTime() {
        return reminderTime;
    }

    public void setReminderTime(Date reminderTime) {
        this.reminderTime = reminderTime;
    }

    public String getOriginalMessageId() {
        return originalMessageId;
    }

    public void setOriginalMessageId(String originalMessageId) {
        this.originalMessageId = originalMessageId;
    }

    @Override
    public String toString() {
        return "Reminder [id=" + id + ", subject=" + subject + ", desc=" + description + ", reminderTime=" + reminderTime + "]";
    }
}
