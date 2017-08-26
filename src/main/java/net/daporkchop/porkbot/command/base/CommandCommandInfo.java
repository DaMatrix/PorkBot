package net.daporkchop.porkbot.command.base;

import net.daporkchop.porkbot.PorkBot;
import net.daporkchop.porkbot.command.Command;
import net.daporkchop.porkbot.command.CommandRegistry;
import net.dv8tion.jda.core.events.message.MessageReceivedEvent;

public class CommandCommandInfo extends Command {

    public CommandCommandInfo() {
        super("commandinfo");
    }

    @Override
    public void execute(MessageReceivedEvent evt, String[] args, String message) {
        if (args.length < 2 || args[1].isEmpty()) {
            sendErrorMessage(evt.getTextChannel(), "You need to have at least one argument!");
            return;
        }

        Command command = CommandRegistry.COMMANDS.getOrDefault(args[1], null);
        if (command == null) {
            PorkBot.sendMessage("No such command: `" + args[1] + "`", evt.getTextChannel());
            return;
        }

        PorkBot.sendMessage("Statistics of command: `" + command.prefix + "`\n\nUses: `" + command.uses + "`", evt.getTextChannel());
    }

    @Override
    public String getUsage() {
        return "..commandinfo <commandName>";
    }

    @Override
    public String getUsageExample() {
        return "..commandinfo mcping";
    }
}
