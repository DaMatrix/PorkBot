package tk.daporkchop.porkbot.command.base;

import net.dv8tion.jda.core.EmbedBuilder;
import net.dv8tion.jda.core.events.message.MessageReceivedEvent;
import tk.daporkchop.porkbot.PorkBot;
import tk.daporkchop.porkbot.command.Command;

import java.awt.*;
import java.time.OffsetDateTime;

public class CommandPing extends Command {

    public CommandPing() {
        super("ping");
    }

    @Override
    public void excecute(MessageReceivedEvent evt, String[] args, String message) {
        EmbedBuilder builder = new EmbedBuilder();
        builder.setColor(Color.BLUE);
        builder.setTitle("PorkBot ping...", "http://www.daporkchop.tk/porkbot");

        builder.addField("**Pong:**", PorkBot.INSTANCE.jda.getPing() + "ms", false);
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