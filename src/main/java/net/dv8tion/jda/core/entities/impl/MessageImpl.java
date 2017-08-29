/*
 * Adapted from the Wizardry License
 *
 * Copyright (c) 2016 DaPorkchop_
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

package net.dv8tion.jda.core.entities.impl;

import net.dv8tion.jda.client.entities.Group;
import net.dv8tion.jda.core.JDA;
import net.dv8tion.jda.core.MessageBuilder;
import net.dv8tion.jda.core.Permission;
import net.dv8tion.jda.core.entities.*;
import net.dv8tion.jda.core.exceptions.InsufficientPermissionException;
import net.dv8tion.jda.core.requests.RestAction;
import net.dv8tion.jda.core.requests.restaction.AuditableRestAction;
import net.dv8tion.jda.core.utils.Checks;
import net.dv8tion.jda.core.utils.Helpers;
import org.json.JSONObject;

import java.io.IOException;
import java.time.OffsetDateTime;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MessageImpl implements Message {
    private static final Pattern EMOTE_PATTERN = Pattern.compile("<:([^:]+):([0-9]+)>");

    private final JDAImpl api;
    private final long id;
    private final MessageType type;
    private final MessageChannel channel;
    private final boolean fromWebhook;
    private boolean mentionsEveryone = false;
    private boolean isTTS = false;
    private boolean pinned;
    private String content;
    private String subContent = null;
    private String strippedContent = null;
    private User author;
    //Not needed by porkbot: private OffsetDateTime time;
    private OffsetDateTime editedTime = null;
    private List<User> mentionedUsers = new LinkedList<>();
    private List<TextChannel> mentionedChannels = new LinkedList<>();
    private List<Role> mentionedRoles = new LinkedList<>();
    private List<Attachment> attachments = new LinkedList<>();
    private List<MessageEmbed> embeds = new LinkedList<>();
    private List<Emote> emotes = null;
    private List<MessageReaction> reactions = new LinkedList<>();

    public MessageImpl(long id, MessageChannel channel, boolean fromWebhook) {
        this(id, channel, fromWebhook, MessageType.DEFAULT);
    }

    public MessageImpl(long id, MessageChannel channel, boolean fromWebhook, MessageType type) {
        this.id = id;
        this.channel = channel;
        this.api = (channel != null) ? (JDAImpl) channel.getJDA() : null;
        this.fromWebhook = fromWebhook;
        this.type = type;
    }

    @Override
    public JDA getJDA() {
        return api;
    }

    @Override
    public boolean isPinned() {
        return pinned;
    }

    public MessageImpl setPinned(boolean pinned) {
        this.pinned = pinned;
        return this;
    }

    @Override
    public RestAction<Void> pin() {
        return channel.pinMessageById(getIdLong());
    }

    @Override
    public RestAction<Void> unpin() {
        return channel.unpinMessageById(getIdLong());
    }

    @Override
    public RestAction<Void> addReaction(Emote emote) {
        Checks.notNull(emote, "Emote");

        MessageReaction reaction = reactions.parallelStream()
                .filter(r -> Objects.equals(r.getEmote().getId(), emote.getId()))
                .findFirst().orElse(null);

        if (reaction == null) {
            checkFake(emote, "Emote");
            if (!emote.canInteract(api.getSelfUser(), channel))
                throw new IllegalArgumentException("Cannot react with the provided emote because it is not available in the current channel.");
        } else if (reaction.isSelf()) {
            return new RestAction.EmptyRestAction<>(getJDA(), null);
        }

        return channel.addReactionById(getIdLong(), emote);
    }

    @Override
    public RestAction<Void> addReaction(String unicode) {
        Checks.notEmpty(unicode, "Provided Unicode");

        MessageReaction reaction = reactions.parallelStream()
                .filter(r -> Objects.equals(r.getEmote().getName(), unicode))
                .findFirst().orElse(null);

        if (reaction != null && reaction.isSelf())
            return new RestAction.EmptyRestAction<>(getJDA(), null);

        return channel.addReactionById(getIdLong(), unicode);
    }

    @Override
    public RestAction<Void> clearReactions() {
        if (!isFromType(ChannelType.TEXT))
            throw new IllegalStateException("Cannot clear reactions from a message in a Group or PrivateChannel.");
        return getTextChannel().clearReactionsById(getId());
    }

    @Override
    public MessageType getType() {
        return type;
    }

    @Override
    public long getIdLong() {
        return id;
    }

    @Override
    public List<User> getMentionedUsers() {
        return Collections.unmodifiableList(mentionedUsers);
    }

    public MessageImpl setMentionedUsers(List<User> mentionedUsers) {
        this.mentionedUsers = mentionedUsers;
        return this;
    }

    @Override
    public boolean isMentioned(User user) {
        return mentionsEveryone() || mentionedUsers.contains(user);
    }

    @Override
    public List<TextChannel> getMentionedChannels() {
        return Collections.unmodifiableList(mentionedChannels);
    }

    public MessageImpl setMentionedChannels(List<TextChannel> mentionedChannels) {
        this.mentionedChannels = mentionedChannels;
        return this;
    }

    @Override
    public List<Role> getMentionedRoles() {
        return Collections.unmodifiableList(mentionedRoles);
    }

    public MessageImpl setMentionedRoles(List<Role> mentionedRoles) {
        this.mentionedRoles = mentionedRoles;
        return this;
    }

    @Override
    public boolean mentionsEveryone() {
        return mentionsEveryone;
    }

    @Override
    public boolean isEdited() {
        return editedTime != null;
    }

    @Override
    public OffsetDateTime getEditedTime() {
        return editedTime;
    }

    public MessageImpl setEditedTime(OffsetDateTime editedTime) {
        this.editedTime = editedTime;
        return this;
    }

    @Override
    public User getAuthor() {
        return author;
    }

    public MessageImpl setAuthor(User author) {
        this.author = author;
        return this;
    }

    @Override
    public Member getMember() {
        return isFromType(ChannelType.TEXT) ? getGuild().getMember(getAuthor()) : null;
    }

    @Override
    public synchronized String getStrippedContent() {
        if (strippedContent == null) {
            String tmp = getContent();
            //all the formatting keys to keep track of
            String[] keys = new String[]{"*", "_", "`", "~~"};

            //find all tokens (formatting strings described above)
            TreeSet<FormatToken> tokens = new TreeSet<>((t1, t2) -> Integer.compare(t1.start, t2.start));
            for (String key : keys) {
                Matcher matcher = Pattern.compile(Pattern.quote(key)).matcher(tmp);
                while (matcher.find()) {
                    tokens.add(new FormatToken(key, matcher.start()));
                }
            }

            //iterate over all tokens, find all matching pairs, and add them to the list toRemove
            Stack<FormatToken> stack = new Stack<>();
            List<FormatToken> toRemove = new ArrayList<>();
            boolean inBlock = false;
            for (FormatToken token : tokens) {
                if (stack.empty() || !stack.peek().format.equals(token.format) || stack.peek().start + token.format.length() == token.start) {
                    //we are at opening tag
                    if (!inBlock) {
                        //we are outside of block -> handle normally
                        if (token.format.equals("`")) {
                            //block start... invalidate all previous tags
                            stack.clear();
                            inBlock = true;
                        }
                        stack.push(token);
                    } else if (token.format.equals("`")) {
                        //we are inside of a block -> handle only block tag
                        stack.push(token);
                    }
                } else if (!stack.empty()) {
                    //we found a matching close-tag
                    toRemove.add(stack.pop());
                    toRemove.add(token);
                    if (token.format.equals("`") && stack.empty()) {
                        //close tag closed the block
                        inBlock = false;
                    }
                }
            }

            //sort tags to remove by their start-index and iteratively build the remaining string
            Collections.sort(toRemove, (t1, t2) -> Integer.compare(t1.start, t2.start));
            StringBuilder out = new StringBuilder();
            int currIndex = 0;
            for (FormatToken formatToken : toRemove) {
                if (currIndex < formatToken.start) {
                    out.append(tmp.substring(currIndex, formatToken.start));
                }
                currIndex = formatToken.start + formatToken.format.length();
            }
            if (currIndex < tmp.length()) {
                out.append(tmp.substring(currIndex));
            }
            //return the stripped text, escape all remaining formatting characters (did not have matching open/close before or were left/right of block
            strippedContent = out.toString().replace("*", "\\*").replace("_", "\\_").replace("~", "\\~");
        }
        return strippedContent;
    }

    @Override
    public synchronized String getContent() {
        if (subContent == null) {
            String tmp = content;
            for (User user : mentionedUsers) {
                if (isFromType(ChannelType.PRIVATE) || isFromType(ChannelType.GROUP)) {
                    tmp = tmp.replace("<@" + user.getId() + '>', '@' + user.getName())
                            .replace("<@!" + user.getId() + '>', '@' + user.getName());
                } else {
                    String name;
                    if (getGuild().isMember(user))
                        name = getGuild().getMember(user).getEffectiveName();
                    else name = user.getName();
                    tmp = tmp.replace("<@" + user.getId() + '>', '@' + name)
                            .replace("<@!" + user.getId() + '>', '@' + name);
                }
            }
            for (Emote emote : getEmotes()) {
                tmp = tmp.replace(emote.getAsMention(), ":" + emote.getName() + ":");
            }
            for (TextChannel mentionedChannel : mentionedChannels) {
                tmp = tmp.replace("<#" + mentionedChannel.getId() + '>', '#' + mentionedChannel.getName());
            }
            for (Role mentionedRole : mentionedRoles) {
                tmp = tmp.replace("<@&" + mentionedRole.getId() + '>', '@' + mentionedRole.getName());
            }
            subContent = tmp;
        }
        return subContent;
    }

    public MessageImpl setContent(String content) {
        this.content = content;
        return this;
    }

    @Override
    public String getRawContent() {
        return content;
    }

    @Override
    public boolean isFromType(ChannelType type) {
        return getChannelType() == type;
    }

    @Override
    public ChannelType getChannelType() {
        return channel.getType();
    }

    @Override
    public MessageChannel getChannel() {
        return channel;
    }

    @Override
    public PrivateChannel getPrivateChannel() {
        return isFromType(ChannelType.PRIVATE) ? (PrivateChannel) channel : null;
    }

    @Override
    public Group getGroup() {
        return isFromType(ChannelType.GROUP) ? (Group) channel : null;
    }

    @Override
    public TextChannel getTextChannel() {
        return isFromType(ChannelType.TEXT) ? (TextChannel) channel : null;
    }

    @Override
    public Guild getGuild() {
        return isFromType(ChannelType.TEXT) ? getTextChannel().getGuild() : null;
    }

    @Override
    public List<Attachment> getAttachments() {
        return Collections.unmodifiableList(attachments);
    }

    public MessageImpl setAttachments(List<Attachment> attachments) {
        this.attachments = attachments;
        return this;
    }

    @Override
    public List<MessageEmbed> getEmbeds() {
        return Collections.unmodifiableList(embeds);
    }

    public MessageImpl setEmbeds(List<MessageEmbed> embeds) {
        this.embeds = embeds;
        return this;
    }

    @Override
    public synchronized List<Emote> getEmotes() {
        if (this.emotes == null) {
            emotes = new LinkedList<>();
            Matcher matcher = EMOTE_PATTERN.matcher(getRawContent());
            while (matcher.find()) {
                final String emoteIdString = matcher.group(2);
                final long emoteId = Long.parseLong(emoteIdString);
                String emoteName = matcher.group(1);
                Emote emote = api.getEmoteById(emoteIdString);
                if (emote == null)
                    emote = new EmoteImpl(emoteId, api).setName(emoteName);
                emotes.add(emote);
            }
            emotes = Collections.unmodifiableList(emotes);
        }
        return emotes;
    }

    @Override
    public List<MessageReaction> getReactions() {
        return Collections.unmodifiableList(new LinkedList<>(reactions));
    }

    public MessageImpl setReactions(List<MessageReaction> reactions) {
        this.reactions = reactions;
        return this;
    }

    @Override
    public boolean isWebhookMessage() {
        return fromWebhook;
    }

    @Override
    public boolean isTTS() {
        return isTTS;
    }

    public MessageImpl setTTS(boolean TTS) {
        isTTS = TTS;
        return this;
    }

    @Override
    public RestAction<Message> editMessage(String newContent) {
        return editMessage(new MessageBuilder().append(newContent).build());
    }

    @Override
    public RestAction<Message> editMessage(MessageEmbed newContent) {
        return editMessage(new MessageBuilder().setEmbed(newContent).build());
    }

    @Override
    public RestAction<Message> editMessageFormat(String format, Object... args) {
        Checks.notBlank(format, "Format String");
        return editMessage(new MessageBuilder().appendFormat(format, args).build());
    }

    @Override
    public RestAction<Message> editMessage(Message newContent) {
        if (!api.getSelfUser().equals(getAuthor()))
            throw new IllegalStateException("Attempted to update message that was not sent by this account. You cannot modify other User's messages!");

        return getChannel().editMessageById(getIdLong(), newContent);
    }

    @Override
    public AuditableRestAction<Void> delete() {
        if (!getJDA().getSelfUser().equals(getAuthor())) {
            if (isFromType(ChannelType.PRIVATE) || isFromType(ChannelType.GROUP))
                throw new IllegalStateException("Cannot delete another User's messages in a Group or PrivateChannel.");
            else if (!getGuild().getSelfMember()
                    .hasPermission((TextChannel) getChannel(), Permission.MESSAGE_MANAGE))
                throw new InsufficientPermissionException(Permission.MESSAGE_MANAGE);
        }
        return channel.deleteMessageById(getIdLong());
    }

    public MessageImpl setMentionsEveryone(boolean mentionsEveryone) {
        this.mentionsEveryone = mentionsEveryone;
        return this;
    }

    public MessageImpl setTime(OffsetDateTime time) {
        //Not needed by porkbot: this.time = time;
        return this;
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof MessageImpl))
            return false;
        MessageImpl oMsg = (MessageImpl) o;
        return this == oMsg || this.id == oMsg.id;
    }

    @Override
    public int hashCode() {
        return Long.hashCode(id);
    }

    @Override
    public String toString() {
        return author != null
                ? String.format("M:%#s:%.20s(%s)", author, this, getId())
                : String.format("M:%.20s", this); // this message was made using MessageBuilder
    }

    public JSONObject toJSONObject() {
        JSONObject obj = new JSONObject();
        obj.put("content", content);
        obj.put("tts", isTTS);
        if (!embeds.isEmpty())
            obj.put("embed", ((MessageEmbedImpl) embeds.get(0)).toJSONObject());
        return obj;
    }

    private void checkPermission(Permission permission) {
        if (channel.getType() == ChannelType.TEXT) {
            Channel location = (Channel) channel;
            if (!location.getGuild().getSelfMember().hasPermission(location, permission))
                throw new InsufficientPermissionException(permission);
        }
    }

    private void checkFake(IFakeable o, String name) {
        if (o.isFake())
            throw new IllegalArgumentException("We are unable to use a fake " + name + " in this situation!");
    }

    @Override
    public void formatTo(Formatter formatter, int flags, int width, int precision) {
        boolean upper = (flags & FormattableFlags.UPPERCASE) == FormattableFlags.UPPERCASE;
        boolean leftJustified = (flags & FormattableFlags.LEFT_JUSTIFY) == FormattableFlags.LEFT_JUSTIFY;
        boolean alt = (flags & FormattableFlags.ALTERNATE) == FormattableFlags.ALTERNATE;

        String out = alt ? getRawContent() : getContent();

        if (upper)
            out = out.toUpperCase(formatter.locale());

        try {
            Appendable appendable = formatter.out();
            if (precision > -1 && out.length() > precision) {
                appendable.append(Helpers.truncate(out, precision - 3)).append("...");
                return;
            }

            if (leftJustified)
                appendable.append(Helpers.rightPad(out, width));
            else
                appendable.append(Helpers.leftPad(out, width));
        } catch (IOException e) {
            throw new AssertionError(e);
        }
    }

    private static class FormatToken {
        public final String format;
        public final int start;

        public FormatToken(String format, int start) {
            this.format = format;
            this.start = start;
        }
    }
}
