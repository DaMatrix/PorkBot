package net.daporkchop.porkbot.command.base.minecraft;

import net.daporkchop.porkbot.PorkBot;
import net.daporkchop.porkbot.command.Command;
import net.daporkchop.porkbot.util.TextFormat;
import net.daporkchop.porkbot.util.mcpinger.MCPing;
import net.dv8tion.jda.core.EmbedBuilder;
import net.dv8tion.jda.core.events.message.MessageReceivedEvent;
import sun.misc.BASE64Decoder;

import java.awt.*;
import java.io.ByteArrayInputStream;
import java.io.IOException;

public class CommandMcPing extends Command {

    public CommandMcPing() {
        super("mcping");
    }

    @Override
    public void excecute(MessageReceivedEvent evt, String[] args, String message) {
        try {
            if (args.length < 2 || args[1].isEmpty()) {
                sendErrorMessage(evt.getTextChannel(), "IP isn't given!");
                return;
            }

            MCPing.McPing ping = null;
            String[] ipPort = args[1].split(":");
            if (ipPort.length == 1) {
                ping = MCPing.pingPc(ipPort[0], 25565, true);
            } else if (ipPort.length == 2) {
                try {
                    ping = MCPing.pingPc(ipPort[0], Integer.parseInt(ipPort[1]), true);
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

                String[] parts = ping.favicon.split("\\,");
                String imageString = parts[1];
                byte[] imageByte;
                BASE64Decoder decoder = new BASE64Decoder();
                imageByte = decoder.decodeBuffer(imageString);
                ByteArrayInputStream bis = new ByteArrayInputStream(imageByte);

                builder.setThumbnail("attachment://image.png");

                builder.addField("**" + args[1] + "**", "Status: ***ONLINE***", false);
                builder.addField("Ping:", ping.ping, false);
                builder.addField("Version:", ping.version, false);
                builder.addField("Players:", ping.players, false);
                builder.addField("MOTD:", TextFormat.clean(ping.motd), false);

                builder.setAuthor("PorkBot", "http://www.daporkchop.net/porkbot", "https://cdn.discordapp.com/avatars/226975061880471552/a_195cf606ffbe9bd5bf1e8764c711253c.gif");

                PorkBot.sendImage(builder, imageByte, "image.png", evt.getTextChannel());
                return;
            } else {
                //server's offline
                builder.setColor(Color.RED);
                builder.addField("**" + args[1] + "**", "Status: ***OFFLINE***", false);
            }

            PorkBot.sendMessage(builder, evt.getTextChannel());
        } catch (IOException e) {
            PorkBot.sendException(e, evt);
        }
    }

    @Override
    public String getUsage() {
        return "..mcping <ip>";
    }

    @Override
    public String getUsageExample() {
        return "..mcping 2b2t.org";
    }
}
