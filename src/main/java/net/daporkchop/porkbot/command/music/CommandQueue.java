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

package net.daporkchop.porkbot.command.music;

import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import net.daporkchop.porkbot.audio.AudioUtils;
import net.daporkchop.porkbot.audio.GuildAudioInfo;
import net.daporkchop.porkbot.command.Command;
import net.daporkchop.porkbot.util.MessageUtils;
import net.dv8tion.jda.core.events.message.MessageReceivedEvent;

import java.util.ArrayList;
import java.util.concurrent.TimeUnit;

public class CommandQueue extends Command {
    public CommandQueue() {
        super("queue");
    }

    public void execute(MessageReceivedEvent evt, String[] args, String rawContent) {
        GuildAudioInfo info = AudioUtils.getGuildAudioPlayer(evt.getGuild(), false);
        if (info == null) {
            evt.getTextChannel().sendMessage("Not playing!").queue();
        } else {
            String msg = "Queue: `" + info.manager.scheduler.queue.size() + "` tracks queued\n\nCurrently playing: `" + info.manager.scheduler.player.getPlayingTrack().getInfo().title + "`\n\nQueue:\n";
            ArrayList<AudioTrack> tracks = new ArrayList<>(info.manager.scheduler.queue);
            for (int i = 0; i < 5 && i < tracks.size(); i++) {
                AudioTrack track = tracks.get(i);
                long minutes = TimeUnit.MILLISECONDS.toMinutes(track.getInfo().length);
                long seconds = TimeUnit.MILLISECONDS.toSeconds(track.getInfo().length) - (minutes * 60L);
                msg += (i + 1) + ": " + tracks.get(i).getInfo().title + "(`" + minutes + ":" + seconds + "`)\n";
            }
            MessageUtils.sendMessage(msg, evt.getTextChannel());
        }
    }
}
