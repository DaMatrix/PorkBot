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

import com.sedmelluq.discord.lavaplayer.player.AudioPlayer;
import com.sedmelluq.discord.lavaplayer.player.event.AudioEventAdapter;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import com.sedmelluq.discord.lavaplayer.track.AudioTrackEndReason;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import net.daporkchop.lib.common.util.PArrays;

import java.util.LinkedList;
import java.util.Queue;

/**
 * @author DaPorkchop_
 */
@RequiredArgsConstructor
public final class TrackScheduler extends AudioEventAdapter {
    @NonNull
    private final AudioPlayer player;
    @NonNull
    private final ServerAudioManager manager;

    private final Queue<AudioTrack> queue = new LinkedList<>();

    public synchronized void enqueue(@NonNull AudioTrack track)  {
        if (!this.player.startTrack(track, true))    {
            this.queue.add(track);
        }
    }

    public synchronized boolean next() {
        AudioTrack next = this.queue.poll();
        this.player.startTrack(next, false);
        return next != null;
    }

    public synchronized void shuffle()  {
        if (this.queue.size() <= 1) {
            return;
        }

        AudioTrack[] tracks = this.queue();
        PArrays.shuffle(tracks);
        this.queue.clear();
        for (AudioTrack track : tracks)  {
            this.queue.add(track);
        }
    }

    public synchronized AudioTrack[] queue()    {
        return this.queue.toArray(new AudioTrack[this.queue.size()]);
    }

    public synchronized void skipAll()  {
        this.queue.clear();
        this.player.destroy();
        this.manager.doDisconnect();
    }

    @Override
    public synchronized void onTrackEnd(AudioPlayer player, AudioTrack track, AudioTrackEndReason endReason) {
        if (endReason.mayStartNext && this.next())    {
            return;
        }

        //shut down, we can't proceed
        this.skipAll();
    }
}
