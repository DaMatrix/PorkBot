package net.daporkchop.porkbot.command.base.music;

import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import net.daporkchop.porkbot.PorkBot;
import net.daporkchop.porkbot.command.Command;
import net.daporkchop.porkbot.music.GuildAudioInfo;
import net.dv8tion.jda.core.events.message.MessageReceivedEvent;

import java.util.ArrayList;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

public class CommandShuffle extends Command {
    public CommandShuffle() {
        super("shuffle");
    }

    public void execute(MessageReceivedEvent evt, String[] split, String rawContent) {
        evt.getTextChannel().sendMessage("Shuffling queue...").queue(message -> {
            GuildAudioInfo info = PorkBot.INSTANCE.getGuildAudioPlayer(evt.getGuild(), false);
            if (info == null)   {
                message.editMessage("Not playing!").queue();
            } else {
                BlockingQueue<AudioTrack> queue = info.manager.scheduler.queue;
                AudioTrack[] tracks = queue.toArray(new AudioTrack[queue.size()]);
                ArrayList<Integer> usedIndexes = new ArrayList<>();
                while (usedIndexes.size() < tracks.length) {
                    int index = PorkBot.random.nextInt(tracks.length);
                    if (!usedIndexes.contains(index)) {
                        AudioTrack track = tracks[index];
                        tracks[index] = track;
                        usedIndexes.add(index);
                    }
                }
                BlockingQueue<AudioTrack> newQueue = new LinkedBlockingQueue<>();
                for (AudioTrack track : tracks) {
                    newQueue.add(track);
                }
                info.manager.scheduler.queue = newQueue;
                message.editMessage("Queue shuffled!").queue();
            }
        });
    }
}
