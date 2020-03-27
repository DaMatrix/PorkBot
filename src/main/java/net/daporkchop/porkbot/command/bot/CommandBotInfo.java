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

package net.daporkchop.porkbot.command.bot;

import net.daporkchop.porkbot.audio.PorkAudio;
import net.daporkchop.porkbot.audio.ServerAudioManager;
import net.daporkchop.porkbot.command.Command;
import net.daporkchop.porkbot.command.CommandRegistry;
import net.daporkchop.porkbot.util.MessageUtils;
import net.daporkchop.porkbot.util.ShardUtils;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.VoiceChannel;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;

import java.awt.Color;
import java.util.Collection;
import java.util.Objects;
import java.util.stream.Collectors;

public class CommandBotInfo extends Command {
    public CommandBotInfo() {
        super("botinfo");
    }

    @Override
    public void execute(GuildMessageReceivedEvent evt, String[] args, String message) {
        Collection<VoiceChannel> playingChannels = PorkAudio.getManagerSnapshot().stream()
                .filter(ServerAudioManager::isPlaying)
                .map(ServerAudioManager::connectedChannel)
                .filter(Objects::nonNull)
                .collect(Collectors.toList());

        MessageUtils.sendMessage(new EmbedBuilder().setColor(Color.BLUE)
                        .setAuthor("PorkBot", "https://www.daporkchop.net/porkbot", "https://www.daporkchop.net/toembed/profilepic-64p.gif")
                        .addField("Total servers:", String.valueOf(ShardUtils.getGuildCount()), true)
                        .addField("Total users:", String.valueOf(ShardUtils.getUserCount()), true)
                        .addField("Used RAM:", String.format("%.2f MiB", (Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory()) / (1024.0d * 1024.0d)), true)
                        .addField("Commands this session:", String.valueOf(CommandRegistry.COMMAND_COUNT_SESSION), true)
                        .addField("Commands all time:", String.valueOf(CommandRegistry.COMMAND_COUNT_TOTAL), true)
                        .addField("", "**SHARD INFO**", false)
                        .addField("Shard #:", String.format("%d/%d", evt.getJDA().getShardInfo().getShardId(), ShardUtils.getShardCount()), true)
                        .addField("Shard servers:", String.valueOf(evt.getJDA().getGuildCache().size()), true)
                        .addField("Shard users:", String.valueOf(evt.getJDA().getUserCache().size()), true)
                        .addField("", "**AUDIO INFO**", false)
                        .addField("Playing in:", playingChannels.size() + " servers", true)
                        .addField("Playing to:", playingChannels.stream().mapToLong(channel -> channel.getMembers().size() - 1).sum() + " users", true),
                evt.getChannel());
    }

    @Override
    public String getUsage() {
        return "..botinfo";
    }

    @Override
    public String getUsageExample() {
        return "..botinfo";
    }
}
