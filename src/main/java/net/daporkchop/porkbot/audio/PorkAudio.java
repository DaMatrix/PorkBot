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

package net.daporkchop.porkbot.audio;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.sedmelluq.discord.lavaplayer.container.MediaContainerRegistry;
import com.sedmelluq.discord.lavaplayer.player.AudioPlayerManager;
import com.sedmelluq.discord.lavaplayer.player.DefaultAudioPlayerManager;
import com.sedmelluq.discord.lavaplayer.source.bandcamp.BandcampAudioSourceManager;
import com.sedmelluq.discord.lavaplayer.source.bandcamp.BandcampAudioTrack;
import com.sedmelluq.discord.lavaplayer.source.beam.BeamAudioSourceManager;
import com.sedmelluq.discord.lavaplayer.source.http.HttpAudioSourceManager;
import com.sedmelluq.discord.lavaplayer.source.soundcloud.SoundCloudAudioSourceManager;
import com.sedmelluq.discord.lavaplayer.source.soundcloud.SoundCloudAudioTrack;
import com.sedmelluq.discord.lavaplayer.source.twitch.TwitchStreamAudioSourceManager;
import com.sedmelluq.discord.lavaplayer.source.twitch.TwitchStreamAudioTrack;
import com.sedmelluq.discord.lavaplayer.source.vimeo.VimeoAudioSourceManager;
import com.sedmelluq.discord.lavaplayer.source.youtube.YoutubeAudioSourceManager;
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
import net.daporkchop.porkbot.audio.source.pplst.PorkCloudAudioSourceManager;
import net.daporkchop.porkbot.audio.source.pplst.PplstAudioSourceManager;
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
import org.apache.http.HttpHost;
import org.apache.http.client.config.CookieSpecs;
import org.apache.http.client.config.RequestConfig;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Formatter;
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
    private final Cache<UUID, WaitingSearchResults> WAITING_SEARCHES = CacheBuilder.newBuilder()
            .expireAfterWrite(1L, TimeUnit.MINUTES)
            .build();

    private final YoutubeAudioSourceManager YOUTUBE_AUDIO_SOURCE_MANAGER = new YoutubeAudioSourceManager(true);
    private final SoundCloudAudioSourceManager SOUND_CLOUD_AUDIO_SOURCE_MANAGER = SoundCloudAudioSourceManager.createDefault();
    private final BandcampAudioSourceManager BANDCAMP_AUDIO_SOURCE_MANAGER = new BandcampAudioSourceManager();
    private final VimeoAudioSourceManager VIMEO_AUDIO_SOURCE_MANAGER = new VimeoAudioSourceManager();
    private final TwitchStreamAudioSourceManager TWITCH_AUDIO_SOURCE_MANAGER = new TwitchStreamAudioSourceManager();
    private final BeamAudioSourceManager BEAM_AUDIO_SOURCE_MANAGER = new BeamAudioSourceManager();
    private final HttpAudioSourceManager HTTP_AUDIO_SOURCE_MANAGER = new HttpAudioSourceManager();
    private final PplstAudioSourceManager PPLST_AUDIO_SOURCE_MANAGER = new PplstAudioSourceManager(MediaContainerRegistry.DEFAULT_REGISTRY, HTTP_AUDIO_SOURCE_MANAGER);
    private final PorkCloudAudioSourceManager PORKCLOUD_AUDIO_SOURCE_MANAGER = new PorkCloudAudioSourceManager(PPLST_AUDIO_SOURCE_MANAGER);

    static {
        //register audio sources
        {
            PLAYER_MANAGER.registerSourceManager(YOUTUBE_AUDIO_SOURCE_MANAGER);
            PLAYER_MANAGER.registerSourceManager(SOUND_CLOUD_AUDIO_SOURCE_MANAGER);
            PLAYER_MANAGER.registerSourceManager(BANDCAMP_AUDIO_SOURCE_MANAGER);
            PLAYER_MANAGER.registerSourceManager(VIMEO_AUDIO_SOURCE_MANAGER);
            PLAYER_MANAGER.registerSourceManager(TWITCH_AUDIO_SOURCE_MANAGER);
            PLAYER_MANAGER.registerSourceManager(BEAM_AUDIO_SOURCE_MANAGER);

            PLAYER_MANAGER.registerSourceManager(PPLST_AUDIO_SOURCE_MANAGER);
            PLAYER_MANAGER.registerSourceManager(PORKCLOUD_AUDIO_SOURCE_MANAGER);
            PLAYER_MANAGER.registerSourceManager(HTTP_AUDIO_SOURCE_MANAGER);
        }

        PLAYER_MANAGER.setHttpBuilderConfigurator(builder -> builder
                .setKeepAliveStrategy((response, context) -> 1L)); //disable keepalive

        PLAYER_MANAGER.setHttpRequestConfigurator(config -> RequestConfig.copy(config)
                .setCookieSpec(CookieSpecs.IGNORE_COOKIES)
                .build());

        YOUTUBE_AUDIO_SOURCE_MANAGER.configureRequests(config -> RequestConfig.copy(config)
                .setCookieSpec(CookieSpecs.IGNORE_COOKIES)
                .setProxy(HttpHost.create("http://localhost:1081"))
                .build());

        //clean up idle audio managers automatically
        PorkBot.SCHEDULED_EXECUTOR.scheduleWithFixedDelay(() -> {
            synchronized (SERVERS) {
                SERVERS.forEach((guildId, manager) -> manager.checkTimedOut());
            }
        }, 10L, 10L, TimeUnit.SECONDS);
    }

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

    public Collection<ServerAudioManager> getManagerSnapshot() {
        synchronized (SERVERS) {
            return new ArrayList<>(SERVERS.values());
        }
    }

    public void addTrackByURL(@NonNull Guild guild, @NonNull TextChannel msgChannel, @NonNull Member requester, @NonNull String url, @NonNull VoiceChannel dstChannel) {
        ServerAudioManager manager = getAudioManager(guild, true).lastAccessedFrom(msgChannel);
        if (!manager.couldConnectToIfNeeded(dstChannel)) {
            msgChannel.sendMessage("Not in the same voice channel!").queue();
            return;
        }

        AudioCacheManager.resolve(url, new FromURLLoadResultHandler(msgChannel, dstChannel, manager, url));
    }

    public void addTrackBySearch(@NonNull Guild guild, @NonNull TextChannel msgChannel, @NonNull Member requester, @NonNull SearchPlatform platform, @NonNull String query, @NonNull VoiceChannel dstChannel) {
        ServerAudioManager manager = getAudioManager(guild, true).lastAccessedFrom(msgChannel);
        if (!manager.couldConnectToIfNeeded(dstChannel)) {
            msgChannel.sendMessage("Not in the same voice channel!").queue();
            return;
        }

        AudioCacheManager.search(
                new SearchQueryWithPlatform(platform, query),
                new SearchLoadResultHandler(msgChannel, dstChannel, manager, query, requester));
    }

    public void handleSearchResults(@NonNull AudioTrack[] tracks, @NonNull ServerAudioManager manager, @NonNull TextChannel msgChannel, @NonNull VoiceChannel dstChannel, @NonNull Member requester, @NonNull SearchPlatform platform, @NonNull String query) {
        if (tracks.length == 0) {
            msgChannel.sendMessage("Search: `" + query + "` returned no results!").queue();
            return;
        } else if (tracks.length == 1) {
            //immediately enqueue
            addIndividualTrack(tracks[0].makeClone(), manager, msgChannel, dstChannel);
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
        if (!manager.lastAccessedFrom(msgChannel).connect(dstChannel, true)) {
            return;
        }

        manager.scheduler().enqueue(new ResolvedTrack(track, dstChannel));

        msgChannel.sendMessage(PorkAudio.embed(track, PorkAudio.findPlatform(track).embed())
                .setTitle("Added to queue", track.getInfo().uri)
                .build()).queue();
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

                if (!results.manager.lastAccessedFrom(event.getChannel()).connect(results.dstChannel, true)) {
                    return;
                }

                results.manager.scheduler().enqueue(new ResolvedTrack(track, results.dstChannel));
            } catch (NumberFormatException | ArrayIndexOutOfBoundsException e) {
                event.getChannel().sendMessage("Invalid selection: `" + text + "` (must be in range 1-" + results.tracks.length + ')').queue();
            }
        }
    }

    public EmbedBuilder embed(@NonNull AudioTrack track, @NonNull EmbedBuilder builder) {
        AudioTrackInfo info = track.getInfo();
        return builder
                .addField("Author", Constants.escape(String.valueOf(info.author)), true)
                .addField("Title", Constants.escape(String.valueOf(info.title)), true)
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
        return formattedTrackLength(length, builder, true);
    }

    public StringBuilder formattedTrackLength(long length, @NonNull StringBuilder builder, boolean code) {
        if (code) {
            builder.append('`');
        }

        if (length == Long.MAX_VALUE || length < 0L) {
            builder.append("unknown");
        } else {
            long seconds = (length / 1000L) % 60L;
            long minutes = (length / (1000L * 60L)) % 60L;
            long hours = (length / (1000L * 60L * 60L)) % 24L;
            long days = length / (1000L * 60L * 60L * 24L);

            Formatter formatter = new Formatter(builder);
            if (days > 0L) {
                formatter.format("%dd %02dh %02dm %02ds", days, hours, minutes, seconds);
            } else if (hours > 0L) {
                formatter.format("%dh %02dm %02ds", hours, minutes, seconds);
            } else {
                formatter.format("%dm %02ds", minutes, seconds);
            }
        }
        return code ? builder.append('`') : builder;
    }

    public StringBuilder appendTrackInfo(@NonNull AudioTrackInfo info, @NonNull StringBuilder builder) {
        return appendTrackInfo(-1L, info, builder);
    }

    public StringBuilder appendTrackInfo(long currentTime, @NonNull AudioTrackInfo info, @NonNull StringBuilder builder) {
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

        builder.append(' ');
        if (currentTime >= 0L) {
            formattedTrackLength(currentTime, builder.append('`'), false);
            formattedTrackLength(info.length, builder.append('/'), false).append('`');
        } else {
            formattedTrackLength(info.length, builder);
        }
        return builder;
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
        } else if (track instanceof PorkCloudAudioSourceManager.PorkCloudAudioTrack) {
            return SearchPlatform.DAPORKCHOP;
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
        private final AudioTrack[] tracks;
        @NonNull
        private final Message message;
        @NonNull
        private final ServerAudioManager manager;
        @NonNull
        private final VoiceChannel dstChannel;
    }
}
