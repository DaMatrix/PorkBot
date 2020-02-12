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

package net.daporkchop.porkbot.audio.track;

import com.sedmelluq.discord.lavaplayer.player.AudioLoadResultHandler;
import com.sedmelluq.discord.lavaplayer.tools.FriendlyException;
import com.sedmelluq.discord.lavaplayer.track.AudioPlaylist;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import com.sedmelluq.discord.lavaplayer.track.AudioTrackInfo;
import lombok.Getter;
import lombok.NonNull;
import lombok.experimental.Accessors;
import net.daporkchop.lib.unsafe.PUnsafe;
import net.daporkchop.porkbot.audio.PorkAudio;
import net.dv8tion.jda.api.entities.VoiceChannel;

import java.util.LinkedList;
import java.util.List;
import java.util.function.BiConsumer;

/**
 * @author DaPorkchop_
 */
@Accessors(fluent = true)
public final class LateResolvingTrack implements FutureTrack, AudioLoadResultHandler {
    @Getter
    private final VoiceChannel                            requestedIn;
    private       List<BiConsumer<AudioTrack, Throwable>> listeners;
    private       Object                                  track;

    private String url;

    public LateResolvingTrack(@NonNull String url, @NonNull VoiceChannel requestedIn) {
        this.requestedIn = requestedIn;
        this.url = url;
    }

    public synchronized void start() {
        if (this.url == null) {
            return;
        }

        //PorkAudio.PLAYER_MANAGER.loadItemOrdered(PorkAudio.getAudioManager(requestedIn.getGuild(), true), url, this);
        PorkAudio.PLAYER_MANAGER.loadItem(this.url, this);

        this.url = null;
    }

    public synchronized int listeners() {
        return this.listeners == null ? 0 : this.listeners.size();
    }

    @Override
    public synchronized AudioTrackInfo getInfo() {
        return this.track instanceof AudioTrack
                ? ((AudioTrack) this.track).getInfo()
                : this.track == null ? UNKNOWN_INFO : FAILED_INFO;
    }

    @Override
    public boolean isResolved() {
        return this.track != null;
    }

    @Override
    public boolean isSuccess() {
        return this.track instanceof AudioTrack;
    }

    @Override
    public  void whenResolved(@NonNull BiConsumer<AudioTrack, Throwable> callback) {
        synchronized (this) {
            if (this.track == null) {
                if (this.listeners == null) {
                    this.listeners = new LinkedList<>();
                }
                this.listeners.add(callback);
                return;
            }
        }
        if (this.track instanceof AudioTrack) {
            callback.accept((AudioTrack) this.track, null);
        } else {
            callback.accept(null, (Throwable) this.track);
        }
    }

    @Override
    public synchronized AudioTrack getNow() {
        if (this.track instanceof AudioTrack)   {
            return (AudioTrack) this.track;
        } else if (this.track instanceof Throwable) {
            PUnsafe.throwException((Throwable) this.track);
            return null;
        } else {
            throw new IllegalStateException();
        }
    }

    @Override
    public void trackLoaded(AudioTrack track) {
        synchronized (this) {
            if (this.track != null) {
                throw new IllegalStateException();
            }
            this.track = track;
        }
        if (this.listeners != null) {
            for (BiConsumer<AudioTrack, Throwable> listener : this.listeners) {
                listener.accept(track, null);
            }
            this.listeners = null;
        }
    }

    @Override
    public void playlistLoaded(AudioPlaylist playlist) {
        //we don't accept playlists being loaded in a late fashion
        this.doFinish(new RuntimeException("Value was a playlist!"));
    }

    @Override
    public void noMatches() {
        this.doFinish(new RuntimeException("No matches!"));
    }

    @Override
    public void loadFailed(FriendlyException exception) {
        this.doFinish(exception);
    }

    private void doFinish(Throwable t) {
        synchronized (this) {
            if (this.track != null) {
                throw new IllegalStateException();
            }
            this.track = t;
        }
        if (this.listeners != null) {
            for (BiConsumer<AudioTrack, Throwable> listener : this.listeners) {
                listener.accept(null, t);
            }
            this.listeners = null;
        }
    }
}
