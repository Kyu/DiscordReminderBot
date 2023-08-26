package com.preciouso.discordreminder.Util;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.time.temporal.ChronoField;
import java.time.temporal.TemporalAccessor;
import java.util.Calendar;

public class DateTimeParser {
    private static final DateTimeFormatter timeFormat12Hour = DateTimeFormatter.ofPattern("h:m a");
    private static final DateTimeFormatter timeFormat24Hour = DateTimeFormatter.ofPattern("H:m");

    private static final DateTimeFormatter dateFormatterNoYear = new DateTimeFormatterBuilder()
            .appendPattern("M-d")
            .parseDefaulting(ChronoField.YEAR, Calendar.getInstance().get(Calendar.YEAR))
            .toFormatter();
    private static final DateTimeFormatter dateFormatterWithYear = new DateTimeFormatterBuilder()
            .appendPattern("M-d-yyyy")
            .toFormatter();


    private String dateString;
    private String timeString;

    private LocalTime localTime;
    private LocalDate localDate;

    private boolean validDate = false;
    private boolean validTime = false;

    public DateTimeParser() {}
    private DateTimeParser(DateTimeParser oldParser) {
        this.dateString = oldParser.dateString;
        this.timeString = oldParser.timeString;

        this.localDate = oldParser.localDate;
        this.localTime = oldParser.localTime;

        this.validDate = oldParser.validDate;
        this.validTime = oldParser.validTime;
    }

    public boolean isValid() {
        return validDate && validTime;
    }

    public boolean isValidDate() {
        return validDate;
    }

    public boolean isValidTime() {
        return validTime;
    }

    public LocalDateTime getDateTime() {
        if (isValid()) {
            return LocalDateTime.of(localDate, localTime);
        } else {
            return null;
        }

    }

    public String getDateString() {
        return dateString;
    }

    public String getTimeString() {
        return timeString;
    }

    public DateTimeParser ofDate(String date) {
        this.dateString = date;
        validDate = parseDate(date);

        return new DateTimeParser(this);
    }

    public DateTimeParser ofTime(String time) {
        this.timeString = time;
        validTime = parseTime(time);

        return new DateTimeParser(this);
    }


    private boolean parseDate(String date) {
        try {
            if (date.chars().filter(chr -> chr == '-').count() == 2) {
                TemporalAccessor temporalAccessor = dateFormatterWithYear.parseBest(date, LocalDate::from, LocalDate::from);
                localDate = (LocalDate) temporalAccessor;
            } else {
                TemporalAccessor temporalAccessor = dateFormatterNoYear.parseBest(date, LocalDate::from, LocalDate::from);
                localDate = (LocalDate) temporalAccessor;
            }
            return true;
        } catch (RuntimeException ignored) {
            return false;
        }
    }

    private boolean parseTime(String time) {
        try {
            if (time.toLowerCase().matches("(.*)[a|p]m$")) {
                localTime = parseTime12Hour(time);
            } else {
                localTime = parseTime24Hour(time);
            }
            return true;
        } catch (RuntimeException ignored) {
            return false;
        }
    }

    private LocalTime parseTime12Hour(String time) {
        return LocalTime.parse(time, timeFormat12Hour);
    }

    private LocalTime parseTime24Hour(String time) {
        return LocalTime.parse(time, timeFormat24Hour);
    }
}
