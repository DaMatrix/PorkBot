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

import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import lombok.experimental.UtilityClass;
import net.daporkchop.lib.binary.oio.reader.UTF8FileReader;
import net.daporkchop.lib.binary.oio.writer.UTF8FileWriter;
import net.daporkchop.lib.common.misc.file.PFiles;
import net.daporkchop.porkbot.util.Constants;
import net.daporkchop.porkbot.util.MessageUtils;
import net.daporkchop.porkbot.util.ObjectDB;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;

import java.io.File;
import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.atomic.AtomicLong;

@UtilityClass
public class CommandRegistry {

    /**
     * A HashMap containing all the commands and their prefix
     */
    public final Map<String, Command> COMMANDS = new HashMap<>();

    public final AtomicLong COMMAND_COUNT_SESSION = new AtomicLong(0L);
    public final AtomicLong COMMAND_COUNT_TOTAL   = new AtomicLong(0L);

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

    public void load() {
        JsonObject obj;
        File file = new File("command_info.json");
        if (PFiles.checkFileExists(file)) {
            try (Reader in = new UTF8FileReader(file)) {
                obj = new JsonParser().parse(in).getAsJsonObject();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        } else if (PFiles.checkFileExists(file = new File(System.getProperty("user.dir") + File.separatorChar + "command_info.dat"))) {
            //convert old format
            ObjectDB old = new ObjectDB(file);
            COMMAND_COUNT_TOTAL.set(old.getLong("totalCommands", 0L));
            for (Command command : COMMANDS.values()) {
                command.uses.set(old.getInteger(command.prefix + "_uses", 0));
            }
            save();
            PFiles.rm(file);
            return;
        } else {
            return;
        }

        COMMAND_COUNT_TOTAL.set(obj.get("totalCommands").getAsLong());
        obj.getAsJsonObject("commandUsages").entrySet().stream()
                .filter(entry -> COMMANDS.containsKey(entry.getKey()))
                .forEach(entry -> COMMANDS.get(entry.getKey()).uses.set(entry.getValue().getAsLong()));
    }

    public void save() {
        JsonObject object = new JsonObject();

        object.addProperty("version", 2);
        object.addProperty("totalCommands", COMMAND_COUNT_TOTAL.get());
        object.add("commandUsages", COMMANDS.values().stream().collect(
                JsonObject::new,
                (obj, command) -> obj.addProperty(command.prefix, command.uses.get()),
                (a, b) -> {
                    throw new IllegalStateException();
                }));

        try (Writer writer = new UTF8FileWriter(PFiles.ensureFileExists(new File("command_info.json")))) {
            new GsonBuilder().setPrettyPrinting().create().toJson(object, writer);
        } catch (IOException e) {
            throw new RuntimeException();
        }
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
                    COMMAND_COUNT_SESSION.incrementAndGet();
                    COMMAND_COUNT_TOTAL.incrementAndGet();
                    cmd.uses.incrementAndGet();
                } catch (Throwable e) {
                    e.printStackTrace();
                    MessageUtils.sendMessage("Error running command: `" + evt.getMessage().getContentRaw() + "`:\n`" + e.getClass().getCanonicalName() + "`", evt.getChannel());
                }
            });
        }
    }
}
