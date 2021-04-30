package io.github.nathannorth.vcBot.commands;

import discord4j.core.event.domain.InteractionCreateEvent;
import discord4j.discordjson.json.ApplicationCommandRequest;
import io.github.nathannorth.vcBot.Util;
import reactor.core.publisher.Mono;

public class Error extends Command {
    public final static Error obj = new Error();

    //cannot be constructed
    private Error() {
    }

    @Override
    protected ApplicationCommandRequest getRequest() {
        return null;
    }

    @Override
    public Mono<?> execute(InteractionCreateEvent event) {
        return Util.followUp(event, "This command cannot be completed. If you believe this in error, please contact the bot creator.");
    }
}
