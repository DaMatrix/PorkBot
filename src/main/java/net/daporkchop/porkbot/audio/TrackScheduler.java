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

import java.util.Collection;
import java.util.LinkedList;
import java.util.Queue;
import java.util.concurrent.ThreadLocalRandom;

/**
 * @author DaPorkchop_
 */
@RequiredArgsConstructor
public final class TrackScheduler extends AudioEventAdapter {
    @NonNull
    private final AudioPlayer player;
    @NonNull
    private final ServerAudioManager manager;

    private final LinkedList<AudioTrack> queue = new LinkedList<>();

    public void enqueue(@NonNull AudioTrack track)  {
        synchronized (this.manager) {
            if (!this.player.startTrack(track, true)) {
                this.queue.add(track);
            }
        }
    }

    public void enqueueAll(@NonNull Collection<AudioTrack> tracks, AudioTrack selectedTrack)   {
        synchronized (this.manager) {
            if (this.manager.shuffled())    {
                this.queue.addAll(tracks);

                if (this.player.getPlayingTrack() == null)  {
                    this.next();
                }
            } else {
                if (selectedTrack != null)  {
                    this.enqueue(selectedTrack);
                }

                for (AudioTrack track : tracks) {
                    if (selectedTrack != null && track == selectedTrack)    {
                        continue;
                    }

                    this.enqueue(track);
                }
            }
        }
    }

    public boolean next() {
        synchronized (this.manager) {
            AudioTrack next = this.manager.shuffled()
                    ? this.queue.remove(ThreadLocalRandom.current().nextInt(this.queue.size())) //probably not the smartest thing to be doing on a linked list, but most people won't be using shuffling anyway
                    : this.queue.poll();
            this.player.startTrack(next, false);
            return next != null;
        }
    }

    public AudioTrack[] queueSnapshot()    {
        synchronized (this.manager) {
            return this.queue.isEmpty() ? null : this.queue.toArray(new AudioTrack[this.queue.size()]);
        }
    }

    public void skipAll()  {
        synchronized (this.manager) {
            this.queue.clear();
            this.player.destroy();
            this.manager.doDisconnect();
        }
    }

    @Override
    public void onTrackEnd(AudioPlayer player, AudioTrack track, AudioTrackEndReason endReason) {
        synchronized (this.manager) {
            //System.out.printf("Ended \"%s\": %s\n", track.getInfo().title, endReason);

            if (endReason == AudioTrackEndReason.REPLACED || endReason.mayStartNext && this.next()) {
                return;
            }

            //shut down, we can't proceed
            this.skipAll();

            if (endReason == AudioTrackEndReason.CLEANUP) {
                this.manager.lastAccessedFrom().sendMessage("Player was idle for too long, stopping.").queue();
            }
        }
    }
}
