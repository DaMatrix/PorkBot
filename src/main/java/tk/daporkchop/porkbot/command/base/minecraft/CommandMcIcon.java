package tk.daporkchop.porkbot.command.base.minecraft;

import net.dv8tion.jda.core.EmbedBuilder;
import net.dv8tion.jda.core.events.message.MessageReceivedEvent;
import tk.daporkchop.porkbot.PorkBot;
import tk.daporkchop.porkbot.command.Command;

import java.awt.*;

/**
 * Created by daporkchop on 25.03.17.
 */
public class CommandMcIcon extends Command {

    public CommandMcIcon() {
        super("mcicon");
    }

    @Override
    public void excecute(MessageReceivedEvent evt) {
        String[] args = evt.getMessage().getRawContent().split(" ");

        if (args.length < 2 || args[1].isEmpty())	{
            sendErrorMessage(evt.getTextChannel(), "IP isn't given!");
            return;
        }

        EmbedBuilder builder = new EmbedBuilder();

        builder.setColor(Color.DARK_GRAY);

        builder.addField(args[1] + "'s icon:", "", false);

        builder.setImage("https://mcapi.ca/query/" + args[1] + "/icon");

        PorkBot.sendMessage(builder, evt.getTextChannel());
    }

    @Override
    public String getUsage() {
        return "..mcicon <ip>";
    }

    @Override
    public String getUsageExample()	{
        return "..mcicon ";
    }
}
