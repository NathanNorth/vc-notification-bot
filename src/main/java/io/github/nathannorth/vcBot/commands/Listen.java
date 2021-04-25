package io.github.nathannorth.vcBot.commands;

import discord4j.core.event.domain.InteractionCreateEvent;
import discord4j.core.object.entity.channel.Channel;
import discord4j.core.object.entity.channel.VoiceChannel;
import discord4j.discordjson.json.ApplicationCommandOptionData;
import discord4j.discordjson.json.ApplicationCommandRequest;
import discord4j.rest.util.ApplicationCommandOptionType;
import reactor.core.publisher.Mono;

public class Listen extends Command {

    @Override
    protected ApplicationCommandRequest getRequest() {
        return ApplicationCommandRequest.builder()
                .name("listen")
                .description("Add a vc to your listening list")
                .addOption(ApplicationCommandOptionData.builder()
                        .name("channel")
                        .description("Channel you want to listen to")
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

        return chan.ofType(VoiceChannel.class).flatMap(channel -> {
            return event.getInteractionResponse().createFollowupMessage("Done!");
        }).switchIfEmpty(event.getInteractionResponse().createFollowupMessage("Channel must be an audio channel!"));
    }
}
