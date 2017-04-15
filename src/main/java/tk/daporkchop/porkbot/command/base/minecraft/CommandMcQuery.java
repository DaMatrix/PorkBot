package tk.daporkchop.porkbot.command.base.minecraft;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonNull;
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
import java.util.Iterator;

/**
 * Created by daporkchop on 25.03.17.
 */
public class CommandMcQuery extends Command {
    public CommandMcQuery() {
        super("mcquery");
    }

    @Override
    public void excecute(MessageReceivedEvent evt, String[] args, String message) {
        if (args.length < 2 || args[1].isEmpty()) {
            sendErrorMessage(evt.getTextChannel(), "IP isn't given!");
            return;
        }

        MCPing.Query query = null;
        MCPing.McPing ping = null;
        String[] ipPort = args[1].split(":");

        if (ipPort.length == 1) {
            query = MCPing.query(ipPort[0], 25565, false);
            ping = MCPing.pingPc(ipPort[0], 25565);
        } else if (ipPort.length == 2) {
            try {
                query = MCPing.query(ipPort[0], Integer.parseInt(ipPort[1]), false);
                ping = MCPing.pingPc(ipPort[0], Integer.parseInt(ipPort[1]));
            } catch (NumberFormatException e) {
                PorkBot.sendMessage("Error getting server info: `java.lang.NumberFormatException`", evt.getTextChannel());
                return;
            }
        } else {
            PorkBot.sendMessage("Unable to parse server ip!", evt.getTextChannel());
            return;
        }

        if (!query.status) {
            if (ping.status) {
                EmbedBuilder builder = new EmbedBuilder();
                builder.setColor(Color.ORANGE);
                builder.addField("**Unable to query**", "The server `" + args[1] + "` is online, but we were unable to query it. Make sure that `enable-query` is set to `true` in `server.properties` and that the server's port is open on UDP!", false);

                PorkBot.sendMessage(builder, evt.getTextChannel());
            } else {
                EmbedBuilder builder = new EmbedBuilder();
                builder.setColor(Color.RED);
                builder.addField("**" + args[1] + "**", "Status: ***OFFLINE***", false);

                PorkBot.sendMessage(builder, evt.getTextChannel());
            }
            return;
        } else {
            try {
                EmbedBuilder builder = new EmbedBuilder();

                builder.setColor(Color.GREEN);
                builder.setThumbnail("https://mc-api.net/v3/server/favicon/" + ipPort[0]);

                builder.addField("**" + args[1] + "**", "Status: ***ONLINE***", false);

                builder.addField("Ping:", ping.ping, false);

                builder.addField("Version:", ping.version, false);

                builder.addField("Players:", ping.players, false);

                if (!query.playerSample.isEmpty())   {
                    builder.addField("Player sample:", query.playerSample, false);
                }

                if (!query.plugins.isEmpty())   {
                    builder.addField("Plugins:", query.plugins, false);
                }

                builder.addField("MOTD:", TextFormat.clean(ping.motd), false);

                PorkBot.sendMessage(builder, evt.getTextChannel());
            } catch (Exception e) {
                e.printStackTrace();
                PorkBot.sendException(e, evt);
                return;
            }
        }
    }

    @Override
    public String getUsage() {
        return "..mcquery <ip>";
    }

    @Override
    public String getUsageExample() {
        return "..mcquery home.daporkchop.tk";
    }
}
