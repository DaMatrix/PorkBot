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

import net.daporkchop.porkbot.audio.PorkAudio;
import net.daporkchop.porkbot.audio.ServerAudioManager;
import net.daporkchop.porkbot.command.Command;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;

/**
 * @author DaPorkchop_
 */
public class CommandStop extends Command {
    public CommandStop() {
        super("stop");
    }

    @Override
    public void execute(GuildMessageReceivedEvent evt, String[] args, String rawContent) {
        ServerAudioManager manager = PorkAudio.getAudioManager(evt.getGuild(), false);
        if (manager == null || manager.player().getPlayingTrack() == null || manager.connectedChannel() == null)    {
            evt.getChannel().sendMessage("Not playing!").queue();
        } else if (manager.connectedChannel() != evt.getMember().getVoiceState().getChannel())  {
            evt.getChannel().sendMessage("Must be in the same voice channel!").queue();
        } else {
            manager.lastAccessedFrom(evt.getChannel()).scheduler().skipAll();
            evt.getChannel().sendMessage("Stopped!").queue();
        }
    }
}
