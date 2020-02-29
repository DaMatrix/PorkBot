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
import java.util.Base64;

/**
 * @author DaPorkchop_
 */
public final class PEPing extends Command {
    public static final int FLAG_MOTD    = 1 << 0;
    public static final int FLAG_COUNT   = 1 << 1;
    public static final int FLAG_VERSION = 1 << 2;
    public static final int FLAG_LATENCY = 1 << 3;

    public static final int FLAG_ALL = FLAG_MOTD | FLAG_COUNT | FLAG_VERSION | FLAG_LATENCY;

    protected final int flags;

    public PEPing(String prefix, int flags) {
        super(prefix);

        this.flags = flags;
    }

    @Override
    public void execute(GuildMessageReceivedEvent evt, String[] args, String rawContent) {
        if (args.length < 2 || args[1].isEmpty()) {
            sendErrorMessage(evt.getChannel(), "IP isn't given!");
            return;
        }

        String[] ipPort = args[1].split(":");

        MCPing.pingPe(ipPort[0], ipPort.length == 1 ? 19132 : Integer.parseInt(ipPort[1])).whenComplete((pe, ex) -> {
            EmbedBuilder builder = new EmbedBuilder();
            builder.setTitle(String.format("**%s%s%s**", ipPort[0], ipPort.length > 1 ? ":" : "", ipPort.length > 1 ? ipPort[1] : ""));

            if (ex == null) {
                //server's online
                builder.setColor(Color.GREEN);

                if ((this.flags & FLAG_COUNT) != 0)   {
                    builder.addField(
                            "Player count:",
                            String.format("%d/%d", pe.onlinePlayers, pe.maxPlayers),
                            true
                    );
                }
                if ((this.flags & FLAG_LATENCY) != 0)   {
                    builder.addField(
                            "Latency:",
                            String.format("%d ms", pe.latency),
                            true
                    );
                }
                if ((this.flags & FLAG_VERSION) != 0)   {
                    builder.addField(
                            "Version:",
                            String.format("%s (Protocol %d)", pe.version, pe.protocol),
                            true
                    );
                }
                if ((this.flags & FLAG_MOTD) != 0)   {
                    builder.addField(
                            "MOTD:",
                            pe.motd,
                            true
                    );
                }
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
        return String.format("..%s play.2p2e.net", this.prefix);
    }
}
