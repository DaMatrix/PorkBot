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
			evt.getChannel().sendMessage("Name isn't long enough or isn't given!\nUsage: `..mcuuid <username>`\nExample: `..mcuuid Notch`").queue();
		}
		
		String s = null;
		try {
			s = HTTPUtils.performGetRequest(HTTPUtils.constantURL("https://api.mojang.com/users/profiles/minecraft/" + args[1]));
		} catch (IOException e) {
			e.printStackTrace();
			evt.getChannel().sendMessage("Error getting player's UUID: `java.io.IOException`").queue();
			return;
		}
		
		JsonObject json = (new JsonParser()).parse(s).getAsJsonObject();
		evt.getChannel().sendMessage(args[1] + "'s UUID: `" + json.get("id").getAsString() + "`").queue();
	}
}
