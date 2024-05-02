package com.palmforest.terrabot;

import io.github.cdimascio.dotenv.Dotenv;
import me.shib.java.lib.diction.DictionService;
import me.shib.java.lib.diction.DictionWord;
import net.didion.jwnl.data.Exc;
import net.didion.jwnl.data.Word;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.entities.emoji.Emoji;
import net.dv8tion.jda.api.events.Event;
import net.dv8tion.jda.api.events.guild.GuildJoinEvent;
import net.dv8tion.jda.api.events.guild.GuildReadyEvent;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.events.message.react.MessageReactionAddEvent;
import net.dv8tion.jda.api.events.message.react.MessageReactionRemoveEvent;
import net.dv8tion.jda.api.events.session.ReadyEvent;
import net.dv8tion.jda.api.exceptions.InvalidTokenException;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import net.dv8tion.jda.api.requests.GatewayIntent;
import net.dv8tion.jda.api.sharding.DefaultShardManagerBuilder;
import net.dv8tion.jda.api.sharding.ShardManager;
import net.dv8tion.jda.api.utils.ChunkingFilter;
import net.dv8tion.jda.api.utils.MemberCachePolicy;
import org.jetbrains.annotations.NotNull;
import rita.wordnet.Wordnet;
import rita.wordnet.WordnetDictionaryFile;

import javax.security.auth.login.LoginException;
import java.awt.*;
import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class TerraBot extends ListenerAdapter
{
    enum Process {
        Wordle,
        Terradle
    }

    List<Process> ActiveProcesses = new ArrayList<>();

    String wordleAnswersPath = "D:/Projects/Discord Bots/TerraBot/target/classes/wordle-answers.txt";
    String wordleValidPath = "D:/Projects/Discord Bots/TerraBot/target/classes/valid-wordle-words.txt";
    List<String> WordleAnswers = new ArrayList<>();
    List<String> WordleValid = new ArrayList<>();

    String currentWordleWord = "";
    DictionService dictionary = new DictionService();
    int guessCount = 0;


    List<String> WordList = new ArrayList<>();
    String wordListPath = "D:/Projects/Discord Bots/TerraBot/target/classes/words_alpha.txt";


    Color EmbedColor = new Color(10, 100, 10);

    private static Dotenv config;
    private static ShardManager shardManager;


    public ShardManager getShardManager() {
        return shardManager;
    }
    public Dotenv getConfig() { return config; }

    Random random = new Random();

    public static void main(String[] args)
    {
        config = Dotenv.configure().load();
        String token = config.get("TOKEN");

        DefaultShardManagerBuilder builder = DefaultShardManagerBuilder.createDefault(token);

        //Cache
        //builder.setMemberCachePolicy(MemberCachePolicy.ONLINE);
        //builder.setChunkingFilter(ChunkingFilter.ALL);
        //builder.enableCache(CacheFlag.);

        //Register Intents
        builder.enableIntents(GatewayIntent.MESSAGE_CONTENT);

        shardManager = builder.build();

        //Register Listeners
        shardManager.addEventListener(/*new EventListener(), new CommandManager(), */new TerraBot());
    }


    @Override
    public void onMessageReceived(MessageReceivedEvent event) {
        if(event.getAuthor().isBot())
            return;

        Message message = event.getMessage();

        if(ActiveProcesses.contains(Process.Wordle))
        {
            String input = message.getContentRaw().toLowerCase();
            if(message.getContentRaw().length() ==  5 && WordleValid.contains(input))
            {
                guessCount++;

                HashMap<Integer, String> characterNames = new HashMap<>();
                HashMap<Integer, String> characterChars = new HashMap<>();

                char[] guessAsCharArray = input.toCharArray();
                char[] currentWordAsCharArray = currentWordleWord.toLowerCase().toCharArray();

                // Checking if letters are in the right spot or not
                for(int i = 0; i < 5; i++)
                {
                    characterChars.put(i, String.valueOf(currentWordAsCharArray[i]));

                    if(guessAsCharArray[i] == currentWordAsCharArray[i])
                    {
                        characterChars.put(i, ":green_square:");
                    }
                    else
                    {
                        characterChars.put(i, ":black_large_square:");
                    }
                }

                //Checking if the word contains it
                for(int i = 0; i < 5; i++)
                {
                    if(!characterChars.get(i).equalsIgnoreCase(":green_square:"))
                    {
                        for(int j = 0; j < 5; j++)
                        {
                            if(guessAsCharArray[i] == currentWordAsCharArray[j])
                            {
                                if(!characterChars.get(j).equalsIgnoreCase(":green_square:"))
                                {
                                    characterChars.put(i, ":yellow_square:");
                                    break;
                                }
                            }
                        }
                    }
                }

                // OUTPUT
                List<String> line1 = new ArrayList<>();
                List<String> line2 = new ArrayList<>();
                for(int i = 0; i < 5; i++)
                {
                    line1.add(":regional_indicator_" + guessAsCharArray[i] + ":");
                    line2.add(characterChars.get(i));
                }

                EmbedBuilder embed = new EmbedBuilder();
                embed.setColor(EmbedColor);
                //embed.setTitle(String.join("", line1));
                //embed.setDescription(String.join("", line2));
                embed.setTitle("Guess #" + guessCount);
                embed.addField("————————", String.join("", line1) + "\n" + String.join("", line2), true);

                //message.replyEmbeds(embed.build()).queue();

                //message.reply(String.join("", line1) + "\n" + String.join("", line2)).queue();


                // WIN CONDITION
                boolean showVictoryEmbed = false;
                EmbedBuilder victoryEmbed = new EmbedBuilder();
                victoryEmbed.setColor(EmbedColor);
                if(message.getContentDisplay().equalsIgnoreCase(currentWordleWord))
                {
                    if(guessCount < 2)
                    {
                        //message.reply("**Congratulations!\nYou guessed the word in *" + guessCount + "* guess.\nThe word was: *" + currentWordleWord + "***").queue();
                        victoryEmbed.setTitle("Congratulations!\nYou guessed the word in **" + guessCount + "** guess.\nThe word was: **" + currentWordleWord + "**");
                    }
                    else
                    {
                        //message.reply("**Congratulations!\nYou guessed the word in *" + guessCount + "* guesses.\nThe word was: *" + currentWordleWord + "***").queue();
                        victoryEmbed.setTitle("Congratulations!\nYou guessed the word in **" + guessCount + "** guesses.\nThe word was: **" + currentWordleWord + "**");
                    }

                    ActiveProcesses.remove(Process.Wordle);

                    showVictoryEmbed = true;
                }

                if(showVictoryEmbed)
                    message.replyEmbeds(embed.build(), victoryEmbed.build()).queue();
                else
                    message.replyEmbeds(embed.build()).queue();
            }
        }
    }


    @Override
    public void onSlashCommandInteraction(@NotNull SlashCommandInteractionEvent event) {
        String command = event.getName();

        switch (command){
            case "wordle" -> {
                OptionMapping option = event.getOption("action");

                EmbedBuilder startEmbed = new EmbedBuilder();
                startEmbed.setColor(EmbedColor);
                startEmbed.setTitle("New Wordle game started by " + event.getUser().getEffectiveName());
                startEmbed.addField("**———————————————————————————————**", "Try to guess the 5 letter word in as little guesses as you can!", false);

                EmbedBuilder endEmbed = new EmbedBuilder();
                endEmbed.setColor(EmbedColor);
                endEmbed.setTitle("Wordle game ended by " + event.getUser().getEffectiveName());
                endEmbed.addField("**———————————————————**", "The word was **" + currentWordleWord + "**.", false);

                if(option != null)
                {
                    switch (option.getAsString())
                    {
                        case "stop" -> {
                            if(ActiveProcesses.contains(Process.Wordle))
                            {
                                ActiveProcesses.remove(Process.Wordle);

                                event.replyEmbeds(endEmbed.build()).queue();
                            }
                        }
                        default -> {
                            if(!ActiveProcesses.contains(Process.Wordle))
                                ActiveProcesses.add(Process.Wordle);

                            StartWordleGame();

                            event.replyEmbeds(startEmbed.build()).queue();
                        }
                    }
                }
                else
                {
                    if(ActiveProcesses.contains(Process.Wordle))
                    {
                        event.replyEmbeds(endEmbed.build()).queue();
                        ActiveProcesses.remove(Process.Wordle);
                    }
                    else
                    {
                        StartWordleGame();

                        event.replyEmbeds(startEmbed.build()).queue();
                        ActiveProcesses.add(Process.Wordle);
                    }
                }
            }
            case "randomword" -> {
                EmbedBuilder rndWordEmbed = new EmbedBuilder();
                rndWordEmbed.setColor(EmbedColor);
                rndWordEmbed.setTitle("Your random word is: **" + GenerateRandomWord() + "**");

                event.replyEmbeds(rndWordEmbed.build()).queue();
            }
            case "definition" -> {
                OptionMapping option = event.getOption("word");

                if(option == null)
                    break;

                EmbedBuilder defEmbed = new EmbedBuilder();
                defEmbed.setColor(EmbedColor);


                List<DictionWord.DictionDesc> definitions = dictionary.getDictionWord(option.getAsString()).getDescriptions();
                if(dictionary.getDictionWord(option.getAsString()) == null || definitions.isEmpty())
                {
                    EmbedBuilder noDefEmbed = new EmbedBuilder();
                    noDefEmbed.setColor(EmbedColor);
                    noDefEmbed.setTitle("No definitions available for '" + option.getAsString() + "'");

                    event.replyEmbeds(noDefEmbed.build()).queue();
                    break;
                }

                List<String> output = new ArrayList<>();

                for(DictionWord.DictionDesc definition : definitions)
                {
                    output.add("- " + definition.getDescription());
                }

                defEmbed.addField("Definitions for **'" + option.getAsString() + "'**", String.join("\n", output), false);
                event.replyEmbeds(defEmbed.build()).queue();
            }
        }
    }


    @Override
    public void onReady(@NotNull ReadyEvent event) {

        // Load the word lists from file path
        try {
            WordleAnswers = LoadWordList(wordleAnswersPath);
            WordleValid = LoadWordList(wordleValidPath);

            WordList = LoadWordList(wordListPath);
        }
        catch (IOException e)
        {
            System.out.println("Holy shit something went wrong");
        }


        // Load Global Commands
        List<CommandData> commandData = new ArrayList<>();

        OptionData wordleOption = new OptionData(OptionType.STRING, "action", "Start or Stop the game", false)
                .addChoice("New Game", "start")
                .addChoice("End Game", "stop");
        commandData.add(Commands.slash("wordle", "Start a new Wordle game or ends a running one")
                .addOptions(wordleOption));

        commandData.add(Commands.slash("randomword", "Generates a random word"));

        commandData.add(Commands.slash("definition", "Gives the definition of a word if available.")
                .addOptions(new OptionData(OptionType.STRING, "word", "Word to provide definition for")));


        event.getJDA().updateCommands().addCommands(commandData).queue();
    }


    void StartWordleGame()
    {
        currentWordleWord = GenerateWordleAnswer();
        guessCount = 0;
    }


    public List<String> LoadWordList(String path) throws IOException
    {
        File file = new File(path);
        try (Stream<String> lines = Files.lines(Paths.get(path)))
        {
            return lines.collect(Collectors.toList());
        }
    }

    public String GenerateWordleAnswer()
    {

        String word = WordleAnswers.get(random.nextInt(WordleAnswers.size()));
        System.out.println("Generated wordle word: " + word);

        return word;
    }

    public String GenerateRandomWord()
    {
        String word = WordList.get(random.nextInt(WordList.size()));
        System.out.println("Generated random word: " + word);

        return word;
    }
}
