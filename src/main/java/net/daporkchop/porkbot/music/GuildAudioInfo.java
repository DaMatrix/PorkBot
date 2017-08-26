package net.daporkchop.porkbot.music;

import net.dv8tion.jda.core.entities.TextChannel;
import net.dv8tion.jda.core.entities.VoiceChannel;

public class GuildAudioInfo {
    public GuildAudioManager manager;
    public VoiceChannel channel;
    public TextChannel textChannel;

    public GuildAudioInfo(GuildAudioManager manager, VoiceChannel channel) {
        this.manager = manager;
        this.channel = channel;
    }

    public GuildAudioInfo(GuildAudioManager manager) {
        this.manager = manager;
    }
}
