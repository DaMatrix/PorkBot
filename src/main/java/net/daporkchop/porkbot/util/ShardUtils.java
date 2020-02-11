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

package net.daporkchop.porkbot.util;

import net.daporkchop.porkbot.PorkListener;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.Activity;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.sharding.DefaultShardManagerBuilder;
import net.dv8tion.jda.api.sharding.ShardManager;
import net.dv8tion.jda.api.utils.cache.CacheFlag;

import java.util.EnumSet;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;

public class ShardUtils {
    private static ShardManager manager;

    static {
        System.out.println("Starting shards...");
        try {
            manager = new DefaultShardManagerBuilder().setToken(KeyGetter.getToken())
                    .setShardsTotal(-1)
                    .addEventListeners(new PorkListener())
                    .setActivity(Activity.of(Activity.ActivityType.STREAMING, "Say ..help", "https://www.twitch.tv/daporkchop_"))
                    .setEnabledCacheFlags(EnumSet.of(CacheFlag.VOICE_STATE))
                    .build();
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
        manager.shutdown();
    }
}
