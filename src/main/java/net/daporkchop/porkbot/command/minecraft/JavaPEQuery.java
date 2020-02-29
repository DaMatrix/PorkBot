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

package net.daporkchop.porkbot.command.minecraft;

import net.daporkchop.porkbot.command.Command;
import net.daporkchop.porkbot.util.MessageUtils;
import net.daporkchop.porkbot.util.mcpinger.MCPing;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;

import java.awt.Color;
import java.util.Arrays;
import java.util.StringJoiner;
import java.util.stream.Stream;

public final class JavaPEQuery extends Command {
    protected final int defaultPort;

    public JavaPEQuery(String prefix, int defaultPort) {
        super(prefix);

        this.defaultPort = defaultPort;
    }

    @Override
    public void execute(GuildMessageReceivedEvent evt, String[] args, String message) {
        if (args.length < 2 || args[1].isEmpty()) {
            sendErrorMessage(evt.getChannel(), "IP isn't given!");
            return;
        }

        String[] ipPort = args[1].split(":");

        MCPing.query(ipPort[0], ipPort.length == 1 ? this.defaultPort : Integer.parseInt(ipPort[1])).whenComplete((query, ex) -> {
            EmbedBuilder builder = new EmbedBuilder();
            builder.setTitle(String.format("**%s%s%s**", ipPort[0], ipPort.length > 1 ? ":" : "", ipPort.length > 1 ? ipPort[1] : ""));

            if (ex == null) {
                //server's online
                builder.setColor(Color.GREEN);

                builder.addField(
                        "Player count:",
                        String.format("%d/%d", query.onlinePlayers, query.maxPlayers),
                        true
                );
                if (query.latency != -1) {
                    builder.addField(
                            "Latency:",
                            String.format("%d ms", query.latency),
                            true
                    );
                }
                builder.addField(
                        "Map name:",
                        query.mapName,
                        true
                );
                builder.addField(
                        "Gamemode:",
                        query.gamemode,
                        true
                );
                builder.addField(
                        "Version:",
                        query.protocol == -1 ? query.version : String.format("%s (Protocol %d)", query.version, query.protocol),
                        true
                );
                if (!query.plugins.isEmpty()) {
                    builder.addField(
                            "Plugins:",
                            query.plugins,
                            true
                    );
                }
                if (query.playerSample.length > 0) {
                    builder.addField(
                            "Player sample:",
                            Stream.of(query.playerSample).collect(() -> new StringJoiner(", "), StringJoiner::add, StringJoiner::merge).toString(),
                            true
                    );
                }
                builder.addField(
                        "MOTD:",
                        query.motd,
                        true
                );
            } else {
                //server's offline
                builder.setColor(Color.RED);
                builder.addField(
                        "***OFFLINE***",
                        "",
                        false
                );
            }
            MessageUtils.sendMessage(builder, evt.getChannel());
        });
    }

    @Override
    public String getUsage() {
        return String.format("..%s <ip>", this.prefix);
    }

    @Override
    public String getUsageExample() {
        return String.format("..%s vps1.daporkchop.net", this.prefix);
    }
}
