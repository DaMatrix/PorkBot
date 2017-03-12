package tk.daporkchop.porkbot.command.base;

import net.dv8tion.jda.core.events.message.MessageReceivedEvent;
import tk.daporkchop.porkbot.command.Command;

public class CommandSay extends Command {

	public CommandSay() {
		super("say");
	}

	@Override
	public void excecute(MessageReceivedEvent evt) {
		String[] args = evt.getMessage().getRawContent().split(" ");
		
		if (args.length < 2 || args[1].isEmpty())	{
			sendErrorMessage(evt.getTextChannel(), "Add a message!");
			return;
		}

		evt.getChannel().sendMessage(evt.getAuthor().getName() + ": " + evt.getMessage().getRawContent().substring(6)).queue();
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
