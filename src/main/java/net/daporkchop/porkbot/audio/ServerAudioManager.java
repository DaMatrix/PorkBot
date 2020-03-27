/*
 * Adapted from The MIT License (MIT)
 *
 * Copyright (c) 2016-2020 DaPorkchop_
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation
 * files (the "Software"), to deal in the Software without restriction, including without limitation the rights to use, copy,
 * modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the Software
 * is furnished to do so, subject to the following conditions:
 *
 * Any persons and/or organizations using this software must include the above copyright notice and this permission notice,
 * provide sufficient credit to the original authors of the project (IE: DaPorkchop_), as well as provide a link to the original project.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES
 * OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS
 * BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 *
 */

package net.daporkchop.porkbot.audio;

import com.sedmelluq.discord.lavaplayer.player.AudioPlayer;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;
import lombok.experimental.Accessors;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.entities.VoiceChannel;
import net.dv8tion.jda.api.managers.AudioManager;

/**
 * @author DaPorkchop_
 */
@Getter
@Accessors(fluent = true, chain = true)
public final class ServerAudioManager {
    @Getter(AccessLevel.NONE)
    private long lastActive = 0L;

    private final Guild          guild;
    private final AudioPlayer    player;
    private final TrackScheduler scheduler;

    @Setter
    @NonNull
    private TextChannel lastAccessedFrom;

    @Setter
    private boolean shuffled = false;

    public ServerAudioManager(@NonNull Guild guild, @NonNull AudioPlayer player) {
        this.guild = guild;
        (this.player = player).addListener(this.scheduler = new TrackScheduler(player, this));
    }

    public AudioPlayerSendHandler sendHandler() {
        return new AudioPlayerSendHandler(this.player);
    }

    public synchronized boolean connect(VoiceChannel dstChannel, boolean errorMsg) {
        if (dstChannel.getGuild() != this.guild) {
            throw new IllegalArgumentException();
        }

        AudioManager manager = this.guild.getAudioManager();
        if (!manager.isConnected() && !manager.isAttemptingToConnect()) {
            if (!dstChannel.getGuild().getMember(dstChannel.getJDA().getSelfUser()).hasPermission(dstChannel, Permission.VOICE_CONNECT))    {
                if (errorMsg && this.lastAccessedFrom != null)   {
                    this.lastAccessedFrom.sendMessage("No permission to join channel `" + dstChannel.getName() + '`').queue();
                }
                return false;
            } else if (!dstChannel.getGuild().getMember(dstChannel.getJDA().getSelfUser()).hasPermission(dstChannel, Permission.VOICE_SPEAK))    {
                if (errorMsg && this.lastAccessedFrom != null)   {
                    this.lastAccessedFrom.sendMessage("No permission to speak in channel `" + dstChannel.getName() + '`').queue();
                }
                return false;
            }
            manager.setSelfDeafened(true);
            manager.setSelfMuted(false);
            manager.openAudioConnection(dstChannel);
        }
        return true;
    }

    synchronized void doDisconnect()   {
        AudioManager manager = this.guild.getAudioManager();
        if (manager.isConnected() || manager.isAttemptingToConnect()) {
            manager.closeAudioConnection();
        }
    }

    public synchronized void handleAllLeft()    {
        this.scheduler.skipAll();
        if (this.lastAccessedFrom != null)  {
            this.lastAccessedFrom.sendMessage("All users have left the voice channel, stopping.").queue();
        }
    }

    public VoiceChannel connectedChannel()  {
        AudioManager manager = this.guild.getAudioManager();
        VoiceChannel channel = manager.getQueuedAudioConnection();
        return channel == null ? manager.getConnectedChannel() : channel;
    }

    public boolean couldConnectToIfNeeded(@NonNull VoiceChannel dstChannel)  {
        VoiceChannel connected = this.connectedChannel();
        return connected == null || connected == dstChannel;
    }

    public void checkTimedOut() {
        if (this.isPlaying()) {
            this.player.checkCleanup(5000L);
            this.lastActive = System.currentTimeMillis();
        } else if (this.lastActive + 5000L < System.currentTimeMillis() && this.connectedChannel() != null) {
            //if we're connected AND the last time a track was playing was over 5 seconds ago, disconnect
            this.guild.getAudioManager().closeAudioConnection();
            this.lastAccessedFrom.sendMessage("Player was idle for too long, stopping.").queue();
        }
    }

    public boolean isPlaying()  {
        return this.player.getPlayingTrack() != null;
    }
}
