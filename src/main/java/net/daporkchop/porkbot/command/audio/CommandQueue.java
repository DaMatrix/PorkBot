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

import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import net.daporkchop.lib.common.pool.handle.Handle;
import net.daporkchop.lib.common.util.PorkUtil;
import net.daporkchop.porkbot.audio.PorkAudio;
import net.daporkchop.porkbot.audio.ServerAudioManager;
import net.daporkchop.porkbot.audio.track.FutureTrack;
import net.daporkchop.porkbot.command.Command;
import net.daporkchop.porkbot.util.Constants;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;

import java.awt.Color;
import java.util.Arrays;

/**
 * @author DaPorkchop_
 */
public class CommandQueue extends Command {
    public CommandQueue() {
        super("queue");
    }

    @Override
    public void execute(GuildMessageReceivedEvent evt, String[] args, String rawContent) {
        ServerAudioManager manager = PorkAudio.getAudioManager(evt.getGuild(), true);
        FutureTrack[] tracks = manager.lastAccessedFrom(evt.getChannel()).scheduler().queueSnapshot();

        int page = 0;
        if (args.length >= 2) {
            try {
                page = Integer.parseUnsignedInt(args[1]) - 1;
            } catch (NumberFormatException e) {
                if (tracks == null) {
                    evt.getChannel().sendMessage("Invalid page number: `" + args[1] + "` (queue is empty)").queue();
                } else {
                    evt.getChannel().sendMessage("Invalid page number: `" + args[1] + "` (must be in range 1-" + (tracks.length / Constants.MAX_SEARCH_RESULTS + 1) + ')').queue();
                }
                return;
            }

            if (tracks != null && (page < 0 || page > tracks.length / Constants.MAX_SEARCH_RESULTS)) {
                evt.getChannel().sendMessage("Invalid page number: `" + (page + 1) + "` (must be in range 1-" + (tracks.length / Constants.MAX_SEARCH_RESULTS + 1) + ')').queue();
                return;
            }
        }

        EmbedBuilder builder = new EmbedBuilder().setColor(Color.BLACK);

        try (Handle<StringBuilder> handle = PorkUtil.STRINGBUILDER_POOL.get()) {
            StringBuilder sb = handle.value();
            sb.setLength(0);

            AudioTrack currentTrack = manager.player().getPlayingTrack();
            if (currentTrack == null) {
                builder.addField("Currently playing:", "*<not playing>*", false);
            } else {
                PorkAudio.appendTrackInfo(currentTrack.getInfo(), sb);
                builder.addField("Currently playing:", sb.toString(), false);
                sb.setLength(0);
            }

            if (tracks == null) {
                builder.addField("Queue:", "*<empty>*", false);
            } else {
                int pageSize = Math.min(Constants.MAX_SEARCH_RESULTS, tracks.length - page * Constants.MAX_SEARCH_RESULTS);
                for (int i = 0; i < pageSize; i++) {
                    sb.append('`').append(page * Constants.MAX_SEARCH_RESULTS + i + 1).append(':').append('`').append(' ');

                    PorkAudio.appendTrackInfo(tracks[page * Constants.MAX_SEARCH_RESULTS + i].getInfo(), sb).append('\n');
                }

                sb.setLength(sb.length() - 1);
                builder.addField("Queue (page " + (page + 1) + '/' + ((tracks.length - 1) / Constants.MAX_SEARCH_RESULTS + 1) + "):", sb.toString(), false);

                builder.addField("Total tracks:", String.valueOf(tracks.length), true)
                        .addField("Total runtime:", PorkAudio.formattedTrackLength(Arrays.stream(tracks).map(FutureTrack::getInfo)
                                .mapToLong(i -> i.length)
                                .filter(l -> l != Long.MAX_VALUE && l >= 0L)
                                .sum()), true)
                        .addField("Shuffled:", manager.shuffled() ? "Yes" : "No", true);
            }
        }

        evt.getChannel().sendMessage(builder.build()).queue();
    }

    @Override
    public String getUsage() {
        return "..queue [page number]";
    }

    @Override
    public String getUsageExample() {
        return "..queue 1";
    }
}
