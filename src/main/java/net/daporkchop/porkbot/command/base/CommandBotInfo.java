package net.daporkchop.porkbot.command.base;

import net.daporkchop.porkbot.PorkBot;
import net.daporkchop.porkbot.command.Command;
import net.daporkchop.porkbot.command.CommandRegistry;
import net.dv8tion.jda.core.EmbedBuilder;
import net.dv8tion.jda.core.events.message.MessageReceivedEvent;

import java.awt.*;

public class CommandBotInfo extends Command {

    public CommandBotInfo() {
        super("botinfo");
    }

    @Override
    public void excecute(MessageReceivedEvent evt, String[] args, String message) {
        EmbedBuilder builder = new EmbedBuilder();
        builder.setColor(Color.BLUE);
        builder.setTitle("**PorkBot info**", "http://www.daporkchop.net/porkbot");

        builder.setThumbnail("https://cdn.discordapp.com/avatars/226975061880471552/a_195cf606ffbe9bd5bf1e8764c711253c.gif?size=256");

        builder.addField("Name:", "PorkBot#" + PorkBot.INSTANCE.jda.getSelfUser().getDiscriminator(), true);

        builder.addField("Servers:", String.valueOf(PorkBot.INSTANCE.jda.getGuilds().size()), true);

        builder.addField("Known users:", String.valueOf(PorkBot.INSTANCE.jda.getUsers().size()), true);

        builder.addField("ID:", PorkBot.INSTANCE.jda.getSelfUser().getId(), true);

        builder.addField("Commands this session:", String.valueOf(CommandRegistry.COMMAND_COUNT), true);

        builder.addField("Commands all time:", String.valueOf(CommandRegistry.COMMAND_COUNT_TOTAL), true);

        builder.addField("Used RAM:", ((Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory()) / 1024 / 1024) + " MB", false);

        PorkBot.sendMessage(builder, evt.getTextChannel());
    }

    @Override
    public String getUsage() {
        return "..ping";
    }

    @Override
    public String getUsageExample() {
        return "..ping";
    }
}
