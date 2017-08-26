package net.daporkchop.porkbot.command.base.music;

import net.daporkchop.porkbot.PorkBot;
import net.daporkchop.porkbot.command.Command;
import net.dv8tion.jda.core.events.message.MessageReceivedEvent;
import org.apache.commons.validator.routines.UrlValidator;

import java.util.concurrent.ExecutionException;

public class CommandPlay extends Command {
    private UrlValidator validator = new UrlValidator();

    public CommandPlay()    {
        super("play");
    }

    public void execute(MessageReceivedEvent evt, String[] split, String rawContent)    {
        if (validator.isValid(split[1])) {
            PorkBot.INSTANCE.loadAndPlay(evt.getTextChannel(), split[1], evt.getMember());
        } else {
            try {
                PorkBot.INSTANCE.loadAndPlay(evt.getTextChannel(), PorkBot.INSTANCE.videoNameCache.get(rawContent.substring(7)), evt.getMember());
            } catch (ExecutionException e)  {
                e.printStackTrace();
            }
        }
    }
}
