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
    public void excecute(MessageReceivedEvent evt) {
        String[] args = evt.getMessage().getRawContent().split(" ");

        if (args.length < 2 || args[1].isEmpty())	{
            sendErrorMessage(evt.getTextChannel(), "IP isn't given!");
            return;
        }

        String s = null;
        String[] ipPort = args[1].split(":");
        try {
            if (ipPort.length == 1) {
                s = HTTPUtils.performGetRequest(HTTPUtils.constantURL("https://mcapi.ca/query/" + ipPort[0] + "/extensive"));
            } else if (ipPort.length == 2)  {
                try {
                    s = HTTPUtils.performGetRequest(HTTPUtils.constantURL("https://mcapi.ca/query/" + ipPort[0] + ":" + Integer.parseInt(ipPort[1]) + "/extensive"));
                } catch (NumberFormatException e)   {
                    PorkBot.sendMessage("Error getting server info: `java.lang.NumberFormatException`", evt.getTextChannel());
                    return;
                }
            } else {
                PorkBot.sendMessage("Unable to parse server ip!", evt.getTextChannel());
                return;
            }
        } catch (IOException e) {
            PorkBot.sendMessage("Error getting server info: `java.io.IOException`", evt.getTextChannel());
            return;
        }

        JsonObject query = null;
        JsonObject ping = null;

        try {
            query = (new JsonParser()).parse(s).getAsJsonObject();

            if (ipPort.length == 1) {
                s = HTTPUtils.performGetRequest(HTTPUtils.constantURL("https://mcapi.ca/query/" + ipPort[0] + "/info"));
            } else if (ipPort.length == 2)  {
                try {
                    s = HTTPUtils.performGetRequest(HTTPUtils.constantURL("https://mcapi.ca/query/" + ipPort[0] + ":" + Integer.parseInt(ipPort[1]) + "/info"));
                } catch (NumberFormatException e)   {
                    PorkBot.sendMessage("Error getting server info: `java.lang.NumberFormatException`", evt.getTextChannel());
                    return;
                }
            } else {
                PorkBot.sendMessage("Unable to parse server ip!", evt.getTextChannel());
                return;
            }

            ping = (new JsonParser()).parse(s).getAsJsonObject();
        } catch (Exception e)   {
            e.printStackTrace();
            PorkBot.sendMessage("Unable to parse server status!", evt.getTextChannel());
            return;
        }

        if (!query.get("status").getAsBoolean())    {
            if (ping.get("status").getAsBoolean())  {
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
        }

        try {
            EmbedBuilder builder = new EmbedBuilder();

            builder.setColor(Color.GREEN);
            builder.setThumbnail("https://mc-api.net/v3/server/favicon/" + ipPort[0]);

            builder.addField("**" + args[1] + "**", "Status: ***ONLINE***", false);

            builder.addField("Ping:", ping.get("ping").getAsInt() + "ms", false);

            builder.addField("Version:", query.get("version").getAsString(), false);

            builder.addField("Software:", query.get("software").getAsString(), false);

            JsonObject onlinePlayers = ping.getAsJsonObject("players");

            builder.addField("Players:", onlinePlayers.get("online").getAsInt() + "**/**" + onlinePlayers.get("max").getAsInt(), false);

            String sample = null;
            Object arrObj = query.get("list");
            if (!(arrObj instanceof JsonNull))    {
                sample = "*";
                Iterator<JsonElement> iter = ((JsonArray) arrObj).iterator();
                while (iter.hasNext())  {
                    sample += iter.next().getAsString().replace("_", "\\_") + ", ";
                }
                sample = sample.substring(0, sample.length() - 2) + "*";
            }
            if (sample != null)   {
                builder.addField("Player sample:", sample, false);
            }

            sample = null;
            arrObj = query.getAsJsonArray("plugins");
            if (!(arrObj instanceof JsonNull))    {
                sample = "*";
                Iterator<JsonElement> iterator = ((JsonArray) arrObj).iterator();
                while (iterator.hasNext())  {
                    sample += iterator.next().getAsString().replace("_", "\\_") + ", ";
                }
                sample = sample.substring(0, sample.length() - 2) + "*";
            }
            if (sample != null) {
                builder.addField("Plugins:", sample, false);
            }

            builder.addField("MOTD:", TextFormat.clean(ping.get("motd").getAsString()), false);

            PorkBot.sendMessage(builder, evt.getTextChannel());
        } catch (Exception e)   {
            e.printStackTrace();
            PorkBot.sendMessage("**Error**:\n" + e.toString(), evt.getTextChannel());
            return;
        }
    }

    @Override
    public String getUsage() {
        return "..mcquery <ip>";
    }

    @Override
    public String getUsageExample()	{
        return "..mcquery home.daporkchop.tk";
    }
}
