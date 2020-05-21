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

package net.daporkchop.porkbot.util;

import lombok.experimental.UtilityClass;
import net.daporkchop.lib.binary.oio.StreamUtil;
import net.daporkchop.lib.binary.oio.appendable.PAppendable;
import net.daporkchop.lib.binary.oio.writer.UTF8FileWriter;
import net.daporkchop.lib.common.function.throwing.EConsumer;
import net.daporkchop.lib.common.misc.file.PFiles;
import net.daporkchop.lib.logging.Logging;
import net.daporkchop.porkbot.PorkListener;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.OnlineStatus;
import net.dv8tion.jda.api.entities.Activity;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.sharding.DefaultShardManagerBuilder;
import net.dv8tion.jda.api.sharding.ShardManager;
import net.dv8tion.jda.api.utils.Compression;
import net.dv8tion.jda.api.utils.cache.CacheFlag;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.EnumSet;
import java.util.Scanner;
import java.util.function.Consumer;
import java.util.stream.Stream;

@UtilityClass
public class ShardUtils {
    private static ShardManager MANAGER;

    public synchronized void start() {
        if (MANAGER != null) {
            throw new IllegalStateException("Already started!");
        }

        Logging.logger.info("Starting shards...");
        try {
            MANAGER = new DefaultShardManagerBuilder().setToken(loadToken())
                    .setCompression(Compression.NONE)
                    .setShardsTotal(-1)
                    .addEventListeners(new PorkListener())
                    .setStatus(OnlineStatus.IDLE)
                    .setActivity(Activity.of(Activity.ActivityType.DEFAULT, "starting..."))
                    .setEnabledCacheFlags(EnumSet.of(CacheFlag.VOICE_STATE, CacheFlag.EMOTE))
                    .build();

            MANAGER.getShards().forEach((EConsumer<JDA>) JDA::awaitReady);

            MANAGER.setStatus(OnlineStatus.ONLINE);
            MANAGER.setActivity(Activity.of(Activity.ActivityType.STREAMING, "Say ..help", "https://www.twitch.tv/daporkchop_"));
        } catch (Exception e) {
            throw new RuntimeException("Unable to start ShardManager!", e);
        }
        Logging.logger.success("Shards started!");
    }

    public synchronized void shutdown() {
        if (MANAGER == null) {
            throw new IllegalStateException("Not running!");
        }

        MANAGER.shutdown();
        MANAGER = null;
    }

    public void forEachGuild(Consumer<Guild> consumer) {
        if (consumer == null) {
            return;
        }
        MANAGER.getGuilds().forEach(consumer);
    }

    public long getGuildCount() {
        return MANAGER.getGuildCache().size();
    }

    public long getUserCount() {
        return MANAGER.getUserCache().size();
    }

    public Stream<JDA> getShards() {
        return MANAGER == null ? Stream.empty() : MANAGER.getShards().stream();
    }

    public long getShardCount() {
        return MANAGER == null ? -1L : MANAGER.getShardsTotal();
    }

    private String loadToken() {
        try {
            File tokenFile = new File("discordtoken.txt");
            if (PFiles.checkFileExists(tokenFile) && tokenFile.length() > 0L) {
                try (InputStream in = new FileInputStream(tokenFile)) {
                    return new String(StreamUtil.toByteArray(in), StandardCharsets.UTF_8).trim();
                }
            } else {
                String token;
                try (Scanner scanner = new Scanner(System.in)) {
                    System.out.print("Enter your discord bot token: ");
                    System.out.flush();
                    token = scanner.nextLine().trim();
                }
                try (PAppendable out = new UTF8FileWriter(PFiles.ensureFileExists(tokenFile))) {
                    out.appendLn(token);
                }
                return token;
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
