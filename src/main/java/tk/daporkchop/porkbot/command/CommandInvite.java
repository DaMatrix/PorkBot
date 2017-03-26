package tk.daporkchop.porkbot.command;

import net.dv8tion.jda.core.events.message.MessageReceivedEvent;
import tk.daporkchop.porkbot.PorkBot;

/**
 * Created by daporkchop on 11.03.17.
 */
public class CommandInvite extends Command {

    public CommandInvite() {
        super("invite");
    }

    @Override
    public void excecute(MessageReceivedEvent evt) {
        PorkBot.sendMessage("***Invite link:***\nhttps://discordapp.com/oauth2/authorize?client_id=287894637165936640&scope=bot&permissions=8", evt.getTextChannel());
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


