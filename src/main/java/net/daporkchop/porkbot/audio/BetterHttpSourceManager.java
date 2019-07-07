/*
 * Adapted from the Wizardry License
 *
 * Copyright (c) 2016-2019 DaPorkchop_
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

import com.sedmelluq.discord.lavaplayer.container.*;
import com.sedmelluq.discord.lavaplayer.player.DefaultAudioPlayerManager;
import com.sedmelluq.discord.lavaplayer.source.ProbingAudioSourceManager;
import com.sedmelluq.discord.lavaplayer.source.http.HttpAudioSourceManager;
import com.sedmelluq.discord.lavaplayer.source.http.HttpAudioTrack;
import com.sedmelluq.discord.lavaplayer.tools.FriendlyException;
import com.sedmelluq.discord.lavaplayer.tools.io.*;
import com.sedmelluq.discord.lavaplayer.track.*;
import net.daporkchop.porkbot.util.HTTPUtils;
import net.dv8tion.jda.core.entities.Message;
import org.apache.commons.validator.routines.UrlValidator;
import org.apache.http.HttpStatus;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.impl.client.HttpClientBuilder;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Collectors;

import static com.sedmelluq.discord.lavaplayer.tools.FriendlyException.Severity.COMMON;
import static com.sedmelluq.discord.lavaplayer.tools.FriendlyException.Severity.SUSPICIOUS;
import static com.sedmelluq.discord.lavaplayer.tools.io.HttpClientTools.getHeaderValue;

/**
 * @author DaPorkchop_
 */
public class BetterHttpSourceManager extends HttpAudioSourceManager {
    private static final Field audioTrackInfo_title;
    private static final Method httpAudioSourceManager_detectContainer;

    static {
        Field f = null;
        try {
            f = AudioTrackInfo.class.getDeclaredField("title");
            f.setAccessible(true);
        } catch (Exception e)   {
            throw new RuntimeException(e);
        } finally {
            audioTrackInfo_title = f;
        }
    }

    static {
        Method m = null;
        try {
            m = HttpAudioSourceManager.class.getDeclaredMethod("detectContainer", AudioReference.class);
            m.setAccessible(true);
        } catch (NoSuchMethodException e)   {
            throw new RuntimeException(e);
        } finally {
            httpAudioSourceManager_detectContainer = m;
        }
    }

    @Override
    public String getSourceName() {
        return "http";
    }

    @Override
    public AudioItem loadItem(DefaultAudioPlayerManager manager, AudioReference reference) {
        return this.loadItem(manager, reference, true);
    }

    public AudioItem loadItem(DefaultAudioPlayerManager manager, AudioReference reference, boolean allowPlaylist) {
        AudioReference httpReference = getAsHttpReference(reference);
        if (httpReference == null) {
            return null;
        }

        if (allowPlaylist && httpReference.identifier.endsWith(".txt"))  {
            try {
                List<AudioTrack> items = Arrays.stream(new String(HTTPUtils.get(httpReference.identifier, 32768), StandardCharsets.UTF_8).trim().split("\n"))
                        .map(String::trim)
                        .filter(s -> !s.isEmpty() && !s.contains("~"))
                        .distinct()
                        .map(s -> new AudioReference(s, null))
                        .map(ref -> this.loadItem(manager, ref, false))
                        .filter(o -> o instanceof AudioTrack)
                        .map(AudioTrack.class::cast)
                        .collect(Collectors.toList());
                items.forEach(track -> {
                    if (track.getInfo().title == null || MediaContainerDetection.UNKNOWN_TITLE.equals(track.getInfo().title))  {
                        String[] split = track.getIdentifier().split("/");
                        try {
                            audioTrackInfo_title.set(track.getInfo(), split[split.length - 1].replaceAll("%20", " "));
                        } catch (Exception e)   {
                            throw new RuntimeException(e);
                        }
                    }
                });
                return new BasicAudioPlaylist(httpReference.identifier, items, null, false);
            } catch (IOException e) {
                e.printStackTrace();
                return null;
            }
        }

        try {
            return handleLoadResult((MediaContainerDetectionResult) httpAudioSourceManager_detectContainer.invoke(this, httpReference));
        } catch (InvocationTargetException | IllegalAccessException e)   {
            throw new RuntimeException(e);
        }
    }
}
