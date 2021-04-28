package io.github.nathannorth.vcBot;

import discord4j.common.util.Snowflake;
import discord4j.core.object.entity.channel.VoiceChannel;
import io.r2dbc.postgresql.PostgresqlConnectionConfiguration;
import io.r2dbc.postgresql.PostgresqlConnectionFactory;
import org.springframework.r2dbc.core.DatabaseClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public class Database {

    private static DatabaseClient client;

    public static void init() {
        getCon(0);
    }
    //catch blocks from hell to try a simple db action on a 2 ^ (attempt) second retry loop
    private static void getCon(int retry) {
        try {
             client = DatabaseClient.create(new PostgresqlConnectionFactory(
                            PostgresqlConnectionConfiguration.builder()
                                    .database(Util.getKeys().get(2))
                                    .username(Util.getKeys().get(2))
                                    .password(Util.getKeys().get(3))
                                    .host(Util.getKeys().get(4))
                                    .port(Integer.parseInt(Util.getKeys().get(5)))
                                    .build()
                    )
             );

            client.sql("CREATE TABLE IF NOT EXISTS " +
                    "chans (channelID BIGINT, userID BIGINT, PRIMARY KEY (channelID, userID))")
                    .then()
                    .block();
        } catch (Exception e) {
            System.out.println("Database connection failure! Retrying in " + Math.pow(2, retry) + " seconds.");
            try {
                Thread.sleep((long) (Math.pow(2, retry) * 1000L));
            } catch (InterruptedException interruptedException) {
                interruptedException.printStackTrace();
            }
            getCon(retry + 1);
        }
    }

    public static Flux<Snowflake> getChans() {
        return client.sql("SELECT channelID FROM chans")
                .map((row, data) -> Snowflake.of(row.get("channelID", Long.class)))
                .all();
    }

    public static Flux<Snowflake> relevantUsersFor(Snowflake channel) {
        return client.sql("SELECT userID FROM chans WHERE channelID = :chan")
                .bind("chan", channel.asLong())
                .map((row, data) -> Snowflake.of(row.get("userID", Long.class)))
                .all()
                .filterWhen(userFlake -> Bot.getClient().getChannelById(channel).ofType(VoiceChannel.class)
                        .flatMap(vc -> vc.isMemberConnected(userFlake).map(bool -> !bool))
                );
    }

    //boolean represents whether or not the user was already added for that channel
    public static Mono<Boolean> addUserForChan(Snowflake channel, Snowflake user) {
        return client.sql("INSERT INTO chans (channelID, userID) VALUES (:chan, :user)")
                .bind("chan", channel.asLong())
                .bind("user", user.asLong())
                .then() //catch errors for when we try to add an already existent user
                .then(Mono.just(true)).onErrorResume(error -> Mono.just(false));
    }

    //boolean represents whether the user falsely attempted to remove themselves from a channel they aren't associated with
    public static Mono<Boolean> removeUserForChan(Snowflake channel, Snowflake user) {
        return exists(channel, user).flatMap(bool ->
                client.sql("DELETE FROM chans WHERE channelID = :chan AND userID = :user")
                        .bind("chan", channel.asLong())
                        .bind("user", user.asLong())
                        .then()
                        .then(Mono.just(true))
        ).switchIfEmpty(Mono.just(false));
    }
    //completes with at true value if exists, else completes empty
    private static Mono<Boolean> exists(Snowflake channel, Snowflake user) {
        return client.sql("SELECT * FROM chans WHERE channelID = :chan AND userID = :user")
                .bind("chan", channel.asLong())
                .bind("user", user.asLong())
                .map((row, metadata) -> true)
                .first();
    }
}
