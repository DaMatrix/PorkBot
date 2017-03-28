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
 * Created by rabi.jose.2015 on 27.03.2017.
 */
public class CommandPePing extends Command {
    public CommandPePing()  {
        super("peping");
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
                s = HTTPUtils.performGetRequest(HTTPUtils.constantURL("http://repo.daporkchop.tk/ExamplePinger.php?ip=" + ipPort[0] + "&port=19132"));
            } else if (ipPort.length == 2)  {
                try {
                    s = HTTPUtils.performGetRequest(HTTPUtils.constantURL("http://repo.daporkchop.tk/ExamplePinger.php?ip=" + ipPort[0] + "&port=" + Integer.parseInt(ipPort[1])));
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
                if (json.get("old").getAsBoolean()) {
                    builder.setColor(Color.ORANGE);
                    builder.addField("***OLD SERVER!***", "PorkBot cannot ping this server, as it runs an older version of MCPE.", false);
                } else {
                    builder.setColor(Color.GREEN);

                    builder.addField("**" + args[1] + "**", "Status: ***ONLINE***", false);

                    builder.addField("MOTD:", json.get("motd").getAsString(), false);

                    builder.addField("Players:", json.get("players").getAsString(), false);

                    builder.addField("Protocol Version:", json.get("protocol").getAsInt() + "", false);

                    builder.addField("Version:", json.get("version").getAsString(), false);

                    builder.addField("Ping:", json.get("ping").getAsString(), false);
                }
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
        return "..peping <ip>";
    }

    @Override
    public String getUsageExample()	{
        return "..peping play.2p2e.tk";
    }
}
