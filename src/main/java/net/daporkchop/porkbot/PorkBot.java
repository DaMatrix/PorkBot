/*
 * Adapted from the Wizardry License
 *
 * Copyright (c) 2016-2019 DaPorkchop_
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

import net.daporkchop.porkbot.audio.AudioUtils;
import net.daporkchop.porkbot.command.CommandRegistry;
import net.daporkchop.porkbot.command.bot.CommandBotInfo;
import net.daporkchop.porkbot.command.bot.CommandCommandInfo;
import net.daporkchop.porkbot.command.bot.CommandHelp;
import net.daporkchop.porkbot.command.bot.CommandInvite;
import net.daporkchop.porkbot.command.bot.CommandPing;
import net.daporkchop.porkbot.command.bot.CommandSay;
import net.daporkchop.porkbot.command.bot.CommandTest;
import net.daporkchop.porkbot.command.minecraft.CommandMcAvatar;
import net.daporkchop.porkbot.command.minecraft.CommandMcHead;
import net.daporkchop.porkbot.command.minecraft.JavaPEQuery;
import net.daporkchop.porkbot.command.minecraft.CommandMcSkin;
import net.daporkchop.porkbot.command.minecraft.CommandMcStatus;
import net.daporkchop.porkbot.command.minecraft.CommandMcUUID;
import net.daporkchop.porkbot.command.minecraft.CommandOfflineUUID;
import net.daporkchop.porkbot.command.minecraft.CommandSkinSteal;
import net.daporkchop.porkbot.command.minecraft.JavaPing;
import net.daporkchop.porkbot.command.minecraft.PEPing;
import net.daporkchop.porkbot.command.misc.CommandDice;
import net.daporkchop.porkbot.command.misc.CommandEmojiID;
import net.daporkchop.porkbot.command.misc.CommandInterject;
import net.daporkchop.porkbot.command.misc.CommandShutdown;
import net.daporkchop.porkbot.command.misc.CommandThonk;
import net.daporkchop.porkbot.command.music.CommandPlay;
import net.daporkchop.porkbot.command.music.CommandQueue;
import net.daporkchop.porkbot.command.music.CommandShuffle;
import net.daporkchop.porkbot.command.music.CommandSkip;
import net.daporkchop.porkbot.command.music.CommandStop;
import net.daporkchop.porkbot.util.ShardUtils;
import net.daporkchop.porkbot.util.UUIDFetcher;

import java.util.Timer;
import java.util.TimerTask;
import java.util.logging.Logger;

public class PorkBot {
    public static PorkBot INSTANCE;
    public static Logger logger;
    public static Timer timer = new Timer();

    public PorkBot() {
        logger.info("Starting PorkBot...");
        ShardUtils.loadClass();
    }

    public static void main(String[] args) {
        logger = Logger.getLogger("PorkBot");
        INSTANCE = new PorkBot();
        INSTANCE.start();
    }

    public void start() {
        UUIDFetcher.init();
        AudioUtils.init();

        //final String authToken = KeyGetter.getAuthtoken();

        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                /*ShardUtils.forEachShard(jda -> {
                    try {
                        HTTPUtils.performPostRequestWithAuth(HTTPUtils.constantURL("https://bots.discord.pw/api/bots/287894637165936640/stats"),
                                "{" +
                                        "\"server_count\": " + jda.getGuilds().size() + "," +
                                        "\"shard_id\": " + jda.getShardInfo().getShardId() + "," +
                                        "\"shard_count\": " + ShardUtils.getShardCount() +
                                        "}",
                                "application/json",
                                authToken);
                    } catch (IOException e) {
                    }
                });*/
                CommandRegistry.save();
            }
        }, 1000, 120000);

        //bot
        CommandRegistry.registerCommand(new CommandBotInfo());
        CommandRegistry.registerCommand(new CommandCommandInfo());
        CommandRegistry.registerCommand(new CommandHelp());
        CommandRegistry.registerCommand(new CommandInvite());
        CommandRegistry.registerCommand(new CommandPing());
        CommandRegistry.registerCommand(new CommandSay());
        CommandRegistry.registerCommand(new CommandTest());

        //minecraft
        CommandRegistry.registerCommand(new CommandMcAvatar());
        CommandRegistry.registerCommand(new CommandMcHead());
        CommandRegistry.registerCommand(new CommandMcSkin());
        CommandRegistry.registerCommand(new CommandMcStatus());
        CommandRegistry.registerCommand(new CommandMcUUID());
        CommandRegistry.registerCommand(new CommandOfflineUUID());
        CommandRegistry.registerCommand(new CommandSkinSteal());

        CommandRegistry.registerCommand(new JavaPing("mcping", JavaPing.FLAG_ALL));
        CommandRegistry.registerCommand(new JavaPing("mcmotd", JavaPing.FLAG_MOTD));
        CommandRegistry.registerCommand(new JavaPing("mccount", JavaPing.FLAG_COUNT));
        CommandRegistry.registerCommand(new JavaPing("mcversion", JavaPing.FLAG_VERSION));
        CommandRegistry.registerCommand(new JavaPing("mclatency", JavaPing.FLAG_LATENCY));
        CommandRegistry.registerCommand(new JavaPing("mcicon", JavaPing.FLAG_FAVICON));

        CommandRegistry.registerCommand(new PEPing("peping", PEPing.FLAG_ALL));
        CommandRegistry.registerCommand(new PEPing("pemotd", PEPing.FLAG_MOTD));
        CommandRegistry.registerCommand(new PEPing("pecount", PEPing.FLAG_COUNT));
        CommandRegistry.registerCommand(new PEPing("peversion", PEPing.FLAG_VERSION));
        CommandRegistry.registerCommand(new PEPing("pelatency", PEPing.FLAG_LATENCY));

        CommandRegistry.registerCommand(new JavaPEQuery("mcquery", 25565));
        CommandRegistry.registerCommand(new JavaPEQuery("pequery", 19132));

        //misc
        CommandRegistry.registerCommand(new CommandDice());
        CommandRegistry.registerCommand(new CommandEmojiID());
        CommandRegistry.registerCommand(new CommandInterject());
        CommandRegistry.registerCommand(new CommandShutdown());
        CommandRegistry.registerCommand(new CommandThonk());

        //music
        CommandRegistry.registerCommand(new CommandPlay());
        //CommandRegistry.registerCommand(new CommandPlayAll());
        CommandRegistry.registerCommand(new CommandQueue());
        CommandRegistry.registerCommand(new CommandShuffle());
        CommandRegistry.registerCommand(new CommandSkip());
        CommandRegistry.registerCommand(new CommandStop());
    }
}
