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

package net.daporkchop.porkbot.command.music;

/*
import net.daporkchop.porkbot.audio.AudioUtils;
import net.daporkchop.porkbot.command.Command;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import org.apache.commons.validator.routines.UrlValidator;

import java.util.concurrent.ExecutionException;

public class CommandPlay extends Command {
    private static UrlValidator validator = new UrlValidator();

    public CommandPlay() {
        super("play");
    }

    public void execute(MessageReceivedEvent evt, String[] args, String rawContent) {
        if (args.length < 2) {
            sendErrorMessage(evt.getTextChannel(), "Not enough arguments!");
            return;
        }
        if (validator.isValid(args[1])) {
            AudioUtils.loadAndPlay(evt.getTextChannel(), args[1], evt.getMember());
        } else {
            try {
                AudioUtils.loadAndPlay(evt.getTextChannel(), AudioUtils.videoNameCache.get(rawContent.substring(7)), evt.getMember());
            } catch (ExecutionException e) {
                e.printStackTrace();
            }
        }
    }

    public String getUsage() {
        return "..play <url>` or `..play <youtube search terms>";
    }

    public String getUsageExample() {
        return "..play https://www.youtube.com/watch?v=IvUU8joBb1Q` or `..play a-ha take on me";
    }
}*/
