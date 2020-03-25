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

package net.daporkchop.porkbot.command.minecraft;

import com.google.common.base.Function;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.google.common.cache.Weigher;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import net.daporkchop.lib.http.Http;
import net.daporkchop.porkbot.command.Command;
import net.daporkchop.porkbot.util.MessageUtils;
import net.daporkchop.porkbot.util.UUIDFetcher;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.MessageBuilder;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;
import net.dv8tion.jda.api.exceptions.PermissionException;

import java.awt.Color;
import java.time.Instant;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ForkJoinPool;

/**
 * @author DaPorkchop_
 */
public final class SkinCommand extends Command {
    protected final Type type;

    public SkinCommand(String prefix, @NonNull Type type) {
        super(prefix);

        this.type = type;
    }

    @Override
    public void execute(GuildMessageReceivedEvent evt, String[] args, String message) {
        try {
            if (args.length < 2 || args[1].isEmpty()) {
                sendErrorMessage(evt.getChannel(), "Name isn't given!");
                return;
            }

            UUIDFetcher.enqueueRequest(args[1], uuid -> {
                if (uuid == null) {
                    MessageUtils.sendMessage("Player " + args[1] + " could not be found! Are they a paid Java Edition user?", evt.getChannel());
                } else {
                    Type.FACE.fetch(uuid).thenAcceptBothAsync(this.type.fetch(uuid), (face, img) -> {
                        String fileNameFace = Type.FACE.fileName(uuid);
                        String fileName = this.type.fileName(uuid);

                        MessageEmbed embed = new EmbedBuilder().setColor(Color.DARK_GRAY)
                                .setAuthor(args[1] + "'s skin", null, "attachment://" + fileNameFace)
                                .setImage("attachment://" + fileName)
                                .setTimestamp(Instant.now())
                                .build();

                        try {
                            evt.getChannel().sendMessage(embed)
                                    .addFile(face, fileNameFace)
                                    .addFile(img, fileName)
                                    .queue();
                        } catch (PermissionException e) {
                            //we can't do anything about it
                            if (e.getPermission() == Permission.MESSAGE_EMBED_LINKS) {
                                evt.getChannel().sendMessage("Lacking permission to embed links!").queue();
                            }
                        }
                    }, ForkJoinPool.commonPool());
                }
            });
        } catch (Exception e) {
            MessageUtils.sendException(e, evt);
        }
    }

    @Override
    public String getUsage() {
        return String.format("..%s <name>", this.prefix);
    }

    @Override
    public String getUsageExample() {
        return String.format("..%s Notch", this.prefix);
    }

    @RequiredArgsConstructor
    public enum Type implements Function<String, byte[]> {
        FACE("https://crafatar.com/avatars/%s?size=128&default=MHF_Steve&overlay"),
        HEAD("https://crafatar.com/renders/head/%s?scale=10&default=MHF_Steve&overlay"),
        BODY("https://crafatar.com/renders/body/%s?scale=10&default=MHF_Steve&overlay"),
        RAW("https://crafatar.com/skins/%s?default=MHF_Steve");

        @NonNull
        private final String format;

        public final String nameLowercase = this.name().toLowerCase();

        private final LoadingCache<String, byte[]> cache = CacheBuilder.newBuilder()
                .weigher((Weigher<String, byte[]>) (k, v) -> k.length() + v.length)
                .maximumWeight(1 << 24L) //16 MiB
                .build(CacheLoader.from(this));

        @Override
        public byte[] apply(@NonNull String uuid) {
            return Http.get(String.format(this.format, uuid));
        }

        public CompletableFuture<byte[]> fetch(@NonNull String uuid) {
            return CompletableFuture.supplyAsync(() -> this.cache.getUnchecked(uuid), ForkJoinPool.commonPool());
        }

        public String fileName(@NonNull String uuid) {
            return String.format("%s-%s.png", this.nameLowercase, uuid);
        }
    }
}
