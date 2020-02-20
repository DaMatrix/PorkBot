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
