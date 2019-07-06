/*
 * Adapted from the Wizardry License
 *
 * Copyright (c) 2016-2018 DaPorkchop_
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

package net.daporkchop.porkbot.command.minecraft;

import net.daporkchop.porkbot.command.Command;
import net.daporkchop.porkbot.util.MessageUtils;
import net.daporkchop.porkbot.util.TextFormat;
import net.daporkchop.porkbot.util.mcpinger.MCPing;
import net.dv8tion.jda.core.EmbedBuilder;
import net.dv8tion.jda.core.events.message.MessageReceivedEvent;
import sun.misc.BASE64Decoder;

import java.awt.*;
import java.io.IOException;

public class CommandMcPing extends Command {

    public CommandMcPing() {
        super("mcping");
    }

    @Override
    public void execute(MessageReceivedEvent evt, String[] args, String message) {
        try {
            if (args.length < 2 || args[1].isEmpty()) {
                sendErrorMessage(evt.getTextChannel(), "IP isn't given!");
                return;
            }

            MCPing.Java ping = null;
            String[] ipPort = args[1].split(":");
            if (ipPort.length == 1) {
                ping = MCPing.pingPc(ipPort[0], 25565);
            } else if (ipPort.length == 2) {
                try {
                    ping = MCPing.pingPc(ipPort[0], Integer.parseInt(ipPort[1]));
                } catch (NumberFormatException e) {
                    MessageUtils.sendMessage("Error getting server info: `java.lang.NumberFormatException`", evt.getTextChannel());
                    return;
                }
            } else {
                MessageUtils.sendMessage("Unable to parse server ip!", evt.getTextChannel());
                return;
            }

            EmbedBuilder builder = new EmbedBuilder();

            if (ping.status) {
                //server's online
                builder.setColor(Color.GREEN);

                String[] parts = ping.favicon.split("\\,");
                String imageString = parts[1];
                byte[] imageByte;
                BASE64Decoder decoder = new BASE64Decoder();
                imageByte = decoder.decodeBuffer(imageString);

                builder.setThumbnail("attachment://image.png");

                builder.addField("**" + args[1] + "**", "Status: ***ONLINE***", false);
                builder.addField("Ping:", ping.ping, false);
                builder.addField("Version:", ping.version, false);
                builder.addField("Players:", ping.players, false);
                builder.addField("MOTD:", TextFormat.clean(ping.motd), false);

                MessageUtils.sendImage(builder, imageByte, "image.png", evt.getTextChannel());
                return;
            } else {
                //server's offline
                builder.setColor(Color.RED);
                builder.addField("**" + args[1] + "**", "Status: ***OFFLINE***", false);
            }

            MessageUtils.sendMessage(builder, evt.getTextChannel());
        } catch (IOException e) {
            MessageUtils.sendException(e, evt);
        }
    }

    @Override
    public String getUsage() {
        return "..mcping <ip>";
    }

    @Override
    public String getUsageExample() {
        return "..mcping 2b2t.org";
    }
}
