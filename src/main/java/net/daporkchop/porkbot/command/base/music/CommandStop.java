package net.daporkchop.porkbot.command.base.music;

import net.daporkchop.porkbot.PorkBot;
import net.daporkchop.porkbot.command.Command;
import net.daporkchop.porkbot.music.GuildAudioInfo;
import net.dv8tion.jda.core.events.message.MessageReceivedEvent;

public class CommandStop extends Command {
    public CommandStop() {
        super("stop");
    }

    public void execute(MessageReceivedEvent evt, String[] split, String rawContent) {
        GuildAudioInfo info = PorkBot.INSTANCE.getGuildAudioPlayer(evt.getGuild(), false);
        if (info == null)   {
            evt.getTextChannel().sendMessage("Not playing!").queue();
        } else {
            info.manager.player.destroy();
            info.channel.getGuild().getAudioManager().closeAudioConnection();
            evt.getTextChannel().sendMessage("Stopped!").queue();
        }
    }
}
