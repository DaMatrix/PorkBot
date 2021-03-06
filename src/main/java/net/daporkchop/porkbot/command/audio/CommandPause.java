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

package net.daporkchop.porkbot.command.audio;

import net.daporkchop.porkbot.audio.PorkAudio;
import net.daporkchop.porkbot.audio.ServerAudioManager;
import net.daporkchop.porkbot.command.Command;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;

/**
 * @author DaPorkchop_
 */
public class CommandPause extends Command {
    public CommandPause() {
        super("pause");
    }

    @Override
    public void execute(GuildMessageReceivedEvent evt, String[] args, String rawContent) {
        ServerAudioManager manager = PorkAudio.getAudioManager(evt.getGuild(), false);
        if (manager == null) {
            evt.getChannel().sendMessage("Not playing!").queue();
            return;
        } else {
            synchronized (manager) {
                if (manager.connectedChannel() == null || manager.player().getPlayingTrack() == null) {
                    evt.getChannel().sendMessage("Not playing!").queue();
                } else if (manager.connectedChannel() != evt.getMember().getVoiceState().getChannel()) {
                    evt.getChannel().sendMessage("Must be in the same voice channel!").queue();
                } else if (manager.player().isPaused()) {
                    evt.getChannel().sendMessage("Already paused!").queue();
                } else {
                    manager.pause();

                    if (evt.getGuild().getMember(evt.getJDA().getSelfUser()).hasPermission(evt.getChannel(), Permission.MESSAGE_MANAGE)) {
                        evt.getMessage().delete().queue();
                    }
                }
            }
        }
    }

    @Override
    public boolean shouldSendTyping() {
        return false;
    }
}
