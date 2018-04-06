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

package net.daporkchop.porkbot.util;

import net.daporkchop.porkbot.PorkListener;
import net.daporkchop.porkbot.command.CommandRegistry;
import net.dv8tion.jda.bot.sharding.DefaultShardManagerBuilder;
import net.dv8tion.jda.bot.sharding.ShardManager;
import net.dv8tion.jda.core.JDA;
import net.dv8tion.jda.core.entities.Game;
import net.dv8tion.jda.core.entities.Guild;
import net.dv8tion.jda.core.managers.AudioManager;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Consumer;

public class ShardUtils {
    public static volatile int guildCount = 0;
    private static Set<JDA> shards = new HashSet<>();
    private static ShardManager manager;

    static {
        System.out.println("Starting shards...");
        try {
            DefaultShardManagerBuilder builder = new DefaultShardManagerBuilder();
            builder.setToken(KeyGetter.getToken());
            builder.setShardsTotal(-1);
            manager = builder.build();
            shards.addAll(manager.getShards());
            shards.forEach(jda -> jda.addEventListener(new PorkListener(jda)));
            manager.setGame(Game.of(Game.GameType.STREAMING, "Say ..help", "https://www.twitch.tv/daporkchop_"));
            guildCount = manager.getGuilds().size();
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
        return guildCount;
    }

    public static int getUserCount() {
        int i = 0;
        for (JDA jda : shards) {
            i += jda.getUsers().size();
        }
        return i;
    }

    public static void forEachShard(Consumer<JDA> consumer) {
        if (consumer == null) return;
        shards.forEach(consumer);
    }

    public static int getShardCount() {
        return manager.getShardsTotal();
    }

    public static void shutdown() {
        CommandRegistry.save();
        manager.shutdown();
    }

    /**
     * WARNING: DO NOT USE FREQUENTLY!
     */
    public static List<AudioManager> getConnectedVoice() {
        List<AudioManager> list = new ArrayList<>();
        for (JDA jda : shards) {
            for (AudioManager manager : jda.getAudioManagers()) {
                if (manager.isConnected()) {
                    list.add(manager);
                }
            }
        }
        return list;
    }
}
