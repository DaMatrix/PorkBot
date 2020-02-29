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

import com.google.gson.Gson;
import com.google.gson.JsonParser;
import lombok.NonNull;
import lombok.experimental.UtilityClass;
import net.daporkchop.lib.common.cache.Cache;
import net.daporkchop.lib.common.pool.handle.Handle;
import net.daporkchop.lib.common.util.PorkUtil;
import net.daporkchop.lib.http.HttpClient;
import net.daporkchop.lib.http.impl.java.JavaHttpClientBuilder;
import net.dv8tion.jda.api.entities.Message;

import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author DaPorkchop_
 */
@UtilityClass
public class Constants {
    public final boolean DEV_MODE = Boolean.valueOf(System.getProperty("porkbot.dev", "false"));

    public final long DELETE_DELAY = 10L;

    public final Gson       GSON        = new Gson();
    public final JsonParser JSON_PARSER = new JsonParser();

    public final String UUID_CAPTURE = "([0-9A-Fa-f]{8}-?[0-9A-Fa-f]{4}-?[0-9A-Fa-f]{4}-?[0-9A-Fa-f]{4}-?[0-9A-Fa-f]{12})";

    public final String BASE_URL       = DEV_MODE ? "https://porkbot-test.daporkchop.net" : "https://porkbot.daporkchop.net";
    public final String COMMAND_PREFIX = DEV_MODE ? ",," : "..";

    public final Pattern        ESCAPE_PATTERN       = Pattern.compile("([*_~`()\\[\\]])");
    public final Cache<Matcher> ESCAPE_MATCHER_CACHE = Cache.soft(() -> ESCAPE_PATTERN.matcher(""));

    public final HttpClient BLOCKING_HTTP = new JavaHttpClientBuilder()
            .blockingRequests(true)
            .build();

    public final int MAX_SEARCH_RESULTS = 5;
    public final int MAX_NAME_LENGTH    = 50;

    public final Consumer<Message> DELETE_LATER = message -> message.delete().queueAfter(DELETE_DELAY, TimeUnit.SECONDS);

    public String escape(@NonNull String src) {
        try (Handle<StringBuilder> handle = PorkUtil.STRINGBUILDER_POOL.get()) {
            StringBuilder builder = handle.value();
            builder.setLength(0);

            appendEscaped(builder, src);
            return builder.toString();
        }
    }

    public void appendEscaped(@NonNull StringBuilder builder, @NonNull String src) {
        appendEscaped(builder, src, 0, src.length());
    }

    public void appendEscaped(@NonNull StringBuilder builder, @NonNull String src, int start, int length) {
        PorkUtil.assertInRangeLen(src.length(), start, length);
        for (int i = 0; i < length; i++) {
            char c = src.charAt(start + i);
            switch (c) {
                case '*':
                case '_':
                case '~':
                case '`':
                case '(':
                case ')':
                case '[':
                case ']':
                    builder.append('\\');
                default:
                    builder.append(c);
            }
        }
    }

    public String escapeUrl(@NonNull String path) {
        try {
            URL url = new URL(path);
            return new URI(url.getProtocol(), url.getUserInfo(), url.getHost(), url.getPort(), url.getPath(), url.getQuery(), url.getRef()).toASCIIString();
        } catch (MalformedURLException | URISyntaxException e) {
            throw new IllegalArgumentException(path, e);
        }
    }
}
