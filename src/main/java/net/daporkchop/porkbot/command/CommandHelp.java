package net.daporkchop.porkbot.command;

import net.daporkchop.porkbot.PorkBot;
import net.dv8tion.jda.core.events.message.MessageReceivedEvent;

/**
 * Created by daporkchop on 11.03.17.
 */
public class CommandHelp extends Command {

    public CommandHelp () {
        super("help");
    }

    @Override
    public void excecute(MessageReceivedEvent evt, String[] args, String message) {
        PorkBot.sendMessage("***Commands:***\nhttp://www.daporkchop.net/porkbot", evt.getTextChannel());
    }

    @Override
    public String getUsage() {
        return "..help";
    }

    @Override
    public String getUsageExample()	{
        return "..help";
    }
}

