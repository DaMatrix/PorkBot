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
import lombok.AllArgsConstructor;
import lombok.NonNull;
import net.daporkchop.porkbot.util.Constants;

import java.util.concurrent.CompletableFuture;

/**
 * @author DaPorkchop_
 */
@AllArgsConstructor
public class FutureSearchLoadResultHandler implements AudioLoadResultHandler {
    @NonNull
    protected final CompletableFuture<AudioTrack[]> future;

    @Override
    public void trackLoaded(AudioTrack track) {
        this.future.complete(new AudioTrack[]{track});
    }

    @Override
    public void playlistLoaded(AudioPlaylist playlist) {
        if (!playlist.isSearchResult()) {
            this.future.completeExceptionally(new IllegalStateException("obtained playlist isn't a search result?!?"));
            return;
        }
        this.future.complete(playlist.getTracks().stream().limit(Constants.MAX_SEARCH_RESULTS).toArray(AudioTrack[]::new));
    }

    @Override
    public void noMatches() {
        this.future.complete(new AudioTrack[0]);
    }

    @Override
    public void loadFailed(FriendlyException exception) {
        new RuntimeException(exception).printStackTrace();
        this.future.completeExceptionally(exception);
    }
}
