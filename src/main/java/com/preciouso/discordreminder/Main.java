package com.preciouso.discordreminder;

import com.preciouso.discordreminder.Database.DatabaseInit;

import java.sql.SQLException;

public class Main {
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

        DatabaseInit databaseInit = new DatabaseInit(postgresPassword);

        JDAInstance.addDatabaseInit(databaseInit);
        JDAInstance.buildJda(discordToken);

        UITools.loadUI();
    }
}
