/*
 * Adapted from the Wizardry License
 *
 * Copyright (c) 2016-2019 DaPorkchop_
 *
 * Permission is hereby granted to any persons and/or organizations using this software to copy, modify, merge, publish, and distribute it.
 * Said persons and/or organizations are not allowed to use the software or any derivatives of the work for commercial use or any other means to generate income, nor are they allowed to claim this software as their own.
 *
 * The persons and/or organizations are also disallowed from sub-licensing and/or trademarking this software without explicit permission from DaPorkchop_.
 *
 * Any persons and/or organizations using this software must disclose their source code and have it publicly available, include this license, provide sufficient credit to the original authors of the project (IE: DaPorkchop_), as well as provide a link to the original project.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NON INFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 *
 */

package net.daporkchop.porkbot.command.bot;

import net.daporkchop.porkbot.command.Command;
import net.daporkchop.porkbot.command.CommandRegistry;
import net.daporkchop.porkbot.util.MessageUtils;
import net.daporkchop.porkbot.util.ShardUtils;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

import java.awt.*;

public class CommandBotInfo extends Command {

    public CommandBotInfo() {
        super("botinfo");
    }

    @Override
    public void execute(MessageReceivedEvent evt, String[] args, String message) {
        EmbedBuilder builder = new EmbedBuilder();
        builder.setColor(Color.BLUE);
        builder.setTitle("**PorkBot info**", "http://www.daporkchop.net/porkbot");

        builder.setThumbnail("https://www.daporkchop.net/toembed/profilepic-512p.gif");

        builder.addField("Name:", "PorkBot#" + evt.getJDA().getSelfUser().getDiscriminator(), true);

        builder.addField("Total servers:", String.valueOf(ShardUtils.getGuildCount()), true);

        builder.addField("Total users:", String.valueOf(ShardUtils.getUserCount()), true);

        builder.addField("ID:", evt.getJDA().getSelfUser().getId(), true);

        builder.addField("Commands this session:", String.valueOf(CommandRegistry.COMMAND_COUNT), true);

        builder.addField("Commands all time:", String.valueOf(CommandRegistry.COMMAND_COUNT_TOTAL), true);

        builder.addField("Used RAM:", ((Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory()) / 1024 / 1024) + " MB", false);

        builder.addField("", "**SHARD INFO**", false);

        builder.addField("Shard #:", String.valueOf(evt.getJDA().getShardInfo().getShardId()), true);
        builder.addField("Shard servers:", String.valueOf(evt.getJDA().getGuilds().size()), true);

        builder.addField("Shard users:", String.valueOf(evt.getJDA().getUsers().size()), true);

        MessageUtils.sendMessage(builder, evt.getTextChannel());
    }

    @Override
    public String getUsage() {
        return "..ping";
    }

    @Override
    public String getUsageExample() {
        return "..ping";
    }
}
