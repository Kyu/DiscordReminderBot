package com.preciouso.discordreminder.Database.Entities;


import com.j256.ormlite.field.DataType;
import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.stmt.QueryBuilder;
import com.j256.ormlite.stmt.Where;
import com.j256.ormlite.table.DatabaseTable;
import com.preciouso.discordreminder.AlertSubmission;
import com.preciouso.discordreminder.Database.DatabaseInit;

import java.sql.SQLException;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;


@DatabaseTable(tableName = "alerts")
public class Alert {
    private final static String REMINDER_FIELD_NAME = "reminder";
    private final static String TIME_FIELD_NAME = "time";
    @DatabaseField(id = true, unique = true, uniqueIndex = true, columnName = TIME_FIELD_NAME)
    private Date time;

    @DatabaseField(foreign = true, columnName = REMINDER_FIELD_NAME)
    private Reminder reminder;

    @DatabaseField
    private boolean fulfilled;

    @DatabaseField(dataType = DataType.SERIALIZABLE)
    private HashSet<String> subscribedUsers;

    public Alert() {}

    public Alert(Date time, Reminder reminder, HashSet<String> subscribedUsers) {
        this.time = time;
        this.reminder = reminder;
        this.subscribedUsers = subscribedUsers;
        this.fulfilled = false;
    }

    public Alert(Date time, Reminder reminder, boolean fulfilled, HashSet<String> subscribedUsers) {
        this.time = time;
        this.reminder = reminder;
        this.fulfilled = fulfilled;
        this.subscribedUsers = subscribedUsers;
    }

    public static ArrayList<Alert> fromAlertSubmission(AlertSubmission alertSubmission, DatabaseInit database) throws SQLException {
        ArrayList<Alert> alertList = new ArrayList<>();

        QueryBuilder<Reminder, String> reminderQb = database.getReminderDao().queryBuilder();
        Where<Reminder, String> whereReminderQuery = reminderQb.where();

        whereReminderQuery.eq(Reminder.ORIGINAL_MESSAGE_ID_FIELD_NAME, alertSubmission.getMessageId()); // TODO August 29 -- reminder.FindByMessageID and private static final var
        Reminder foundReminder = database.getReminderDao().queryForFirst(whereReminderQuery.prepare());

        if (foundReminder != null) {
            Date now = Date.from(Instant.now());


            for (long alertOffset: alertSubmission.getTimeOffsets()) {
                long alertTime = foundReminder.getReminderTime().getTime() + alertOffset;
                Date alertDate = new Date(alertTime);

                QueryBuilder<Alert, String> alertQb = database.getAlertDao().queryBuilder();

                if (alertDate.after(now) && alertDate.before(foundReminder.getReminderTime())) {
                    Where<Alert, String> whereAlertQb = alertQb.where();

                    whereAlertQb.eq(Alert.REMINDER_FIELD_NAME, foundReminder);
                    whereAlertQb.and();
                    whereAlertQb.eq(Alert.TIME_FIELD_NAME, alertDate);

                    Alert foundAlert = database.getAlertDao().queryForFirst(whereAlertQb.prepare());

                    if (foundAlert != null) {
                        if (! foundAlert.isFulfilled()) {
                            foundAlert.subscribedUsers.add(alertSubmission.getAlerterId());
                            alertList.add(foundAlert);
                        }
                    } else {
                        Alert newAlert = new Alert(alertDate, foundReminder, new HashSet<>(Collections.singletonList(alertSubmission.getAlerterId())));
                        alertList.add(newAlert);
                    }
                }
            }
        } else {
            System.out.println("couldn't find reminder with message id = " + alertSubmission.getMessageId());
        }

        return alertList;
    }

    public Date getTime() {
        return time;
    }

    public void setTime(Date time) {
        this.time = time;
    }

    public Reminder getReminderId() {
        return reminder;
    }

    public void setReminderId(Reminder reminder) {
        this.reminder = reminder;
    }

    public boolean isFulfilled() {
        return fulfilled;
    }

    public void setFulfilled() {
        this.fulfilled = true;
    }

    public HashSet<String> getSubscribedUsers() {
        return subscribedUsers;
    }

    public void setSubscribedUsers(HashSet<String> subscribedUsers) {
        this.subscribedUsers = subscribedUsers;
    }

    @Override
    public String toString() {
        return "Alert [Time=" + time + ", reminder=" + reminder + ", subscribedUsers=" + subscribedUsers + "]";
    }
}
