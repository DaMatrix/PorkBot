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
import lombok.RequiredArgsConstructor;
import net.daporkchop.porkbot.PorkBot;
import net.daporkchop.porkbot.PorkListener;
import net.daporkchop.porkbot.command.Command;
import net.dv8tion.jda.api.MessageBuilder;
import net.dv8tion.jda.api.OnlineStatus;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Emote;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageReaction;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;
import net.dv8tion.jda.api.events.message.guild.react.GuildMessageReactionAddEvent;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import static net.daporkchop.porkbot.util.Constants.*;

/**
 * this was a request from an irl friend lol
 *
 * @author DaPorkchop_
 */
public class CommandNoseTouch extends Command {
    public CommandNoseTouch() {
        super("nosetouch");
    }

    @Override
    public void execute(GuildMessageReceivedEvent evt, String[] args, String rawContent) {
        TextChannel channel = evt.getChannel();

        if (!evt.getGuild().getMember(evt.getJDA().getSelfUser()).hasPermission(channel, Permission.MESSAGE_ADD_REACTION)) {
            channel.sendMessage("Missing permission to add reactions!").queue();
            return;
        } else if (!evt.getGuild().getMember(evt.getJDA().getSelfUser()).hasPermission(channel, Permission.MESSAGE_HISTORY)) {
            channel.sendMessage("Missing permission to view message history! (needed to add reactions)").queue();
            return;
        }

        Optional<Emote> optionalEmote = evt.getMessage().getEmotes().stream().findAny();
        Optional<String> optionalEmoji = optionalEmote.isPresent() ? Optional.empty()
                : Arrays.stream(args).filter(s -> EMOJI_PATTERN.matcher(s).find()).findAny();

        Set<Member> members = CommandSelectRandom.getMentionedMembers(evt, rawContent, true);
        if (members.isEmpty()) {
            channel.sendMessage("Must mention at least one role or member!").queue();
            return;
        } else if (members.size() == 1) {
            channel.sendMessage("Must mention at least 2 users!").queue();
            return;
        }

        if (optionalEmote.isPresent() && !optionalEmote.get().canInteract(evt.getJDA().getSelfUser(), channel)) {
            channel.sendMessage("Unable to use given emote!").queue();
            return;
        }
        Object emote = optionalEmote.isPresent() ? optionalEmote.get() :
                optionalEmoji.isPresent() ? optionalEmoji.get() : evt.getJDA().getEmoteById(692688500331642900L);

        MessageBuilder builder = new MessageBuilder().append("React with ");
        if (emote instanceof String) {
            builder.append((String) emote);
        } else {
            builder.append((Emote) emote);
        }
        channel.sendMessage(builder.append("! (").append(members.size()).append(" participants)").build()).queue(new Handler(members, emote));
    }

    @RequiredArgsConstructor
    private static class Handler implements Predicate<GuildMessageReactionAddEvent>, Consumer<Message>, Runnable {
        @NonNull
        protected final Set<Member> members;
        @NonNull
        protected final Object      emote;

        protected Message   message;
        protected Future<?> completeFuture;

        @Override
        public void accept(Message message) {
            this.message = message;

            PorkListener.REACTION_ADD_HANDLERS.put(message.getIdLong(), this);
            this.completeFuture = PorkBot.SCHEDULED_EXECUTOR.schedule(this, 2L, TimeUnit.MINUTES);

            if (this.emote instanceof String) {
                message.addReaction((String) this.emote).queue();
            } else {
                message.addReaction((Emote) this.emote).queue();
            }
        }

        @Override
        public synchronized boolean test(GuildMessageReactionAddEvent event) {
            if (!this.completeFuture.isDone() && this.members.size() > 1) {
                MessageReaction.ReactionEmote reactionEmote = event.getReactionEmote();

                boolean flag = this.emote instanceof String
                        ? reactionEmote.isEmoji() && reactionEmote.getEmoji().equals(this.emote)
                        : reactionEmote.isEmote() && reactionEmote.getEmote() == this.emote;
                if (flag && this.members.remove(event.getMember()) && this.members.size() == 1) {
                    Member last = this.members.iterator().next();
                    this.message.editMessage(new MessageBuilder().append(last).append(" was last!").build()).queue();

                    this.completeFuture.cancel(true);
                } else {
                    return false;
                }
            }
            return true;
        }

        @Override
        public synchronized void run() {
            if (!this.completeFuture.isDone()) {
                try {
                    MessageBuilder builder = new MessageBuilder().append("Cancelled due to inactivity.\nRemaining: ");
                    for (Iterator<Member> itr = this.members.iterator(); itr.hasNext(); ) {
                        builder.append(itr.next());
                        if (itr.hasNext()) {
                            builder.append(", ");
                        }
                    }
                    this.message.editMessage(builder.build()).queue();
                } catch (Exception e)   {
                    this.message.editMessage("Cancelled due to inactivity.").queue();
                }
            }
        }
    }
}
