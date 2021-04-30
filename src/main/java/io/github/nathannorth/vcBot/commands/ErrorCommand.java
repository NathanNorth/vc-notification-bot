package io.github.nathannorth.vcBot.commands;

import discord4j.core.event.domain.InteractionCreateEvent;
import discord4j.discordjson.json.ApplicationCommandRequest;
import io.github.nathannorth.vcBot.Util;
import reactor.core.publisher.Mono;

public class ErrorCommand extends Command {
    public final static ErrorCommand obj = new ErrorCommand();

    //cannot be constructed
    private ErrorCommand() {
    }

    @Override
    protected ApplicationCommandRequest getRequest() {
        return ApplicationCommandRequest.builder()
                .name("placeholder")
                .description("should never actually be used")
                .build();
    }

    @Override
    public Mono<?> execute(InteractionCreateEvent event) {
        return Util.followUp(event, "This command cannot be completed. If you believe this in error, please contact the bot creator.");
    }
}
