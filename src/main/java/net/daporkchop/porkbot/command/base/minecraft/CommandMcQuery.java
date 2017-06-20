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

public class CommandMcQuery extends Command {
    public CommandMcQuery() {
        super("mcquery");
    }

    @Override
    public void excecute(MessageReceivedEvent evt, String[] args, String message) {
        if (args.length < 2 || args[1].isEmpty()) {
            sendErrorMessage(evt.getTextChannel(), "IP isn't given!");
            return;
        }

        MCPing.Query query = null;
        String[] ipPort = args[1].split(":");

        if (ipPort.length == 1) {
            query = MCPing.query(ipPort[0], 25565, false, true);
        } else if (ipPort.length == 2) {
            try {
                query = MCPing.query(ipPort[0], Integer.parseInt(ipPort[1]), false, true);
            } catch (NumberFormatException e) {
                PorkBot.sendMessage("Error getting server info: `java.lang.NumberFormatException`", evt.getTextChannel());
                return;
            }
        } else {
            PorkBot.sendMessage("Unable to parse server ip!", evt.getTextChannel());
            return;
        }

        if (query.status) {
            if (query.noQuery) {
                EmbedBuilder builder = new EmbedBuilder();
                builder.setColor(Color.ORANGE);
                builder.addField("**Unable to query**", "The server `" + args[1] + "` is online, but we were unable to query it. Make sure that `enable-query` is set to `true` in `server.properties` and that the server's port is open on UDP!", false);

                PorkBot.sendMessage(builder, evt.getTextChannel());
                return;
            } else {
                EmbedBuilder builder = new EmbedBuilder();

                builder.setColor(Color.GREEN);

                String[] parts = query.favicon.split("\\,");
                String imageString = parts[1];
                byte[] imageByte = null;
                BASE64Decoder decoder = new BASE64Decoder();
                try {
                    imageByte = decoder.decodeBuffer(imageString);
                } catch (IOException e) {
                    //whatever lol
                }

                builder.setThumbnail("attachment://image.png");

                builder.addField("**" + args[1] + "**", "Status: ***ONLINE***", false);

                builder.addField("Ping", query.ping, true);

                builder.addField("Version", query.version, true);

                builder.addField("Players", query.players, true);

                builder.addField("MOTD", TextFormat.clean(query.motd), false);

                builder.addField("Gamemode", query.gamemode, true);

                builder.addField("World name", query.mapName, true);

                if (query.playerSample != null && !query.playerSample.isEmpty()) {
                    builder.addField("Player sample", query.playerSample, false);
                }

                if (query.plugins != null && !query.plugins.isEmpty()) {
                    builder.addField("Plugins", query.plugins, false);
                }

                PorkBot.sendImage(builder, imageByte, "image.png", evt.getTextChannel());
                return;
            }
        } else {
            if (query.noQuery) {
                EmbedBuilder builder = new EmbedBuilder();
                builder.setColor(Color.ORANGE);
                builder.addField("**Unable to query**", "The server `" + args[1] + "` is online, but we were unable to query it. Make sure that `enable-query` is set to `true` in `server.properties` and that the server's port is open on UDP!", false);

                PorkBot.sendMessage(builder, evt.getTextChannel());
                return;
            } else {
                EmbedBuilder builder = new EmbedBuilder();
                builder.setColor(Color.RED);
                builder.addField("**" + args[1] + "**", "Status: ***OFFLINE***", false);

                PorkBot.sendMessage(builder, evt.getTextChannel());
                return;
            }
        }
    }

    @Override
    public String getUsage() {
        return "..mcquery <ip>";
    }

    @Override
    public String getUsageExample() {
        return "..mcquery home.daporkchop.net";
    }
}
