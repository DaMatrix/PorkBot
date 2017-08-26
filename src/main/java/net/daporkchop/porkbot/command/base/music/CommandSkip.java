package net.daporkchop.porkbot.command.base.music;

import net.daporkchop.porkbot.PorkBot;
import net.daporkchop.porkbot.command.Command;
import net.daporkchop.porkbot.music.GuildAudioInfo;
import net.dv8tion.jda.core.events.message.MessageReceivedEvent;

public class CommandSkip extends Command {
    public CommandSkip() {
        super("skip");
    }

    public void execute(MessageReceivedEvent evt, String[] split, String rawContent) {
        PorkBot.INSTANCE.skipTrack(evt.getTextChannel());
    }
}
