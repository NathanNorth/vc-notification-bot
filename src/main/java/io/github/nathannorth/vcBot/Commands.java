package io.github.nathannorth.vcBot;

import discord4j.discordjson.json.ApplicationCommandRequest;
import io.github.nathannorth.vcBot.commands.Command;
import io.github.nathannorth.vcBot.commands.Watch;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Commands {

    private static final Map<String, Command> responses = new HashMap<>();

    public static void init() {
        //declare temp arrays
        List<Command> commands = new ArrayList<>();
        List<ApplicationCommandRequest> slashes = new ArrayList<>();

        //load all commands into the list
        System.out.print("Loading commands...");
        commands.add(new Watch());
        System.out.println(commands.size() + " commands loaded!");

        //populate lists of commandRequests and map of commands
        for(Command c: commands) {
            slashes.add(c.commandRequest);
            responses.put(c.name, c);
        }

        //override any existing commands
        System.out.print("Bulk overriding slashes...");
        Bot.getRestClient().getApplicationService().bulkOverwriteGlobalApplicationCommand(Bot.getAppID(), slashes).subscribe();
        System.out.println("done!");
    }

    public static Command getCommand(String name) {
        return responses.get(name);
    }
}
