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
