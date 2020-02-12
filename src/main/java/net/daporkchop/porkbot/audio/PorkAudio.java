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

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.sedmelluq.discord.lavaplayer.player.AudioPlayerManager;
import com.sedmelluq.discord.lavaplayer.player.DefaultAudioPlayerManager;
import com.sedmelluq.discord.lavaplayer.source.AudioSourceManagers;
import com.sedmelluq.discord.lavaplayer.source.bandcamp.BandcampAudioTrack;
import com.sedmelluq.discord.lavaplayer.source.soundcloud.SoundCloudAudioTrack;
import com.sedmelluq.discord.lavaplayer.source.twitch.TwitchStreamAudioTrack;
import com.sedmelluq.discord.lavaplayer.source.youtube.YoutubeAudioTrack;
import com.sedmelluq.discord.lavaplayer.track.AudioPlaylist;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import com.sedmelluq.discord.lavaplayer.track.AudioTrackInfo;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.experimental.UtilityClass;
import net.daporkchop.lib.common.pool.handle.Handle;
import net.daporkchop.lib.common.util.PorkUtil;
import net.daporkchop.porkbot.PorkBot;
import net.daporkchop.porkbot.audio.load.FromURLLoadResultHandler;
import net.daporkchop.porkbot.audio.load.SearchLoadResultHandler;
import net.daporkchop.porkbot.audio.track.ResolvedTrack;
import net.daporkchop.porkbot.util.Constants;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.entities.VoiceChannel;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

/**
 * @author DaPorkchop_
 */
@UtilityClass
public class PorkAudio {
    public final AudioPlayerManager PLAYER_MANAGER = new DefaultAudioPlayerManager();

    private final Map<Long, ServerAudioManager> SERVERS = new HashMap<>();

    static {
        //register audio sources
        AudioSourceManagers.registerRemoteSources(PLAYER_MANAGER);

        //clean up idle audio managers automatically
        PorkBot.SCHEDULED_EXECUTOR.scheduleWithFixedDelay(() -> {
            synchronized (SERVERS) {
                SERVERS.forEach((guildId, manager) -> manager.checkTimedOut());
            }
        }, 10L, 10L, TimeUnit.SECONDS);
    }

    private final Cache<UUID, WaitingSearchResults> WAITING_SEARCHES = CacheBuilder.newBuilder()
            .expireAfterWrite(1L, TimeUnit.MINUTES)
            .build();

    private final Cache<String, AudioTrack[]> SEARCH_CACHE = CacheBuilder.newBuilder()
            .expireAfterWrite(7L, TimeUnit.DAYS)
            .softValues()
            .build();

    public ServerAudioManager getAudioManager(@NonNull Guild guild, boolean create) {
        synchronized (SERVERS) {
            ServerAudioManager manager = SERVERS.get(guild.getIdLong());

            if (manager == null && create) {
                SERVERS.put(guild.getIdLong(), manager = new ServerAudioManager(guild, PLAYER_MANAGER.createPlayer()));
                guild.getAudioManager().setSendingHandler(manager.sendHandler());
            }

            return manager;
        }
    }

    public void addTrackByURL(@NonNull Guild guild, @NonNull TextChannel msgChannel, @NonNull Member requester, @NonNull String url, @NonNull VoiceChannel dstChannel) {
        ServerAudioManager manager = getAudioManager(guild, true).lastAccessedFrom(msgChannel);
        if (!manager.couldConnectToIfNeeded(dstChannel)) {
            msgChannel.sendMessage("Not in the same voice channel!").queue();
            return;
        }

        PLAYER_MANAGER.loadItem(url, new FromURLLoadResultHandler(msgChannel, dstChannel, manager, url));
    }

    public void addTrackBySearch(@NonNull Guild guild, @NonNull TextChannel msgChannel, @NonNull Member requester, @NonNull SearchPlatform platform, @NonNull String query, @NonNull VoiceChannel dstChannel) {
        ServerAudioManager manager = getAudioManager(guild, true).lastAccessedFrom(msgChannel);
        if (!manager.couldConnectToIfNeeded(dstChannel)) {
            msgChannel.sendMessage("Not in the same voice channel!").queue();
            return;
        }

        String prefixed = platform.prefix(query);

        AudioTrack[] cachedTracks = SEARCH_CACHE.getIfPresent(prefixed);
        if (cachedTracks != null) {
            handleSearchResults(cachedTracks, manager, msgChannel, dstChannel, requester, platform, query);
        } else {
            PLAYER_MANAGER.loadItem(prefixed, new SearchLoadResultHandler(msgChannel, dstChannel, manager, query, requester, SEARCH_CACHE, prefixed));
        }
    }

    public void handleSearchResults(@NonNull AudioTrack[] tracks, @NonNull ServerAudioManager manager, @NonNull TextChannel msgChannel, @NonNull VoiceChannel dstChannel, @NonNull Member requester, @NonNull SearchPlatform platform, @NonNull String query) {
        if (tracks.length == 0) {
            msgChannel.sendMessage("Search: `" + query + "` returned no results!").queue();
            return;
        } else if (tracks.length == 1) {
            //immediately enqueue
            addIndividualTrack(tracks[0], manager, msgChannel, dstChannel);
            return;
        }

        EmbedBuilder builder = platform.embed()
                .setTitle("Type the number of the track you want to play.");

        try (Handle<StringBuilder> handle = PorkUtil.STRINGBUILDER_POOL.get()) {
            StringBuilder sb = handle.value();
            sb.setLength(0);

            for (int i = 0; i < tracks.length; i++) {
                sb.append('`').append(i + 1).append(':').append('`').append(' ');

                appendTrackInfo(tracks[i].getInfo(), sb).append('\n');
            }

            sb.setLength(sb.length() - 1);
            builder.addField("Search results for: `" + query + '`', sb.toString(), false);
        }

        msgChannel.sendMessage(builder.build()).queue(message -> WAITING_SEARCHES.put(
                new UUID(message.getTextChannel().getIdLong(), requester.getIdLong()),
                new WaitingSearchResults(tracks, message, manager, dstChannel)));
    }

    public void addIndividualTrack(@NonNull AudioTrack track, @NonNull ServerAudioManager manager, @NonNull TextChannel msgChannel, @NonNull VoiceChannel dstChannel) {
        msgChannel.sendMessage(PorkAudio.embed(track, PorkAudio.findPlatform(track).embed())
                .setTitle("Added to queue", track.getInfo().uri)
                .build()).queue();

        manager.connect(dstChannel).lastAccessedFrom(msgChannel).scheduler()
                .enqueue(new ResolvedTrack(track, dstChannel));
    }

    public void checkSearchResponse(@NonNull GuildMessageReceivedEvent event, @NonNull String text) {
        UUID id = new UUID(event.getChannel().getIdLong(), event.getAuthor().getIdLong());
        WaitingSearchResults results = WAITING_SEARCHES.getIfPresent(id);
        if (results != null) {
            WAITING_SEARCHES.invalidate(id);

            if (event.getGuild().getMember(event.getJDA().getSelfUser()).hasPermission(event.getChannel(), Permission.MESSAGE_MANAGE)) {
                event.getMessage().delete().queue();
            }

            try {
                int i = Integer.parseUnsignedInt(text) - 1;

                AudioTrack track = results.tracks[i].makeClone();
                results.message.editMessage(embed(track, findPlatform(track).embed())
                        .setTitle("Added to queue!", track.getInfo().uri)
                        .build()).queue();

                results.manager.connect(results.dstChannel).lastAccessedFrom(event.getChannel()).scheduler()
                        .enqueue(new ResolvedTrack(track, results.dstChannel));
            } catch (NumberFormatException | ArrayIndexOutOfBoundsException e) {
                event.getChannel().sendMessage("Invalid selection: `" + text + "` (must be in range 1-" + results.tracks.length + ')').queue();
            }
        }
    }

    public EmbedBuilder embed(@NonNull AudioTrack track, @NonNull EmbedBuilder builder) {
        AudioTrackInfo info = track.getInfo();
        return builder
                .addField("Title", Constants.escape(String.valueOf(info.title)), true)
                .addField("Author", Constants.escape(String.valueOf(info.author)), true)
                .addField("Length", formattedTrackLength(info.length), true);
    }

    public String formattedTrackLength(long length) {
        try (Handle<StringBuilder> handle = PorkUtil.STRINGBUILDER_POOL.get()) {
            StringBuilder builder = handle.value();
            builder.setLength(0);

            return formattedTrackLength(length, builder).toString();
        }
    }

    public StringBuilder formattedTrackLength(long length, @NonNull StringBuilder builder) {
        builder.append('`');
        if (length == Long.MAX_VALUE || length < 0L) {
            builder.append("unknown length");
        } else {
            long seconds = length / 1000L;
            long minutes = seconds / 60L;
            long hours = minutes / 60L;

            if (hours > 0L) {
                builder.append(hours).append(':');
                if (minutes % 60L < 10L) {
                    builder.append('0');
                }
            }

            builder.append(minutes % 60L).append(':');
            if (seconds % 60L < 10L) {
                builder.append('0');
            }
            builder.append(seconds % 60L);
        }
        return builder.append('`');
    }

    public StringBuilder appendTrackInfo(@NonNull AudioTrackInfo info, @NonNull StringBuilder builder) {
        builder.append('*');
        if (info.author.length() + info.title.length() >= Constants.MAX_NAME_LENGTH) {
            Constants.appendEscaped(builder, info.author, 0, Math.min(Constants.MAX_NAME_LENGTH - 6, info.author.length()));
            if (info.author.length() >= Constants.MAX_NAME_LENGTH - 6) {
                builder.append("...* - ...");
            } else {
                builder.append("* - ");
                Constants.appendEscaped(builder, info.title, 0, Math.min(Constants.MAX_NAME_LENGTH - 3 - info.author.length(), info.title.length()));
                builder.append("...");
            }
        } else {
            Constants.appendEscaped(builder, info.author);
            Constants.appendEscaped(builder.append("* - "), info.title);
        }

        return formattedTrackLength(info.length, builder.append(' '));
    }

    public SearchPlatform findPlatform(@NonNull AudioTrack track) {
        if (track instanceof YoutubeAudioTrack) {
            return SearchPlatform.YOUTUBE;
        } else if (track instanceof SoundCloudAudioTrack) {
            return SearchPlatform.SOUNDCLOUD;
        } else if (track instanceof BandcampAudioTrack) {
            return SearchPlatform.BANDCAMP;
        } else if (track instanceof TwitchStreamAudioTrack) {
            return SearchPlatform.TWITCH;
        } else {
            return SearchPlatform.INTERNET;
        }
    }

    public SearchPlatform findPlatform(@NonNull AudioPlaylist playlist) {
        if (playlist.getSelectedTrack() != null) {
            return findPlatform(playlist.getSelectedTrack());
        } else if (!playlist.getTracks().isEmpty()) {
            return findPlatform(playlist.getTracks().get(0));
        } else {
            return SearchPlatform.INTERNET;
        }
    }

    @RequiredArgsConstructor
    private static final class WaitingSearchResults {
        @NonNull
        private final AudioTrack[]       tracks;
        @NonNull
        private final Message            message;
        @NonNull
        private final ServerAudioManager manager;
        @NonNull
        private final VoiceChannel       dstChannel;
    }
}
