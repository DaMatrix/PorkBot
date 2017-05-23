package net.daporkchop.porkbot.command;

import net.daporkchop.porkbot.PorkBot;
import net.dv8tion.jda.core.events.message.MessageReceivedEvent;

/**
 * Created by daporkchop on 11.03.17.
 */
public class CommandInvite extends Command {

    public CommandInvite() {
        super("invite");
    }

    @Override
    public void excecute(MessageReceivedEvent evt, String[] args, String message) {
        PorkBot.sendMessage("***Invite link is on bot site:***\nhttp://www.daporkchop.net/porkbot", evt.getTextChannel());
    }

    @Override
    public String getUsage() {
        return "..invite";
    }

    @Override
    public String getUsageExample()	{
        return "..invite";
    }
}


