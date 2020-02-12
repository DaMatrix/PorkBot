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

package net.daporkchop.porkbot.audio.load;

import com.sedmelluq.discord.lavaplayer.player.AudioLoadResultHandler;
import com.sedmelluq.discord.lavaplayer.tools.FriendlyException;
import com.sedmelluq.discord.lavaplayer.track.AudioPlaylist;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import net.daporkchop.porkbot.audio.PorkAudio;
import net.daporkchop.porkbot.audio.ServerAudioManager;
import net.daporkchop.porkbot.audio.TrackScheduler;
import net.daporkchop.porkbot.audio.track.ResolvedTrack;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.entities.VoiceChannel;

import java.util.stream.Collectors;

/**
 * @author DaPorkchop_
 */
@RequiredArgsConstructor
public class FromURLLoadResultHandler implements AudioLoadResultHandler {
    @NonNull
    protected final TextChannel        msgChannel;
    @NonNull
    protected final VoiceChannel       dstChannel;
    @NonNull
    protected final ServerAudioManager manager;
    @NonNull
    protected final String             input;

    @Override
    public void trackLoaded(AudioTrack track) {
        PorkAudio.addIndividualTrack(track, this.manager, this.msgChannel, this.dstChannel);
    }

    @Override
    public void playlistLoaded(AudioPlaylist playlist) {
        this.manager.connect(this.dstChannel).lastAccessedFrom(this.msgChannel).scheduler()
                .enqueueAll(playlist.getTracks().stream().map(track -> new ResolvedTrack(track, this.dstChannel)).collect(Collectors.toList()));

        this.msgChannel.sendMessage(PorkAudio.findPlatform(playlist).embed()
                .setTitle("Added playlist to queue", this.input)
                .addField("Name", playlist.getName(), true)
                .addField("Tracks", String.valueOf(playlist.getTracks().size()), true)
                .addField("Total runtime", PorkAudio.formattedTrackLength(playlist.getTracks().stream().map(AudioTrack::getInfo).mapToLong(i -> i.length).sum()), true)
                .build()).queue();
    }

    @Override
    public void noMatches() {
        this.msgChannel.sendMessage("Nothing found for `" + this.input + '`').queue();
    }

    @Override
    public void loadFailed(FriendlyException exception) {
        this.msgChannel.sendMessage("Unable to find `" + this.input + "`: " + exception.getMessage()).queue();
    }
}
