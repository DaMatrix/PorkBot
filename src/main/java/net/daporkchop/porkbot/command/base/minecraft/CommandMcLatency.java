package net.daporkchop.porkbot.command.base.minecraft;

import net.daporkchop.porkbot.PorkBot;
import net.daporkchop.porkbot.command.Command;
import net.daporkchop.porkbot.util.mcpinger.MCPing;
import net.dv8tion.jda.core.EmbedBuilder;
import net.dv8tion.jda.core.events.message.MessageReceivedEvent;

import java.awt.*;

public class CommandMcLatency extends Command {
    public CommandMcLatency() {
        super("mclatency");
    }

    @Override
    public void execute(MessageReceivedEvent evt, String[] args, String rawContent) {
        if (args.length < 2 || args[1].isEmpty()) {
            sendErrorMessage(evt.getTextChannel(), "IP isn't given!");
            return;
        }

        MCPing.McPing ping = null;
        String[] ipPort = args[1].split(":");
        if (ipPort.length == 1) {
            ping = MCPing.pingPc(ipPort[0], 25565, true);
        } else if (ipPort.length == 2) {
            try {
                ping = MCPing.pingPc(ipPort[0], Integer.parseInt(ipPort[1]), true);
            } catch (NumberFormatException e) {
                PorkBot.sendMessage("Error getting server info: `java.lang.NumberFormatException`", evt.getTextChannel());
                return;
            }
        } else {
            PorkBot.sendMessage("Unable to parse server ip!", evt.getTextChannel());
            return;
        }

        EmbedBuilder builder = new EmbedBuilder();

        if (ping.status) {
            //server's online
            builder.setColor(Color.GREEN);

            builder.addField("Server ping", ping.ping + "", false);
        } else {
            //server's offline
            builder.setColor(Color.RED);
            builder.addField("**" + args[1] + "**", "***OFFLINE***", false);
        }

        PorkBot.sendMessage(builder, evt.getTextChannel());
    }

    @Override
    public String getUsage() {
        return "..mcversion <ip>";
    }

    @Override
    public String getUsageExample() {
        return "..mcversion 2b2t.org";
    }
}
