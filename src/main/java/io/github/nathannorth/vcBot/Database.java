package io.github.nathannorth.vcBot;

import discord4j.common.util.Snowflake;
import discord4j.core.object.entity.channel.VoiceChannel;
import io.r2dbc.postgresql.PostgresqlConnectionConfiguration;
import io.r2dbc.postgresql.PostgresqlConnectionFactory;
import org.springframework.r2dbc.core.DatabaseClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.Duration;

public class Database {

    private static final DatabaseClient client =
            DatabaseClient.create(new PostgresqlConnectionFactory(
                    PostgresqlConnectionConfiguration.builder()
                            .database(Util.getKeys().get(2))
                            .username(Util.getKeys().get(2))
                            .password(Util.getKeys().get(3))
                            .host(Util.getKeys().get(4))
                            .port(Integer.parseInt(Util.getKeys().get(5)))
                            .build()
            )
    );

    public static void init() {
        getCon(0);
        //purge messageIDs on startup
        client.sql("UPDATE chans SET messageID = :empty")
                .bind("empty", 0L)
                .then()
                .block();
    }
    //try an database query and if that fails retry on a 2^(attempt) second delay
    private static void getCon(int retry) {
        try {
            client.sql("CREATE TABLE IF NOT EXISTS " +
                    "chans (channelID BIGINT, userID BIGINT, messageID BIGINT, PRIMARY KEY (channelID, userID))")
                    .then()
                    .block();
        } catch (Exception e) {
            System.out.println("Database connection failure! Retrying in " + Math.pow(2, retry) + " seconds.");
            Mono.delay(Duration.ofSeconds((long) Math.pow(2, retry))).block();
            getCon(retry + 1);
        }
    }

    public static Flux<Snowflake> getChans() {
        return client.sql("SELECT DISTINCT channelID FROM chans")
                .map((row, data) -> Snowflake.of(row.get("channelID", Long.class)))
                .all();
    }

    public static Flux<User> relevantUsersFor(Snowflake channel) {
        return client.sql("SELECT * FROM chans WHERE channelID = :chan")
                .bind("chan", channel.asLong())
                .map((row, data) -> new User(row.get("userID", Long.class), row.get("messageID", Long.class)))
                .all()
                .filterWhen(user -> Bot.getClient().getChannelById(channel).ofType(VoiceChannel.class)
                        .flatMap(vc -> vc.isMemberConnected(user.userID).map(bool -> !bool))
                );
    }

    //boolean represents whether or not the user was already added for that channel
    public static Mono<Boolean> addUserForChan(Snowflake channel, Snowflake user) {
        return client.sql("INSERT INTO chans (channelID, userID, messageID) VALUES (:chan, :user, :empty)")
                .bind("chan", channel.asLong())
                .bind("user", user.asLong())
                .bind("empty", 0L)
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

    public static Mono<Void> updateMessage(Snowflake channel, Snowflake user, Snowflake newMsgID) {
        return client.sql("UPDATE chans SET messageID = :msg WHERE channelID = :chan AND userID = :user")
                .bind("chan", channel.asLong())
                .bind("user", user.asLong())
                .bind("msg", newMsgID.asLong())
                .then();
    }
}
