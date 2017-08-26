package net.daporkchop.porkbot.command.base.misc;

import net.daporkchop.porkbot.PorkBot;
import net.daporkchop.porkbot.command.Command;
import net.dv8tion.jda.core.events.message.MessageReceivedEvent;

public class CommandDice extends Command {
    public CommandDice() {
        super("dice");
    }

    public void execute(MessageReceivedEvent evt, String[] split, String rawContent) {
        PorkBot.sendMessage(":game_die: | I rolled a **" + (PorkBot.random.nextInt(6) + 1) + "**!", evt.getTextChannel());
    }
}
