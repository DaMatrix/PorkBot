package tk.daporkchop.porkbot.command;

import net.dv8tion.jda.core.entities.TextChannel;
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

    /**
     * Gets the command's usage
     * @return
     */
	public abstract String getUsage();

	/**
	 * Gets and example of using the command
	 * @return
	 */
	public abstract String getUsageExample();

	public void sendErrorMessage(TextChannel channel, String message)	{
		channel.sendMessage((message == null ? "" : message + "\n") + "Usage: " + getUsage() + "\nExample: " + getUsageExample()).queue();
	}
}
