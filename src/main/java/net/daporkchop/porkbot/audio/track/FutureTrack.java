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

package net.daporkchop.porkbot.audio.track;

import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import com.sedmelluq.discord.lavaplayer.track.AudioTrackInfo;
import com.sedmelluq.discord.lavaplayer.track.info.AudioTrackInfoBuilder;
import lombok.NonNull;
import net.dv8tion.jda.api.entities.VoiceChannel;

import java.util.function.BiConsumer;

/**
 * An audio track that may or may not have been resolved yet.
 *
 * @author DaPorkchop_
 */
public interface FutureTrack {
    AudioTrackInfo UNKNOWN_INFO = AudioTrackInfoBuilder.empty()
            .setTitle("Loading title...").setAuthor("Loading author...")
            .build();
    AudioTrackInfo FAILED_INFO = AudioTrackInfoBuilder.empty()
            .setTitle("Failed to load").setAuthor("Failed to load")
            .build();

    /**
     * @return the {@link VoiceChannel} that the track was requested to be played in
     */
    VoiceChannel requestedIn();

    /**
     * @return gets the track's {@link AudioTrackInfo}, or {@link #UNKNOWN_INFO} if it hasn't been resolved yet
     */
    AudioTrackInfo getInfo();

    /**
     * @return whether or not this track has been resolved
     */
    boolean isResolved();

    /**
     * @return whether or not this track has been resolved successfully
     */
    boolean isSuccess();

    /**
     * Runs the given callback function as soon as this track has been resolved.
     * <p>
     * If the track has already been resolved, this method may be run on the calling thread in a blocking manner.
     *
     * @param callback the function to run
     */
    void whenResolved(@NonNull BiConsumer<AudioTrack, Throwable> callback);

    AudioTrack getNow();
}
