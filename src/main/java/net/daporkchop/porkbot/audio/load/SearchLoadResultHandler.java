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

import com.google.common.cache.Cache;
import com.sedmelluq.discord.lavaplayer.track.AudioPlaylist;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import lombok.NonNull;
import net.daporkchop.porkbot.audio.PorkAudio;
import net.daporkchop.porkbot.audio.ServerAudioManager;
import net.daporkchop.porkbot.util.Constants;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.entities.VoiceChannel;

import java.util.List;

/**
 * @author DaPorkchop_
 */
public class SearchLoadResultHandler extends FromURLLoadResultHandler {
    protected final Member requester;
    protected final Cache<String, AudioTrack[]> searchCache;
    protected final String                      prefixed;

    public SearchLoadResultHandler(TextChannel msgChannel, VoiceChannel dstChannel, ServerAudioManager manager, String input, @NonNull Member requester, @NonNull Cache<String, AudioTrack[]> searchCache, @NonNull String prefixed) {
        super(msgChannel, dstChannel, manager, input);

        this.requester = requester;
        this.searchCache = searchCache;
        this.prefixed = prefixed;
    }

    @Override
    public void trackLoaded(@NonNull AudioTrack track) {
        //if a single track is loaded, cache it and then call super to add it to the queue
        if (!Constants.DEV_MODE)    {
            this.searchCache.put(this.prefixed, new AudioTrack[]{ track });
        }

        super.trackLoaded(track);
    }

    @Override
    public void playlistLoaded(AudioPlaylist playlist) {
        if (!playlist.isSearchResult()) {
            this.msgChannel.sendMessage("Obtained playlist isn't a search result?!?").queue();
            return;
        }

        List<AudioTrack> list = playlist.getTracks();
        AudioTrack[] tracks = new AudioTrack[Math.min(Constants.MAX_SEARCH_RESULTS, list.size())];
        for (int i = 0; i < tracks.length; i++) {
            tracks[i] = list.get(i);
        }
        if (!Constants.DEV_MODE)    {
            this.searchCache.put(this.prefixed, tracks);
        }

        PorkAudio.handleSearchResults(tracks, this.manager, this.msgChannel, this.dstChannel, this.requester, PorkAudio.findPlatform(playlist), this.input);
    }
}
