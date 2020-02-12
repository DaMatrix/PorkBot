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

package net.daporkchop.porkbot;

import net.daporkchop.porkbot.audio.PorkAudio;
import net.daporkchop.porkbot.audio.ServerAudioManager;
import net.daporkchop.porkbot.command.CommandRegistry;
import net.daporkchop.porkbot.util.Constants;
import net.daporkchop.porkbot.util.ShardUtils;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.ChannelType;
import net.dv8tion.jda.api.entities.VoiceChannel;
import net.dv8tion.jda.api.events.guild.voice.GuildVoiceMoveEvent;
import net.dv8tion.jda.api.events.guild.voice.GuildVoiceUpdateEvent;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;
import net.dv8tion.jda.api.exceptions.PermissionException;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

import javax.annotation.Nonnull;

public class PorkListener extends ListenerAdapter {
    @Override
    public void onGuildMessageReceived(@Nonnull GuildMessageReceivedEvent event) {
        if (event.getAuthor().isBot()
                || !event.getGuild().getMember(event.getJDA().getSelfUser()).hasPermission(event.getChannel(), Permission.MESSAGE_WRITE)) {
            return;
        }
        String message = event.getMessage().getContentRaw();

        if (message.startsWith(Constants.COMMAND_PREFIX))   {
            CommandRegistry.runCommand(event, message);
        } else {
            PorkAudio.checkSearchResponse(event, message);
        }
    }

    @Override
    public void onGuildVoiceUpdate(@Nonnull GuildVoiceUpdateEvent event) {
        if (event.getChannelLeft() != null) {
            ServerAudioManager manager = PorkAudio.getAudioManager(event.getChannelLeft().getGuild(), false);
            VoiceChannel connectedChannel = manager == null ? null : manager.connectedChannel();
            if (connectedChannel != null && connectedChannel.getMembers().size() == 1) {
                //we're the only one left in the channel
                manager.handleAllLeft();
            }
        }
    }
}
