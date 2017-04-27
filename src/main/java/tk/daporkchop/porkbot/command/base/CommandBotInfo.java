package tk.daporkchop.porkbot.command.base;

import net.dv8tion.jda.core.EmbedBuilder;
import net.dv8tion.jda.core.events.message.MessageReceivedEvent;
import tk.daporkchop.porkbot.PorkBot;
import tk.daporkchop.porkbot.command.Command;

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

        builder.addField("Name:", "PorkBot#" + PorkBot.INSTANCE.jda.getSelfUser().getDiscriminator(), false);

        builder.addField("Servers:", String.valueOf(PorkBot.INSTANCE.jda.getGuilds().size()), false);

        builder.addField("Known users:", String.valueOf(PorkBot.INSTANCE.jda.getUsers().size()), false);

        builder.addField("ID:", PorkBot.INSTANCE.jda.getSelfUser().getId(), false);

        PorkBot.sendMessage(builder, evt.getTextChannel());
    }

    @Override
    public String getUsage() {
        return "..ping";
    }

    @Override
    public String getUsageExample()	{
        return "..ping";
    }
}
