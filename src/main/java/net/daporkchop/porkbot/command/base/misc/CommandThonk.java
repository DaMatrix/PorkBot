package net.daporkchop.porkbot.command.base.misc;

import net.daporkchop.porkbot.PorkBot;
import net.daporkchop.porkbot.command.Command;
import net.dv8tion.jda.core.events.message.MessageReceivedEvent;

public class CommandThonk extends Command {
    public CommandThonk() {
        super("thonk");
    }

    public void excecute(MessageReceivedEvent evt, String[] split, String rawContent) {
        PorkBot.sendMessage("<:thonk:324070259265110016>", evt.getTextChannel());
    }
}
