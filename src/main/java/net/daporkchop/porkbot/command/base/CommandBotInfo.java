/*
 * Adapted from the Wizardry License
 *
 * Copyright (c) 2016 DaPorkchop_
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

package net.daporkchop.porkbot.command.base;

import net.daporkchop.porkbot.PorkBot;
import net.daporkchop.porkbot.command.Command;
import net.daporkchop.porkbot.command.CommandRegistry;
import net.dv8tion.jda.core.EmbedBuilder;
import net.dv8tion.jda.core.events.message.MessageReceivedEvent;

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

        builder.setThumbnail("https://cdn.discordapp.com/avatars/226975061880471552/a_195cf606ffbe9bd5bf1e8764c711253c.gif?size=256");

        builder.addField("Name:", "PorkBot#" + PorkBot.INSTANCE.jda.getSelfUser().getDiscriminator(), true);

        builder.addField("Servers:", String.valueOf(PorkBot.INSTANCE.jda.getGuilds().size()), true);

        builder.addField("Known users:", String.valueOf(PorkBot.INSTANCE.jda.getUsers().size()), true);

        builder.addField("ID:", PorkBot.INSTANCE.jda.getSelfUser().getId(), true);

        builder.addField("Commands this session:", String.valueOf(CommandRegistry.COMMAND_COUNT), true);

        builder.addField("Commands all time:", String.valueOf(CommandRegistry.COMMAND_COUNT_TOTAL), true);

        builder.addField("Used RAM:", ((Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory()) / 1024 / 1024) + " MB", false);

        PorkBot.sendMessage(builder, evt.getTextChannel());
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
