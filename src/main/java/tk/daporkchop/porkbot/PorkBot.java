package tk.daporkchop.porkbot;

import com.google.common.cache.CacheBuilder;
import net.dv8tion.jda.core.AccountType;
import net.dv8tion.jda.core.JDA;
import net.dv8tion.jda.core.JDABuilder;
import net.dv8tion.jda.core.entities.Game;
import net.dv8tion.jda.core.entities.impl.GameImpl;
import net.dv8tion.jda.core.exceptions.RateLimitedException;
import tk.daporkchop.porkbot.command.CommandRegistry;
import tk.daporkchop.porkbot.command.base.CommandPing;
import tk.daporkchop.porkbot.command.base.minecraft.CommandMcAvatar;
import tk.daporkchop.porkbot.command.base.minecraft.CommandMcHead;
import tk.daporkchop.porkbot.command.base.minecraft.CommandMcPing;
import tk.daporkchop.porkbot.command.base.minecraft.CommandMcSkin;
import tk.daporkchop.porkbot.command.base.minecraft.CommandMcStatus;
import tk.daporkchop.porkbot.command.base.minecraft.CommandMcUUID;
import tk.daporkchop.porkbot.command.base.CommandSay;
import tk.daporkchop.porkbot.command.base.minecraft.CommandMcpePing;
import tk.daporkchop.porkbot.command.CommandHelp;
import tk.daporkchop.porkbot.command.CommandInvite;

import javax.security.auth.login.LoginException;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.util.Scanner;
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

    public PorkBot()    {
        logger.info("Starting PorkBot...");
        try {
            jda = new JDABuilder(AccountType.BOT)
                    .setToken(getToken())
                    .addListener(new PorkListener())
                    .buildBlocking();
        } catch (LoginException e)  {
            e.printStackTrace();
            System.exit(0);
        } catch (InterruptedException e)    {
            e.printStackTrace();
            System.exit(0);
        } catch (RateLimitedException e)    {
            e.printStackTrace();
            System.exit(0);
        }
    }

    public static void main(String[] args)  {
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

    public void start() {
        jda.getPresence().setGame(new GameImpl("Say ..help", "https://www.twitch.tv/daporkchop_", Game.GameType.TWITCH));
        
        CommandRegistry.registerCommand(new CommandHelp());
        CommandRegistry.registerCommand(new CommandInvite());
        CommandRegistry.registerCommand(new CommandMcUUID());
        CommandRegistry.registerCommand(new CommandSay());
        CommandRegistry.registerCommand(new CommandMcPing());
        CommandRegistry.registerCommand(new CommandMcpePing());
        CommandRegistry.registerCommand(new CommandMcSkin());
        CommandRegistry.registerCommand(new CommandMcAvatar());
        CommandRegistry.registerCommand(new CommandMcHead());
        CommandRegistry.registerCommand(new CommandMcStatus());
        CommandRegistry.registerCommand(new CommandPing());
        
        while (true)    {
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e)    {

            }
        }
    }
}