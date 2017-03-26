package tk.daporkchop.porkbot.command.base.minecraft;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import net.dv8tion.jda.core.EmbedBuilder;
import net.dv8tion.jda.core.events.message.MessageReceivedEvent;
import tk.daporkchop.porkbot.PorkBot;
import tk.daporkchop.porkbot.command.Command;
import tk.daporkchop.porkbot.util.HTTPUtils;

import java.awt.*;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Map;

/**
 * Created by daporkchop on 12.03.17.
 */
public class CommandMcStatus extends Command {

    public CommandMcStatus() {
        super("mcstatus");
    }

    @Override
    public void excecute(MessageReceivedEvent evt) {
        String s;
        try {
            s = HTTPUtils.performGetRequest(HTTPUtils.constantURL("https://mcapi.ca/mcstatus"));
        } catch (IOException e) {
            e.printStackTrace();
            PorkBot.sendMessage("Error getting server info: `java.io.IOException`", evt.getTextChannel());
            return;
        }

        try {
            JsonObject json = (new JsonParser()).parse(s).getAsJsonObject();

            EmbedBuilder builder = new EmbedBuilder();

            builder.setTitle("**Mojang status:**");

            boolean isAllOnline = true;
            int onlineCount = 0;

            for (Map.Entry<String, JsonElement> entry : json.entrySet())    {
                if (entry.getValue().getAsJsonObject().get("status").getAsString().equals("Online"))    {
                    onlineCount++;
                    builder.addField(entry.getKey(), "*Online*", false);
                } else {
                    isAllOnline = false;
                    builder.addField(entry.getKey(), "*Offline*", false);
                }
            }

            if (isAllOnline)    {
                //everything is online
                builder.setColor(Color.GREEN);
            } else {
                if (onlineCount > 0)    {
                    //some stuff is online
                    builder.setColor(Color.ORANGE);
                } else {
                    //nothing is online
                    builder.setColor(Color.RED);
                }
            }

            PorkBot.sendMessage(builder, evt.getTextChannel());
        } catch (IllegalStateException e)   {
            PorkBot.sendMessage("Unable to parse minecraft status!", evt.getTextChannel());
            return;
        }
    }

    @Override
    public String getUsage() {
        return "..mcstatus";
    }

    @Override
    public String getUsageExample()	{
        return "..mcstatus";
    }
}
