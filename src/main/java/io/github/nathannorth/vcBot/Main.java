package io.github.nathannorth.vcBot;

import discord4j.common.util.Snowflake;
import discord4j.core.event.ReactiveEventAdapter;
import discord4j.core.event.domain.InteractionCreateEvent;
import discord4j.core.event.domain.VoiceStateUpdateEvent;
import discord4j.core.object.entity.channel.VoiceChannel;
import discord4j.rest.http.client.ClientException;
import org.reactivestreams.Publisher;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public class Main {
    public static void main(String[] args) {
        //init systems
        Database.init();
        Bot.init();
        Commands.init();

        //subscribe to REA's
        Flux.merge(Bot.getClient().on(slashInteraction), Bot.getClient().on(joinEvents), Bot.getClient().on(leaveEvents))
                .subscribe();

        //block to keep program running
        Bot.getClient().onDisconnect().block();
    }
    //handle slash commands
    public static final ReactiveEventAdapter slashInteraction = new ReactiveEventAdapter() {
        @Override
        public Publisher<?> onInteractionCreate(InteractionCreateEvent source) {
            return source.acknowledgeEphemeral()
                    .then(Commands.getCommand(source.getCommandName()).execute(source));
        }
    };
    //handle join events
    public static final ReactiveEventAdapter joinEvents = new ReactiveEventAdapter() {
        @Override
        public Publisher<?> onVoiceStateUpdate(VoiceStateUpdateEvent source) {
            return Mono.just(source)
                    .filter(event -> event.isJoinEvent() || event.isMoveEvent())
                    .map(event -> event.getCurrent())
                    .filterWhen(current -> Database.getChans().any(snowflake -> snowflake.equals(current.getChannelId().get())))
                    .flatMapMany(current -> Database.relevantUsersFor(current.getChannelId().get())
                            .flatMap(userSnowflake -> alert(userSnowflake, current.getUserId(), current.getChannelId().get()))
                    );
        }
    };
    //handle leave events
    public static final ReactiveEventAdapter leaveEvents = new ReactiveEventAdapter() {
        @Override
        public Publisher<?> onVoiceStateUpdate(VoiceStateUpdateEvent source) {
            return Mono.just(source)
                    .filter(event -> event.isLeaveEvent() || event.isMoveEvent())
                    .map(event -> event.getOld().get())
                    .filterWhen(event -> event.getChannel()
                            .flatMap(chan -> chan.getVoiceStates().count().map(count -> count == 0))) //we only care about leave events when the vc hits 0 people
                    .filterWhen(current -> Database.getChans().any(snowflake -> snowflake.equals(current.getChannelId().get())))
                    .flatMapMany(e -> Database.relevantUsersFor(e.getChannelId().get())
                            .flatMap(user -> Database.updateMessage(e.getChannelId().get(), user.userID, Snowflake.of(0L)))
                    );
        }
    };
    private static Mono<Void> alert(User userObj, Snowflake aboutWhom, Snowflake joiningWhere) {
        Mono<Void> returnable;

        //declare a bunch of publishers
        Mono<VoiceChannel> channelMono = Bot.getClient().getChannelById(joiningWhere).ofType(VoiceChannel.class);
        Mono<String> channelName = channelMono.map(chan -> chan.getName());
        Mono<String> guildName = channelMono.flatMap(chan -> chan.getGuild().map(guild -> guild.getName()));
        Mono<String> userName = Bot.getClient().getUserById(aboutWhom)
                .map(person -> person.getUsername() + "#" + person.getDiscriminator());

        if(userObj.messageID == null) { //first time user has been notified about this channel
            //merge publishers
            Mono<String> message = Mono.zip(channelName, guildName, userName)
                    .map(tuple -> {
                        String chan = tuple.getT1();
                        String guild = tuple.getT2();
                        String user = tuple.getT3();
                        return "**[" + user + "]** joined the **[" + chan + "]** channel in the **[" + guild + "]** server";
                    });
            //get dm channel, send message, update database with said message's id
            returnable = message.flatMap(string ->
                    Bot.getClient().getUserById(userObj.userID)
                            .flatMap(user -> user.getPrivateChannel()
                                    .flatMap(priv -> priv.createMessage(string)
                                            .flatMap(messageObj -> Database.updateMessage(joiningWhere, userObj.userID, messageObj.getId())))
                            ));
        }
        else { //user has an existing notification we want to edit
            Mono<Long> numMono = channelMono.flatMap(chan -> chan.getVoiceStates().count());
            Mono<String> message = Mono.zip(channelName, guildName, numMono)
                    .map(tuple -> {
                        String chan = tuple.getT1();
                        String guild = tuple.getT2();
                        long num = tuple.getT3();
                        return "**[" + num + " users]** have joined the **[" + chan + "]** channel in the **[" + guild + "]** server";
                    });
            returnable = message.flatMap(string ->
                    Bot.getClient().getUserById(userObj.userID)
                            .flatMap(user -> user.getPrivateChannel()
                                    .flatMap(privateChannel -> Bot.getClient().getMessageById(privateChannel.getId(), userObj.messageID)
                                            .flatMap(sentmsg -> sentmsg.edit(edit -> edit.setContent(string))))
                            )).then();
        }

        return returnable.onErrorResume(e -> {
            if(e instanceof ClientException) {
                System.out.println("Permissions Error caught...printing trace:");
                e.printStackTrace();
                return Mono.empty(); //throw away permission errors
            }
            else return Mono.error(e); //send error upstream to the program if something else goes wrong
        });
    }
}