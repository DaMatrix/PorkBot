package net.daporkchop.porkbot.command.base.minecraft;

import net.daporkchop.porkbot.PorkBot;
import net.daporkchop.porkbot.command.Command;
import net.dv8tion.jda.core.EmbedBuilder;
import net.dv8tion.jda.core.events.message.MessageReceivedEvent;

import java.awt.*;

/**
 * Created by daporkchop on 12.03.17.
 */
public class CommandMcHead extends Command {

    public CommandMcHead() {
        super("mchead");
    }

    @Override
    public void excecute(MessageReceivedEvent evt, String[] args, String message) {
        if (args.length < 2 || args[1].isEmpty()) {
            sendErrorMessage(evt.getTextChannel(), "Name isn't given!");
            return;
        }

        EmbedBuilder builder = new EmbedBuilder();

        builder.setColor(Color.DARK_GRAY);
        builder.setImage("https://crafatar.com/renders/head/" + args[1] + "?size=128&overlay");

        builder.addField(args[1] + "'s avatar", "", false);

        PorkBot.sendMessage(builder, evt.getTextChannel());
    }

    @Override
    public String getUsage() {
        return "..mchead <name>";
    }

    @Override
    public String getUsageExample() {
        return "..mchead Notch";
    }
}
