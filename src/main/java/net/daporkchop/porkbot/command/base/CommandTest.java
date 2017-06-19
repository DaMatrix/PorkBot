package net.daporkchop.porkbot.command.base;

import net.daporkchop.porkbot.PorkBot;
import net.daporkchop.porkbot.command.Command;
import net.dv8tion.jda.core.events.message.MessageReceivedEvent;

public class CommandTest extends Command {
    public CommandTest() {
        super("test");
    }

    @Override
    public void excecute(MessageReceivedEvent evt, String[] args, String message) {
        PorkBot.sendMessage("Don't use this! It doesn't do anything!!!", evt.getTextChannel());
    }

    @Override
    public String getUsage() {
        return "..say <stuff you want to say>";
    }

    @Override
    public String getUsageExample() {
        return "..say Hello World!";
    }
}
