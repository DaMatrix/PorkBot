/*
 * Adapted from the Wizardry License
 *
 * Copyright (c) 2016-2019 DaPorkchop_
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
import net.daporkchop.porkbot.util.mcpinger.MCPing;
import net.dv8tion.jda.core.EmbedBuilder;
import net.dv8tion.jda.core.events.message.MessageReceivedEvent;

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
    public void execute(MessageReceivedEvent evt, String[] args, String rawContent) {
        if (args.length < 2 || args[1].isEmpty()) {
            sendErrorMessage(evt.getTextChannel(), "IP isn't given!");
            return;
        }

        String[] ipPort = args[1].split(":");

        MCPing.pingPe(ipPort[0], ipPort.length == 1 ? 19132 : Integer.parseInt(ipPort[1])).whenComplete((pe, ex) -> {
            EmbedBuilder builder = new EmbedBuilder();
            builder.setTitle(String.format("**%s%s%s**", ipPort[0], ipPort.length > 1 ? ":" : "", ipPort.length > 1 ? ipPort[1] : ""));

            if (ex == null) {
                //server's online
                builder.setColor(Color.GREEN);

                if ((this.flags & FLAG_MOTD) != 0)   {
                    builder.addField(
                            "MOTD:",
                            pe.motd,
                            false
                    );
                }
                if ((this.flags & FLAG_COUNT) != 0)   {
                    builder.addField(
                            "Player count:",
                            String.format("%d/%d", pe.onlinePlayers, pe.maxPlayers),
                            false
                    );
                }
                if ((this.flags & FLAG_VERSION) != 0)   {
                    builder.addField(
                            "Version:",
                            String.format("%s (Protocol %d)", pe.version, pe.protocol),
                            false
                    );
                }
                if ((this.flags & FLAG_LATENCY) != 0)   {
                    builder.addField(
                            "Latency:",
                            String.format("%d ms", pe.latency),
                            false
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
            MessageUtils.sendMessage(builder, evt.getTextChannel());
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
