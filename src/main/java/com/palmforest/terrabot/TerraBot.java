package com.palmforest.terrabot;

import net.dv8tion.jda.api.sharding.DefaultShardManagerBuilder;
import net.dv8tion.jda.api.sharding.ShardManager;

import javax.security.auth.login.LoginException;

public class TerraBot
{
    private final ShardManager shardManager;

    public TerraBot() throws LoginException
    {
        String token = "MTE0MjMzMzc0OTIxNjk0NDIwOA.G1A-_J.4ffw6W98HFC0pwn-jMiT_g-paky7TN4UlhfFdM";
        DefaultShardManagerBuilder builder = DefaultShardManagerBuilder.createDefault(token);
        shardManager = builder.build();
    }

    public ShardManager getShardManager() {
        return shardManager;
    }


    public static void main(String[] args)
    {
        try {
            TerraBot bot = new TerraBot();
        } catch (LoginException e) {
            System.out.println("Failed to start bot. The bot token provided is invalid.");
        }
    }
}
