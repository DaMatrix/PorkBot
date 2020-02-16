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

package net.daporkchop.porkbot.audio;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NonNull;
import lombok.experimental.Accessors;
import net.dv8tion.jda.api.EmbedBuilder;

import java.util.Map;
import java.util.TreeMap;

/**
 * @author DaPorkchop_
 */
@Getter
@Accessors(fluent = true)
public final class SearchPlatform {
    private static final Map<String, SearchPlatform> PLATFORM_LOOKUP = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);

    public static final SearchPlatform BANDCAMP   = new SearchPlatform(0xFF1DA0C3, "bandcamp", "bandcamp");
    public static final SearchPlatform INTERNET   = new SearchPlatform(0xFF3B88C3, "Web URL", "globe-wireframe");
    public static final SearchPlatform SOUNDCLOUD = new SearchPlatform(0xFFF7620E, "scsearch:", "SoundCloud", "sc", "soundc");
    public static final SearchPlatform TWITCH     = new SearchPlatform(0xFF6C2498, "Twitch", "twitch");
    public static final SearchPlatform YOUTUBE    = new SearchPlatform(0xFFDD473A, "ytsearch:", "YouTube", "yt");

    public static SearchPlatform from(@NonNull String name) {
        return PLATFORM_LOOKUP.get(name);
    }

    public static String[] getAllPlatformNamesAndAliases() {
        return PLATFORM_LOOKUP.keySet().toArray(new String[PLATFORM_LOOKUP.size()]);
    }

    private final int    color;
    @Getter(AccessLevel.NONE)
    private final String prefix;
    private final String icon;
    private final String name;

    private SearchPlatform(int color, @NonNull String prefix, @NonNull String name, String... aliases) {
        this.color = color;
        this.prefix = prefix;
        this.icon = "https://cloud.daporkchop.net/static/img/logo/128/" + name.toLowerCase() + ".png";
        this.name = name;

        if (PLATFORM_LOOKUP.putIfAbsent(name, this) != null) {
            throw new IllegalStateException(name);
        }
        for (String alias : aliases) {
            if (PLATFORM_LOOKUP.putIfAbsent(alias, this) != null) {
                throw new IllegalStateException(alias + " (alias of " + name + ')');
            }
        }
    }

    private SearchPlatform(int color, @NonNull String name, @NonNull String icon) {
        this.color = color;
        this.prefix = "";
        this.icon = "https://cloud.daporkchop.net/static/img/logo/128/" + icon + ".png";
        this.name = name;
    }

    public String prefix(@NonNull String query) {
        return query.startsWith(this.prefix) ? query : this.prefix + query;
    }

    public EmbedBuilder embed() {
        return this.embed(new EmbedBuilder());
    }

    public EmbedBuilder embed(@NonNull EmbedBuilder builder) {
        return builder.setColor(this.color)
                .setAuthor(this.name, null, this.icon);
    }
}