package io.github.nathannorth.vcBot;

import discord4j.discordjson.json.ApplicationCommandRequest;
import io.github.nathannorth.vcBot.commands.Command;
import io.github.nathannorth.vcBot.commands.Listen;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Commands {

    private static final List<Command> commands = new ArrayList<>();
    private static final List<ApplicationCommandRequest> slashes = new ArrayList<>();
    public static final Map<String, Command> responses = new HashMap<>();

    public static void init() {
        System.out.print("Loading commands...");
        commands.add(new Listen());
        System.out.println(commands.size() + " commands loaded!");

        for(Command c: commands) {
            slashes.add(c.commandRequest);
            responses.put(c.name, c);
        }

        System.out.print("Bulk overriding slashes...");
        Bot.getRestClient().getApplicationService().bulkOverwriteGlobalApplicationCommand(Bot.getAppID(), slashes).subscribe();
        System.out.println("done!");
    }
}
