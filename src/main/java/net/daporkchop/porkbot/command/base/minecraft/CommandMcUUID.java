package net.daporkchop.porkbot.command.base.minecraft;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import net.daporkchop.porkbot.PorkBot;
import net.daporkchop.porkbot.command.Command;
import net.daporkchop.porkbot.util.HTTPUtils;
import net.daporkchop.porkbot.util.UUIDFetcher;
import net.dv8tion.jda.core.events.message.MessageReceivedEvent;

import java.io.IOException;

public class CommandMcUUID extends Command {

    public CommandMcUUID() {
        super("mcuuid");
    }

    @Override
    public void excecute(MessageReceivedEvent evt, String[] args, String message) {
        if (args.length < 2 || args[1].isEmpty()) {
            sendErrorMessage(evt.getTextChannel(), "Name isn't long enough or isn't given!");
            return;
        }

        String s = null;
        try {
            s = HTTPUtils.performGetRequest(HTTPUtils.constantURL("https://api.mojang.com/users/profiles/minecraft/" + args[1]));
        } catch (IOException e) {
            e.printStackTrace();
            PorkBot.sendMessage("Error getting player's UUID: `java.io.IOException`", evt.getTextChannel());
            return;
        }
        try {
            JsonObject json = (new JsonParser()).parse(s).getAsJsonObject();
            //                                                    this makes the UUId look nice
            PorkBot.sendMessage(args[1] + "'s UUID:\n```\n" + UUIDFetcher.getUUID(json.get("id").getAsString()).toString() + "\n```", evt.getTextChannel());
        } catch (IllegalStateException e) {
            PorkBot.sendMessage("Player " + args[1] + " could not be found! Are they a payed PC user?", evt.getTextChannel());
        } catch (Exception e) {
            PorkBot.sendMessage("Error processing UUID, is Mojang down?", evt.getTextChannel());
        }
    }

    @Override
    public String getUsage() {
        return "..mcuuid <username>";
    }

    @Override
    public String getUsageExample() {
        return "..mcuuid Notch";
    }
}
