/*
 * Adapted from the Wizardry License
 *
 * Copyright (c) 2016-2020 DaPorkchop_
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

import net.daporkchop.lib.common.util.PorkUtil;
import net.daporkchop.porkbot.command.CommandRegistry;
import net.daporkchop.porkbot.command.bot.CommandBotInfo;
import net.daporkchop.porkbot.command.bot.CommandCommandInfo;
import net.daporkchop.porkbot.command.bot.CommandHelp;
import net.daporkchop.porkbot.command.bot.CommandInvite;
import net.daporkchop.porkbot.command.bot.CommandPing;
import net.daporkchop.porkbot.command.bot.CommandSay;
import net.daporkchop.porkbot.command.bot.CommandTest;
import net.daporkchop.porkbot.command.minecraft.CommandMcUUID;
import net.daporkchop.porkbot.command.minecraft.CommandOfflineUUID;
import net.daporkchop.porkbot.command.minecraft.JavaPEQuery;
import net.daporkchop.porkbot.command.minecraft.JavaPing;
import net.daporkchop.porkbot.command.minecraft.PEPing;
import net.daporkchop.porkbot.command.minecraft.SkinCommand;
import net.daporkchop.porkbot.command.misc.CommandDice;
import net.daporkchop.porkbot.command.misc.CommandEmojiID;
import net.daporkchop.porkbot.command.misc.CommandInterject;
import net.daporkchop.porkbot.command.misc.CommandShutdown;
import net.daporkchop.porkbot.util.ShardUtils;
import net.daporkchop.porkbot.util.UUIDFetcher;
import net.daporkchop.porkbot.web.PorkBotWebServer;

import java.util.Timer;
import java.util.TimerTask;

public class PorkBot {
    public static PorkBot INSTANCE;

    public static Timer            TIMER      = new Timer();
    public static PorkBotWebServer WEB_SERVER = new PorkBotWebServer();

    public static void main(String[] args) {
        INSTANCE = new PorkBot();
        INSTANCE.start();
    }

    public PorkBot() {
        ShardUtils.loadClass();
    }

    public void start() {
        UUIDFetcher.init();
        //AudioUtils.init();

        //final String authToken = KeyGetter.getAuthtoken();

        TIMER.schedule(new TimerTask() {
            @Override
            public void run() {
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
        CommandRegistry.registerCommand(new CommandMcUUID());
        CommandRegistry.registerCommand(new CommandOfflineUUID());

        new SkinCommand.SkinApiMethod("face", "/avatars/%s?size=256&default=MHF_Steve&overlay");
        new SkinCommand.SkinApiMethod("head", "/renders/head/%s?scale=10&default=MHF_Steve&overlay");
        new SkinCommand.SkinApiMethod("body", "/renders/body/%s?scale=10&default=MHF_Steve&overlay");
        new SkinCommand.SkinApiMethod("raw", "/skins/%s?default=MHF_Steve");
        CommandRegistry.registerCommand(new SkinCommand("mcavatar", "face"));
        CommandRegistry.registerCommand(new SkinCommand("mchead", "head"));
        CommandRegistry.registerCommand(new SkinCommand("mcskin", "body"));
        CommandRegistry.registerCommand(new SkinCommand("skinsteal", "raw"));

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

        //music
        //CommandRegistry.registerCommand(new CommandPlay());
        //CommandRegistry.registerCommand(new CommandPlayAll());
        //CommandRegistry.registerCommand(new CommandQueue());
        //CommandRegistry.registerCommand(new CommandShuffle());
        //CommandRegistry.registerCommand(new CommandSkip());
        //CommandRegistry.registerCommand(new CommandStop());
    }

    public void shutdown() {
        WEB_SERVER.shutdown();
        CommandRegistry.save();
        ShardUtils.shutdown();
        PorkBot.TIMER.cancel();
        PorkBot.TIMER.purge();
        PorkUtil.sleep(2000L);
        System.exit(0);
    }
}
