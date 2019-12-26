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

package net.daporkchop.porkbot.command.misc;

import net.daporkchop.porkbot.command.Command;
import net.daporkchop.porkbot.util.MessageUtils;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

import java.util.Arrays;
import java.util.StringJoiner;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Stream;

public class CommandDice extends Command {
    public CommandDice() {
        super("dice");
    }

    public void execute(MessageReceivedEvent evt, String[] args, String rawContent) {
        if (args.length != 1) {
            if (args.length == 2) {
                int cnt;
                try {
                    cnt = Integer.parseInt(args[1]);
                } catch (NumberFormatException e) {
                    sendErrorMessage(evt.getTextChannel(), "Not a number!");
                    return;
                }
                if (cnt < 1 || cnt > 16)    {
                    sendErrorMessage(evt.getTextChannel(), "Invalid number of dice! Must be between 1 and 16 (inclusive).");
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
                    ), evt.getTextChannel());
                    return;
                }
            } else {
                sendErrorMessage(evt.getTextChannel(), "IP isn't given!");
                return;
            }
        }
        MessageUtils.sendMessage("I rolled a **" + (ThreadLocalRandom.current().nextInt(6) + 1) + "**!", evt.getTextChannel());
    }
}
