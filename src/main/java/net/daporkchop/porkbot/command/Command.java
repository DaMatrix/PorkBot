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

package net.daporkchop.porkbot.command;

import net.daporkchop.porkbot.util.MessageUtils;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;

import java.util.concurrent.atomic.AtomicLong;

public abstract class Command {

    public String prefix;
    public final AtomicLong uses = new AtomicLong(0L);

    public transient String[] aliases;

    public Command(String prefix) {
        this(prefix, new String[0]);
    }

    public Command(String prefix, String... aliases) {
        this.prefix = prefix;

        this.aliases = aliases;
    }

    /**
     * Does command logic!
     *
     * @param evt The GuildMessageReceivedEvent to be parsed
     */
    public abstract void execute(GuildMessageReceivedEvent evt, String[] args, String rawContent);

    /**
     * Gets the command's usage
     * <p>
     * you should probably override this
     *
     * @return
     */
    public String getUsage() {
        return ".." + prefix;
    }

    /**
     * Gets and example of using the command
     * <p>
     * you should probably override this
     *
     * @return
     */
    public String getUsageExample() {
        return ".." + prefix;
    }

    public void sendErrorMessage(TextChannel channel, String message) {
        MessageUtils.sendMessage(String.format("%sUsage: `%s`\nExample: `%s`", message == null ? "" : String.format("%s\n", message), this.getUsage(), this.getUsageExample()), channel);
        //MessageUtils.sendMessage((message == null ? "" : message + "\n") + "Usage: `" + getUsage() + "`\nExample: `" + getUsageExample() + "`", channel);
    }

    public boolean shouldSendTyping() {
        return true;
    }
}
