package com.preciouso.discordreminder.Database.Entities;

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

@DatabaseTable(tableName = "users")
public class User {
    public static final String ID_COLUMN_NAME = "id";

    @DatabaseField(id = true, columnName = ID_COLUMN_NAME)
    private String id;

    @DatabaseField
    private String timezone;

    public User() {}

    public User(String id, String timezone) {
        this.id = id;
        this.timezone = timezone;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getTimezone() {
        return timezone;
    }

    public void setTimezone(String timezone) {
        this.timezone = timezone;
    }
}
