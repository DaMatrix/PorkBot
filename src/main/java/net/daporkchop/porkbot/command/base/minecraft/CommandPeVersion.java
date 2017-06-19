package net.daporkchop.porkbot.command.base.minecraft;

import net.daporkchop.porkbot.PorkBot;
import net.daporkchop.porkbot.command.Command;
import net.daporkchop.porkbot.util.mcpinger.MCPing;
import net.dv8tion.jda.core.EmbedBuilder;
import net.dv8tion.jda.core.events.message.MessageReceivedEvent;

import java.awt.*;

public class CommandPeVersion extends Command {
    public CommandPeVersion() {
        super("peversion");
    }

    @Override
    public void excecute(MessageReceivedEvent evt, String[] args, String message) {
        if (args.length < 2 || args[1].isEmpty()) {
            sendErrorMessage(evt.getTextChannel(), "IP isn't given!");
            return;
        }

        MCPing.PePing ping = null;
        String[] ipPort = args[1].split(":");

        if (ipPort.length == 1) {
            ping = MCPing.pingPe(ipPort[0], 19132);
        } else if (ipPort.length == 2) {
            try {
                ping = MCPing.pingPe(ipPort[0], Integer.parseInt(ipPort[1]));
            } catch (NumberFormatException e) {
                PorkBot.sendMessage("Unable to interpret port number!", evt.getTextChannel());
                return;
            }
        } else {
            PorkBot.sendMessage("Unable to parse server ip!", evt.getTextChannel());
            return;
        }

        try {
            EmbedBuilder builder = new EmbedBuilder();

            if (ping.status) {
                //server's online
                if (ping.old) {
                    builder.setColor(Color.ORANGE);
                    builder.addField("***OLD SERVER!***", "PorkBot cannot ping this server, as it runs an older version of MCPE.", false);
                } else {
                    builder.setColor(Color.GREEN);

                    builder.addField("**" + args[1] + "**", "Status: ***ONLINE***", false);

                    builder.addField("Protocol Version:", ping.protocol + "", false);

                    builder.addField("Version:", ping.version, false);
                }
            } else {
                if (ping.errored) {
                    PorkBot.sendException(ping.error, evt);
                } else {
                    //server's offline
                    builder.setColor(Color.RED);
                    builder.addField("**" + args[1] + "**", "Status: ***OFFLINE***", false);
                }
            }

            PorkBot.sendMessage(builder, evt.getTextChannel());
        } catch (IllegalStateException e) {
            PorkBot.sendMessage("Unable to parse server status!", evt.getTextChannel());
            return;
        }
    }

    @Override
    public String getUsage() {
        return "..peversion <ip>";
    }

    @Override
    public String getUsageExample() {
        return "..peversion play.2p2e.tk";
    }
}
