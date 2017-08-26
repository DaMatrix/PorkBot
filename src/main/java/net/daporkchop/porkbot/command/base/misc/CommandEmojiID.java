package net.daporkchop.porkbot.command.base.misc;

import net.daporkchop.porkbot.PorkBot;
import net.daporkchop.porkbot.command.Command;
import net.dv8tion.jda.core.events.message.MessageReceivedEvent;

public class CommandEmojiID extends Command {
    public CommandEmojiID() {
        super("emojiid");
    }

    public void execute(MessageReceivedEvent evt, String[] split, String rawContent) {
        if (split.length != 2) {
            this.sendErrorMessage(evt.getTextChannel(), "You need to have 2 arguments!");
            return;
        }

        PorkBot.sendMessage("Emote ID: `" + split[1] + "`", evt.getTextChannel());
    }
}
