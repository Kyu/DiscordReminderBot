package com.preciouso.discordreminder;

import com.preciouso.discordreminder.Database.DatabaseInit;
import com.preciouso.discordreminder.EventHandlers.ButtonEventHandler;
import com.preciouso.discordreminder.EventHandlers.ModalEventHandler;
import com.preciouso.discordreminder.EventHandlers.SelectStringDropdownHandler;
import com.preciouso.discordreminder.EventHandlers.SlashCommandHandler;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.entities.Activity;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.events.GenericEvent;
import net.dv8tion.jda.api.events.guild.GuildJoinEvent;
import net.dv8tion.jda.api.events.session.ReadyEvent;
import net.dv8tion.jda.api.events.session.ShutdownEvent;
import net.dv8tion.jda.api.hooks.EventListener;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import org.jetbrains.annotations.NotNull;

import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;

public class JDAInstance implements EventListener {
    private static JDA jda;
    private static DatabaseInit database;
    private static String alertzyUrl = "";

    private static HttpClient client = HttpClient.newHttpClient();
    public static final String permissionsInteger = "826781322304"; // https://discord.com/oauth2/authorize?client_id=1144323688192290826&permissions=536870912&scope=bot%20applications.commands
    public static JDA getJDA() {
        return jda;
    }
    public static void buildJda(String token) {
        JDABuilder jdaBuilder = JDABuilder.createDefault(token);
        jdaBuilder.setActivity(Activity.watching("the Clock"));
        jdaBuilder.addEventListeners(new JDAInstance());
        jda = jdaBuilder.build();
    }

    public static void addDatabaseInit(DatabaseInit databaseInit) {
        database = databaseInit;
    }

    public static DatabaseInit getDatabase() {
        return database;
    }

    public static void setAlertzyUrl(String key) {
        alertzyUrl = "https://alertzy.app/send?accountKey=" + key;
    }

    @Override
    public void onEvent(@NotNull GenericEvent event) {
        if (event instanceof ReadyEvent) {
            addJdaCommands();
            System.out.println("JDA is ready!");
        } else if (event instanceof ShutdownEvent) {
            if (database != null) {
                try {
                    database.closeDatabase();
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            }
            System.out.println("Closing JDA");
        } else if (event instanceof GuildJoinEvent guildJoinEvent) {
            Guild guild = guildJoinEvent.getGuild();
            String guildString = getGuildString(guild);

            System.out.println("Joined server: " + guildJoinEvent.getGuild().getName());
            System.out.println(guildString);

            if (alertzyUrl.isEmpty()) {
                return;
            }

            String args = "&title=" + URLEncoder.encode("ReminderBot joined "+ guild.getName(), StandardCharsets.UTF_8)
                    + "&message=" + URLEncoder.encode(guildString, StandardCharsets.UTF_8);

            HttpRequest request = HttpRequest.newBuilder()
                    .POST(HttpRequest.BodyPublishers.noBody())
                    .uri(URI.create(alertzyUrl + args))
                    .timeout(Duration.ofSeconds(20))
                    .build();

            client.sendAsync(request, HttpResponse.BodyHandlers.ofString());


        }
    }

    private String getGuildString(Guild guild) {
        String guildString = guild.getName() + " has " + guild.getMemberCount() + " members!\n";
        guildString += "Description: " + guild.getDescription() + "\n";
        guildString += "Owner: " + guild.getOwner() + "[" + guild.getOwnerId() + "]\n";
        if (guild.getVanityUrl() != null) {
            guildString += "Vanity: " + guild.getVanityUrl();
        }
        // https://alertzy.app/send?accountKey=p4a4o96sa0l4rf6&title=text1&message=text2
        return guildString;
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
