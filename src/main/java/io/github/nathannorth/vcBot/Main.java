package io.github.nathannorth.vcBot;

import discord4j.common.util.Snowflake;
import discord4j.core.event.ReactiveEventAdapter;
import discord4j.core.event.domain.Event;
import discord4j.core.event.domain.InteractionCreateEvent;
import discord4j.core.event.domain.VoiceStateUpdateEvent;
import discord4j.core.object.entity.Message;
import discord4j.core.object.entity.channel.VoiceChannel;
import discord4j.rest.http.client.ClientException;
import org.reactivestreams.Publisher;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public class Main {
    public static void main(String[] args) {
        //init systems
        Bot.init();
        Commands.init();
        Database.init();

        //define fluxes
        Flux<Event> slashInteraction = Bot.getClient().on(slashAdapter);
        Flux<Message> channelListener = Bot.getClient().on(VoiceStateUpdateEvent.class)
                .filter(event -> event.isJoinEvent() || event.isMoveEvent()) //ignore leave events
                .map(event -> event.getCurrent())
                .filterWhen(current -> Database.getChans().any(snowflake -> snowflake.equals(current.getChannelId().get())))
                .flatMap(current -> Database.relevantUsersFor(current.getChannelId().get())
                        .flatMap(userSnowflake -> alert(userSnowflake, current.getUserId(), current.getChannelId().get()))
                );

        //subscribe to fluxes
        Flux.merge(slashInteraction, channelListener)
                .subscribe();

        //block to keep program running
        Bot.getClient().onDisconnect().block();
    }
    private static final ReactiveEventAdapter slashAdapter = new ReactiveEventAdapter() {
        @Override
        public Publisher<?> onInteractionCreate(InteractionCreateEvent event) {
            return event.acknowledgeEphemeral().then(Commands.getCommand(event.getCommandName()).execute(event));
        }
    };
    private static Mono<Message> alert(Snowflake toWho, Snowflake aboutWhom, Snowflake joiningWhere) {
        Mono<String> stringMono = Bot.getClient().getChannelById(joiningWhere).ofType(VoiceChannel.class)
                .flatMap(voiceChannel -> voiceChannel.getGuild().map(guild -> voiceChannel.getName() + "]** channel in the **[" + guild.getName() + "]** server."))
                .flatMap(channelInfo -> Bot.getClient().getUserById(aboutWhom)
                        .map(user -> "**[" + user.getUsername() + "#" + user.getDiscriminator() + "]** joined the **[" + channelInfo));

        return stringMono.flatMap(string ->
                Bot.getClient().getUserById(toWho)
                        .flatMap(user -> user.getPrivateChannel()
                                .flatMap(priv -> priv.createMessage(string))))
                .onErrorResume(e -> {
                    if(e instanceof ClientException) return Mono.empty(); //throw away permission errors
                    else return Mono.error(e); //still crash the program if something else goes wrong
                });
    }
}
