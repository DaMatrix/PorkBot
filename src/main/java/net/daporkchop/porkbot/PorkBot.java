/*
 * Adapted from the Wizardry License
 *
 * Copyright (c) 2016-2018 DaPorkchop_
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
import net.daporkchop.porkbot.command.CommandHelp;
import net.daporkchop.porkbot.command.CommandInvite;
import net.daporkchop.porkbot.command.CommandRegistry;
import net.daporkchop.porkbot.command.base.CommandBotInfo;
import net.daporkchop.porkbot.command.base.CommandCommandInfo;
import net.daporkchop.porkbot.command.base.CommandPing;
import net.daporkchop.porkbot.command.base.CommandSay;
import net.daporkchop.porkbot.command.base.CommandTest;
import net.daporkchop.porkbot.command.base.minecraft.CommandMcAvatar;
import net.daporkchop.porkbot.command.base.minecraft.CommandMcCount;
import net.daporkchop.porkbot.command.base.minecraft.CommandMcHead;
import net.daporkchop.porkbot.command.base.minecraft.CommandMcIcon;
import net.daporkchop.porkbot.command.base.minecraft.CommandMcLatency;
import net.daporkchop.porkbot.command.base.minecraft.CommandMcMOTD;
import net.daporkchop.porkbot.command.base.minecraft.CommandMcPing;
import net.daporkchop.porkbot.command.base.minecraft.CommandMcQuery;
import net.daporkchop.porkbot.command.base.minecraft.CommandMcSkin;
import net.daporkchop.porkbot.command.base.minecraft.CommandMcStatus;
import net.daporkchop.porkbot.command.base.minecraft.CommandMcUUID;
import net.daporkchop.porkbot.command.base.minecraft.CommandMcVersion;
import net.daporkchop.porkbot.command.base.minecraft.CommandOfflineUUID;
import net.daporkchop.porkbot.command.base.minecraft.CommandPeCount;
import net.daporkchop.porkbot.command.base.minecraft.CommandPeLatency;
import net.daporkchop.porkbot.command.base.minecraft.CommandPeMOTD;
import net.daporkchop.porkbot.command.base.minecraft.CommandPePing;
import net.daporkchop.porkbot.command.base.minecraft.CommandPeQuery;
import net.daporkchop.porkbot.command.base.minecraft.CommandPeVersion;
import net.daporkchop.porkbot.command.base.minecraft.CommandSkinSteal;
import net.daporkchop.porkbot.command.base.misc.CommandDice;
import net.daporkchop.porkbot.command.base.misc.CommandEmojiID;
import net.daporkchop.porkbot.command.base.misc.CommandInterject;
import net.daporkchop.porkbot.command.base.misc.CommandShutdown;
import net.daporkchop.porkbot.command.base.misc.CommandThonk;
import net.daporkchop.porkbot.command.base.music.CommandPlay;
import net.daporkchop.porkbot.command.base.music.CommandQueue;
import net.daporkchop.porkbot.command.base.music.CommandShuffle;
import net.daporkchop.porkbot.command.base.music.CommandSkip;
import net.daporkchop.porkbot.command.base.music.CommandStop;
import net.daporkchop.porkbot.util.HTTPUtils;
import net.daporkchop.porkbot.util.KeyGetter;
import net.daporkchop.porkbot.util.ShardUtils;
import net.daporkchop.porkbot.util.UUIDFetcher;

import java.io.IOException;
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

        final String authToken = KeyGetter.getAuthtoken();

        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                ShardUtils.forEachShard(jda -> {
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
                });
                CommandRegistry.save();
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
