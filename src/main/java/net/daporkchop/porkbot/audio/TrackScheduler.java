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
import com.sedmelluq.discord.lavaplayer.track.AudioTrackState;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import net.daporkchop.porkbot.PorkBot;
import net.daporkchop.porkbot.audio.track.FutureTrack;
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
