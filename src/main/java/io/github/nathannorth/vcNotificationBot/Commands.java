package io.github.nathannorth.vcNotificationBot;

import discord4j.discordjson.json.ApplicationCommandOptionData;
import discord4j.discordjson.json.ApplicationCommandRequest;
import discord4j.rest.util.ApplicationCommandOptionType;

import java.util.ArrayList;
import java.util.List;

public class Commands {
    public static void init() {
        List<ApplicationCommandRequest> commands = new ArrayList<>();

        ApplicationCommandRequest listen = ApplicationCommandRequest.builder()
                .name("listen")
                .description("Add a vc to your listening list")
                .addOption(ApplicationCommandOptionData.builder()
                        .name("channel")
                        .description("Channel you want to listen to")
                        .type(ApplicationCommandOptionType.CHANNEL.getValue())
                        .build())
                .build();
        commands.add(listen);

        for(ApplicationCommandRequest r: commands) {
            Bot.getRestClient().getApplicationService()
                    .createGlobalApplicationCommand(Bot.getAppID(), r)
                    .block();
        }
    }
}
