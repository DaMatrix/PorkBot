package tk.daporkchop.porkbot.command.base.minecraft;

import net.dv8tion.jda.core.EmbedBuilder;
import net.dv8tion.jda.core.events.message.MessageReceivedEvent;
import tk.daporkchop.porkbot.PorkBot;
import tk.daporkchop.porkbot.command.Command;

import java.awt.*;
import java.util.UUID;

/**
 * Created by rabi.jose.2015 on 27.03.2017.
 */
public class CommandOfflineUUID extends Command {
    public CommandOfflineUUID() {
        super("offlineuuid");
    }

    @Override
    public void excecute(MessageReceivedEvent evt) {
        String[] args = evt.getMessage().getRawContent().split(" ");

        if (args.length < 2 || args[1].isEmpty())	{
            sendErrorMessage(evt.getTextChannel(), "Name isn't given!");
            return;
        }

        String msg = args[1] + "'s offline UUID:\n```\n" + UUID.fromString("OfflinePlayer:" + args[1]).toString() + "\n```";

        PorkBot.sendMessage(msg, evt.getTextChannel());
    }

    @Override
    public String getUsage() {
        return "..offlineuuid <name>";
    }

    @Override
    public String getUsageExample()	{
        return "..offlineuuid Notch";
    }
}
