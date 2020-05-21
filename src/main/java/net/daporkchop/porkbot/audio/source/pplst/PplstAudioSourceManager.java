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

package net.daporkchop.porkbot.audio.source.pplst;

import com.sedmelluq.discord.lavaplayer.container.MediaContainerDescriptor;
import com.sedmelluq.discord.lavaplayer.container.MediaContainerHints;
import com.sedmelluq.discord.lavaplayer.container.MediaContainerProbe;
import com.sedmelluq.discord.lavaplayer.container.MediaContainerRegistry;
import com.sedmelluq.discord.lavaplayer.player.DefaultAudioPlayerManager;
import com.sedmelluq.discord.lavaplayer.source.AudioSourceManager;
import com.sedmelluq.discord.lavaplayer.source.http.HttpAudioSourceManager;
import com.sedmelluq.discord.lavaplayer.source.http.HttpAudioTrack;
import com.sedmelluq.discord.lavaplayer.tools.FriendlyException;
import com.sedmelluq.discord.lavaplayer.track.AudioItem;
import com.sedmelluq.discord.lavaplayer.track.AudioReference;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import com.sedmelluq.discord.lavaplayer.track.AudioTrackInfo;
import com.sedmelluq.discord.lavaplayer.track.BasicAudioPlaylist;
import com.sedmelluq.discord.lavaplayer.track.info.AudioTrackInfoBuilder;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import net.daporkchop.lib.common.ref.Ref;
import net.daporkchop.lib.common.ref.ThreadRef;
import net.daporkchop.lib.common.util.PorkUtil;
import net.daporkchop.lib.http.request.Request;
import net.daporkchop.porkbot.util.Constants;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.BiConsumer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author DaPorkchop_
 */
@RequiredArgsConstructor
public final class PplstAudioSourceManager implements AudioSourceManager {
    private static final Pattern FILENAME_PATTERN = Pattern.compile("^https?://.*/(.*?)\\.pplst$");
    static final Ref<Matcher> FILENAME_PATTERN_MATCHER_CACHE = ThreadRef.soft(() -> FILENAME_PATTERN.matcher(""));

    private static final Pattern PPLST_PATTERN = Pattern.compile("^pplst(?>,name=\"(.*?)\")?$", Pattern.MULTILINE);
    static final Ref<Matcher> PPLST_PATTERN_MATCHER_CACHE = ThreadRef.soft(() -> PPLST_PATTERN.matcher(""));

    private static final Pattern LINE_PATTERN = Pattern.compile("^track(?>,album=\"(.*?)\")?(?>,artist=\"(.*?)\")?,format=\"(.*?)\",length=\"([0-9]+\\.[0-9]+)\"(?>,name=\"(.*?)\")?,path=\"(.*?)\"$", Pattern.MULTILINE);
    static final Ref<Matcher> LINE_PATTERN_MATCHER_CACHE = ThreadRef.soft(() -> LINE_PATTERN.matcher(""));

    @NonNull
    private final MediaContainerRegistry registry;
    @NonNull
    final HttpAudioSourceManager httpSourceManager;

    @Override
    public String getSourceName() {
        return "pplst";
    }

    @Override
    public AudioItem loadItem(DefaultAudioPlayerManager manager, AudioReference reference) {
        if (!(reference.identifier.startsWith("http://") || reference.identifier.startsWith("https://")) || !reference.identifier.endsWith(".pplst")) {
            return null;
        }

        AtomicReference<String> playlistName = new AtomicReference<>("Unknown playlist");

        List<AudioTrack> tracks = new ArrayList<>();
        this.loadPPLST(reference.identifier, playlistName,
                (info, probe) -> tracks.add(new HttpAudioTrack(info, new MediaContainerDescriptor(probe, null), this.httpSourceManager)));

        return new BasicAudioPlaylist(playlistName.get(), tracks, null, false);
    }

    public void loadPPLST(@NonNull String identifier, AtomicReference<String> playlistName, @NonNull BiConsumer<AudioTrackInfo, MediaContainerProbe> callback) {
        Request<String> request = Constants.BLOCKING_HTTP.request(identifier)
                .aggregateToString()
                .maxLength(1 << 22) // 4 MiB
                .send();

        if (!request.bodyFuture().awaitUninterruptibly().isSuccess()) {
            throw new FriendlyException("Unable to fetch .pplst playlist!", FriendlyException.Severity.COMMON, request.bodyFuture().cause());
        }
        String body = request.bodyFuture().getNow().body();

        if (playlistName != null) {
            Matcher matcher = FILENAME_PATTERN_MATCHER_CACHE.get().reset(identifier);
            if (matcher.find()) {
                playlistName.set(matcher.group(1));
            }

            matcher = PPLST_PATTERN_MATCHER_CACHE.get().reset(body);
            while (matcher.find()) {
                playlistName.set(PorkUtil.fallbackIfNull(matcher.group(1), playlistName.get()));
            }
        }

        Matcher matcher = LINE_PATTERN_MATCHER_CACHE.get().reset(body);
        while (matcher.find()) {
            String format = matcher.group(3);
            if ("m4a".equalsIgnoreCase(format)) {
                format = "mp4";
            }
            MediaContainerProbe probe = this.registry.find(format);
            if (probe == null) {
                MediaContainerHints hints = MediaContainerHints.from(null, format);
                for (MediaContainerProbe testProbe : this.registry.getAll()) {
                    if (testProbe.matchesHints(hints)) {
                        probe = testProbe;
                        break;
                    }
                }

                if (probe == null) {
                    continue;
                }
            }

            String path = matcher.group(6);
            try {
                //escape string correctly
                path = Constants.escapeUrl(path);
            } catch (IllegalArgumentException e) {
                continue; //invalid path
            }

            AudioTrackInfo info = AudioTrackInfoBuilder.empty()
                    .setAuthor(matcher.group(2))
                    .setLength((long) (Double.parseDouble(matcher.group(4)) * 1000.0d))
                    .setTitle(matcher.group(5))
                    .setIdentifier(path)
                    .setUri(path)
                    .build();

            callback.accept(info, probe);
        }
    }

    @Override
    public boolean isTrackEncodable(AudioTrack track) {
        return false;
    }

    @Override
    public void encodeTrack(AudioTrack track, DataOutput output) throws IOException {
        throw new UnsupportedOperationException();
    }

    @Override
    public AudioTrack decodeTrack(AudioTrackInfo trackInfo, DataInput input) throws IOException {
        throw new UnsupportedOperationException();
    }

    @Override
    public void shutdown() {
    }
}
