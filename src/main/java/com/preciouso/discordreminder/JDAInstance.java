package com.preciouso.discordreminder;

import com.preciouso.discordreminder.Database.DatabaseInit;
import com.preciouso.discordreminder.EventHandlers.ButtonEventHandler;
import com.preciouso.discordreminder.EventHandlers.ModalEventHandler;
import com.preciouso.discordreminder.EventHandlers.SelectStringDropdownHandler;
import com.preciouso.discordreminder.EventHandlers.SlashCommandHandler;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.entities.Activity;
import net.dv8tion.jda.api.events.GenericEvent;
import net.dv8tion.jda.api.events.session.ReadyEvent;
import net.dv8tion.jda.api.events.session.ShutdownEvent;
import net.dv8tion.jda.api.hooks.EventListener;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import org.jetbrains.annotations.NotNull;

public class JDAInstance implements EventListener {
    private static JDA jda;
    private static DatabaseInit database;
    public static final String permissionsInteger = "826781322304"; // https://discord.com/api/oauth2/authorize?client_id=826781322304&permissions=536870912&scope=bot%20applications.commands

    public static JDA getJDA() {
        return jda;
    }
    public static void buildJda(String token) {
        JDABuilder jdaBuilder = JDABuilder.createDefault(token);
        jdaBuilder.setActivity(Activity.watching("the Clock"));
        jdaBuilder.addEventListeners(new JDAInstance());
        jda = jdaBuilder.build();

        addJdaCommands();
    }

    public static void addDatabaseInit(DatabaseInit databaseInit) {
        database = databaseInit;
    }

    public static DatabaseInit getDatabase() {
        return database;
    }

    @Override
    public void onEvent(@NotNull GenericEvent event) {
        if (event instanceof ReadyEvent) {
            System.out.println("JDA is ready!");
        } if (event instanceof ShutdownEvent) {
            if (database != null) {
                try {
                    database.closeDatabase();
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            }
            System.out.println("Closing JDA");
        }
    }

    private static void addJdaCommands() {
        // todo add a command callback
        jda.updateCommands().addCommands(
                Commands.slash("ping", "Calculate the ping of the bot"),
                Commands.slash("reminder", "Start a new reminder for the server")
        ).queue();

        jda.addEventListener(new SlashCommandHandler());
        jda.addEventListener(new ModalEventHandler());
        jda.addEventListener(new SelectStringDropdownHandler());
        jda.addEventListener(new ButtonEventHandler());
    }
}
