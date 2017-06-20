package net.daporkchop.porkbot.command.base.minecraft;

import net.daporkchop.porkbot.PorkBot;
import net.daporkchop.porkbot.command.Command;
import net.daporkchop.porkbot.util.mcpinger.MCPing;
import net.dv8tion.jda.core.EmbedBuilder;
import net.dv8tion.jda.core.events.message.MessageReceivedEvent;
import sun.misc.BASE64Decoder;

import java.awt.*;
import java.io.ByteArrayInputStream;
import java.io.IOException;

/**
 * Created by daporkchop on 25.03.17.
 */
public class CommandMcIcon extends Command {

    public CommandMcIcon() {
        super("mcicon");
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
                ping = MCPing.pingPc(ipPort[0], 25565, false);
            } else if (ipPort.length == 2) {
                try {
                    ping = MCPing.pingPc(ipPort[0], Integer.parseInt(ipPort[1]), false);
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
                builder.setTitle("**" + ipPort[0] + (ipPort.length == 2 ? ":" + ipPort[1] : "") + "**'s favicon");

                String[] parts = ping.favicon.split("\\,");
                String imageString = parts[1];
                byte[] imageByte;
                BASE64Decoder decoder = new BASE64Decoder();
                imageByte = decoder.decodeBuffer(imageString);

                builder.setThumbnail("attachment://image.png");

                PorkBot.sendImage(builder, imageByte, "image.png", evt.getTextChannel());
                return;
            } else {
                //server's offline
                builder.setColor(Color.RED);
                builder.addField("**" + args[1] + "**", "***OFFLINE***", false);
            }

            PorkBot.sendMessage(builder, evt.getTextChannel());
        } catch (IOException e) {
            PorkBot.sendException(e, evt);
        }
    }

    @Override
    public String getUsage() {
        return "..mcicon <ip>";
    }

    @Override
    public String getUsageExample() {
        return "..mcicon 2b2t.org";
    }
}
