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

package net.daporkchop.porkbot.util;

import net.daporkchop.porkbot.PorkBot;
import net.daporkchop.porkbot.PorkListener;
import net.daporkchop.porkbot.command.CommandRegistry;
import net.dv8tion.jda.bot.sharding.DefaultShardManagerBuilder;
import net.dv8tion.jda.bot.sharding.ShardManager;
import net.dv8tion.jda.core.JDA;
import net.dv8tion.jda.core.entities.Game;
import net.dv8tion.jda.core.entities.Guild;
import net.dv8tion.jda.core.managers.AudioManager;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;

public class ShardUtils {
    private static ShardManager manager;

    static {
        System.out.println("Starting shards...");
        try {
            DefaultShardManagerBuilder builder = new DefaultShardManagerBuilder();
            builder.setToken(KeyGetter.getToken());
            builder.setShardsTotal(-1);
            builder.addEventListeners(new PorkListener());
            builder.setGame(Game.of(Game.GameType.STREAMING, "Say ..help", "https://www.twitch.tv/daporkchop_"));
            manager = builder.build();
        } catch (Throwable t) {
            t.printStackTrace();
            System.exit(1);
        }
        System.out.println("Shards started!");
    }

    public static void loadClass() {
    }

    public static void forEachGuild(Consumer<Guild> consumer) {
        if (consumer == null) return;
        manager.getGuilds().forEach(consumer);
    }

    public static int getGuildCount() {
        return (int) manager.getGuildCache().size();
    }

    public static int getUserCount() {
        AtomicInteger i = new AtomicInteger(0);
        manager.getShards().forEach(jda -> {
            i.addAndGet(jda.getUsers().size());
        });
        return i.get();
    }

    public static void forEachShard(Consumer<JDA> consumer) {
        if (consumer == null) return;
        manager.getShards().forEach(consumer);
    }

    public static int getShardCount() {
        return manager.getShardsTotal();
    }

    public static void shutdown() {
        CommandRegistry.save();
        manager.shutdown();
        PorkBot.TIMER.cancel();
        PorkBot.TIMER.purge();
        try {
            Thread.sleep(5000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        } finally {
            System.exit(0);
        }
    }

    /**
     * WARNING: DO NOT USE FREQUENTLY!
     */
    public static List<AudioManager> getConnectedVoice() {
        List<AudioManager> list = new ArrayList<>();
        manager.getShards().forEach(jda -> {
            for (AudioManager manager : jda.getAudioManagers()) {
                if (manager.isConnected()) {
                    list.add(manager);
                }
            }
        });
        return list;
    }
}
