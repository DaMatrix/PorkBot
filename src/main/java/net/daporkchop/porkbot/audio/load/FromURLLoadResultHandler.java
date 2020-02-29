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
        if (!this.manager.lastAccessedFrom(this.msgChannel).connect(this.dstChannel, true))   {
            return;
        }

        this.manager.scheduler()
                .enqueueAll(playlist.getTracks().stream().map(track -> new ResolvedTrack(track, this.dstChannel)).collect(Collectors.toList()));

        this.msgChannel.sendMessage(PorkAudio.findPlatform(playlist).embed()
                .setTitle("Added playlist to queue", this.input)
                .addField("Name", playlist.getName(), true)
                .addField("Tracks", String.valueOf(playlist.getTracks().size()), true)
                .addField("Total runtime", PorkAudio.formattedTrackLength(playlist.getTracks().stream().map(AudioTrack::getInfo)
                        .mapToLong(i -> i.length)
                        .filter(l -> l != Long.MAX_VALUE && l >= 0L)
                        .sum()), true)
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
