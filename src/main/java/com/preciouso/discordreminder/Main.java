package com.preciouso.discordreminder;

import com.preciouso.discordreminder.Database.DatabaseInit;

import java.sql.SQLException;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;

import static java.util.concurrent.TimeUnit.MINUTES;
import static java.util.concurrent.TimeUnit.SECONDS;

public class Main {
    private static final ScheduledExecutorService scheduler =
            Executors.newScheduledThreadPool(1);
    public static void main(String[] args) throws SQLException, ClassNotFoundException {
        String discordToken = System.getenv("JDA_DISCORD_TOKEN");
        if (discordToken == null || discordToken.isEmpty()) {
            throw new RuntimeException("No JDA_DISCORD_TOKEN exists in Environment Arguments! Exiting");
        }

        // su postgres then:
        // https://alvinalexander.com/blog/post/postgresql/log-in-postgresql-database/
        // https://stackoverflow.com/a/769706/3875151
        String postgresPassword = System.getenv("JDA_POSTGRES_PASSWORD");
        if (postgresPassword == null || postgresPassword.isEmpty()) {
            throw new RuntimeException("No JDA_POSTGRES_PASSWORD exists in Environment Arguments! Exiting");
        }

        String postgresHostname = System.getenv("JDA_POSTGRES_HOST");
        if (postgresHostname == null || postgresHostname.isEmpty()) {
            postgresHostname = "localhost";
        }

        String postgresPort = System.getenv("JDA_POSTGRES_PORT");
        if (postgresPort == null || postgresPort.isEmpty()) {
            postgresPort = "5432";
        }

        String postgresUsername = System.getenv("JDA_POSTGRES_USERNAME");
        if (postgresUsername == null || postgresUsername.isEmpty()) {
            postgresUsername = "postgres";
        }

        String alertzyKey = System.getenv("JDA_ALERTZY_KEY");
        if ( !(alertzyKey == null || alertzyKey.isEmpty())) {
            JDAInstance.setAlertzyUrl(alertzyKey);
        } else {
            System.out.println("No alertzy key defined");
        }

        DatabaseInit databaseInit = new DatabaseInit(postgresHostname, postgresPort, postgresUsername, postgresPassword);

        JDAInstance.addDatabaseInit(databaseInit);
        JDAInstance.buildJda(discordToken);

        final AlertChecker alertChecker = new AlertChecker(10);
        final AlertFulfilment alertFulfilment = new AlertFulfilment(5);

        // https://math.stackexchange.com/a/2210615/689853


        final ScheduledFuture<?> alertCheckerHandle =
                scheduler.scheduleAtFixedRate(alertChecker, 0, 1, MINUTES);

        final ScheduledFuture<?> alertFulfilmentHandle =
                scheduler.scheduleAtFixedRate(alertFulfilment, 0, 10, SECONDS);

        // scheduler.schedule(() -> { beeperHandle.cancel(true); }, 60 * 60, SECONDS);

        UITools.loadUI();
    }
}
