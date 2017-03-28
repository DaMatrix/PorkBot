package tk.daporkchop.porkbot.command.base;

import net.dv8tion.jda.core.events.message.MessageReceivedEvent;
import tk.daporkchop.porkbot.PorkBot;
import tk.daporkchop.porkbot.command.Command;

public class CommandSay extends Command {

	public CommandSay() {
		super("say");
	}

	@Override
	public void excecute(MessageReceivedEvent evt, String[] args, String message) {
		if (args.length < 2 || args[1].isEmpty())	{
			sendErrorMessage(evt.getTextChannel(), "Add a message!");
			return;
		}

		PorkBot.sendMessage(evt.getAuthor().getName() + ": " + message.substring(6), evt.getTextChannel());
	}

	@Override
	public String getUsage() {
		return "..say <stuff you want to say>";
	}

	@Override
	public String getUsageExample()	{
		return "..say Hello World!";
	}
}
