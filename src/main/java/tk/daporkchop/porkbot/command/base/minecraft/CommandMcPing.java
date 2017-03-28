package tk.daporkchop.porkbot.command.base.minecraft;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import net.dv8tion.jda.core.EmbedBuilder;
import net.dv8tion.jda.core.events.message.MessageReceivedEvent;
import tk.daporkchop.porkbot.PorkBot;
import tk.daporkchop.porkbot.util.HTTPUtils;
import tk.daporkchop.porkbot.command.Command;
import tk.daporkchop.porkbot.util.TextFormat;

import java.awt.*;
import java.io.IOException;

/**
 * Created by daporkchop on 11.03.17.
 */
public class CommandMcPing extends Command {

    public CommandMcPing() {
        super("mcping");
    }

    @Override
    public void excecute(MessageReceivedEvent evt, String[] args, String message) {
        if (args.length < 2 || args[1].isEmpty())	{
            sendErrorMessage(evt.getTextChannel(), "IP isn't given!");
            return;
        }

        String s = null;
        String[] ipPort = args[1].split(":");
        try {
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
        } catch (IOException e) {
            e.printStackTrace();
            PorkBot.sendMessage("Error getting server info: `java.io.IOException`", evt.getTextChannel());
            return;
        }

        try {
            JsonObject json = (new JsonParser()).parse(s).getAsJsonObject();

            EmbedBuilder builder = new EmbedBuilder();

            if (json.get("status").getAsBoolean())  {
                //server's online
                builder.setColor(Color.GREEN);
                builder.setThumbnail("https://mc-api.net/v3/server/favicon/" + ipPort[0]);

                builder.addField("**" + args[1] + "**", "Status: ***ONLINE***", false);

                builder.addField("Ping:", json.get("ping").getAsInt() + "ms", false);
                builder.addField("Version:", json.get("version").getAsString(), false);

                JsonObject onlinePlayers = json.getAsJsonObject("players");

                builder.addField("Players:", onlinePlayers.get("online").getAsInt() + "**/**" + onlinePlayers.get("max").getAsInt(), false);

                builder.addField("MOTD:", TextFormat.clean(json.get("motd").getAsString()), false);
            } else {
                //server's offline
                builder.setColor(Color.RED);
                builder.addField("**" + args[1] + "**", "Status: ***OFFLINE***", false);
            }

            PorkBot.sendMessage(builder, evt.getTextChannel());
        } catch (IllegalStateException e)   {
            PorkBot.sendMessage("Unable to parse server status!", evt.getTextChannel());
            return;
        }
    }

    @Override
    public String getUsage() {
        return "..mcping <ip>";
    }

    @Override
    public String getUsageExample()	{
        return "..mcping 2b2t.org";
    }
}
