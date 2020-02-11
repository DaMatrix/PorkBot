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

package net.daporkchop.porkbot.util;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.MessageBuilder;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;
import net.dv8tion.jda.api.exceptions.PermissionException;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.sql.Timestamp;
import java.time.Instant;

public class MessageUtils {
    /**
     * Sends a message to a channel
     *
     * @param s
     * @param channel
     */
    public static void sendMessage(String s, TextChannel channel) {
        try {
            Thread.sleep(500);
            channel.sendMessage(s).queue();
        } catch (PermissionException | InterruptedException e) {
            //we can't do anything about it
        }
    }

    /**
     * Sends a message to a channel
     *
     * @param builder
     * @param channel
     */
    public static void sendMessage(EmbedBuilder builder, TextChannel channel) {
        try {
            builder.setTimestamp(Instant.now());
            Thread.sleep(500);
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
     * Sends an image with the embed
     *
     * @param builder
     * @param image
     * @param name
     * @param channel
     */
    public static void sendImage(EmbedBuilder builder, byte[] image, String name, TextChannel channel) {
        try {
            builder.setTimestamp(Instant.now());
            channel.sendFile(image, name)
                    .embed(builder.build())
                    .queue();
        } catch (PermissionException e) {
            //we can't do anything about it
            if (e.getPermission() == Permission.MESSAGE_EMBED_LINKS) {
                channel.sendMessage("Lacking permission to embed links!").queue();
            }
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
