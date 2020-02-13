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

package net.daporkchop.porkbot.command.audio;

import net.daporkchop.lib.binary.oio.StreamUtil;
import net.daporkchop.lib.common.cache.Cache;
import net.daporkchop.lib.common.function.PFunctions;
import net.daporkchop.lib.common.util.PorkUtil;
import net.daporkchop.lib.http.Http;
import net.daporkchop.porkbot.audio.PorkAudio;
import net.daporkchop.porkbot.audio.SearchPlatform;
import net.daporkchop.porkbot.audio.ServerAudioManager;
import net.daporkchop.porkbot.audio.track.FutureTrack;
import net.daporkchop.porkbot.audio.track.LateResolvingTrack;
import net.daporkchop.porkbot.audio.track.LateResolvingTrackGroup;
import net.daporkchop.porkbot.command.Command;
import net.daporkchop.porkbot.util.Constants;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.VoiceChannel;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Queue;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author DaPorkchop_
 */
public class CommandPlayList extends Command {
    private static final Pattern        URL_PATTERN               = Pattern.compile("^(https?://.+)$", Pattern.MULTILINE);
    private static final Cache<Matcher> URL_PATTERN_MATCHER_CACHE = Cache.soft(() -> URL_PATTERN.matcher(""));

    public CommandPlayList() {
        super("playlist");
    }

    @Override
    public void execute(GuildMessageReceivedEvent evt, String[] args, String rawContent) {
        Matcher matcher;
        VoiceChannel dstChannel;

        if (args.length < 2 || args[1].isEmpty()) {
            this.sendErrorMessage(evt.getChannel(), "No list URL given!");
        } else if ((dstChannel = evt.getMember().getVoiceState().getChannel()) == null) {
            evt.getChannel().sendMessage("You must be in a voice channel to play audio!").queue();
        } else if ((matcher = URL_PATTERN_MATCHER_CACHE.get().reset(args[1])).matches()) {
            try {
                matcher.reset(Http.getString(args[1]));

                Queue<LateResolvingTrack> tracks = new LinkedList<>();
                while (matcher.find())  {
                    tracks.add(new LateResolvingTrack(Constants.escapeUrl(matcher.group(1)), dstChannel));
                }
                PorkAudio.getAudioManager(evt.getGuild(), true).lastAccessedFrom(evt.getChannel()).scheduler().enqueueAll((Collection<FutureTrack>) (Object) tracks);

                new LateResolvingTrackGroup(tracks).run();

                evt.getChannel().sendMessage("Enqueued " + tracks.size() + " tracks!").queue();
            } catch (Exception e)   {
                evt.getChannel().sendMessage("Unable to fetch or parse list!").queue();
            }
        } else {
            this.sendErrorMessage(evt.getChannel(), "Invalid URL!");
        }
    }

    @Override
    public String getUsage() {
        return "..playlist <url>";
    }

    @Override
    public String getUsageExample() {
        return "..playlist https://cloud.daporkchop.net/random/music/httplist.txt";
    }
}
