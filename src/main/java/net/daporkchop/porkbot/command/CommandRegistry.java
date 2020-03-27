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

import lombok.experimental.UtilityClass;
import net.daporkchop.porkbot.util.Config;
import net.daporkchop.porkbot.util.Constants;
import net.daporkchop.porkbot.util.MessageUtils;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ForkJoinPool;

@UtilityClass
public class CommandRegistry {

    /**
     * A HashMap containing all the commands and their prefix
     */
    public final Map<String, Command> COMMANDS = new HashMap<>();

    /**
     * Registers a command to the command registry.
     *
     * @param cmd
     * @return cmd again lul
     */
    public Command registerCommand(Command cmd) {
        COMMANDS.put(cmd.prefix, cmd);
        for (String alias : cmd.aliases) {
            COMMANDS.put(alias, cmd);
        }
        return cmd;
    }

    public void runCommand(GuildMessageReceivedEvent evt, String rawContent) {
        ForkJoinPool.commonPool().submit(() -> doRunCommand(evt, rawContent));
    }

    private void doRunCommand(GuildMessageReceivedEvent evt, String rawContent) {
        String[] split = rawContent.split(" ");
        Command cmd = COMMANDS.getOrDefault(split[0].substring(Constants.COMMAND_PREFIX.length()), null);
        if (cmd != null) {
            evt.getChannel().sendTyping().queue(v -> {
                try {
                    cmd.execute(evt, split, rawContent);
                    Config.COMMAND_COUNT_SESSION.incrementAndGet();
                    Config.COMMAND_COUNT_TOTAL.incrementAndGet();
                    cmd.uses.incrementAndGet();
                } catch (Throwable e) {
                    e.printStackTrace();
                    MessageUtils.sendMessage("Error running command: `" + evt.getMessage().getContentRaw() + "`:\n`" + e.getClass().getCanonicalName() + "`", evt.getChannel());
                }
            });
        }
    }
}
