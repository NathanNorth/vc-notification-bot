package io.github.nathannorth.vcBot;

import discord4j.common.util.Snowflake;
import io.r2dbc.postgresql.PostgresqlConnectionConfiguration;
import io.r2dbc.postgresql.PostgresqlConnectionFactory;
import reactor.core.publisher.Mono;

import java.util.List;

public class Database {
    private static final PostgresqlConnectionFactory factory =
            new PostgresqlConnectionFactory(PostgresqlConnectionConfiguration.builder()
                    .host("localhost")
                    .username("postgres")
                    .password("password")
                    .database("postgres")
                    .build());

    public static void init() {
        Mono.from(factory.create())
                .flatMapMany(connection -> connection
                        .createStatement(
                                "CREATE TABLE IF NOT EXISTS guilds" +
                                "(" +
                                "guildID INTEGER" +
                                ", userID INTEGER" +
                                ", PRIMARY KEY(guildID, userID)" +
                                ")"
                        ).execute()).blockFirst();
    }

    public static List<Snowflake> getChans() {
        return null;
    }

    public static List<Snowflake> getUsersFor(Snowflake channel) {
        return null;
    }

    //boolean represents whether or not the user was already added for that channel
    public static boolean addUserForChan(Snowflake channel) {
        return false;
    }

    //boolean represents whether the user falsely attempted to remove themselves from a channel they aren't associated with
    public static boolean removeUserForChan(Snowflake channel) {
        return false;
    }
}
