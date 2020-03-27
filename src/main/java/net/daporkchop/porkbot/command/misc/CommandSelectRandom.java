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

package net.daporkchop.porkbot.command.misc;

import lombok.NonNull;
import net.daporkchop.porkbot.command.Command;
import net.dv8tion.jda.api.MessageBuilder;
import net.dv8tion.jda.api.OnlineStatus;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Collectors;

/**
 * @author DaPorkchop_
 */
public class CommandSelectRandom extends Command {
    public static Set<Member> getMentionedMembers(@NonNull GuildMessageReceivedEvent evt, String rawContent, boolean fallbackToEveryone) {
        TextChannel channel = evt.getChannel();

        Set<Role> roles = new HashSet<>(evt.getMessage().getMentionedRoles());
        Set<Member> members = rawContent.contains("@here")
                ? channel.getMembers().stream().filter(m -> m.getOnlineStatus() == OnlineStatus.ONLINE).collect(Collectors.toSet())
                : new HashSet<>(rawContent.contains("@everyone") ? channel.getMembers() : evt.getMessage().getMentionedMembers());

        if (!roles.isEmpty()) {
            for (Member member : channel.getMembers()) {
                if (members.contains(member)) {
                    continue;
                }
                for (Role role : member.getRoles()) {
                    if (roles.contains(role)) {
                        members.add(member);
                    }
                }
            }
        } else if (members.isEmpty() && fallbackToEveryone) {
            members = new HashSet<>(channel.getMembers());
        }

        members.remove(channel.getGuild().getMember(evt.getJDA().getSelfUser()));
        return members;
    }

    public CommandSelectRandom() {
        super("select");
    }

    @Override
    public void execute(GuildMessageReceivedEvent evt, String[] args, String rawContent) {
        TextChannel channel = evt.getChannel();

        Set<Member> members = getMentionedMembers(evt, rawContent, false);
        List<Member> membersList = members.isEmpty() ? evt.getChannel().getMembers() : new ArrayList<>(members);

        channel.sendMessage(new MessageBuilder()
                .append(membersList.get(ThreadLocalRandom.current().nextInt(membersList.size())))
                .append(" has been selected!").build()).queue();
    }
}
