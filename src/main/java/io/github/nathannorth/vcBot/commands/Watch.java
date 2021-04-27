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

public class Watch extends Command {

    @Override
    protected ApplicationCommandRequest getRequest() {
        return ApplicationCommandRequest.builder()
                .name("watch")
                .description("Add a voice channel to your watchlist")
                .addOption(ApplicationCommandOptionData.builder()
                        .name("channel")
                        .description("Channel for the bot to watch")
                        .type(ApplicationCommandOptionType.CHANNEL.getValue()).required(true)
                        .build())
                .build();
    }

    @Override
    public Mono<?> execute(InteractionCreateEvent event) {
        Mono<Channel> chan = event.getInteraction().getCommandInteraction().getOption("channel")
                .flatMap(e -> e.getValue())
                .map(e -> e.asChannel())
                .get(); //shouldn't be possible to make this throw errors

        return chan.ofType(VoiceChannel.class).flatMap(channel ->
                Database.addUserForChan(channel.getId(), event.getInteraction().getUser().getId())
                        .flatMap(bool -> bool
                                ? Util.followUp(event, "The **" + channel.getName() + "** channel is now being watched.")
                                : Util.followUp(event, "The **" + channel.getName() + "** channel was already being watched.")))
                .switchIfEmpty(Util.followUp(event, "The channel must be a voice channel."));
    }
}
