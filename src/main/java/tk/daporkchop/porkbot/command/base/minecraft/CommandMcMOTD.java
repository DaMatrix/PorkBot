package tk.daporkchop.porkbot.command.base.minecraft;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import net.dv8tion.jda.core.EmbedBuilder;
import net.dv8tion.jda.core.events.message.MessageReceivedEvent;
import tk.daporkchop.porkbot.command.Command;
import tk.daporkchop.porkbot.util.HTTPUtils;
import tk.daporkchop.porkbot.util.TextFormat;

import java.awt.*;
import java.io.IOException;

public class CommandMcMOTD extends Command {
    public CommandMcMOTD() {
        super("mcmotd");
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
                s = HTTPUtils.performGetRequest(HTTPUtils.constantURL("https://mcapi.ca/query/" + ipPort[0] + "/motd"));
            } else if (ipPort.length == 2)  {
                try {
                    s = HTTPUtils.performGetRequest(HTTPUtils.constantURL("https://mcapi.ca/query/" + ipPort[0] + ":" + Integer.parseInt(ipPort[1]) + "/motd"));
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

                builder.addField("**" + args[1] + "** MOTD:", TextFormat.clean(json.get("motd").getAsString()), false);
            } else {
                //server's offline
                builder.setColor(Color.RED);
                builder.addField("**" + args[1] + "**", "***OFFLINE***", false);
            }

            evt.getChannel().sendMessage(builder.build()).queue();
        } catch (IllegalStateException e)   {
            evt.getChannel().sendMessage("Unable to parse server status!").queue();
            return;
        }
    }

    @Override
    public String getUsage() {
        return "..mcmotd <ip>";
    }

    @Override
    public String getUsageExample()	{
        return "..mcmotd 2b2t.org";
    }
}
