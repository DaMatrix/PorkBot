package net.daporkchop.porkbot.command.base.minecraft;

import net.daporkchop.porkbot.PorkBot;
import net.daporkchop.porkbot.command.Command;
import net.daporkchop.porkbot.util.TextFormat;
import net.daporkchop.porkbot.util.mcpinger.MCPing;
import net.dv8tion.jda.core.EmbedBuilder;
import net.dv8tion.jda.core.events.message.MessageReceivedEvent;

import java.awt.*;

/**
 * Created by daporkchop on 11.03.17.
 */
public class CommandPeQuery extends Command {

    public CommandPeQuery() {
        super("pequery");
    }

    @Override
    public void excecute(MessageReceivedEvent evt, String[] args, String message) {
        if (args.length < 2 || args[1].isEmpty()) {
            sendErrorMessage(evt.getTextChannel(), "IP isn't given!");
            return;
        }

        MCPing.PePing ping = null;
        MCPing.Query query = null;
        String[] ipPort = args[1].split(":");

        if (ipPort.length == 1) {
            query = MCPing.query(ipPort[0], 19132, true, true);
        } else if (ipPort.length == 2) {
            try {
                int port = Integer.parseInt(ipPort[1]);

                query = MCPing.query(ipPort[0], port, true, true);
            } catch (NumberFormatException e) {
                PorkBot.sendMessage("Error getting server info: `java.lang.NumberFormatException`", evt.getTextChannel());
                return;
            }
        } else {
            PorkBot.sendMessage("Unable to parse server ip!", evt.getTextChannel());
            return;
        }

        EmbedBuilder builder = new EmbedBuilder();

        if (query.status && ping.status) {
            //server's online
            builder.setColor(Color.GREEN);

            builder.addField("**" + args[1] + "**", "Status: ***ONLINE***", false);

            builder.addField("Version:", query.version, false);

            //TODO: make PorkPingAPI support this
            //builder.addField("Software:", query.software, false);

            builder.addField("Protocol Version:", ping.protocol + "", false);

            builder.addField("Players:", ping.players, false);

            builder.addField("MOTD:", TextFormat.clean(ping.motd), false);

            if (query.playerSample != null && !query.playerSample.isEmpty()) {
                builder.addField("Player sample:", query.playerSample, false);
            } else {
                builder.addField("Player sample:", "None!", false);
            }

            if (query.plugins != null && !query.plugins.isEmpty()) {
                builder.addField("Plugins:", query.plugins, false);
            } else {
                builder.addField("Plugins:", "No plugins!", false);
            }
        } else if (ping.status && !query.status) {
            builder.setColor(Color.ORANGE);
            builder.addField("**Unable to query**", "The server `" + args[1] + "` is online, but we were unable to query it. Make sure that `enable-query` is set to `true` in `server.properties` and that the server's port is open on UDP!", false);
        } else if (!ping.status && !query.status) {
            //server's offline
            builder.setColor(Color.RED);
            builder.addField("**" + args[1] + "**", "Status: ***OFFLINE***", false);
        } else {
            builder.setColor(Color.RED);
            builder.addField("**Something's wrong!**", "We could query `" + args[1] + "`, but not ping it! Either there was a network failiure, or something is SERIOUSLY wrong!", false);
        }

        PorkBot.sendMessage(builder, evt.getTextChannel());
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
