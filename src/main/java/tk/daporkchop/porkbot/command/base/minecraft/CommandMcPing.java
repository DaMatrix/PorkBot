package tk.daporkchop.porkbot.command.base.minecraft;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import net.dv8tion.jda.core.EmbedBuilder;
import net.dv8tion.jda.core.events.message.MessageReceivedEvent;
import tk.daporkchop.porkbot.HTTPUtils;
import tk.daporkchop.porkbot.command.Command;

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
                s = HTTPUtils.performGetRequest(HTTPUtils.constantURL("https://mcapi.ca/query/" + ipPort[0] + "/info"));
            } else if (ipPort.length == 2)  {
                try {
                    s = HTTPUtils.performGetRequest(HTTPUtils.constantURL("https://mcapi.ca/query/" + ipPort[0] + ":" + Integer.parseInt(ipPort[1]) + "/info"));
                } catch (NumberFormatException e)   {
                    evt.getChannel().sendMessage("Error getting server info: `java.lang.NumberFormatException`").queue();
                    return;
                }
            } else {
                evt.getChannel().sendMessage("Unable to parse server ip!").queue();
                return;
            }
        } catch (IOException e) {
            e.printStackTrace();
            evt.getChannel().sendMessage("Error getting server info: `java.io.IOException`").queue();
            return;
        }

        try {
            JsonObject json = (new JsonParser()).parse(s).getAsJsonObject();

            EmbedBuilder builder = new EmbedBuilder();

            if (json.get("status").getAsBoolean())  {
                //server's online
                builder.setColor(Color.GREEN);
                builder.setThumbnail("https://eu.mc-api.net/v3/server/favicon/" + ipPort[0]);

                builder.addField(args[1], "Status: ***ONLINE***", false);
            } else {
                //server's offline
                builder.setColor(Color.RED);
                builder.addField(args[1], "Status: ***OFFLINE***", false);
            }

            evt.getChannel().sendMessage(builder.build()).queue();
        } catch (IllegalStateException e)   {
            evt.getChannel().sendMessage("Unable to parse server status!").queue();
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
