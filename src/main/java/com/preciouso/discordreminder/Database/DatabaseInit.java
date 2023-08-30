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

    public DatabaseInit(String postgresPassword) throws SQLException, ClassNotFoundException {
        this.connectionString = "jdbc:postgresql://" + "localhost:5432/" + postgresDatabase;
        createDataSource(postgresPassword);
    }

    private void createDataSource (String password) throws SQLException, ClassNotFoundException {
        dbConnectionSource = new JdbcConnectionSource(connectionString, "postgres", password);
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
