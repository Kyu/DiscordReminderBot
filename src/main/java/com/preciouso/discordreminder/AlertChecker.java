package com.preciouso.discordreminder;

import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.stmt.QueryBuilder;
import com.j256.ormlite.stmt.Where;
import com.preciouso.discordreminder.Database.DatabaseInit;
import com.preciouso.discordreminder.Database.Entities.Alert;
import com.preciouso.discordreminder.Database.Entities.Reminder;
import net.dv8tion.jda.api.JDA;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

public class AlertChecker implements Runnable {
    private final int minuteLookAhead;
    public AlertChecker(int minuteLookAhead) {
        this.minuteLookAhead = minuteLookAhead;
    }

    @Override
    public void run() {
        // System.out.println("Alert Checker Start");
        Date tenMinsFromNow = new Date(System.currentTimeMillis() + TimeUnit.MINUTES.toMillis(minuteLookAhead));
        DatabaseInit database = JDAInstance.getDatabase();

        ArrayList<Alert> alertsFound = new ArrayList<>();

        try {
            QueryBuilder<Alert, String> alertQb = database.getAlertDao().queryBuilder();
            Where<Alert, String> whereAlert = alertQb.where();

            whereAlert.eq(Alert.FULFILLED_FIELD_NAME, false);
            whereAlert.and();
            whereAlert.le(Alert.TIME_FIELD_NAME, tenMinsFromNow);

            List<Alert> soonAlerts = JDAInstance.getDatabase().getAlertDao().query(whereAlert.prepare());
            for (Alert alert: soonAlerts) {
                UUID id = alert.getReminder().getId();
                QueryBuilder<Reminder, String> queryBuilder = database.getReminderDao().queryBuilder();

                Where<Reminder, String> whereReminder = queryBuilder.where();
                whereReminder.eq(Reminder.ID_FIELD_NAME, id);

                Reminder r = database.getReminderDao().queryForFirst(whereReminder.prepare());
                if (r != null) {
                    alert.setReminder(r);
                }

                alertsFound.add(alert);
            }
        } catch (SQLException e) {
            e.printStackTrace(System.err);
            throw new RuntimeException(e);
        } catch (Exception e) {
            e.printStackTrace(System.err);
        } finally {
            AlertFulfilment.addPendingAlerts(alertsFound);
        }

        // System.out.println("Alert Checker End");
    }
}
