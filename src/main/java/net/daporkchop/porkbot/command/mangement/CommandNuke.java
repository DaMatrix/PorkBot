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

import lombok.NonNull;
import net.daporkchop.porkbot.command.Command;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageHistory;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;
import net.dv8tion.jda.api.utils.MiscUtil;
import net.dv8tion.jda.api.utils.TimeUtil;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

/**
 * @author DaPorkchop_
 */
public class CommandNuke extends Command {
    public CommandNuke() {
        super("nuke");
    }

    @Override
    public void execute(GuildMessageReceivedEvent evt, String[] args, String rawContent) {
        Member member = evt.getMember();
        TextChannel channel = evt.getChannel();
        if (!member.hasPermission(channel, Permission.MESSAGE_MANAGE)) {
            evt.getChannel().sendMessage("You do not have permission to manage messages!").queue();
            return;
        } else if (!evt.getGuild().getMember(evt.getJDA().getSelfUser()).hasPermission(channel, Permission.MESSAGE_MANAGE)) {
            evt.getChannel().sendMessage("Bot does not have permission to manage messages!").queue();
            return;
        } else if (!evt.getGuild().getMember(evt.getJDA().getSelfUser()).hasPermission(channel, Permission.MESSAGE_HISTORY)) {
            evt.getChannel().sendMessage("Bot does not have permission to view message history!").queue();
            return;
        }

        evt.getChannel().sendMessage("Nuking channel...").queue(msg -> new Nuker(msg).run());
    }

    private static class Nuker implements Runnable, Consumer<MessageHistory> {
        private final Message     message;
        private final TextChannel channel;

        private String currentMessage;

        private long count = 0L;

        public Nuker(@NonNull Message message) {
            this.message = message;
            this.channel = message.getTextChannel();
            this.currentMessage = message.getId();
        }

        @Override
        public void run() {
            this.channel.getHistoryBefore(this.message, 100).queue(this);
        }

        @Override
        public void accept(MessageHistory messageHistory) {
            List<Message> messages = new ArrayList<>(messageHistory.getRetrievedHistory());

            long twoWeeksAgo = TimeUtil.getDiscordTimestamp((System.currentTimeMillis() - (14 * 24 * 60 * 60 * 1000) + 5000L));
            boolean trimmed = messages.removeIf(msg -> MiscUtil.parseSnowflake(msg.getId()) < twoWeeksAgo);

            int size = messages.size();
            if (size > 1) {
                this.channel.deleteMessages(messages).queue(v -> {
                    this.count += size;
                    if (size == 100) {
                        this.message.editMessage("Nuking channel...\nDeleted " + this.count + " messages so far.").queue(msg -> this.run());
                    } else if (trimmed) {
                        this.message.editMessage("Nuked channel, deleting " + this.count + " messages.\n" +
                                "Note that not all messages could be deleted, as Discord does not allow bulk deletion of messages older than 2 weeks.").queue();
                    } else {
                        this.message.editMessage("Nuked channel, deleting " + this.count + " messages.").queue();
                    }
                });
            } else {
                if (size == 1) {
                    messages.get(0).delete().queue();
                }

                if (trimmed) {
                    this.message.editMessage("Nuked channel, deleting " + (this.count + size) + " messages.\n" +
                            "Note that not all messages could be deleted, as Discord does not allow bulk deletion of messages older than 2 weeks.").queue();
                } else {
                    this.message.editMessage("Nuked channel, deleting " + (this.count + size) + " messages.").queue();
                }
            }
        }
    }
}
