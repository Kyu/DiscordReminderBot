package com.preciouso.discordreminder.Database;

import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.dao.DaoManager;
import com.j256.ormlite.jdbc.JdbcConnectionSource;
import com.j256.ormlite.support.ConnectionSource;
import com.j256.ormlite.table.TableUtils;
import com.preciouso.discordreminder.Database.Entities.Alert;
import com.preciouso.discordreminder.Database.Entities.Reminder;
import com.preciouso.discordreminder.Database.Entities.User;

import java.sql.SQLException;


public class DatabaseInit {
    private final String connectionString;
    private final String postgresDatabase = "reminder-bot";
    private ConnectionSource dbConnectionSource;
    private Dao<Reminder, String> reminderDao;
    private Dao<Alert, String> alertDao;
    private Dao<User, String> userDao;

    public DatabaseInit(String hostname, String port, String username, String password) throws SQLException {
        this.connectionString = "jdbc:postgresql://" + hostname + ":" + port + "/" + postgresDatabase;
        System.err.println(connectionString);
        createDataSource(username, password);
    }

    private void createDataSource (String username, String password) throws SQLException {
        dbConnectionSource = new JdbcConnectionSource(connectionString, username, password);
        setupDatabase();
    }

    private void setupDatabase() throws SQLException {
        reminderDao = DaoManager.createDao(dbConnectionSource, Reminder.class);
        TableUtils.createTableIfNotExists(dbConnectionSource, Reminder.class);

        alertDao = DaoManager.createDao(dbConnectionSource, Alert.class);
        TableUtils.createTableIfNotExists(dbConnectionSource, Alert.class);

        userDao = DaoManager.createDao(dbConnectionSource, User.class);
        TableUtils.createTableIfNotExists(dbConnectionSource, User.class);
    }

    public void closeDatabase() throws Exception {
        dbConnectionSource.close();
    }

    public Dao<Reminder, String> getReminderDao() {
        return reminderDao;
    }

    public Dao<Alert, String> getAlertDao() {
        return alertDao;
    }

    public Dao<User, String> getUserDao() {
        return userDao;
    }
}
