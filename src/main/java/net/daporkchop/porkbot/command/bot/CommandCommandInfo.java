/*
 * Adapted from the Wizardry License
 *
 * Copyright (c) 2016-2020 DaPorkchop_
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

package net.daporkchop.porkbot.command.bot;

import net.daporkchop.porkbot.command.Command;
import net.daporkchop.porkbot.command.CommandRegistry;
import net.daporkchop.porkbot.util.MessageUtils;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;

public class CommandCommandInfo extends Command {

    public CommandCommandInfo() {
        super("commandinfo");
    }

    @Override
    public void execute(GuildMessageReceivedEvent evt, String[] args, String message) {
        if (args.length < 2 || args[1].isEmpty()) {
            sendErrorMessage(evt.getChannel(), "You need to have at least one argument!");
            return;
        }

        Command command = CommandRegistry.COMMANDS.getOrDefault(args[1], null);
        if (command == null) {
            MessageUtils.sendMessage("No such command: `" + args[1] + "`", evt.getChannel());
            return;
        }

        MessageUtils.sendMessage("Statistics of command: `" + command.prefix + "`\n\nUses: `" + command.uses + "`", evt.getChannel());
    }

    @Override
    public String getUsage() {
        return "..commandinfo <commandName>";
    }

    @Override
    public String getUsageExample() {
        return "..commandinfo mcping";
    }
}
