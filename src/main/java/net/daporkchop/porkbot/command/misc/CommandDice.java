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

package net.daporkchop.porkbot.command.misc;

import net.daporkchop.porkbot.command.Command;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;

import java.util.Formatter;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.IntStream;

public class CommandDice extends Command {
    public CommandDice() {
        super("dice", "die");
    }

    public void execute(GuildMessageReceivedEvent evt, String[] args, String rawContent) {
        int count;
        try {
            count = args.length == 1 ? 1 : Integer.parseUnsignedInt(args[1]);
        } catch (NumberFormatException e) {
            evt.getChannel().sendMessageFormat("Invalid number: `%s`", args[1]).queue();
            return;
        }

        if (count == 1) {
            evt.getChannel().sendMessageFormat("I rolled a **%d**!", ThreadLocalRandom.current().nextInt(6) + 1).queue();
            return;
        } else if (count < 1 || count > 100) {
            evt.getChannel().sendMessage("Die count must be between 1 and 100!").queue();
            return;
        }

        StringBuilder builder = new StringBuilder()
                .append("I rolled ").append(count).append(" dice! Results:\n`");

        int sum = IntStream.range(0, count)
                .map(i -> ThreadLocalRandom.current().nextInt(6) + 1)
                .peek(i -> builder.append(i).append(", "))
                .sum();

        builder.setLength(builder.length() - 2);
        builder.append("`\nSum: ").append(sum).append(" Average: ");
        new Formatter(builder).format("%.3f", sum / (double) count); //haha yes microoptimiztion

        evt.getChannel().sendMessage(builder).queue();
    }
}
