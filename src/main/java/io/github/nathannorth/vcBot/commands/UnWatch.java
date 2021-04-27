package io.github.nathannorth.vcBot.commands;

import discord4j.core.event.domain.InteractionCreateEvent;
import discord4j.core.object.entity.channel.Channel;
import discord4j.core.object.entity.channel.VoiceChannel;
import discord4j.discordjson.json.ApplicationCommandOptionData;
import discord4j.discordjson.json.ApplicationCommandRequest;
import discord4j.rest.util.ApplicationCommandOptionType;
import io.github.nathannorth.vcBot.Database;
import io.github.nathannorth.vcBot.Util;
import reactor.core.publisher.Mono;

public class UnWatch extends Command {
    @Override
    protected ApplicationCommandRequest getRequest() {
         return ApplicationCommandRequest.builder()
                 .name("unwatch")
                 .description("Remove a voice channel from your watchlist")
                 .addOption(ApplicationCommandOptionData.builder()
                         .name("channel")
                         .description("Channel for the bot stop watching")
                         .type(ApplicationCommandOptionType.CHANNEL.getValue()).required(true)
                         .build())
                 .build();
    }

    @Override
    public Mono<?> execute(InteractionCreateEvent event) {
        return Util.getChanArg(event).ofType(VoiceChannel.class).flatMap(channel ->
                Database.removeUserForChan(channel.getId(), event.getInteraction().getUser().getId())
                        .flatMap(bool -> bool
                                ? Util.followUp(event, "The **" + channel.getName() + "** channel is no longer being watched.")
                                : Util.followUp(event, "The **" + channel.getName() + "** channel was not being watched.")))
                .switchIfEmpty(Util.followUp(event, "The channel must be a voice channel."));
    }
}
