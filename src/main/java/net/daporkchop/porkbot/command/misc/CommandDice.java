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
import net.daporkchop.porkbot.util.MessageUtils;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;

import java.util.Arrays;
import java.util.StringJoiner;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Stream;

public class CommandDice extends Command {
    public CommandDice() {
        super("dice");
    }

    public void execute(GuildMessageReceivedEvent evt, String[] args, String rawContent) {
        if (args.length != 1) {
            if (args.length == 2) {
                int cnt;
                try {
                    cnt = Integer.parseInt(args[1]);
                } catch (NumberFormatException e) {
                    sendErrorMessage(evt.getChannel(), "Not a number!");
                    return;
                }
                if (cnt < 1 || cnt > 16)    {
                    sendErrorMessage(evt.getChannel(), "Invalid number of dice! Must be between 1 and 16 (inclusive).");
                    return;
                } else if (cnt != 1) {
                    int[] rolls = new int[cnt];
                    int sum = 0;
                    ThreadLocalRandom r = ThreadLocalRandom.current();
                    for (int i = cnt - 1; i >= 0; i--)  {
                        sum += rolls[i] = r.nextInt(6) + 1;
                    }
                    MessageUtils.sendMessage(String.format(
                            "I rolled %d dice. The results were:\n**%s**\nTotal: **%d**\nAverage: **%.3f**",
                            cnt,
                            Arrays.stream(rolls).collect(() -> new StringJoiner("**, **"), (joiner, i) -> joiner.add(String.valueOf(i)), StringJoiner::merge),
                            sum,
                            (double) sum / (double) cnt
                    ), evt.getChannel());
                    return;
                }
            } else {
                sendErrorMessage(evt.getChannel(), "IP isn't given!");
                return;
            }
        }
        MessageUtils.sendMessage("I rolled a **" + (ThreadLocalRandom.current().nextInt(6) + 1) + "**!", evt.getChannel());
    }
}
