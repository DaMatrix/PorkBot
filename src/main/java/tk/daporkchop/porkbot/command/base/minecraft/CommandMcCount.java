package tk.daporkchop.porkbot.command.base.minecraft;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import net.dv8tion.jda.core.EmbedBuilder;
import net.dv8tion.jda.core.events.message.MessageReceivedEvent;
import tk.daporkchop.porkbot.PorkBot;
import tk.daporkchop.porkbot.command.Command;
import tk.daporkchop.porkbot.util.HTTPUtils;

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
        if (args.length < 2 || args[1].isEmpty())	{
            sendErrorMessage(evt.getTextChannel(), "IP isn't given!");
            return;
        }

        String s = null;
        String[] ipPort = args[1].split(":");
        try {
            if (ipPort.length == 1) {
                s = HTTPUtils.performGetRequest(HTTPUtils.constantURL("https://mcapi.ca/query/" + ipPort[0] + "/players"));
            } else if (ipPort.length == 2)  {
                try {
                    s = HTTPUtils.performGetRequest(HTTPUtils.constantURL("https://mcapi.ca/query/" + ipPort[0] + ":" + Integer.parseInt(ipPort[1]) + "/players"));
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

                builder.addField("**" + args[1] + "** player count:", json.getAsJsonObject("players").get("online").getAsInt() + "/" + json.getAsJsonObject("players").get("max").getAsInt(), false);
            } else {
                //server's offline
                builder.setColor(Color.RED);
                builder.addField("**" + args[1] + "**", "***OFFLINE***", false);
            }

            PorkBot.sendMessage(builder, evt.getTextChannel());
        } catch (IllegalStateException e)   {
            PorkBot.sendMessage("Unable to parse server status!", evt.getTextChannel());
            return;
        }
    }

    @Override
    public String getUsage() {
        return "..mccount <ip>";
    }

    @Override
    public String getUsageExample()	{
        return "..mccount 2b2t.org";
    }
}
