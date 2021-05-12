package io.github.nathannorth.vcBot;

import discord4j.core.event.domain.InteractionCreateEvent;
import discord4j.core.object.entity.channel.Channel;
import discord4j.discordjson.json.MessageData;
import reactor.core.publisher.Mono;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;

public class Util {
    //keys.txt is stored in root dir and holds instance-specific data (eg. bot token)
    private static List<String> keys = null;
    public static List<String> getKeys() {
        if (keys == null) {
            try {
                keys = Files.readAllLines(Paths.get("./keys.txt"));
            } catch (IOException e) {
                e.printStackTrace();
            }
            //filter out things we commented out in our keys
            for (int i = keys.size() - 1; i >= 0; i--) {
                if (keys.get(i).indexOf('#') == 0) keys.remove(i);
            }
            if(keys.get(0).equals("[INSERT YOUR TOKEN HERE]")) throw new NotEnoughKeysException("Bot token in keys.txt is not defined.");
            if(keys.size() < 6) throw new NotEnoughKeysException("Not enough keys defined!");
        }
        return keys;
    }
    private static class NotEnoughKeysException extends RuntimeException {
        private NotEnoughKeysException(String in) {
            super(in);
        }
    }

    //shortcut method for a follow up to a InteractionCreateEvent
    public static Mono<MessageData> followUp(InteractionCreateEvent event, String message) {
        return event.getInteractionResponse().createFollowupMessage(message);
    }

    //shortcut method to get a channel from a slash command that requires a channel arg
    public static Mono<Channel> getChanArg(InteractionCreateEvent event) {
        return event.getInteraction().getCommandInteraction().getOption("channel")
                .flatMap(e -> e.getValue())
                .map(e -> e.asChannel())
                .get();
    }
}
