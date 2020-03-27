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

import com.sedmelluq.discord.lavaplayer.player.AudioPlayer;
import com.sedmelluq.discord.lavaplayer.player.event.AudioEventAdapter;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import com.sedmelluq.discord.lavaplayer.track.AudioTrackEndReason;
import com.sedmelluq.discord.lavaplayer.track.AudioTrackState;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import net.daporkchop.porkbot.PorkBot;
import net.daporkchop.porkbot.audio.track.FutureTrack;
import net.daporkchop.porkbot.util.Config;
import net.dv8tion.jda.api.entities.Message;

import java.util.Collection;
import java.util.LinkedList;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;

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

    public int queueSize()  {
        return this.queue.size();
    }

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
                    synchronized (this.manager) {
                        if (e == null && this.manager.connect(next.requestedIn(), false)) {
                            this.player.startTrack(track, false);
                        } else {
                            //use executor to avoid stack overflow
                            PorkBot.SCHEDULED_EXECUTOR.submit(this::next);
                        }
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
    public void onTrackStart(AudioPlayer player, AudioTrack track) {
        synchronized (this.manager) {
            if (player.getPlayingTrack() != track) {
                //not sure if this could ever happen, but don't bother doing anything if it does
                return;
            }

            switch (track.getState()) {
                case INACTIVE:
                case LOADING:
                    //track isn't loaded yet, so wait a moment before trying again
                    PorkBot.SCHEDULED_EXECUTOR.schedule(() -> this.onTrackStart(player, track), 500L, TimeUnit.MILLISECONDS);
                    break;
                case PLAYING:
                    if (track.getUserData() != null) {
                        System.err.println("Message was already sent?!? A");
                        return;
                    }
                    this.manager.lastAccessedFrom().sendMessage(PorkAudio.embed(track, PorkAudio.findPlatform(track).embed())
                            .setTitle("Now playing", track.getInfo().uri)
                            .build())
                            .setCheck(() -> track.getState() == AudioTrackState.PLAYING)
                            .queue(message -> {
                                synchronized (this.manager) {
                                    if (track.getUserData() != null) {
                                        System.err.println("Message was already sent?!? B");
                                        return;
                                    }

                                    track.setUserData(message);
                                }
                            });
                    break;
            }
        }
    }

    @Override
    public void onTrackEnd(AudioPlayer player, AudioTrack track, AudioTrackEndReason endReason) {
        if (endReason != AudioTrackEndReason.LOAD_FAILED) {
            Config.TIME_PLAYED_TOTAL.getAndAdd(track.getPosition());
            Config.TRACKS_PLAYED_TOTAL.getAndIncrement();
        }

        synchronized (this.manager) {
            Message message = track.getUserData(Message.class);
            if (message != null) {
                message.delete().queue();
            }

            System.out.printf("Removed track: \"%s\", state: %s, reason: %s\n", track.getInfo().title, track.getState(), endReason);

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
