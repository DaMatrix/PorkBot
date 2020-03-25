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

package net.daporkchop.porkbot.command.mangement;

import net.daporkchop.porkbot.command.Command;
import net.daporkchop.porkbot.util.Constants;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.VoiceChannel;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;
import net.dv8tion.jda.api.requests.restaction.AuditableRestAction;

import java.util.function.Function;

/**
 * @author DaPorkchop_
 */
public class CommandMuteAll extends Command implements Function<Member, AuditableRestAction<Void>> {
    protected final String word;
    protected final boolean state;

    public CommandMuteAll(String prefix, String word, boolean state) {
        super(prefix);

        this.word = word;
        this.state = state;
    }

    @Override
    public void execute(GuildMessageReceivedEvent evt, String[] args, String rawContent) {
        Member member = evt.getMember();
        VoiceChannel channel = member.getVoiceState().getChannel();
        if (channel == null) {
            evt.getChannel().sendMessage("You are not connected to a voice channel!").queue();
            return;
        } else if (!member.hasPermission(channel, Permission.VOICE_MUTE_OTHERS)) {
            evt.getChannel().sendMessage("You do not have permission to mute others!").queue();
            return;
        } else if (!evt.getGuild().getMember(evt.getJDA().getSelfUser()).hasPermission(channel, Permission.VOICE_MUTE_OTHERS)) {
            evt.getChannel().sendMessage("Bot does not have permission to mute others!").queue();
            return;
        }

        channel.getMembers().stream()
                .filter(m -> !m.hasPermission(channel, Permission.VOICE_MUTE_OTHERS))
                .map(this)
                .forEach(AuditableRestAction::queue);

        evt.getChannel().sendMessage(this.word + " all users in " + channel.getName())
                .queue(Constants.DELETE_LATER);

        if (evt.getGuild().getMember(evt.getJDA().getSelfUser()).hasPermission(evt.getChannel(), Permission.MESSAGE_MANAGE)) {
            evt.getMessage().delete().queue();
        }
    }

    @Override
    public AuditableRestAction<Void> apply(Member member) {
        return member.mute(this.state);
    }
}