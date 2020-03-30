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

package net.daporkchop.porkbot.util;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.MessageBuilder;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;
import net.dv8tion.jda.api.exceptions.PermissionException;
import net.dv8tion.jda.api.requests.restaction.MessageAction;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.sql.Timestamp;
import java.time.Instant;

public class MessageUtils {
    public static void sendMessage(String s, TextChannel channel) {
        sendMessage(channel.sendMessage(s), channel);
    }

    public static void sendMessage(EmbedBuilder builder, TextChannel channel) {
        builder.setTimestamp(Instant.now());
        sendMessage(channel.sendMessage(builder.build()), channel);
    }

    public static void sendMessage(MessageAction action, TextChannel channel) {
        try {
            action.queue();
        } catch (PermissionException e) {
            //we can't do anything about it
            if (e.getPermission() != Permission.MESSAGE_WRITE) {
                channel.sendMessage("Missing permission: "+ e.getPermission()).queue();
            }
        }
    }

    /**
     * Sends a message to a channel
     *
     * @param builder
     * @param channel
     */
    public static void sendMessage(MessageBuilder builder, TextChannel channel) {
        try {
            if (false)  {
                Thread.sleep(500);
            }
            channel.sendMessage(builder.build()).queue();
        } catch (PermissionException e) {
            //we can't do anything about it
            if (e.getPermission() == Permission.MESSAGE_EMBED_LINKS) {
                channel.sendMessage("Lacking permission to embed links!").queue();
            }
        } catch (InterruptedException e) {
            //wtf java
        }
    }

    /**
     * Sends an excepion using the given GuildMessageReceivedEvent's channel
     *
     * @param e   the exception to print
     * @param evt the channel from this event is used to send the message
     */
    public static void sendException(Exception e, GuildMessageReceivedEvent evt) {
        e.printStackTrace();
        sendMessage("Error running command: `" + evt.getMessage().getContentRaw() + "`:\n`" + e.getClass().getCanonicalName() + "`: " + e.getMessage(), evt.getChannel());
    }

}
