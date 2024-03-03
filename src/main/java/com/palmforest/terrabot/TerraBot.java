package com.palmforest.terrabot;

import io.github.cdimascio.dotenv.Dotenv;
import net.dv8tion.jda.api.exceptions.InvalidTokenException;
import net.dv8tion.jda.api.sharding.DefaultShardManagerBuilder;
import net.dv8tion.jda.api.sharding.ShardManager;

import javax.security.auth.login.LoginException;

public class TerraBot
{
    private final Dotenv config;
    private final ShardManager shardManager;

    public TerraBot() throws InvalidTokenException
    {
        config = Dotenv.configure().load();
        String token = config.get("TOKEN");

        DefaultShardManagerBuilder builder = DefaultShardManagerBuilder.createDefault(token);
        shardManager = builder.build();
    }

    public ShardManager getShardManager() {
        return shardManager;
    }
    public Dotenv getConfig() { return config; }


    public static void main(String[] args)
    {
        try {
            TerraBot bot = new TerraBot();
        } catch (InvalidTokenException e) {
            System.out.println("Failed to start bot. The provided bot token is invalid.");
        }
    }
}
