package net.daporkchop.porkbot.command.base.music;

import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import net.daporkchop.porkbot.PorkBot;
import net.daporkchop.porkbot.command.Command;
import net.daporkchop.porkbot.music.GuildAudioInfo;
import net.dv8tion.jda.core.events.message.MessageReceivedEvent;

import java.util.ArrayList;
import java.util.concurrent.TimeUnit;

public class CommandQueue extends Command {
    public CommandQueue() {
        super("queue");
    }

    public void execute(MessageReceivedEvent evt, String[] split, String rawContent) {
        GuildAudioInfo info = PorkBot.INSTANCE.getGuildAudioPlayer(evt.getGuild(), false);
        if (info == null)   {
            evt.getTextChannel().sendMessage("Not playing!").queue();
        } else {
            String msg = "Queue: `" + info.manager.scheduler.queue.size() + "` tracks queued\n\nCurrently playing: `" + info.manager.scheduler.player.getPlayingTrack().getInfo().title + "`\n\nQueue:\n";
            ArrayList<AudioTrack> tracks = new ArrayList<>(info.manager.scheduler.queue);
            for (int i = 0; i < 5 && i < tracks.size(); i++) {
                AudioTrack track = tracks.get(i);
                long minutes = TimeUnit.MILLISECONDS.toMinutes(track.getInfo().length);
                long seconds = TimeUnit.MILLISECONDS.toSeconds(track.getInfo().length);
                msg += (i + 1) + ": " + tracks.get(i).getInfo().title + "(`" + minutes + ":" + seconds + "`)\n";
            }
            PorkBot.sendMessage(msg, evt.getTextChannel());
        }
    }
}
