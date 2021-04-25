package io.github.nathannorth.vcNotificationBot;

import discord4j.core.DiscordClientBuilder;
import discord4j.core.GatewayDiscordClient;
import discord4j.gateway.intent.IntentSet;

public class Bot {
    public static final GatewayDiscordClient client =
            DiscordClientBuilder.create(Util.getKeys().get(0))
                    .build()
                    .gateway().setEnabledIntents(IntentSet.all()) //todo: don't require all
                    .login()
                    .block();
}
