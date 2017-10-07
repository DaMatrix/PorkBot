/*
 * Adapted from the Wizardry License
 *
 * Copyright (c) 2016 DaPorkchop_
 *
 * Permission is hereby granted to any persons and/or organizations using this software to copy, modify, merge, publish, and distribute it.
 * Said persons and/or organizations are not allowed to use the software or any derivatives of the work for commercial use or any other means to generate income, nor are they allowed to claim this software as their own.
 *
 * The persons and/or organizations are also disallowed from sub-licensing and/or trademarking this software without explicit permission from DaPorkchop_.
 *
 * Any persons and/or organizations using this software must disclose their source code and have it publicly available, include this license, provide sufficient credit to the original authors of the project (IE: DaPorkchop_), as well as provide a link to the original project.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NON INFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 *
 */

package net.daporkchop.porkbot;

import net.daporkchop.porkbot.command.CommandHelp;
import net.daporkchop.porkbot.command.CommandInvite;
import net.daporkchop.porkbot.command.CommandRegistry;
import net.daporkchop.porkbot.command.base.*;
import net.daporkchop.porkbot.command.base.minecraft.*;
import net.daporkchop.porkbot.command.base.misc.*;
import net.daporkchop.porkbot.command.base.music.*;
import net.daporkchop.porkbot.util.*;
import net.dv8tion.jda.core.AccountType;
import net.dv8tion.jda.core.JDA;
import net.dv8tion.jda.core.JDABuilder;
import net.dv8tion.jda.core.entities.Game;
import net.dv8tion.jda.core.exceptions.RateLimitedException;

import javax.security.auth.login.LoginException;
import java.util.ArrayList;
import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;
import java.util.logging.Logger;

public class PorkBot {

    //REEEEEEE
    public
    static
    final
    int
            shardCount
            =
            2;

    public static PorkBot INSTANCE;
    public static Logger logger;
    /**
     * A central Random instance, for random things
     * literally
     * kek
     * eks dee
     */
    public static Random random = new Random(System.currentTimeMillis());
    public static Timer timer = new Timer();
    public ArrayList<JDA> shards = new ArrayList<>();

    public PorkBot() {
        logger.info("Starting PorkBot...");
        try {
            for (int i = 0; i < shardCount; i++) {
                JDA newlyCreated;
                shards.add(newlyCreated = new JDABuilder(AccountType.BOT)
                        .useSharding(i, shardCount)
                        .setToken(KeyGetter.getToken())
                        .buildBlocking());
                newlyCreated.addEventListener(new PorkListener(newlyCreated));
                System.out.println("Started shard " + (i + 1) + " out of " + shardCount);
                Thread.sleep(5000); //rate limiting
            }
        } catch (LoginException | InterruptedException | RateLimitedException e) {
            e.printStackTrace();
            System.exit(0);
        }
    }

    public static void main(String[] args) {
        logger = Logger.getLogger("PorkBot");
        INSTANCE = new PorkBot();
        INSTANCE.start();
    }

    public void start() {
        ShardUtils.setShards(shards);
        ShardUtils.setGame(Game.of("Say ..help", "https://www.twitch.tv/daporkchop_"));

        UUIDFetcher.init();
        AudioUtils.init();

        final String authToken = KeyGetter.getAuthtoken();

        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                try {
                    for (JDA jda : shards) {
                        HTTPUtils.performPostRequestWithAuth(HTTPUtils.constantURL("https://bots.discord.pw/api/bots/287894637165936640/stats"),
                                "{" +
                                        "\"server_count\": " + jda.getGuilds().size() + "," +
                                        "\"shard_id\": " + jda.getShardInfo().getShardId() + "," +
                                        "\"shard_count\": " + shardCount +
                                        "}",
                                "application/json",
                                authToken);
                    }
                } catch (Exception e) {
                }
            }
        }, 1000, 120000);

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
        CommandRegistry.registerCommand(new CommandDice());
        CommandRegistry.registerCommand(new CommandEmojiID());
        CommandRegistry.registerCommand(new CommandThonk());
        CommandRegistry.registerCommand(new CommandInterject());
        CommandRegistry.registerCommand(new CommandCommandInfo());
        CommandRegistry.registerCommand(new CommandPlay());
        CommandRegistry.registerCommand(new CommandShuffle());
        CommandRegistry.registerCommand(new CommandQueue());
        CommandRegistry.registerCommand(new CommandStop());
        CommandRegistry.registerCommand(new CommandSkip());
        CommandRegistry.registerCommand(new CommandSkinSteal());
        CommandRegistry.registerCommand(new CommandShutdown());
    }
}
