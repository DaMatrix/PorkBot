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
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import net.daporkchop.porkbot.PorkBot;

import java.util.Collection;
import java.util.Comparator;
import java.util.Queue;
import java.util.function.BiConsumer;

/**
 * @author DaPorkchop_
 */
@RequiredArgsConstructor
public final class LateResolvingTrackGroup implements Runnable {
    @NonNull
    private final Queue<LateResolvingTrack> tracks;

    @Override
    public synchronized void run()    {
        if (this.tracks.isEmpty())  {
            return;
        }

        LateResolvingTrack track = this.tracks.stream()
                .max(Comparator.comparingInt(LateResolvingTrack::listeners))
                .get();

        track.start();
        track.whenResolved((realTrack, t) -> {
            this.tracks.remove(track);

            PorkBot.SCHEDULED_EXECUTOR.submit(this);
        });
    }
}
