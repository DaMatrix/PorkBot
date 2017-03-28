package tk.daporkchop.porkbot.command.base;

import net.dv8tion.jda.core.events.message.MessageReceivedEvent;
import tk.daporkchop.porkbot.PorkBot;
import tk.daporkchop.porkbot.command.Command;

import java.time.OffsetDateTime;

/**
 * Created by rabi.jose.2015 on 20.03.2017.
 */
public class CommandPing extends Command {

    public CommandPing() {
        super("ping");
    }

    @Override
    public void excecute(MessageReceivedEvent evt, String[] args, String message) {
        PorkBot.sendMessage("Pong: " + PorkBot.INSTANCE.jda.getPing() + "ms", evt.getTextChannel());
    }

    @Override
    public String getUsage() {
        return "..ping";
    }

    @Override
    public String getUsageExample()	{
        return "..ping";
    }
}