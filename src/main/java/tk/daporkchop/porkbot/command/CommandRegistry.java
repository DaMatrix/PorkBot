package tk.daporkchop.porkbot.command;

import java.util.HashMap;

import net.dv8tion.jda.core.events.message.MessageReceivedEvent;
import tk.daporkchop.porkbot.PorkBot;

public abstract class CommandRegistry {
	
	/**
	 * A HashMap containing all the commands and their prefix
	 */
	private static final HashMap<String, Command> COMMANDS = new HashMap<>();

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
	public static void runCommand(MessageReceivedEvent evt, String rawContent)	{
		try {
			String[] split = rawContent.split(" ");
			Command cmd = COMMANDS.getOrDefault(split[0].substring(2), null);
			if (cmd != null)	{
				evt.getTextChannel().sendTyping().queue();

				new Thread() {
					@Override
					public void run()	{
						cmd.excecute(evt, split, rawContent);
					}
				}.start();
			}
		} catch (Exception e)	{
			e.printStackTrace();
			PorkBot.sendMessage("Error running command: `" + evt.getMessage().getRawContent() + "`:\n`" + e.getClass().getCanonicalName() + "`", evt.getTextChannel());
		}
	}
}
