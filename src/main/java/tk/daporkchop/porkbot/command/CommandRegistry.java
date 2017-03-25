package tk.daporkchop.porkbot.command;

import java.util.HashMap;

import net.dv8tion.jda.core.events.message.MessageReceivedEvent;

public abstract class CommandRegistry {
	
	/**
	 * A HashMap containing all the commands and their prefix
	 */
	private static final HashMap<String, Command> COMMANDS = new HashMap<>();

	public static final Command IGNORE = new CommandIgnore();
	
	/**
	 * Registers a command to the command registry.
	 * @param cmd
	 * @return cmd again lul
	 */
	public static final Command registerCommand(Command cmd)	{
		COMMANDS.put(cmd.prefix, cmd);
		return cmd;
	}
	
	/**
	 * Runs a comamnd
	 * @param evt
	 */
	public static void runCommand(MessageReceivedEvent evt)	{
		try {
			COMMANDS.getOrDefault(evt.getMessage().getRawContent().split(" ")[0].substring(2), IGNORE).excecute(evt);
		} catch (Exception e)	{
			evt.getChannel().sendMessage("Error running command: `" + evt.getMessage().getRawContent() + "`:\n`" + e.getClass().getCanonicalName() + "`");
		}
	}
}
