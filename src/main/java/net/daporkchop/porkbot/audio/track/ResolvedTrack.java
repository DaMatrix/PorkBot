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

import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import com.sedmelluq.discord.lavaplayer.track.AudioTrackInfo;
import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.experimental.Accessors;
import net.dv8tion.jda.api.entities.VoiceChannel;

import java.util.function.BiConsumer;

/**
 * A {@link FutureTrack} that has already been resolved.
 *
 * @author DaPorkchop_
 */
@RequiredArgsConstructor
@Accessors(fluent = true)
public final class ResolvedTrack implements FutureTrack {
    @NonNull
    private final AudioTrack track;
    @Getter
    @NonNull
    private final VoiceChannel requestedIn;

    @Override
    public AudioTrackInfo getInfo() {
        return this.track.getInfo();
    }

    @Override
    public boolean isResolved() {
        return true;
    }

    @Override
    public boolean isSuccess() {
        return true;
    }

    @Override
    public void whenResolved(@NonNull BiConsumer<AudioTrack, Throwable> callback) {
        callback.accept(this.track, null);
    }

    @Override
    public AudioTrack getNow() {
        return this.track;
    }
}
