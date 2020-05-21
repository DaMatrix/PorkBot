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

package net.daporkchop.porkbot.util;

import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import lombok.experimental.UtilityClass;
import net.daporkchop.lib.binary.oio.reader.UTF8FileReader;
import net.daporkchop.lib.binary.oio.writer.UTF8FileWriter;
import net.daporkchop.lib.common.misc.file.PFiles;
import net.daporkchop.porkbot.command.CommandRegistry;

import java.io.File;
import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.util.concurrent.atomic.AtomicLong;

/**
 * @author DaPorkchop_
 */
@UtilityClass
public class Config {
    public final AtomicLong COMMAND_COUNT_SESSION = new AtomicLong(0L);
    public final AtomicLong COMMAND_COUNT_TOTAL = new AtomicLong(0L);

    public final AtomicLong TRACKS_PLAYED_TOTAL = new AtomicLong(0L);
    public final AtomicLong TIME_PLAYED_TOTAL = new AtomicLong(0L);

    public void load() {
        File file = new File("command_info.json");
        if (!PFiles.checkFileExists(file)) {
            save();
            return;
        }

        JsonObject obj;
        try (Reader in = new UTF8FileReader(file)) {
            obj = new JsonParser().parse(in).getAsJsonObject();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        switch (obj.get("version").getAsInt()) {
            case 3:
                TRACKS_PLAYED_TOTAL.set(obj.get("tracksPlayed").getAsLong());
                TIME_PLAYED_TOTAL.set(obj.get("timePlayed").getAsLong());
            case 2:
                COMMAND_COUNT_TOTAL.set(obj.get("totalCommands").getAsLong());
                obj.getAsJsonObject("commandUsages").entrySet().stream()
                        .filter(entry -> CommandRegistry.COMMANDS.containsKey(entry.getKey()))
                        .forEach(entry -> CommandRegistry.COMMANDS.get(entry.getKey()).uses.set(entry.getValue().getAsLong()));
                break;
            default:
                throw new IllegalArgumentException("Unknown config version: " + obj.get("version").getAsInt());
        }
    }

    public void save() {
        JsonObject object = new JsonObject();

        object.addProperty("version", 3);

        object.addProperty("totalCommands", COMMAND_COUNT_TOTAL.get());
        object.add("commandUsages", CommandRegistry.COMMANDS.values().stream().collect(
                JsonObject::new,
                (obj, command) -> obj.addProperty(command.prefix, command.uses.get()),
                (a, b) -> {
                    throw new IllegalStateException();
                }));

        object.addProperty("tracksPlayed", TRACKS_PLAYED_TOTAL.get());
        object.addProperty("timePlayed", TIME_PLAYED_TOTAL.get());

        try (Writer writer = new UTF8FileWriter(PFiles.ensureFileExists(new File("command_info.json")))) {
            new GsonBuilder().setPrettyPrinting().create().toJson(object, writer);
        } catch (IOException e) {
            throw new RuntimeException();
        }
    }
}
