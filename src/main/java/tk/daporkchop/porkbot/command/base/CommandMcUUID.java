package tk.daporkchop.porkbot.command.base;

import java.io.IOException;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import net.dv8tion.jda.core.events.message.MessageReceivedEvent;
import tk.daporkchop.porkbot.HTTPUtils;
import tk.daporkchop.porkbot.command.Command;

public class CommandMcUUID extends Command {

	public CommandMcUUID() {
		super("mcuuid");
	}

	@Override
	public void excecute(MessageReceivedEvent evt) {
		String[] args = evt.getMessage().getRawContent().split(" ");
		
		if (args.length < 2 || args[1].isEmpty())	{
			sendErrorMessage(evt.getTextChannel(), "Name isn't long enough or isn't given!");
		}
		
		String s = null;
		try {
			s = HTTPUtils.performGetRequest(HTTPUtils.constantURL("https://api.mojang.com/users/profiles/minecraft/" + args[1]));
		} catch (IOException e) {
			e.printStackTrace();
			evt.getChannel().sendMessage("Error getting player's UUID: `java.io.IOException`").queue();
			return;
		}
		try {
			JsonObject json = (new JsonParser()).parse(s).getAsJsonObject();
			evt.getChannel().sendMessage(args[1] + "'s UUID: `" + json.get("id").getAsString() + "`").queue();
		} catch (IllegalStateException e)	{
			evt.getChannel().sendMessage("Player " + args[1] + " could not be found! Are they a payed PC user?").queue();
		}
	}

	@Override
	public String getUsage() {
		return "..mcuuid <username>";
	}

	@Override
	public String getUsageExample()	{
		return "..mcuuid Notch";
	}
}
