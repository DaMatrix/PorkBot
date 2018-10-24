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

package net.daporkchop.porkbot.audio;

import com.google.api.client.http.HttpRequest;
import com.google.api.client.http.HttpRequestInitializer;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.services.youtube.YouTube;
import com.google.api.services.youtube.model.SearchListResponse;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.sedmelluq.discord.lavaplayer.player.AudioLoadResultHandler;
import com.sedmelluq.discord.lavaplayer.player.AudioPlayerManager;
import com.sedmelluq.discord.lavaplayer.player.DefaultAudioPlayerManager;
import com.sedmelluq.discord.lavaplayer.source.AudioSourceManagers;
import com.sedmelluq.discord.lavaplayer.source.bandcamp.BandcampAudioSourceManager;
import com.sedmelluq.discord.lavaplayer.source.beam.BeamAudioSourceManager;
import com.sedmelluq.discord.lavaplayer.source.soundcloud.SoundCloudAudioSourceManager;
import com.sedmelluq.discord.lavaplayer.source.twitch.TwitchStreamAudioSourceManager;
import com.sedmelluq.discord.lavaplayer.source.vimeo.VimeoAudioSourceManager;
import com.sedmelluq.discord.lavaplayer.source.youtube.YoutubeAudioSourceManager;
import com.sedmelluq.discord.lavaplayer.tools.FriendlyException;
import com.sedmelluq.discord.lavaplayer.track.AudioPlaylist;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import gnu.trove.impl.sync.TSynchronizedLongObjectMap;
import gnu.trove.map.TLongObjectMap;
import gnu.trove.map.hash.TLongObjectHashMap;
import gnu.trove.set.TLongSet;
import gnu.trove.set.hash.TLongHashSet;
import net.daporkchop.porkbot.PorkBot;
import net.daporkchop.porkbot.util.KeyGetter;
import net.daporkchop.porkbot.util.MessageUtils;
import net.daporkchop.porkbot.util.ShardUtils;
import net.dv8tion.jda.core.entities.Guild;
import net.dv8tion.jda.core.entities.Member;
import net.dv8tion.jda.core.entities.TextChannel;
import net.dv8tion.jda.core.entities.VoiceChannel;
import net.dv8tion.jda.core.managers.AudioManager;

import java.io.IOException;
import java.util.List;
import java.util.TimerTask;
import java.util.concurrent.TimeUnit;

public class AudioUtils {
    protected static YouTube youTube;
    protected static String devKey;

    public static LoadingCache<String, String> videoNameCache = CacheBuilder.newBuilder()
            .maximumSize(10000)
            .expireAfterWrite(1, TimeUnit.DAYS)
            .build(new CacheLoader<String, String>() {
                public String load(String key) {
                    return AudioUtils.searchYoutube(key);
                }
            });

    protected static AudioPlayerManager playerManager;
    protected static TLongObjectMap<GuildAudioInfo> musicManagers;

    public static VoiceChannel connectToFirstVoiceChannel(AudioManager audioManager, Member user, TextChannel channel) {
        VoiceChannel toReturn = null;
        if (!audioManager.isConnected() && !audioManager.isAttemptingToConnect()) {
            if (user.getVoiceState().inVoiceChannel()) {
                audioManager.openAudioConnection(toReturn = user.getVoiceState().getChannel());
            } else {
                MessageUtils.sendMessage("You're not in a voice channel! REEEEEEEE", channel);
            }
        } else {
            MessageUtils.sendMessage("PorkBot is already connected to a voice channel in this server!!! REEEEEEEEEEEE", channel);
        }

        return toReturn;
    }

    public static synchronized GuildAudioInfo getGuildAudioPlayer(Guild guild, boolean create, Object... params) {
        long guildId = guild.getIdLong();
        GuildAudioInfo info = musicManagers.get(guildId);
        if (create && info == null) {
            info = new GuildAudioInfo(new GuildAudioManager(playerManager));
            musicManagers.put(guildId, info);
            guild.getAudioManager().setSendingHandler(info.manager.getSendHandler());
            if (false) {
                if (!guild.getAudioManager().isConnected()) {
                    info.channel = connectToFirstVoiceChannel(guild.getAudioManager(), (Member) params[0], (TextChannel) params[1]);
                }
                System.out.println("Creating new info for guild " + guildId);
            }
        }
        return info;
    }

    public static synchronized void tryConnectToVoice(Guild guild, GuildAudioInfo info, Object... params)    {
        if (info != null && !info.started)  {
            info.started = true;
            if (!guild.getAudioManager().isConnected()) {
                info.channel = connectToFirstVoiceChannel(guild.getAudioManager(), (Member) params[0], (TextChannel) params[1]);
            }
            System.out.println("Creating new info for guild " + guild.getIdLong());
        }
    }

    public static void loadAndPlay(final TextChannel channel, final String trackUrl, final Member user) {
        loadAndPlay(channel, trackUrl, user, true);
    }

    public static void loadAndPlay(final TextChannel channel, final String trackUrl, final Member user, final boolean notify) {
        if (!user.getVoiceState().inVoiceChannel()) {
            MessageUtils.sendMessage("You're not in a voice channel! REEEEEEEE", channel);
            return;
        }

        GuildAudioInfo musicManager = getGuildAudioPlayer(channel.getGuild(), true, user, channel);

        playerManager.loadItemOrdered(musicManager, trackUrl, new AudioLoadResultHandler() {
            @Override
            public void trackLoaded(AudioTrack track) {
                tryConnectToVoice(channel.getGuild(), musicManager, user, channel);

                if (notify) {
                    channel.sendMessage("Adding to queue: " + track.getInfo().title).queue();
                }

                play(channel.getGuild(), musicManager, track, user, channel);

                /*synchronized (trackUrl) {
                    trackUrl.notifyAll();
                }*/
            }

            @Override
            public void playlistLoaded(AudioPlaylist playlist) {
                List<AudioTrack> tracks = playlist.getTracks();

                if (tracks == null || tracks.size() == 0) {
                    channel.sendMessage("Empty or invalid playlist, not loading").queue();
                }
                tryConnectToVoice(channel.getGuild(), musicManager, user, channel);

                if (notify){
                    channel.sendMessage("Adding " + tracks.size() + " tracks from playlist " + playlist.getName() + " to queue").queue();
                }

                playList(channel.getGuild(), musicManager, tracks, user, channel);

                /*synchronized (trackUrl) {
                    trackUrl.notifyAll();
                }*/

                //tryConnectToVoice(channel.getGuild(), musicManager, user, channel);
            }

            @Override
            public void noMatches() {
                channel.sendMessage("Nothing found by " + trackUrl).queue();
            }

            @Override
            public void loadFailed(FriendlyException exception) {
                channel.sendMessage("Could not play: " + exception.getMessage()).queue();
            }
        });
    }

    public static boolean play(Guild guild, GuildAudioInfo musicManager, AudioTrack track, Member user, TextChannel channel) {
        synchronized (guild) {
            if (musicManager.channel != null) {
                musicManager.textChannel = channel;

                musicManager.manager.scheduler.queue(track);
            }

            return musicManager.channel != null;
        }
    }

    public static boolean playList(Guild guild, GuildAudioInfo musicManager, List<AudioTrack> tracks, Member user, TextChannel channel) {
        synchronized (guild) {
            musicManager.textChannel = channel;

            for (AudioTrack track : tracks) {
                musicManager.manager.scheduler.queue(track);
            }

            return musicManager.channel != null;
        }
    }

    public static void skipTrack(TextChannel channel) {
        GuildAudioInfo musicManager = getGuildAudioPlayer(channel.getGuild(), false);
        if (musicManager == null) {
            channel.sendMessage("Not playing!").queue();
        } else {
            if (musicManager.manager.scheduler.queue.size() == 0) {
                musicManager.manager.player.destroy();
                musicManager.channel.getGuild().getAudioManager().closeAudioConnection();
                musicManager.textChannel.sendMessage("Stopped!").queue();
            } else {
                musicManager.manager.scheduler.nextTrack();

                channel.sendMessage("Skipped to next track.").queue();
            }
        }
    }

    public static String searchYoutube(String query) {
        try {
            YouTube.Search.List search = youTube.search().list("id,snippet");
            search.setKey(devKey);
            search.setQ(query);
            search.setType("video");
            search.setFields("items(id/kind,id/videoId,snippet/title,snippet/thumbnails/default/url)");
            search.setMaxResults(1L);
            SearchListResponse searchResponse = search.execute();
            return "https://www.youtube.com/watch?v=" + searchResponse.getItems().get(0).getId().getVideoId();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return "";
    }

    public static void init() {
        youTube = new YouTube.Builder(new NetHttpTransport(), new JacksonFactory(), new HttpRequestInitializer() {
            @Override
            public void initialize(HttpRequest request) {
            }
        }).setApplicationName("youtube-cmdline-search-sample").build();
        devKey = KeyGetter.getDevKey();

        musicManagers = new TSynchronizedLongObjectMap<>(new TLongObjectHashMap<>());
        playerManager = new DefaultAudioPlayerManager();
        if (false) {
            AudioSourceManagers.registerRemoteSources(playerManager);
            AudioSourceManagers.registerLocalSource(playerManager);
        } else {
            playerManager.registerSourceManager(new YoutubeAudioSourceManager(true));
            playerManager.registerSourceManager(new SoundCloudAudioSourceManager());
            playerManager.registerSourceManager(new BandcampAudioSourceManager());
            playerManager.registerSourceManager(new VimeoAudioSourceManager());
            playerManager.registerSourceManager(new TwitchStreamAudioSourceManager());
            playerManager.registerSourceManager(new BeamAudioSourceManager());
            playerManager.registerSourceManager(new BetterHttpSourceManager());
        }

        PorkBot.timer.schedule(new TimerTask() {
            @Override
            public void run() {
                TLongSet toRemove = new TLongHashSet();
                long now = System.currentTimeMillis();
                musicManagers.forEachEntry((key, value) -> {
                    if (!value.started) {
                        return true;
                    }
                    A:
                    {
                        if (value.channel == null) {
                            //System.out.println("Removing guild because: missing channel");
                            toRemove.add(key);
                            break A;
                        }
                        if (value.created + 10000 < now
                                && (value.channel.getMembers().size() < 2
                                || (value.manager.scheduler.queue.size() == 0 && value.manager.player.getPlayingTrack() == null))) { //nobody's in the channel
                            //System.out.println("Removing guild because: empty");
                            value.manager.player.destroy();
                            value.channel.getGuild().getAudioManager().closeAudioConnection();
                            toRemove.add(key);
                            //break A;
                        }
                    }
                    return true;
                });
                toRemove.forEach(l -> {
                    musicManagers.remove(l);
                    return true;
                });

                //not sure why or how this is happening, but it does apparently
                List<AudioManager> list = ShardUtils.getConnectedVoice();
                for (AudioManager manager : list) {
                    if (manager.getConnectedChannel().getMembers().size() < 2) {
                        manager.closeAudioConnection();
                    }
                }
            }
        }, 10000, 5000);
    }
}
