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
import net.daporkchop.porkbot.PorkBot;
import net.daporkchop.porkbot.audio.track.FutureTrack;

import java.util.Collection;
import java.util.LinkedList;
import java.util.concurrent.ThreadLocalRandom;

/**
 * @author DaPorkchop_
 */
@RequiredArgsConstructor
public final class TrackScheduler extends AudioEventAdapter {
    @NonNull
    private final AudioPlayer        player;
    @NonNull
    private final ServerAudioManager manager;

    private final LinkedList<FutureTrack> queue = new LinkedList<>();

    public void enqueue(@NonNull FutureTrack track) {
        synchronized (this.manager) {
            this.queue.add(track);

            if (this.player.getPlayingTrack() == null) {
                this.next();
            }
        }
    }

    public void enqueueAll(@NonNull Collection<FutureTrack> tracks) {
        synchronized (this.manager) {
            this.queue.addAll(tracks);

            if (this.player.getPlayingTrack() == null) {
                this.next();
            }
        }
    }

    public boolean next() {
        synchronized (this.manager) {
            FutureTrack next = this.manager.shuffled()
                    ? this.queue.remove(ThreadLocalRandom.current().nextInt(this.queue.size())) //probably not the smartest thing to be doing on a linked list, but most people won't be using shuffling anyway
                    : this.queue.poll();
            if (next != null) {
                next.whenResolved((track, e) -> {
                    if (e == null) {
                        this.manager.connect(next.requestedIn());
                        this.player.startTrack(track, false);
                    } else {
                        //use executor to avoid stack overflow if there are a lot of consecutive errored tracks
                        PorkBot.SCHEDULED_EXECUTOR.submit(this::next);
                    }
                });
                return true;
            } else {
                this.player.startTrack(null, false);
                return false;
            }
        }
    }

    public FutureTrack[] queueSnapshot() {
        synchronized (this.manager) {
            return this.queue.isEmpty() ? null : this.queue.toArray(new FutureTrack[this.queue.size()]);
        }
    }

    public void skipAll() {
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
