package net.daporkchop.porkbot;

import com.google.common.cache.CacheBuilder;
import net.daporkchop.porkbot.command.CommandHelp;
import net.daporkchop.porkbot.command.CommandInvite;
import net.daporkchop.porkbot.command.CommandRegistry;
import net.daporkchop.porkbot.command.base.CommandBotInfo;
import net.daporkchop.porkbot.command.base.CommandPing;
import net.daporkchop.porkbot.command.base.CommandSay;
import net.daporkchop.porkbot.command.base.CommandTest;
import net.daporkchop.porkbot.command.base.minecraft.*;
import net.daporkchop.porkbot.util.HTTPUtils;
import net.daporkchop.porkbot.util.UUIDFetcher;
import net.dv8tion.jda.core.*;
import net.dv8tion.jda.core.entities.Game;
import net.dv8tion.jda.core.entities.TextChannel;
import net.dv8tion.jda.core.entities.impl.GameImpl;
import net.dv8tion.jda.core.events.message.MessageReceivedEvent;
import net.dv8tion.jda.core.exceptions.PermissionException;
import net.dv8tion.jda.core.exceptions.RateLimitedException;

import javax.security.auth.login.LoginException;
import java.io.*;
import java.net.URL;
import java.util.Scanner;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;

/**
 * Created by daporkchop on 05.03.17.
 */
public class PorkBot {

    public static PorkBot INSTANCE;
    public static Logger logger;

    public JDA jda;

    /**
     * The bot's main cache!
     */
    public ConcurrentMap<Object, Object> botCache = CacheBuilder.newBuilder()
            .maximumSize(5000)
            .expireAfterWrite(30, TimeUnit.MINUTES)
            .build().asMap();

    public PorkBot() {
        logger.info("Starting PorkBot...");
        try {
            jda = new JDABuilder(AccountType.BOT)
                    .setToken(getToken())
                    .addEventListener(new PorkListener())
                    .buildBlocking();
        } catch (LoginException e) {
            e.printStackTrace();
            System.exit(0);
        } catch (InterruptedException e) {
            e.printStackTrace();
            System.exit(0);
        } catch (RateLimitedException e) {
            e.printStackTrace();
            System.exit(0);
        }
    }

    public static void main(String[] args) {
        logger = Logger.getLogger("PorkBot");
        INSTANCE = new PorkBot();
        INSTANCE.start();
    }

    public static String getToken() {
        File f = new File(System.getProperty("user.dir") + "/discordtoken.txt");
        String token = "";

        if (!f.exists()) {
            try {
                PrintWriter writer = new PrintWriter(f.getAbsolutePath(), "UTF-8");
                Scanner s = new Scanner(System.in);

                logger.info("Please enter your discord bot token");
                token = s.nextLine();
                writer.println(token);
                logger.info("Successful. Starting...");

                s.close();
                writer.close();
            } catch (FileNotFoundException e) {
                logger.severe("impossible error kek");
                e.printStackTrace();
                System.exit(0);
            } catch (UnsupportedEncodingException e) {
                logger.severe("File encoding not supported!");
                e.printStackTrace();
                System.exit(0);
            }
        } else {
            try {
                Scanner s = new Scanner(f);

                token = s.nextLine();

                s.close();
            } catch (FileNotFoundException e) {
                logger.severe("impossible error kek");
                e.printStackTrace();
                System.exit(0);
            }
        }

        return token.trim();
    }

    /**
     * Sends a message to a channel
     *
     * @param s
     * @param channel
     */
    public static void sendMessage(String s, TextChannel channel) {
        try {
            channel.sendMessage(s).queue();
        } catch (PermissionException e) {
            //we can't do anything about it
        }
    }

    /**
     * Sends a message to a channel
     *
     * @param builder
     * @param channel
     */
    public static void sendMessage(EmbedBuilder builder, TextChannel channel) {
        try {
            channel.sendMessage(builder.build()).queue();
        } catch (PermissionException e) {
            //we can't do anything about it
        }
    }

    /**
     * Sends an image with the embed
     *
     * @param builder
     * @param image
     * @param name
     * @param channel
     */
    public static void sendImage(EmbedBuilder builder, byte[] image, String name, TextChannel channel) {
        try {
            channel.sendFile(image, name, new MessageBuilder().setEmbed(builder.build()).build()).queue();
        } catch (PermissionException e) {
            //we can't do anything about it
        }
    }

    /**
     * Sends an excepion using the given MessageReceivedEvent's channel
     *
     * @param e   the exception to print
     * @param evt the channel from this event is used to send the message
     */
    public static void sendException(Exception e, MessageReceivedEvent evt) {
        e.printStackTrace();
        PorkBot.sendMessage("Error running command: `" + evt.getMessage().getRawContent() + "`:\n`" + e.getClass().getCanonicalName() + "`", evt.getTextChannel());
    }

    /**
     * Downloads an image
     *
     * @param address
     * @return
     */
    public static byte[] downloadImage(String address) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        InputStream is = null;
        URL url = null;
        try {
            url = new URL(address);
            is = url.openStream();
            byte[] byteChunk = new byte[4096]; // Or whatever size you want to read in at a time.
            int n;

            while ((n = is.read(byteChunk)) > 0) {
                baos.write(byteChunk, 0, n);
            }
        } catch (IOException e) {
            System.err.printf("Failed while reading bytes from %s: %s", url.toExternalForm(), e.getMessage());
            e.printStackTrace();
            // Perform any other exception handling that's appropriate.
        } finally {
            try {
                if (is != null) {
                    is.close();
                }
            } catch (IOException e) {

            }
        }

        return baos.toByteArray();
    }

    public static String getAuthtoken() {
        File f = new File(System.getProperty("user.dir") + "/authtoken.txt");
        String token = "";

        if (!f.exists()) {
            try {
                PrintWriter writer = new PrintWriter(f.getAbsolutePath(), "UTF-8");
                Scanner s = new Scanner(System.in);

                logger.info("Please enter your auth token for bots.discord.pw");
                token = s.nextLine();
                writer.println(token);
                logger.info("Successful. connecting...");

                s.close();
                writer.close();
            } catch (FileNotFoundException e) {
                logger.severe("impossible error kek");
                e.printStackTrace();
                System.exit(0);
            } catch (UnsupportedEncodingException e) {
                logger.severe("File encoding not supported!");
                e.printStackTrace();
                System.exit(0);
            }
        } else {
            try {
                Scanner s = new Scanner(f);

                token = s.nextLine();

                s.close();
            } catch (FileNotFoundException e) {
                logger.severe("impossible error kek");
                e.printStackTrace();
                System.exit(0);
            }
        }

        return token.trim();
    }

    public void start() {
        jda.getPresence().setGame(new GameImpl("Say ..help", "https://www.twitch.tv/daporkchop_", Game.GameType.TWITCH));

        UUIDFetcher.init();

        CommandRegistry.registerCommand(new CommandHelp());
        CommandRegistry.registerCommand(new CommandInvite());
        CommandRegistry.registerCommand(new CommandMcUUID());
        CommandRegistry.registerCommand(new CommandSay());
        CommandRegistry.registerCommand(new CommandMcPing());
        CommandRegistry.registerCommand(new CommandPeQuery());
        CommandRegistry.registerCommand(new CommandMcSkin());
        CommandRegistry.registerCommand(new CommandMcAvatar());
        CommandRegistry.registerCommand(new CommandMcHead());
        CommandRegistry.registerCommand(new CommandMcStatus());
        CommandRegistry.registerCommand(new CommandPing());
        CommandRegistry.registerCommand(new CommandMcMOTD());
        CommandRegistry.registerCommand(new CommandMcCount());
        CommandRegistry.registerCommand(new CommandMcIcon());
        CommandRegistry.registerCommand(new CommandMcQuery());
        CommandRegistry.registerCommand(new CommandPePing());
        CommandRegistry.registerCommand(new CommandOfflineUUID());
        CommandRegistry.registerCommand(new CommandBotInfo());
        CommandRegistry.registerCommand(new CommandTest());
        CommandRegistry.registerCommand(new CommandMcLatency());
        CommandRegistry.registerCommand(new CommandMcVersion());
        CommandRegistry.registerCommand(new CommandPeCount());
        CommandRegistry.registerCommand(new CommandPeLatency());
        CommandRegistry.registerCommand(new CommandPeMOTD());
        CommandRegistry.registerCommand(new CommandPeVersion());

        final String authToken = getAuthtoken();

        new Timer().schedule(new TimerTask() {
            @Override
            public void run() {
                try {
                    HTTPUtils.performPostRequestWithAuth(HTTPUtils.constantURL("https://bots.discord.pw/api/bots/287894637165936640/stats"),
                            "{ \"server_count\": " + PorkBot.INSTANCE.jda.getGuilds().size() + " }",
                            "application/json",
                            authToken);
                } catch (Exception e) {
                }
            }
        }, 1000, 120000);

        /*jda.getUserById("226975061880471552").openPrivateChannel().queue((channel) -> {
                    channel.sendMessage("Started!").queue();
                }
        );*/

        while (true) { //don't remember why i put this here, but there's got to be a reason :P
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {

            }
        }
    }
}
