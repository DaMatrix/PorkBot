package tk.daporkchop.porkbot.command;

import net.dv8tion.jda.core.events.message.MessageReceivedEvent;
import tk.daporkchop.porkbot.PorkBot;

/**
 * Created by daporkchop on 11.03.17.
 */
public class CommandHelp extends Command {

    public CommandHelp () {
        super("help");
    }

    @Override
    public void excecute(MessageReceivedEvent evt, String[] args, String message) {
        PorkBot.sendMessage("***Commands:***\nhttp://www.daporkchop.tk/porkbot", evt.getTextChannel());
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

