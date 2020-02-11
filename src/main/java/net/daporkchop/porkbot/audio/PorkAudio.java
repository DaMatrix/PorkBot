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

import com.sedmelluq.discord.lavaplayer.player.AudioLoadResultHandler;
import com.sedmelluq.discord.lavaplayer.player.AudioPlayerManager;
import com.sedmelluq.discord.lavaplayer.player.DefaultAudioPlayerManager;
import com.sedmelluq.discord.lavaplayer.source.AudioSourceManagers;
import com.sedmelluq.discord.lavaplayer.tools.FriendlyException;
import com.sedmelluq.discord.lavaplayer.track.AudioPlaylist;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import com.sedmelluq.discord.lavaplayer.track.AudioTrackInfo;
import gnu.trove.impl.sync.TSynchronizedLongObjectMap;
import gnu.trove.map.TLongObjectMap;
import gnu.trove.map.hash.TLongObjectHashMap;
import lombok.NonNull;
import lombok.experimental.UtilityClass;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.GuildVoiceState;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.entities.VoiceChannel;

import java.util.HashMap;
import java.util.Map;

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

    public synchronized ServerAudioManager getAudioManager(@NonNull Guild guild, boolean create)    {
        ServerAudioManager manager = SERVERS.get(guild.getIdLong());

        if (manager == null && create)    {
            SERVERS.put(guild.getIdLong(), manager = new ServerAudioManager(guild, PLAYER_MANAGER.createPlayer()));
            guild.getAudioManager().setSendingHandler(manager.sendHandler());
        }

        return manager;
    }

    public void addTrackToQueue(@NonNull Guild guild, @NonNull TextChannel msgChannel, @NonNull Member requester, @NonNull String url)    {
        VoiceChannel dstChannel = requester.getVoiceState().getChannel();
        if (dstChannel == null) {
            msgChannel.sendMessage("You must be in a voice channel to add tracks to the play queue!").queue();
            return;
        }
        ServerAudioManager manager = getAudioManager(guild, true).lastAccessedFrom(msgChannel);

        PLAYER_MANAGER.loadItemOrdered(manager, url, new AudioLoadResultHandler() {
            @Override
            public void trackLoaded(AudioTrack track) {
                AudioTrackInfo info = track.getInfo();
                EmbedBuilder builder = new EmbedBuilder();
                builder.setTitle("Added to queue!", url);
                builder.addField("Title", String.valueOf(info.title), true);
                builder.addField("Author", String.valueOf(info.author), true);
                builder.addField("Length", String.format("%d:%d", info.length / 1000L / 60L, info.length / 1000L % 60L), false);
                builder.addField("Identifier", String.valueOf(info.identifier), true);
                builder.addField("URI", String.valueOf(info.uri), false);
                msgChannel.sendMessage(builder.build()).queue();

                manager.connect(dstChannel).lastAccessedFrom(msgChannel).scheduler().enqueue(track);
            }

            @Override
            public void playlistLoaded(AudioPlaylist playlist) {
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
}
