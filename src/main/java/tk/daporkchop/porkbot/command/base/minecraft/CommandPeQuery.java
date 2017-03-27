package tk.daporkchop.porkbot.command.base.minecraft;

import com.google.gson.*;
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
 * Created by daporkchop on 11.03.17.
 */
public class CommandPeQuery extends Command {

    public CommandPeQuery() {
        super("pequery");
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
                s = HTTPUtils.performGetRequest(HTTPUtils.constantURL("https://mcapi.ca/query/" + ipPort[0] + "/mcpe"));
            } else if (ipPort.length == 2)  {
                try {
                    s = HTTPUtils.performGetRequest(HTTPUtils.constantURL("https://mcapi.ca/query/" + ipPort[0] + ":" + Integer.parseInt(ipPort[1]) + "/mcpe"));
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

                builder.addField("**" + args[1] + "**", "Status: ***ONLINE***", false);

                builder.addField("Version:", json.get("version").getAsString(), false);
                builder.addField("Software:", json.get("software").getAsString(), false);

                JsonObject onlinePlayers = json.getAsJsonObject("players");

                builder.addField("Players:", onlinePlayers.get("online").getAsInt() + "**/**" + onlinePlayers.get("max").getAsInt(), false);

                builder.addField("MOTD:", json.getAsJsonObject("motds").get("clean").getAsString(), false);

                String sample = null;
                Object arrObj = json.get("list");
                if (!(arrObj instanceof JsonNull))    {
                    if (arrObj instanceof JsonArray) {
                        sample = "*";
                        Iterator<JsonElement> iter = ((JsonArray) arrObj).iterator();
                        while (iter.hasNext()) {
                            sample += iter.next().getAsString().replace("_", "\\_") + ", ";
                        }
                        sample = sample.substring(0, sample.length() - 2) + "*";
                    } else if (arrObj instanceof JsonPrimitive) {
                        if (!((JsonPrimitive) arrObj).getAsString().contains(";"))   {
                            sample = "No players!";
                        }
                    } else {
                        throw new IllegalStateException();
                    }
                }
                if (sample != null)   {
                    builder.addField("Player sample:", sample, false);
                }

                sample = null;
                arrObj = json.getAsJsonArray("plugins");
                if (!(arrObj instanceof JsonNull))    {
                    if (arrObj instanceof JsonArray) {
                        sample = "*";
                        Iterator<JsonElement> iterator = ((JsonArray) arrObj).iterator();
                        while (iterator.hasNext()) {
                            sample += iterator.next().getAsString().replace("_", "\\_") + ", ";
                        }
                        sample = sample.substring(0, sample.length() - 2) + "*";
                    } else if (arrObj instanceof JsonPrimitive) {
                        if (!((JsonPrimitive) arrObj).getAsString().contains(";"))   {
                            sample = "No plugins!";
                        }
                    } else {
                        throw new IllegalStateException();
                    }
                }
                if (sample != null) {
                    builder.addField("Plugins:", sample, false);
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
        return "..pequery <ip>";
    }

    @Override
    public String getUsageExample()	{
        return "..pequery play.2p2e.tk";
    }
}
