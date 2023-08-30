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
import java.util.*;


@DatabaseTable(tableName = "alerts")
public class Alert {
    private final static String REMINDER_FIELD_NAME = "reminder";
    public final static String TIME_FIELD_NAME = "time";
    public final static String FULFILLED_FIELD_NAME = "fulfilled";

    @DatabaseField(id = true)
    private UUID id;
    @DatabaseField(columnName = TIME_FIELD_NAME, uniqueCombo = true)
    private Date time;

    @DatabaseField(foreign = true, columnName = REMINDER_FIELD_NAME, uniqueCombo = true)
    private Reminder reminder;

    @DatabaseField(columnName = FULFILLED_FIELD_NAME)
    private boolean fulfilled;

    @DatabaseField(dataType = DataType.SERIALIZABLE)
    private HashSet<String> subscribedUsers;

    public Alert() {}

    public Alert(Date time, Reminder reminder, HashSet<String> subscribedUsers) {
        this(time, reminder, false, subscribedUsers);
    }

    public Alert(Date time, Reminder reminder, boolean fulfilled, HashSet<String> subscribedUsers) {
        this.id = UUID.randomUUID();
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
                long alertTime = foundReminder.getReminderTime().getTime() + alertOffset; // for the .before check
                if (alertOffset == 0L) {
                    alertTime -= 1L; // can also check on LX if alertDate == alertTime
                }
                Date alertDate = new Date(alertTime);

                QueryBuilder<Alert, String> alertQb = database.getAlertDao().queryBuilder();

                if (alertDate.after(now) && alertDate.before(foundReminder.getReminderTime())) { // LX
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

    public UUID getId() {
        return id;
    }

    public Date getTime() {
        return time;
    }

    public void setTime(Date time) {
        this.time = time;
    }

    public Reminder getReminder() {
        return reminder;
    }

    public void setReminder(Reminder reminder) {
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
        return "Alert [Id=" + id + ", time=" + time +  ", subscribedUsers=" + subscribedUsers + ", reminder=" + reminder + "]";
    }
}
