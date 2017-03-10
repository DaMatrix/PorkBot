package tk.daporkchop.porkbot.command;

import net.dv8tion.jda.core.events.message.MessageReceivedEvent;

public abstract class Command {
	
	public String prefix;
	
	public Command(String prefix)	{
		this.prefix = prefix;
	}
	
	/**
	 * Does command logic!
	 * @param evt 
	 * 		The MessageReceivedEvent to be parsed
	 */
	public abstract void excecute(MessageReceivedEvent evt);
}
