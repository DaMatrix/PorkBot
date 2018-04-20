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

import java.awt.*;

public class CommandPeQuery extends Command {

    public CommandPeQuery() {
        super("pequery");
    }

    @Override
    public void execute(MessageReceivedEvent evt, String[] args, String message) {
        if (args.length < 2 || args[1].isEmpty()) {
            sendErrorMessage(evt.getTextChannel(), "IP isn't given!");
            return;
        }

        MCPing.Query query = null;
        String[] ipPort = args[1].split(":");

        if (ipPort.length == 1) {
            query = MCPing.query(ipPort[0], 19132, true, true);
        } else if (ipPort.length == 2) {
            try {
                int port = Integer.parseInt(ipPort[1]);

                query = MCPing.query(ipPort[0], port, true, true);
            } catch (NumberFormatException e) {
                MessageUtils.sendMessage("Error getting server info: `java.lang.NumberFormatException`", evt.getTextChannel());
                return;
            }
        } else {
            MessageUtils.sendMessage("Unable to parse server ip!", evt.getTextChannel());
            return;
        }

        if (query.status) {
            if (query.noQuery) {
                EmbedBuilder builder = new EmbedBuilder();
                builder.setColor(Color.ORANGE);
                builder.addField("**Unable to query**", "The server `" + args[1] + "` is online, but we were unable to query it. Make sure that `enable-query` is set to `true` in `server.properties` and that the server's port is open on UDP!", false);

                MessageUtils.sendMessage(builder, evt.getTextChannel());
                return;
            } else {
                EmbedBuilder builder = new EmbedBuilder();

                builder.setColor(Color.GREEN);
                builder.addField("**" + args[1] + "**", "Status: ***ONLINE***", false);

                builder.addField("Ping", query.ping, true);

                builder.addField("Version", query.version, true);

                builder.addField("Players", query.players, true);

                builder.addField("MOTD", TextFormat.clean(query.motd), false);

                builder.addField("Gamemode", query.gamemode, true);

                builder.addField("World name", query.mapName, true);

                if (query.playerSample != null && !query.playerSample.isEmpty()) {
                    builder.addField("Player sample", query.playerSample, false);
                }

                if (query.plugins != null && !query.plugins.isEmpty()) {
                    builder.addField("Plugins", query.plugins, false);
                }

                MessageUtils.sendMessage(builder, evt.getTextChannel());
                return;
            }
        } else {
            if (query.noQuery) {
                EmbedBuilder builder = new EmbedBuilder();
                builder.setColor(Color.ORANGE);
                builder.addField("**Unable to query**", "The server `" + args[1] + "` is online, but we were unable to query it. Make sure that `enable-query` is set to `true` in `server.properties` and that the server's port is open on UDP!", false);

                MessageUtils.sendMessage(builder, evt.getTextChannel());
                return;
            } else {
                EmbedBuilder builder = new EmbedBuilder();
                builder.setColor(Color.RED);
                builder.addField("**" + args[1] + "**", "Status: ***OFFLINE***", false);

                MessageUtils.sendMessage(builder, evt.getTextChannel());
                return;
            }
        }
    }

    @Override
    public String getUsage() {
        return "..pequery <ip>";
    }

    @Override
    public String getUsageExample() {
        return "..pequery play.2p2e.tk";
    }
}
