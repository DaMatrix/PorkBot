/*
 * Adapted from the Wizardry License
 *
 * Copyright (c) 2016-2018 DaPorkchop_
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

package net.daporkchop.porkbot.command.base.minecraft;

import net.daporkchop.porkbot.command.Command;
import net.daporkchop.porkbot.util.MessageUtils;
import net.daporkchop.porkbot.util.UUIDFetcher;
import net.dv8tion.jda.core.EmbedBuilder;
import net.dv8tion.jda.core.JDA;
import net.dv8tion.jda.core.events.message.MessageReceivedEvent;

import java.awt.*;

public class CommandMcSkin extends Command {

    public CommandMcSkin() {
        super("mcskin");
    }

    @Override
    public void execute(MessageReceivedEvent evt, String[] args, String message, JDA thisShardJDA) {
        try {
            if (args.length < 2 || args[1].isEmpty()) {
                sendErrorMessage(evt.getTextChannel(), "Name isn't given!");
                return;
            }

            UUIDFetcher.enqueueRequest(args[1], uuid -> {
                EmbedBuilder builder = new EmbedBuilder();
                builder.setImage("attachment://image.png");
                builder.setColor(Color.DARK_GRAY);

                byte[] outBytes = MessageUtils.downloadImage("https://crafatar.com/renders/body/" + uuid + "?overlay&default=MHF_Steve");

                builder.addField(args[1] + "'s skin", "", false);

                MessageUtils.sendImage(builder, outBytes, "image.png", evt.getTextChannel());
            });
        } catch (Exception e) {
            MessageUtils.sendException(e, evt);
        }
    }

    @Override
    public String getUsage() {
        return "..mcskin <name>";
    }

    @Override
    public String getUsageExample() {
        return "..mcskin Notch";
    }
}
