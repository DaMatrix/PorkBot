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
import com.sedmelluq.discord.lavaplayer.player.AudioLoadResultHandler;
import com.sedmelluq.discord.lavaplayer.player.AudioPlayerManager;
import com.sedmelluq.discord.lavaplayer.player.DefaultAudioPlayerManager;
import com.sedmelluq.discord.lavaplayer.source.AudioSourceManagers;
import com.sedmelluq.discord.lavaplayer.tools.FriendlyException;
import com.sedmelluq.discord.lavaplayer.track.AudioPlaylist;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import com.sedmelluq.discord.lavaplayer.track.AudioTrackInfo;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.experimental.UtilityClass;
import net.daporkchop.lib.common.pool.handle.Handle;
import net.daporkchop.lib.common.util.PorkUtil;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.entities.VoiceChannel;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;

import java.util.HashMap;
import java.util.List;
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
        AudioSourceManagers.registerRemoteSources(PLAYER_MANAGER);
        //AudioSourceManagers.registerLocalSource(PLAYER_MANAGER);
    }

    private final Cache<UUID, WaitingSearchResults> WAITING_SEARCHES = CacheBuilder.newBuilder()
            .expireAfterWrite(1L, TimeUnit.MINUTES)
            .build();

    private final Cache<String, AudioTrack[]> SEARCH_CACHE = CacheBuilder.newBuilder()
            .expireAfterWrite(7L, TimeUnit.DAYS)
            .softValues()
            .build();

    public synchronized ServerAudioManager getAudioManager(@NonNull Guild guild, boolean create) {
        ServerAudioManager manager = SERVERS.get(guild.getIdLong());

        if (manager == null && create) {
            SERVERS.put(guild.getIdLong(), manager = new ServerAudioManager(guild, PLAYER_MANAGER.createPlayer()));
            guild.getAudioManager().setSendingHandler(manager.sendHandler());
        }

        return manager;
    }

    public void addTrackToQueue(@NonNull Guild guild, @NonNull TextChannel msgChannel, @NonNull Member requester, @NonNull String url) {
        VoiceChannel dstChannel = requester.getVoiceState().getChannel();
        ServerAudioManager manager = getAudioManager(guild, true).lastAccessedFrom(msgChannel);

        PLAYER_MANAGER.loadItemOrdered(manager, url, new AudioLoadResultHandler() {
            @Override
            public void trackLoaded(AudioTrack track) {
                msgChannel.sendMessage(embed(track)
                        .setTitle("Added to queue!", track.getInfo().uri)
                        .build()).queue();

                manager.connect(dstChannel).lastAccessedFrom(msgChannel).scheduler().enqueue(track);
            }

            @Override
            public void playlistLoaded(AudioPlaylist playlist) {
                msgChannel.sendMessage("Loaded playlist: " + playlist.getName()).queue();
                //TODO
            }

            @Override
            public void noMatches() {
                msgChannel.sendMessage("Unable to find `" + url + '`').queue();
            }

            @Override
            public void loadFailed(FriendlyException exception) {
                msgChannel.sendMessage("Unable to find `" + url + "`: " + exception.getMessage()).queue();
            }
        });
    }

    public void addTrackBySearch(@NonNull Guild guild, @NonNull TextChannel msgChannel, @NonNull Member requester, @NonNull SearchPlatform platform, @NonNull String query) {
        String prefixed = platform.prefix(query);
        VoiceChannel dstChannel = requester.getVoiceState().getChannel();
        ServerAudioManager manager = getAudioManager(guild, true).lastAccessedFrom(msgChannel);

        AudioTrack[] cachedTracks = SEARCH_CACHE.getIfPresent(prefixed);
        if (cachedTracks != null) {
            promptForSearchChoice(cachedTracks, msgChannel, requester, platform, query, dstChannel, manager);
        } else {
            PLAYER_MANAGER.loadItem(prefixed, new AudioLoadResultHandler() {
                @Override
                public void trackLoaded(AudioTrack track) {
                    msgChannel.sendMessage("Search only returned one result!").queue();
                }

                @Override
                public void playlistLoaded(AudioPlaylist playlist) {
                    List<AudioTrack> list = playlist.getTracks();
                    AudioTrack[] tracks = new AudioTrack[Math.min(5, list.size())];
                    for (int i = 0; i < tracks.length; i++) {
                        tracks[i] = list.get(i);
                    }

                    SEARCH_CACHE.put(prefixed, tracks);
                    promptForSearchChoice(tracks, msgChannel, requester, platform, query, dstChannel, manager);
                }

                @Override
                public void noMatches() {
                    msgChannel.sendMessage("No results for `" + query + '`').queue();
                }

                @Override
                public void loadFailed(FriendlyException exception) {
                    msgChannel.sendMessage("Unable to search for `" + query + "`: " + exception.getMessage()).queue();
                }
            });
        }
    }

    private void promptForSearchChoice(@NonNull AudioTrack[] tracks, @NonNull TextChannel msgChannel, @NonNull Member requester, @NonNull SearchPlatform platform, @NonNull String query, @NonNull VoiceChannel dstChannel, @NonNull ServerAudioManager manager) {
        EmbedBuilder builder = new EmbedBuilder()
                .setColor(platform.color())
                .setAuthor(platform.name(), null, platform.icon())
                .setTitle("Type the number of the track you want to play.");

        try (Handle<StringBuilder> handle = PorkUtil.STRINGBUILDER_POOL.get()) {
            StringBuilder sb = handle.value();
            sb.setLength(0);

            for (int i = 0; i < tracks.length; i++) {
                AudioTrackInfo info = tracks[i].getInfo();

                sb.append('`').append(i + 1).append('.').append('`').append(' ')
                        .append('*').append(info.author).append("* - ")
                        .append(info.title).append(' ');

                formattedTrackLength(info.length, sb.append('`')).append('`').append('\n');
            }

            sb.setLength(sb.length() - 1);
            builder.addField("Search results for: `" + query + '`', sb.toString(), false);
        }

        msgChannel.sendMessage(builder.build()).queue(message -> WAITING_SEARCHES.put(
                new UUID(message.getTextChannel().getIdLong(), requester.getIdLong()),
                new WaitingSearchResults(tracks, message, manager, dstChannel, builder)));
    }

    public void checkSearchResponse(@NonNull GuildMessageReceivedEvent event, @NonNull String text) {
        UUID id = new UUID(event.getChannel().getIdLong(), event.getAuthor().getIdLong());
        WaitingSearchResults results = WAITING_SEARCHES.getIfPresent(id);
        if (results != null) {
            WAITING_SEARCHES.invalidate(id);

            if (event.getGuild().getMember(event.getJDA().getSelfUser()).hasPermission(Permission.MESSAGE_MANAGE)) {
                event.getMessage().delete().queue();
            }

            try {
                int i = Integer.parseUnsignedInt(text) - 1;

                AudioTrack track = results.tracks[i].makeClone();
                results.message.editMessage(embed(track, results.builder.clearFields())
                        .setTitle("Added to queue!", track.getInfo().uri)
                        .build()).queue();

                results.manager.connect(results.dstChannel).lastAccessedFrom(event.getChannel()).scheduler().enqueue(track);
            } catch (NumberFormatException | ArrayIndexOutOfBoundsException e) {
                event.getChannel().sendMessage("Invalid selection: `" + text + "` (must be in range 1-" + results.tracks.length + ')').queue();
            }
        }
    }

    public EmbedBuilder embed(@NonNull AudioTrack track) {
        return embed(track, new EmbedBuilder());
    }

    public EmbedBuilder embed(@NonNull AudioTrack track, @NonNull EmbedBuilder builder) {
        AudioTrackInfo info = track.getInfo();
        return builder
                .addField("Title", String.valueOf(info.title), true)
                .addField("Author", String.valueOf(info.author), true)
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
        return builder;
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
        @NonNull
        private final EmbedBuilder       builder;
    }
}
