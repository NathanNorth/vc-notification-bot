package io.github.nathannorth.vcBot;

import discord4j.core.DiscordClientBuilder;
import discord4j.core.GatewayDiscordClient;
import discord4j.core.object.entity.User;
import discord4j.core.object.presence.ClientActivity;
import discord4j.core.object.presence.ClientPresence;
import discord4j.gateway.intent.Intent;
import discord4j.gateway.intent.IntentSet;
import discord4j.rest.RestClient;

public class Bot {
    private static GatewayDiscordClient client;
    private static RestClient restClient;
    private static long appID;
    private static User self;

    //blocking init to get our client and self
    public static void init() {
        client = DiscordClientBuilder.create(Util.getKeys().get(0))
                .build()
                //we want all intents except the fancy ones that need verification
                .gateway().setDisabledIntents(IntentSet.of(Intent.GUILD_PRESENCES, Intent.GUILD_MEMBERS))
                .login()
                .block();

        restClient = client.getRestClient();
        appID = restClient.getApplicationId().block();
        self = client.getSelf().block();

        //if keys specify a status, use it
        if(Util.getKeys().size() > 1) {
            client.updatePresence(ClientPresence.online(ClientActivity.playing(Util.getKeys().get(1)))).block();
            System.out.println("Status set from keys.");
        }
        else {
            System.out.println("No status specified, no status set.");
        }
    }

    public static GatewayDiscordClient getClient() {
        return client;
    }

    public static RestClient getRestClient() {
        return restClient;
    }

    public static long getAppID() {
        return appID;
    }

    public static User getSelf() {
        return self;
    }
}
