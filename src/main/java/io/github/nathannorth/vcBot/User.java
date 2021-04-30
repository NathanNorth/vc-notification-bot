package io.github.nathannorth.vcBot;

import discord4j.common.util.Snowflake;

public class User {
    public final Snowflake userID;
    public final Snowflake messageID;

    public User(Long userID, Long messageID) {
        this.userID = Snowflake.of(userID);
        if(messageID == 0) this.messageID = null;
        else this.messageID = Snowflake.of(messageID);
    }
}
