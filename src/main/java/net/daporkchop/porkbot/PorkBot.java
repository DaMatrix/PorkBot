/*
 * Adapted from The MIT License (MIT)
 *
 * Copyright (c) 2016-2020 DaPorkchop_
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation
 * files (the "Software"), to deal in the Software without restriction, including without limitation the rights to use, copy,
 * modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the Software
 * is furnished to do so, subject to the following conditions:
 *
 * Any persons and/or organizations using this software must include the above copyright notice and this permission notice,
 * provide sufficient credit to the original authors of the project (IE: DaPorkchop_), as well as provide a link to the original project.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES
 * OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS
 * BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 *
 */

package net.daporkchop.porkbot;

import io.netty.util.concurrent.DefaultEventExecutor;
import io.netty.util.concurrent.EventExecutorGroup;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import net.daporkchop.lib.common.function.io.IORunnable;
import net.daporkchop.lib.common.util.PorkUtil;
import net.daporkchop.lib.logging.Logging;
import net.daporkchop.porkbot.command.CommandRegistry;
import net.daporkchop.porkbot.command.audio.CommandOrdered;
import net.daporkchop.porkbot.command.audio.CommandPlay;
import net.daporkchop.porkbot.command.audio.CommandPlayList;
import net.daporkchop.porkbot.command.audio.CommandQueue;
import net.daporkchop.porkbot.command.audio.CommandShuffle;
import net.daporkchop.porkbot.command.audio.CommandSkip;
import net.daporkchop.porkbot.command.audio.CommandStop;
import net.daporkchop.porkbot.command.bot.CommandBotInfo;
import net.daporkchop.porkbot.command.bot.CommandCommandInfo;
import net.daporkchop.porkbot.command.bot.CommandHelp;
import net.daporkchop.porkbot.command.bot.CommandInvite;
import net.daporkchop.porkbot.command.bot.CommandPing;
import net.daporkchop.porkbot.command.bot.CommandSay;
import net.daporkchop.porkbot.command.bot.CommandTest;
import net.daporkchop.porkbot.command.mangement.CommandMuteAll;
import net.daporkchop.porkbot.command.misc.CommandNoseTouch;
import net.daporkchop.porkbot.command.minecraft.CommandMcUUID;
import net.daporkchop.porkbot.command.minecraft.CommandOfflineUUID;
import net.daporkchop.porkbot.command.minecraft.JavaPEQuery;
import net.daporkchop.porkbot.command.minecraft.JavaPing;
import net.daporkchop.porkbot.command.minecraft.PEPing;
import net.daporkchop.porkbot.command.minecraft.SkinCommand;
import net.daporkchop.porkbot.command.misc.CommandDice;
import net.daporkchop.porkbot.command.misc.CommandInterject;
import net.daporkchop.porkbot.command.misc.CommandSelectRandom;
import net.daporkchop.porkbot.command.misc.CommandShutdown;
import net.daporkchop.porkbot.util.Config;
import net.daporkchop.porkbot.util.ShardUtils;
import net.daporkchop.porkbot.util.UUIDFetcher;

import java.util.concurrent.TimeUnit;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class PorkBot {
    public static final PorkBot INSTANCE = new PorkBot();

    public static final EventExecutorGroup SCHEDULED_EXECUTOR = new DefaultEventExecutor();

    public static void main(String[] args) {
        INSTANCE.start();
    }

    public void start() {
        UUIDFetcher.init();

        SCHEDULED_EXECUTOR.scheduleAtFixedRate((IORunnable) Config::save, 1L, 1L, TimeUnit.HOURS);

        //general
        CommandRegistry.registerCommand(new CommandBotInfo());
        CommandRegistry.registerCommand(new CommandCommandInfo());
        CommandRegistry.registerCommand(new CommandHelp());
        CommandRegistry.registerCommand(new CommandInvite());
        CommandRegistry.registerCommand(new CommandPing());
        CommandRegistry.registerCommand(new CommandSay());
        CommandRegistry.registerCommand(new CommandTest());

        //minecraft
        CommandRegistry.registerCommand(new CommandMcUUID());
        CommandRegistry.registerCommand(new CommandOfflineUUID());

        CommandRegistry.registerCommand(new SkinCommand("mcavatar", SkinCommand.Type.FACE));
        CommandRegistry.registerCommand(new SkinCommand("mchead", SkinCommand.Type.HEAD));
        CommandRegistry.registerCommand(new SkinCommand("mcskin", SkinCommand.Type.BODY));
        CommandRegistry.registerCommand(new SkinCommand("skinsteal", SkinCommand.Type.RAW));

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
        CommandRegistry.registerCommand(new CommandInterject());
        CommandRegistry.registerCommand(new CommandNoseTouch());
        CommandRegistry.registerCommand(new CommandSelectRandom());
        CommandRegistry.registerCommand(new CommandShutdown());

        //management
        CommandRegistry.registerCommand(new CommandMuteAll("muteall", "Muted", true));
        CommandRegistry.registerCommand(new CommandMuteAll("unmuteall", "Un-muted", false));

        //audio
        CommandRegistry.registerCommand(new CommandOrdered());
        CommandRegistry.registerCommand(new CommandPlay());
        CommandRegistry.registerCommand(new CommandPlayList());
        CommandRegistry.registerCommand(new CommandQueue());
        CommandRegistry.registerCommand(new CommandShuffle());
        CommandRegistry.registerCommand(new CommandSkip());
        CommandRegistry.registerCommand(new CommandStop());
        //CommandRegistry.registerCommand(new CommandPlayAll());
        //CommandRegistry.registerCommand(new CommandQueue());
        //CommandRegistry.registerCommand(new CommandStop());

        //load state
        Config.load();
        //actually start bot
        ShardUtils.start();
    }

    public void shutdown() {
        Logging.logger.info("Shutting down shard manager...");
        ShardUtils.shutdown();
        Logging.logger.info("Shutting down scheduled task executor...");
        SCHEDULED_EXECUTOR.shutdownGracefully().syncUninterruptibly();
        Logging.logger.info("Saving command data...");
        Config.save();

        Logging.logger.info("Exiting...");
        PorkUtil.sleep(1000L);
        System.exit(0);
    }
}
