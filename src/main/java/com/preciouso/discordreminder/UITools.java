package com.preciouso.discordreminder;

import com.j256.ormlite.stmt.QueryBuilder;
import com.j256.ormlite.stmt.Where;
import com.preciouso.discordreminder.Database.DatabaseInit;
import com.preciouso.discordreminder.Database.Entities.Alert;
import com.preciouso.discordreminder.Database.Entities.User;
import com.preciouso.discordreminder.Util.DateTimeParser;
import com.preciouso.discordreminder.Util.MessageInteractionCallbackStore;
import com.preciouso.discordreminder.Util.ModalWithAction;
import com.preciouso.discordreminder.Util.StringSelectDropdownWithAction;
import net.dv8tion.jda.api.entities.EmbedType;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.events.interaction.ModalInteractionEvent;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.events.interaction.component.StringSelectInteractionEvent;
import net.dv8tion.jda.api.interactions.Interaction;
import net.dv8tion.jda.api.interactions.components.ActionRow;
import net.dv8tion.jda.api.interactions.components.LayoutComponent;
import net.dv8tion.jda.api.interactions.components.buttons.Button;
import net.dv8tion.jda.api.interactions.components.selections.SelectOption;
import net.dv8tion.jda.api.interactions.components.text.TextInput;
import net.dv8tion.jda.api.interactions.components.text.TextInputStyle;
import net.dv8tion.jda.api.interactions.modals.ModalMapping;
import net.dv8tion.jda.api.requests.restaction.interactions.ReplyCallbackAction;

import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.*;
import java.util.function.Function;

public class UITools {
    private static final String iconUrl = "https://gist.githubusercontent.com/Kyu/ed47af2ee6c65fda1fbabbfb4e472de1/raw/2c92a61e6d38e1d805eac66aa729ad7a5187b260/alarm_FILL0_wght400_GRAD0_opsz48.png";
    private static final ArrayList<String> timezoneList = new ArrayList<>();
    private static final HashMap<String, ReminderSubmission> reminderSumbissions = new HashMap<>();

    public static void loadUI() {
        loadTimezones();
        registerReuseTimezoneButton();
        registerTimezonePicker();
        registerTimeZoneSelectorModal();
        registerNewReminderModal();
    }

    private static void loadTimezones() {
        for (String tzString: TimeZone.getAvailableIDs()) {
            if (! tzString.contains("Etc")  && tzString.chars().filter(chr -> chr == '/').count() == 1) {
                timezoneList.add(tzString);
            } else if (tzString.contains("Etc/GMT")) {
                String[] gmtSplit = tzString.split("/");
                if (gmtSplit.length > 1) {
                    String gmtString = gmtSplit[1];
                    String gmtSpaceString = gmtString.replaceFirst("GMT", "GMT ");
                    String utcString = gmtString.replaceFirst("GMT", "UTC");
                    String utcSpaceString = gmtString.replaceFirst("GMT", "UTC ");

                    timezoneList.add(gmtString);
                    timezoneList.add(gmtSpaceString);
                    timezoneList.add(utcString);
                    timezoneList.add(utcSpaceString);
                }
            }
        }
    }

    private static boolean isInteger(String s) {
        return isInteger(s,10);
    }

    private static boolean isInteger(String s, int radix) {
        if(s.isEmpty()) return false;
        for(int i = 0; i < s.length(); i++) {
            if(i == 0 && (s.charAt(i) == '-') || s.charAt(i) == '+') {
                if(s.length() == 1) return false;
                else continue;
            }
            if(Character.digit(s.charAt(i),radix) < 0) return false;
        }
        return true;
    }

    // TODO Aug 29 -- allow users to remove themselves
    private static void registerTimeSelectionDropdown() {
        // ---- Select dropdown of when a user wants to be reminded ---- \\
        SelectOption onTimeOption = SelectOption.of("On Time", "on-time").withDescription("Sets a reminder at the exact time.");
        SelectOption twelveHoursOption = SelectOption.of("12 hours before", "12h-before").withDescription("Sets a reminder 12 hours before.");
        SelectOption oneDayOption = SelectOption.of("1 day before", "1d-before").withDescription("Sets a reminder 1 day before.");
        SelectOption sevenDaysOption = SelectOption.of("7 days before", "7d-before").withDescription("Sets a reminder 7 days before.");

        List<SelectOption> selectOptionsList = Arrays.asList(onTimeOption, twelveHoursOption, oneDayOption, sevenDaysOption);

        Function<StringSelectInteractionEvent, Void> selectAction = (eventData) ->  {
            AlertSubmission alertSubmission = new AlertSubmission(eventData);

            ArrayList<Alert> alertEntityList;
            DatabaseInit database = JDAInstance.getDatabase();

            try {
                alertEntityList = Alert.fromAlertSubmission(alertSubmission, database);
                for (Alert al: alertEntityList) {
                    database.getAlertDao().createOrUpdate(al);
                }
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }

            if (eventData.getValues().isEmpty()) {
                eventData.reply("Nothing chosen!").queue();
            } else {
                eventData.reply("You will be reminded about this event: " + String.join(", ", eventData.getValues()))
                        .setEphemeral(true).queue();
            }

            return null;
        };


        StringSelectDropdownWithAction selectReminderTimeMenu = new StringSelectDropdownWithAction("choose-time", "Remind me ...", 0, 4, false, selectOptionsList, selectAction);
        MessageInteractionCallbackStore.registerStringSelectDropDown(selectReminderTimeMenu.getId(), selectReminderTimeMenu);
    }

    // TODO Aug 29 -- tell user what they chose to get this reminder
    private static void registerCustomTimeSelectionModal() {
        // ---- Select button with custom input of when a user wants to be reminded ---- \\

        // modal that the button pops up
        Function<ModalInteractionEvent, Void> customTimeModalAction = (eventData) -> {
            AlertSubmission alertSubmission = new AlertSubmission(eventData);

            ArrayList<Alert> alertEntityList;
            DatabaseInit database = JDAInstance.getDatabase();

            try {
                alertEntityList = Alert.fromAlertSubmission(alertSubmission, database);
                for (Alert al: alertEntityList) {
                    database.getAlertDao().createOrUpdate(al);
                }
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
            String units = eventData.getValues().get(0).getAsString();

            if (AlertSubmission.validTimeUnits.containsKey(units.toLowerCase())) {
                String num = eventData.getValues().get(1).getAsString();
                eventData.reply("You will be reminded " + num + " " + units + " before this event.").queue();
            } else {
                eventData.reply("Invalid time unit chosen: **" + units + "**").queue();
            }

            return null;
        };

        TextInput timeUnit = TextInput.create("time-unit", "Unit of Time", TextInputStyle.SHORT)
                .setPlaceholder("Minutes/Hours/Days")
                .setMinLength(1)
                .setMaxLength(7) // or setRequiredRange(10, 100)
                .build();

        TextInput timeAmount = TextInput.create("time-amount", "Amount of Time", TextInputStyle.SHORT)
                .setPlaceholder("Amount of time")
                .setMinLength(1)
                .setMaxLength(3) // or setRequiredRange(10, 100)
                .build();

        List<LayoutComponent> customTimeModalRows = Arrays.asList(ActionRow.of(timeUnit), ActionRow.of(timeAmount));
        ModalWithAction customTimeModal = new ModalWithAction("custom-time-reminder", "Remind me ...", customTimeModalRows, customTimeModalAction);
        MessageInteractionCallbackStore.registerModal(customTimeModal.getId(), customTimeModal);
    }

    // TODO Aug 25 -- register selectmenus, registerselectbuttons, register new timezone selectmenu
    // TODO Aug 25 -- do editing past reminders
    private static void registerNewReminderModal() {
        registerTimeSelectionDropdown();
        registerCustomTimeSelectionModal();


        Function<ButtonInteractionEvent, Void> selectTimesAction = (eventData) -> {
            ModalWithAction timeModal = MessageInteractionCallbackStore.getModal("custom-time-reminder");
            if (timeModal != null) {
                eventData.replyModal(timeModal).queue();
            }
            return null;
        };

        MessageInteractionCallbackStore.registerButton("select-times", selectTimesAction);

        // ---- Modal to create the actual reminder ---- \\
        TextInput subject = TextInput.create("subject", "Subject", TextInputStyle.SHORT)
                .setPlaceholder("Subject of this reminder")
                .setMinLength(3)
                .setMaxLength(50) // or setRequiredRange(10, 100)
                .build();

        TextInput date = TextInput.create("date", "Date", TextInputStyle.SHORT) // TODO auto populate date based on timezone
                // todo warn on invalid timezone
                .setPlaceholder("Date format: MM-DD-YYYY or MM-DD")
                .setMinLength(5)
                .setMaxLength(10)
                .build();

        TextInput time = TextInput.create("time", "Time", TextInputStyle.SHORT)
                .setPlaceholder("12 or 24hr time. Defaults to 12:00 AM")
                .setRequired(false)
                .setMinLength(5)
                .setMaxLength(8)
                .build();

        TextInput description = TextInput.create("description", "Description", TextInputStyle.PARAGRAPH)
                .setPlaceholder("Subject of this reminder")
                .setRequired(false)
                .build();

        List<LayoutComponent> places = Arrays.asList(ActionRow.of(subject), ActionRow.of(date), ActionRow.of(time),
                ActionRow.of(description));

        ModalWithAction modal = getModalWithAction(places);
        MessageInteractionCallbackStore.registerModal(modal.getId(), modal);
    }

    // TODO Aug 26 -- rename
    private static ModalWithAction getModalWithAction(List<LayoutComponent> places) {
        Function<ModalInteractionEvent, Void> modalAction = (eventData) ->  {
            // eventData.getValues().forEach((x) -> System.out.println(x.getAsString()));
            DateTimeParser eventDateTime = validateDateTimeSubmission(eventData);

            if (! eventDateTime.isValid()) {
                String errorString = "";
                if (! eventDateTime.isValidDate()) {
                    errorString += "Invalid Date: **" + eventDateTime.getDateString() + "**. ";
                }
                if (! eventDateTime.isValidTime()) {
                    errorString += "Invalid Time: **" + eventDateTime.getTimeString() + "**. ";
                }

                eventData
                        .reply(errorString + "\n\n" +
                                "Remember Date Format: __MM-DD-YYYY__ or __MM-DD__, and Time Format __HH:MM__ or __HH:MM AM/PM__"
                        )
                        .setEphemeral(true)
                        .queue();
            } else {
                ReminderSubmission submission = new ReminderSubmission(eventData, eventDateTime);

                reminderSumbissions.put(Objects.requireNonNull(eventData.getMember()).getId(), submission);
                registerTimeZoneForNewReminder(eventData);

                // eventData.getHook().retrieveOriginal().queue(x -> System.out.println("New Submission: id=" + x.getId()));
            }

            return null;
        };

        String modalId = "reminderForm";
        return new ModalWithAction(modalId, "New Reminder", places, modalAction);
    }

    private static void registerReuseTimezoneButton() {
        Button selectTimesButton = Button.primary("reuse-timezone", "Select custom times");

        Function<ButtonInteractionEvent, Void> reuseTimezoneAction = (eventData) -> {
            ReminderSubmission submission = reminderSumbissions.get(Objects.requireNonNull(eventData.getMember()).getId());
            if (submission != null) {
                eventData.getHook().retrieveOriginal().queue(x -> {
                    try {
                        JDAInstance.getDatabase().getReminderDao().create(submission.toReminderEntity(x.getId()));
                    } catch (SQLException e) {
                        throw new RuntimeException(e);
                    }
                });

                if (reminderSumbissions.containsKey(Objects.requireNonNull(eventData.getMember()).getId())) {
                    eventData.reply("")
                            .addActionRow(MessageInteractionCallbackStore.getStringSelectDropdown("choose-time"))
                            .addActionRow(selectTimesButton)
                            .addEmbeds(createReminderEmbed(eventData))
                            .queue(x -> reminderSumbissions.remove(eventData.getMember().getId()));
                } else {
                }
            }

            return null;
        };

        MessageInteractionCallbackStore.registerButton(selectTimesButton.getId(), reuseTimezoneAction);
    }

    private static void registerTimeZoneSelectorModal() {
        Button selectTimesButton = Button.primary("select-times", "Select custom times");

        Function<ModalInteractionEvent, Void> modalAction = (eventData) ->  {
            ModalMapping m = eventData.getValue("timezone");
            if (m == null) {
                return null;
            }

            String tzString = m.getAsString();
            if (isInteger(tzString)) {
                int tzOffset = Integer.parseInt(tzString);
                if (tzOffset > 0) {
                    tzString = "GMT+" + tzOffset;
                } else {
                    tzString = "GMT" + tzOffset;
                }
            }

            if (timezoneList.contains(tzString)) {
                ReminderSubmission submission = reminderSumbissions.get(Objects.requireNonNull(eventData.getMember()).getId());
                if (submission != null) {
                    submission.setTimeZone(tzString);
                    // reminderSumbissions.put(eventData.getMember().getId(), submission);

                    eventData.getHook().retrieveOriginal().queue(x -> {
                        try {
                            JDAInstance.getDatabase().getReminderDao().create(submission.toReminderEntity(x.getId()));
                            JDAInstance.getDatabase().getUserDao()
                                    .createOrUpdate(new User(eventData.getMember().getId(), submission.getTimezone().getID()));
                        } catch (SQLException e) {
                            throw new RuntimeException(e);
                        }
                    });

                    eventData.reply("")
                            .addActionRow(MessageInteractionCallbackStore.getStringSelectDropdown("choose-time"))
                            .addActionRow(selectTimesButton)
                            .addEmbeds(createReminderEmbed(eventData))
                            .queue(x -> reminderSumbissions.remove(eventData.getMember().getId()));
                    // eventData.getHook().retrieveOriginal().queue(x -> System.out.println("New tzPick: id=" + x.getId()));
                }
            } else {
                eventData.reply("Could not find a valid timezone for **" + m.getAsString() + "**.")
                        .setEphemeral(true)
                        .queue();
            }

            return null;
        };

        TextInput timezone = TextInput.create("timezone", "Timezone for this reminder", TextInputStyle.SHORT)
                .setPlaceholder("(Continent/Region) or (UTC +x) or (+x) are allowed")
                .setMinLength(1)
                .setRequired(false)
                .build();

        List<LayoutComponent> places = List.of(ActionRow.of(timezone));

        String modalId = "timezone-picker";
        ModalWithAction timezoneModal = new ModalWithAction(modalId, "Select a Timezone", places, modalAction);
        MessageInteractionCallbackStore.registerModal(modalId, timezoneModal);
    }

    private static void registerTimezonePicker() {
        Function<ButtonInteractionEvent, Void> selectTimesAction = (eventData) -> {
            // timezoneSelectorForm
            ModalWithAction timeModal = MessageInteractionCallbackStore.getModal("timezone-picker");
            if (timeModal != null) {
                eventData.replyModal(timeModal).queue();
            }
            return null;
        };

        // TODO Aug 27 -- add a "i remember your timezone" button, also populate modal.value with same
        MessageInteractionCallbackStore.registerButton("select-timezone", selectTimesAction);
    }

    private static void registerTimeZoneForNewReminder(ModalInteractionEvent event) {
        Button selectTimezoneButton = Button.primary("select-timezone", "Select timezone"); // todo is string

        QueryBuilder<User, String> userQb = JDAInstance.getDatabase().getUserDao().queryBuilder();
        Button timezoneButton = null;

        try {
            Where<User, String> userWhere = userQb.where().eq(User.ID_COLUMN_NAME, event.getUser().getId());
            User user = JDAInstance.getDatabase().getUserDao().queryForFirst(userWhere.prepare());

            if (user != null) {
                ReminderSubmission submission = reminderSumbissions.get(user.getId());
                submission.setTimeZone(user.getTimezone());

                timezoneButton = Button.primary("reuse-timezone", "Use timezone: " + user.getTimezone());
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }

        ReplyCallbackAction preparedResponse = event.reply("Please enter your timezone region. You can use https://kevinnovak.github.io/Time-Zone-Picker/ for help!")
                .addActionRow(selectTimezoneButton)
                .setEphemeral(true);

        if (timezoneButton != null) {
            preparedResponse = preparedResponse.addActionRow(timezoneButton);
        }

        preparedResponse.queue();
        // event.getHook().retrieveOriginal().queue(x -> System.out.println("New rtz: id=" + x.getId()));
    }

    private static MessageEmbed createAskDateTimeDropDown(ModalInteractionEvent event, LocalDateTime dateTimeInfo) {
        return null;
    }

    private static MessageEmbed createReminderEmbed(Interaction event) {
        ReminderSubmission reminderSubmission = reminderSumbissions.get(Objects.requireNonNull(event.getMember()).getId());

        MessageEmbed.AuthorInfo authorInfo = new MessageEmbed.AuthorInfo("New Reminder!", "", iconUrl, "");

        MessageEmbed.Field descriptionField = new MessageEmbed.Field("Description", reminderSubmission.getDescription(), false);
        MessageEmbed.Field timeField = new MessageEmbed.Field("Time", reminderSubmission.getFormattedTime(), true);

        ArrayList<MessageEmbed.Field> allFields = new ArrayList<>(List.of(timeField));
        if (! reminderSubmission.getDescription().isEmpty()) {
            allFields.add(descriptionField);
        }

        String embedUrl = "";
        if (event instanceof ButtonInteractionEvent bie) {
            embedUrl = bie.getMessage().getJumpUrl();
        } else if (event instanceof ModalInteractionEvent mie) {
            embedUrl = Objects.requireNonNull(mie.getMessage()).getJumpUrl();
        }

        return new MessageEmbed(embedUrl, reminderSubmission.getSubject(), "", EmbedType.RICH,
                null, 0x0099FF, null, null, authorInfo, null, null, null,
                allFields);
    }

    private static DateTimeParser validateDateTimeSubmission(ModalInteractionEvent event) {
        ModalMapping date = event.getValue("date");
        String dateString = "";
        if (date != null) {
            String dateEntry = date.getAsString();
            if (! dateEntry.isEmpty()) {
                dateString = dateEntry;
            }
        }

        ModalMapping time = event.getValue("time");
        String timeString = "00:00";
        if (time != null) {
            String timeEntry = time.getAsString();
            if (! timeEntry.isEmpty()) {
                timeString = timeEntry;
            }
        }

        return new DateTimeParser().ofDate(dateString).ofTime(timeString);
    }
}
