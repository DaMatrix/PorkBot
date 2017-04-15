package tk.daporkchop.porkbot.command.base.minecraft;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import net.dv8tion.jda.core.EmbedBuilder;
import net.dv8tion.jda.core.events.message.MessageReceivedEvent;
import tk.daporkchop.porkbot.PorkBot;
import tk.daporkchop.porkbot.command.Command;
import tk.daporkchop.porkbot.util.HTTPUtils;
import tk.daporkchop.porkbot.util.TextFormat;
import tk.daporkchop.porkbot.util.mcpinger.MCPing;

import java.awt.*;
import java.io.IOException;

public class CommandMcMOTD extends Command {
    public CommandMcMOTD() {
        super("mcmotd");
    }

    @Override
    public void excecute(MessageReceivedEvent evt, String[] args, String message) {
        if (args.length < 2 || args[1].isEmpty()) {
            sendErrorMessage(evt.getTextChannel(), "IP isn't given!");
            return;
        }

        MCPing.McPing ping = null;
        String[] ipPort = args[1].split(":");
        if (ipPort.length == 1) {
            ping = MCPing.pingPc(ipPort[0], 25565);
        } else if (ipPort.length == 2) {
            try {
                ping = MCPing.pingPc(ipPort[0], Integer.parseInt(ipPort[1]));
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

            builder.addField("**" + args[1] + "** MOTD:", TextFormat.clean(ping.motd), false);
        } else {
            //server's offline
            builder.setColor(Color.RED);
            builder.addField("**" + args[1] + "**", "***OFFLINE***", false);
        }

        PorkBot.sendMessage(builder, evt.getTextChannel());
    }

    @Override
    public String getUsage() {
        return "..mcmotd <ip>";
    }

    @Override
    public String getUsageExample() {
        return "..mcmotd 2b2t.org";
    }
}
