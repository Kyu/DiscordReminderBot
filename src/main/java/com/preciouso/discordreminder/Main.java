package com.preciouso.discordreminder;

import com.preciouso.discordreminder.Util.DateTimeParser;
import com.preciouso.discordreminder.Util.MessageInteractionCallbackStore;
import com.preciouso.discordreminder.Util.ModalWithAction;
import com.preciouso.discordreminder.Util.StringSelectDropdownWithAction;
import net.dv8tion.jda.api.entities.EmbedType;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.events.interaction.ModalInteractionEvent;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.events.interaction.component.StringSelectInteractionEvent;
import net.dv8tion.jda.api.interactions.components.ActionRow;
import net.dv8tion.jda.api.interactions.components.LayoutComponent;
import net.dv8tion.jda.api.interactions.components.buttons.Button;
import net.dv8tion.jda.api.interactions.components.selections.SelectOption;
import net.dv8tion.jda.api.interactions.components.text.TextInput;
import net.dv8tion.jda.api.interactions.components.text.TextInputStyle;
import net.dv8tion.jda.api.interactions.modals.ModalMapping;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.Function;

public class Main {
    private static final String iconUrl = "https://gist.githubusercontent.com/Kyu/ed47af2ee6c65fda1fbabbfb4e472de1/raw/2c92a61e6d38e1d805eac66aa729ad7a5187b260/alarm_FILL0_wght400_GRAD0_opsz48.png";

    public static void main(String[] args) {
        String discordToken = System.getenv("JDA_DISCORD_TOKEN");
        if (discordToken == null || discordToken.isEmpty()) {
            throw new RuntimeException("No JDA_DISCORD_TOKEN exists in Environment Arguments! Exiting");
        }

        JDAInstance.buildJda(discordToken);
        registerNewReminderModal();
    }

    private static void registerTimeSelectionDropdown() {
        // ---- Select dropdown of when a user wants to be reminded ---- \\
        SelectOption onTimeOption = SelectOption.of("On Time", "on-time").withDescription("Sets a reminder at the exact time.");
        SelectOption twelveHoursOption = SelectOption.of("12 hours before", "12h-before").withDescription("Sets a reminder 12 hours before.");
        SelectOption oneDayOption = SelectOption.of("1 day before", "1d-before").withDescription("Sets a reminder 1 day before.");
        SelectOption sevenDaysOption = SelectOption.of("7 days before", "7d-before").withDescription("Sets a reminder 7 days before.");

        List<SelectOption> selectOptionsList = Arrays.asList(onTimeOption, twelveHoursOption, oneDayOption, sevenDaysOption);

        Function<StringSelectInteractionEvent, Void> selectAction = (eventData) ->  {
            eventData.reply("You chose " + String.join(", ", eventData.getValues())).setEphemeral(true).queue();
            System.out.println("replied to msg=" + eventData.getMessageId());
            return null;
        };

        StringSelectDropdownWithAction selectReminderTimeMenu = new StringSelectDropdownWithAction("choose-time", "Remind me ...", 0, 4, false, selectOptionsList, selectAction);
        MessageInteractionCallbackStore.registerStringSelectDropDown(selectReminderTimeMenu.getId(), selectReminderTimeMenu);
    }

    private static void registerCustomTimeSelectionModal() {
        // ---- Select button with custom input of when a user wants to be reminded ---- \\

        // modal that the button pops up
        Function<ModalInteractionEvent, Void> customTimeModalAction = (eventData) -> {
            ArrayList<String> tmp = new ArrayList<>();
            eventData.getValues().forEach(x -> tmp.add(x.getAsString()));
            eventData.reply("You picked " + String.join(", ", tmp)).setEphemeral(true).queue();
            return null;
        };

        TextInput timeUnit = TextInput.create("time-unit", "Unit of Time", TextInputStyle.SHORT)
                .setPlaceholder("Minutes/Hours/Days")
                .setMinLength(4)
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
    // TODO Aug 25 -- do saving reminder to database
    // TODO Aug 25 -- do actually reminding users
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

        Button selectTimesButton = Button.primary("select-times", "Select custom reminder");
        MessageInteractionCallbackStore.registerButton(selectTimesButton.getId(), selectTimesAction);

        // ---- Modal to create the actual reminder ---- \\
        TextInput subject = TextInput.create("subject", "Subject", TextInputStyle.SHORT)
                .setPlaceholder("Subject of this reminder")
                .setMinLength(3)
                .setMaxLength(50) // or setRequiredRange(10, 100)
                .build();

        TextInput date = TextInput.create("date", "Date", TextInputStyle.SHORT)
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

        TextInput timezone = TextInput.create("timezone", "timezone", TextInputStyle.SHORT)
                .setPlaceholder("UTC offset or timezone identifier. Defaults to 0.")
                .setRequired(false)
                .setValue("CST")
                .setMinLength(2)
                .setMaxLength(3)
                .build();

        TextInput description = TextInput.create("description", "Description", TextInputStyle.PARAGRAPH)
                .setPlaceholder("Subject of this reminder")
                .setRequired(false)
                .build();

        List<LayoutComponent> places = Arrays.asList(ActionRow.of(subject), ActionRow.of(date), ActionRow.of(time),
                ActionRow.of(timezone), ActionRow.of(description));

        Function<ModalInteractionEvent, Void> modalAction = (eventData) ->  {
            // eventData.getValues().forEach((x) -> System.out.println(x.getAsString()));
            DateTimeParser eventDateTime = doShitWithModalData(eventData);

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
                eventData.reply("")
                        .addActionRow(MessageInteractionCallbackStore.getStringSelectDropdown("choose-time"))
                        .addActionRow(selectTimesButton)
                        .addEmbeds(createReminderEmbed(eventData, eventDateTime))
                        .queue();

                eventData.getHook().retrieveOriginal().queue(x -> System.out.println("New Submission: id=" + x.getId()));
            }

            return null;
        };

        String modalId = "reminderForm";
        ModalWithAction modal = new ModalWithAction(modalId, "New Reminder", places, modalAction);
        MessageInteractionCallbackStore.registerModal(modal.getId(), modal);
    }


    private static MessageEmbed createAskDateTimeDropDown(ModalInteractionEvent event, LocalDateTime dateTimeInfo) {
        return null;
    }

    private static MessageEmbed createReminderEmbed(ModalInteractionEvent event, DateTimeParser dateTimeInfo) {
        ModalMapping subject = event.getValue("subject");
        String subjectString = subject == null ? "" : subject.getAsString();

        ModalMapping description = event.getValue("description");
        String descriptionString = description == null ? "" : description.getAsString();

        ModalMapping date = event.getValue("date");
        String dateString = date == null ? "" : date.getAsString();

        ModalMapping time = event.getValue("time");
        String timeString = time == null ? "" : time.getAsString();

        ModalMapping timezone = event.getValue("timezone");
        String timeZoneString = timezone == null ? "+0" : timezone.getAsString();

        MessageEmbed.AuthorInfo authorInfo = new MessageEmbed.AuthorInfo("New Reminder!", "", iconUrl, "");

        MessageEmbed.Field descriptionField = new MessageEmbed.Field("Description", descriptionString, false);
        MessageEmbed.Field dateField = new MessageEmbed.Field("Date", dateString, true);
        MessageEmbed.Field timeField = new MessageEmbed.Field("Time", timeString, true);

        ArrayList<MessageEmbed.Field> allFields = new ArrayList<>(Arrays.asList(dateField, timeField));
        if (!descriptionString.isEmpty()) {
            allFields.add(descriptionField);
        }

        String embedUrl = event.getGuildChannel().getJumpUrl();
        if (event.getMessage() != null) {
            embedUrl = event.getMessage().getJumpUrl();
        }

        return new MessageEmbed(embedUrl, subjectString, "", EmbedType.RICH,
                null, 0x0099FF, null, null, authorInfo, null, null, null,
                allFields);
    }

    private static DateTimeParser doShitWithModalData(ModalInteractionEvent event) {
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
