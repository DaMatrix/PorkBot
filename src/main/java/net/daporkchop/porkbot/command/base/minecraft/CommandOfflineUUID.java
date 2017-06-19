package net.daporkchop.porkbot.command.base.minecraft;

import net.daporkchop.porkbot.PorkBot;
import net.daporkchop.porkbot.command.Command;
import net.dv8tion.jda.core.events.message.MessageReceivedEvent;
import org.apache.commons.io.Charsets;

import java.util.UUID;

/**
 * Created by rabi.jose.2015 on 27.03.2017.
 */
public class CommandOfflineUUID extends Command {
    public CommandOfflineUUID() {
        super("offlineuuid");
    }

    @Override
    @SuppressWarnings("deprecation")
    public void excecute(MessageReceivedEvent evt, String[] args, String message) {
        if (args.length < 2 || args[1].isEmpty()) {
            sendErrorMessage(evt.getTextChannel(), "Name isn't given!");
            return;
        }

        String msg = args[1] + "'s offline UUID:\n```\n" + UUID.nameUUIDFromBytes(("OfflinePlayer:" + args[1]).getBytes(Charsets.UTF_8)).toString() + "\n```";

        PorkBot.sendMessage(msg, evt.getTextChannel());
    }

    @Override
    public String getUsage() {
        return "..offlineuuid <name>";
    }

    @Override
    public String getUsageExample() {
        return "..offlineuuid Notch";
    }
}
