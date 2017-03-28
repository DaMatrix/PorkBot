package tk.daporkchop.porkbot.command.base.minecraft;

import net.dv8tion.jda.core.EmbedBuilder;
import net.dv8tion.jda.core.events.message.MessageReceivedEvent;
import tk.daporkchop.porkbot.PorkBot;
import tk.daporkchop.porkbot.command.Command;

import java.awt.*;

/**
 * Created by daporkchop on 12.03.17.
 */
public class CommandMcSkin extends Command {

    public CommandMcSkin() {
        super("mcskin");
    }

    @Override
    public void excecute(MessageReceivedEvent evt, String[] args, String message) {
        if (args.length < 2 || args[1].isEmpty())	{
            sendErrorMessage(evt.getTextChannel(), "Name isn't given!");
            return;
        }

        EmbedBuilder builder = new EmbedBuilder();

        builder.setColor(Color.DARK_GRAY);
        builder.setImage("https://crafatar.com/renders/body/" + args[1] + "?overlay");

        builder.addField(args[1] + "'s skin", "", false);

        PorkBot.sendMessage(builder, evt.getTextChannel());
    }

    @Override
    public String getUsage() {
        return "..mcskin <name>";
    }

    @Override
    public String getUsageExample()	{
        return "..mcskin Notch";
    }
}
