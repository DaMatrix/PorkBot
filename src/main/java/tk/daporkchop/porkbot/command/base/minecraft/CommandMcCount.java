package tk.daporkchop.porkbot.command.base.minecraft;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import net.dv8tion.jda.core.EmbedBuilder;
import net.dv8tion.jda.core.events.message.MessageReceivedEvent;
import tk.daporkchop.porkbot.PorkBot;
import tk.daporkchop.porkbot.command.Command;
import tk.daporkchop.porkbot.util.HTTPUtils;
import tk.daporkchop.porkbot.util.mcpinger.MCPing;

import java.awt.*;
import java.io.IOException;

/**
 * Created by daporkchop on 25.03.17.
 */
public class CommandMcCount extends Command {
    public CommandMcCount() {
        super("mccount");
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

            builder.addField("**" + args[1] + "** player count:", ping.players, false);
        } else {
            //server's offline
            builder.setColor(Color.RED);
            builder.addField("**" + args[1] + "**", "***OFFLINE***", false);
        }

        PorkBot.sendMessage(builder, evt.getTextChannel());
    }

    @Override
    public String getUsage() {
        return "..mccount <ip>";
    }

    @Override
    public String getUsageExample() {
        return "..mccount 2b2t.org";
    }
}
