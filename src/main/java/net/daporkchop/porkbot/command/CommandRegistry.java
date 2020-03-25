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

import net.daporkchop.porkbot.util.Constants;
import net.daporkchop.porkbot.util.MessageUtils;
import net.daporkchop.porkbot.util.ObjectDB;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;

import java.io.File;
import java.util.HashMap;
import java.util.concurrent.ForkJoinPool;

public abstract class CommandRegistry {

    /**
     * A HashMap containing all the commands and their prefix
     */
    public static final HashMap<String, Command> COMMANDS      = new HashMap<>();
    /**
     * Counts all commands run this session
     */
    public static       long                     COMMAND_COUNT = 0L;
    /**
     * Counts all commands run
     */
    public static  long     COMMAND_COUNT_TOTAL;
    private static ObjectDB command_save;

    static {
        command_save = new ObjectDB(new File(System.getProperty("user.dir") + File.separatorChar + "command_info.dat"));
        COMMAND_COUNT_TOTAL = command_save.getLong("totalCommands", 0L);
    }

    /**
     * Registers a command to the command registry.
     *
     * @param cmd
     * @return cmd again lul
     */
    public static Command registerCommand(Command cmd) {
        cmd.uses = command_save.getInteger(cmd.prefix + "_uses", 0);
        COMMANDS.put(cmd.prefix, cmd);
        for (String alias : cmd.aliases) {
            COMMANDS.put(alias, cmd);
        }
        return cmd;
    }

    /**
     * Runs a command
     *
     * @param evt
     */
    public static void runCommand(GuildMessageReceivedEvent evt, String rawContent) {
        ForkJoinPool.commonPool().submit(() -> doRunCommand(evt, rawContent));
    }

    private static void doRunCommand(GuildMessageReceivedEvent evt, String rawContent) {
        String[] split = rawContent.split(" ");
        Command cmd = COMMANDS.getOrDefault(split[0].substring(Constants.COMMAND_PREFIX.length()), null);
        if (cmd != null) {
            evt.getChannel().sendTyping().queue(v -> {
                try {
                    cmd.execute(evt, split, rawContent);
                    COMMAND_COUNT++;
                    COMMAND_COUNT_TOTAL++;
                    cmd.uses++;
                } catch (Throwable e) {
                    e.printStackTrace();
                    MessageUtils.sendMessage("Error running command: `" + evt.getMessage().getContentRaw() + "`:\n`" + e.getClass().getCanonicalName() + "`", evt.getChannel());
                }
            });
        }
    }

    public static void save() {
        for (Command cmd : COMMANDS.values()) {
            command_save.setInteger(cmd.prefix + "_uses", cmd.uses);
        }
        command_save.setLong("totalCommands", COMMAND_COUNT_TOTAL);
        command_save.save();
    }
}
