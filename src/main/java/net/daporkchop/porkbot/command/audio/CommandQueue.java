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
import com.sedmelluq.discord.lavaplayer.track.AudioTrackInfo;
import net.daporkchop.lib.common.pool.handle.Handle;
import net.daporkchop.lib.common.util.PorkUtil;
import net.daporkchop.porkbot.audio.PorkAudio;
import net.daporkchop.porkbot.audio.ServerAudioManager;
import net.daporkchop.porkbot.command.Command;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;

import java.awt.Color;

/**
 * @author DaPorkchop_
 */
public class CommandQueue extends Command {
    public CommandQueue() {
        super("queue");
    }

    @Override
    public void execute(GuildMessageReceivedEvent evt, String[] args, String rawContent) {
        ServerAudioManager manager = PorkAudio.getAudioManager(evt.getGuild(), false);
        if (manager == null || manager.player().getPlayingTrack() == null || manager.connectedChannel() == null)    {
            evt.getChannel().sendMessage("Not playing!").queue();
        } else {
            AudioTrack[] tracks = manager.lastAccessedFrom(evt.getChannel()).scheduler().queue();

            EmbedBuilder builder = new EmbedBuilder().setColor(Color.BLACK);
            try (Handle<StringBuilder> handle = PorkUtil.STRINGBUILDER_POOL.get())  {
                StringBuilder sb = handle.value();
                sb.setLength(0);

                for (int i = 0; i < tracks.length; i++) {
                    AudioTrackInfo info = tracks[i].getInfo();

                    sb.append('`').append(i + 1).append('.').append('`').append(' ')
                            .append('*').append(info.author).append("* - ")
                            .append(info.title).append(' ');

                    PorkAudio.formattedTrackLength(info.length, sb.append('`')).append('`').append('\n');
                }

                sb.setLength(sb.length() - 1);
                builder.addField("Queue:", sb.toString(), false);
            }

            evt.getChannel().sendMessage(builder.build()).queue();
        }
    }
}
