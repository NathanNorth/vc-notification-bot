package io.github.nathannorth.vcBot.commands;

import discord4j.core.event.domain.InteractionCreateEvent;
import discord4j.discordjson.json.ApplicationCommandRequest;
import reactor.core.publisher.Mono;

public abstract class Command {
    public final String name;
    public final ApplicationCommandRequest commandRequest;

    public Command() {
        this.commandRequest = getRequest();
        this.name = commandRequest.name();
    }

    protected abstract ApplicationCommandRequest getRequest();

    public abstract Mono<?> execute(InteractionCreateEvent event);
}
