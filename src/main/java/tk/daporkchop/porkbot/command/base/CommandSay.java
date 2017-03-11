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
			evt.getChannel().sendMessage("Give some arguments!\nUsage: `..say <stuff you want to say>`\nExample: `..say Toasters are the future!`").queue();
			return;
		}
		
		String s = evt.getMessage().getRawContent().substring(6);
	    
		evt.getChannel().sendMessage(s).queue();
	}
}
