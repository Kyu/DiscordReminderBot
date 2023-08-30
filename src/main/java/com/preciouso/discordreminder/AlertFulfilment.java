package com.preciouso.discordreminder;

import com.preciouso.discordreminder.Database.Entities.Alert;

import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class AlertFulfilment implements Runnable {
    private static final Lock concurrentLock = new ReentrantLock();
    private static final HashMap<UUID, Alert> pendingAlerts = new HashMap<>();
    private static final SimpleDateFormat sdf = new SimpleDateFormat("MM/dd/yyyy HH:mm:ss");
    private final int secondsBuffer;

    public AlertFulfilment(int secondsBuffer) {
        this.secondsBuffer = secondsBuffer;
    }

    public static void addPendingAlerts(ArrayList<Alert> listOfAlerts) {
        try {
            concurrentLock.lock();
            for (Alert alert: listOfAlerts) {
                pendingAlerts.put(alert.getId(), alert);
            }
        } finally {
            concurrentLock.unlock();
            /* System.out.println(pendingAlerts.size());
            for (Map.Entry<UUID, Alert> p: pendingAlerts.entrySet()) {
                System.out.println(p);
            }
             */
        }
    }

    @Override
    public void run() {
        // System.out.println("Alert Fulfill Start");
        try {
            concurrentLock.lock();

            pendingAlerts.entrySet().removeIf(pa -> pa.getValue().isFulfilled());

            Date fiveSecondsFromNow = new Date(System.currentTimeMillis() + TimeUnit.SECONDS.toMillis(secondsBuffer));
            pendingAlerts.entrySet().stream().filter(x -> fiveSecondsFromNow.after(x.getValue().getTime()))
                    .forEach(sPa -> fulfill(sPa.getValue()));
        } finally {
            concurrentLock.unlock();
        }

        // System.out.println("Alert Fulfill End");
    }

    private void fulfill(Alert alert) {
        String subject = alert.getReminder().getSubject();
        String description = alert.getReminder().getDescription();
        Date date = alert.getReminder().getReminderTime();
        String message = "You asked to be reminded about **" + subject + "** which takes place at <t:" + date.getTime() / 1000L + ":f>";
        if (! description.isEmpty()) {
            message += "\n___Description___\n" + description;
        }

        final String msg = message;

        alert.getSubscribedUsers().forEach(x -> JDAInstance.getJDA().openPrivateChannelById(x).onSuccess(privateChannel ->
                privateChannel.sendMessage(msg)
                        .queue()).queue());

        alert.setFulfilled();
        try {
            JDAInstance.getDatabase().getAlertDao().createOrUpdate(alert);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }
}
