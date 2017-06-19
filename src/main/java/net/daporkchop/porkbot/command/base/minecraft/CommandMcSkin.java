package net.daporkchop.porkbot.command.base.minecraft;

import net.daporkchop.porkbot.PorkBot;
import net.daporkchop.porkbot.command.Command;
import net.dv8tion.jda.core.EmbedBuilder;
import net.dv8tion.jda.core.events.message.MessageReceivedEvent;
import org.apache.commons.io.IOUtils;

import java.awt.*;
import java.io.InputStream;
import java.net.URL;

public class CommandMcSkin extends Command {

    public CommandMcSkin() {
        super("mcskin");
    }

    @Override
    public void excecute(MessageReceivedEvent evt, String[] args, String message) {
        try {
            if (args.length < 2 || args[1].isEmpty()) {
                sendErrorMessage(evt.getTextChannel(), "Name isn't given!");
                return;
            }

            EmbedBuilder builder = new EmbedBuilder();
            builder.setImage("attachment://image.png");
            builder.setColor(Color.DARK_GRAY);

            URL website = new URL("https://crafatar.com/renders/body/" + args[1] + "?overlay");
            byte[] outBytes = new byte[1024 * 1024 * 8 - 64];
            InputStream stream = website.openStream();
            IOUtils.read(stream, outBytes, 0, outBytes.length);
            if (stream.read() != -1) {
                throw new IllegalStateException("Skin image too large!");
            }

            builder.addField(args[1] + "'s skin", "", false);

            PorkBot.sendImage(builder, outBytes, "image.png", evt.getTextChannel());
        } catch (Exception e) {
            PorkBot.sendException(e, evt);
        }
    }

    @Override
    public String getUsage() {
        return "..mcskin <name>";
    }

    @Override
    public String getUsageExample() {
        return "..mcskin Notch";
    }
}
