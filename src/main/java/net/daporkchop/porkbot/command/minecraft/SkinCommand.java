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

import lombok.NonNull;
import net.daporkchop.lib.http.Http;
import net.daporkchop.lib.http.entity.content.type.ContentType;
import net.daporkchop.lib.http.server.ResponseBuilder;
import net.daporkchop.lib.http.util.StatusCodes;
import net.daporkchop.porkbot.PorkBot;
import net.daporkchop.porkbot.command.Command;
import net.daporkchop.porkbot.util.Constants;
import net.daporkchop.porkbot.util.MessageUtils;
import net.daporkchop.porkbot.util.UUIDFetcher;
import net.daporkchop.porkbot.web.ApiMethod;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;

import java.awt.Color;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author DaPorkchop_
 */
public final class SkinCommand extends Command {
    //protected static final String BASE_URL = "https://crafatar.daporkchop.net";

    protected final String target;

    public SkinCommand(String prefix, String target) {
        super(prefix);

        this.target = Constants.BASE_URL + "/api/mc/skin/" + target + '/';
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
                    EmbedBuilder builder = new EmbedBuilder();
                    builder.setAuthor(args[1] + "'s skin", null, Constants.BASE_URL + "/api/mc/skin/face/" + uuid + ".png");
                    builder.setImage(this.target + uuid + ".png");
                    builder.setColor(Color.DARK_GRAY);

                    //builder.addField(args[1] + "'s skin", "", false);

                    MessageUtils.sendMessage(builder, evt.getChannel());
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

    public static final class SkinApiMethod implements ApiMethod {
        private static final String      BASE_URL = "https://crafatar.com";
        private static final ContentType PNG_TYPE = ContentType.of("image/png");

        private final Pattern pattern;
        private final String  format;

        public SkinApiMethod(@NonNull String name, @NonNull String format) {
            this.pattern = Pattern.compile("^/api/mc/skin/" + name + '/' + Constants.UUID_CAPTURE + "\\.png$");
            this.format = format;

            PorkBot.WEB_SERVER.register("/api/mc/skin/" + name, this);
        }

        @Override
        public void handle(@NonNull String path, @NonNull ResponseBuilder response) throws Exception {
            Matcher matcher = this.pattern.matcher(path);
            if (!matcher.find()) {
                throw StatusCodes.BAD_REQUEST.exception();
            }

            response.status(StatusCodes.OK)
                    .body(PNG_TYPE, Http.get(BASE_URL + String.format(this.format, matcher.group(1))));
        }
    }
}
