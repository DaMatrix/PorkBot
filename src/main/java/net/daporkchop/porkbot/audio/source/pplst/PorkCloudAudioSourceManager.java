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

import com.google.common.collect.Comparators;
import com.sedmelluq.discord.lavaplayer.container.MediaContainerDescriptor;
import com.sedmelluq.discord.lavaplayer.container.MediaContainerProbe;
import com.sedmelluq.discord.lavaplayer.player.DefaultAudioPlayerManager;
import com.sedmelluq.discord.lavaplayer.source.AudioSourceManager;
import com.sedmelluq.discord.lavaplayer.source.http.HttpAudioSourceManager;
import com.sedmelluq.discord.lavaplayer.source.http.HttpAudioTrack;
import com.sedmelluq.discord.lavaplayer.track.AudioItem;
import com.sedmelluq.discord.lavaplayer.track.AudioReference;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import com.sedmelluq.discord.lavaplayer.track.AudioTrackInfo;
import com.sedmelluq.discord.lavaplayer.track.BasicAudioPlaylist;
import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.experimental.Accessors;
import net.daporkchop.lib.unsafe.PUnsafe;
import net.daporkchop.porkbot.audio.SearchPlatform;
import net.daporkchop.porkbot.util.Constants;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * @author DaPorkchop_
 */
@RequiredArgsConstructor
public final class PorkCloudAudioSourceManager implements AudioSourceManager {
    private static final String URL = "https://cloud.daporkchop.net/random/music/list.pplst";

    private static final Pattern SEARCH_PATTERN = Pattern.compile("(?: |^porksearch:)(.*?)(?= |$)");

    @NonNull
    private final PplstAudioSourceManager pplstSourceManager;

    @Override
    public String getSourceName() {
        return SearchPlatform.DAPORKCHOP.name();
    }

    @Override
    public AudioItem loadItem(DefaultAudioPlayerManager manager, AudioReference reference) {
        if (!reference.identifier.startsWith(SearchPlatform.DAPORKCHOP.prefix())) {
            return null;
        }

        Collection<Track> tracks = new ArrayList<>();
        this.pplstSourceManager.loadPPLST(URL, null, (info, probe) -> tracks.add(new Track(info, probe)));
        Set<String> words = Collections.newSetFromMap(new TreeMap<>(String.CASE_INSENSITIVE_ORDER));
        Matcher matcher = SEARCH_PATTERN.matcher(reference.identifier);
        while (matcher.find())  {
            words.add(matcher.group(1));
        }

        for (Track track : tracks)  {
            for (String s : track.author().split(" "))  {
                if (words.contains(s))   {
                    track.increment();
                }
            }
            for (String s : track.title().split(" "))  {
                if (words.contains(s))   {
                    track.increment();
                }
            }
        }

        List<AudioTrack> realTracks = tracks.stream()
                .sorted(Comparator.comparingInt(Track::references).reversed())
                .limit(Constants.MAX_SEARCH_RESULTS)
                .map(track -> new PorkCloudAudioTrack(track.info, new MediaContainerDescriptor(track.probe, null), this.pplstSourceManager.httpSourceManager))
                .collect(Collectors.toList());

        return new BasicAudioPlaylist("Search results", realTracks, null, true);
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

    public static class PorkCloudAudioTrack extends HttpAudioTrack  {
        private static final long CONTAINERTRACKFACTORY_OFFSET = PUnsafe.pork_getOffset(HttpAudioTrack.class, "containerTrackFactory");
        private static final long SOURCEMANAGER_OFFSET = PUnsafe.pork_getOffset(HttpAudioTrack.class, "sourceManager");

        public PorkCloudAudioTrack(AudioTrackInfo trackInfo, MediaContainerDescriptor containerTrackFactory, HttpAudioSourceManager sourceManager) {
            super(trackInfo, containerTrackFactory, sourceManager);
        }

        @Override
        protected AudioTrack makeShallowClone() {
            return new PorkCloudAudioTrack(this.trackInfo, PUnsafe.getObject(this, CONTAINERTRACKFACTORY_OFFSET), PUnsafe.getObject(this, SOURCEMANAGER_OFFSET));
        }
    }

    @RequiredArgsConstructor
    @Accessors(fluent = true)
    private static final class Track    {
        @NonNull
        private final AudioTrackInfo info;
        @NonNull
        private final MediaContainerProbe probe;

        @Getter
        private int references = 0;

        public void increment() {
            this.references++;
        }

        public String author()   {
            return this.info.author;
        }

        public String title()   {
            return this.info.title;
        }
    }
}
