package io.github.nathannorth.vcBot;

import discord4j.common.util.Snowflake;
import io.r2dbc.postgresql.PostgresqlConnectionConfiguration;
import io.r2dbc.postgresql.PostgresqlConnectionFactory;
import org.springframework.r2dbc.core.DatabaseClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public class Database {
    private static final PostgresqlConnectionFactory factory =
            new PostgresqlConnectionFactory(PostgresqlConnectionConfiguration.builder()
                    .host("localhost")
                    .username("postgres")
                    .password("password")
                    .database("postgres")
                    .build());

    public static void init() {

        DatabaseClient client = DatabaseClient.create(factory);
//        Mono.from(factory.create())
//                .flatMapMany(connection -> connection
//                        .createStatement("INSERT INTO chans (channelID, userID) VALUES ('piss', 'poop')")
//                        .execute())
//                .doOnNext(postgresqlResult -> System.out.println(postgresqlResult))
//                .subscribe();

        Mono.from(factory.create())
                .flatMapMany(connection -> connection
                        .createStatement(
                                "CREATE TABLE IF NOT EXISTS chans" +
                                        "(" +
                                        "channelID BIGINT" +
                                        ", userID BIGINT" +
                                        ", PRIMARY KEY(channelID, userID)" +
                                        ")")
                        .execute())
                .then()
                .block(); //block because run during init

        Mono.from(factory.create())
                .flatMapMany(connection -> connection
                        .createStatement("INSERT INTO chans (channelID, userID) VALUES ($1, $2)")
                        .bind("$1", 234L)
                        .bind("$2", 22340003000L)
                        .execute())
                .doOnNext(postgresqlResult -> System.out.println(postgresqlResult))
                .subscribe();

        client
                .sql("INSERT INTO chans (channelID, userID) VALUES (:chan, :use)")
                .bind("chan", 234L)
                .bind("use", 22340003000L)
                .then().block();


        Mono.never().block();
    }

    public static Flux<Snowflake> getChans() {
        return Mono.from(factory.create())
                .flatMapMany(connection -> connection
                        .createStatement("SELECT channelID FROM chans")
                        .execute())
                .flatMap(result -> result
                        .map((row, rowData) -> row.get("channelID", String.class)))
                .map(someLong -> Snowflake.of(someLong));
    }

    public static Flux<Snowflake> getUsersFor(Snowflake channel) {
        return Mono.from(factory.create())
                .flatMapMany(connection -> connection
                        .createStatement("SELECT userID WHERE channelID = :chan")
                        .bind("chan", channel.asString())
                        .execute())
                .flatMap(result -> result
                        .map((row, rowData) -> row.get("userID", String.class)))
                .map(someLong -> Snowflake.of(someLong));
    }

    //boolean represents whether or not the user was already added for that channel
    public static Mono<Boolean> addUserForChan(Snowflake channel, Snowflake user) {
        System.out.println("chan " + channel.asLong() + " user " + user.asLong());
        return Mono.from(factory.create())
                .flatMapMany(connection -> connection
                        .createStatement("INSERT INTO chans (channelID, userID) VALUES ($1, $2)")
                        .bind("$1", 233)
                        .bind("$2", 234)
                        .execute())
                .then(Mono.just(true)).onErrorResume(e -> {
                    e.printStackTrace();
                    return Mono.just(false);
                });
    }

    //boolean represents whether the user falsely attempted to remove themselves from a channel they aren't associated with
    public static Mono<Void> removeUserForChan(Snowflake channel, Snowflake user) {
        return Mono.from(factory.create())
                .flatMapMany(connection -> connection
                        .createStatement("DELETE FROM chans WHERE channelID = :chan AND userID = :user")
                        .bind("chan", channel.asString())
                        .bind("user", user.asString())
                        .execute())
                .then();
    }
}
