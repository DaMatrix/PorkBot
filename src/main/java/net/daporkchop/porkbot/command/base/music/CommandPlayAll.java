/*
 * Adapted from the Wizardry License
 *
 * Copyright (c) 2016-2018 DaPorkchop_
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

package net.daporkchop.porkbot.command.base.music;

import net.daporkchop.porkbot.audio.AudioUtils;
import net.daporkchop.porkbot.command.Command;
import net.daporkchop.porkbot.util.HTTPUtils;
import net.daporkchop.porkbot.util.MessageUtils;
import net.dv8tion.jda.core.entities.Message;
import net.dv8tion.jda.core.events.message.MessageReceivedEvent;
import org.apache.commons.validator.routines.UrlValidator;

/**
 * @author DaPorkchop_
 */
public class CommandPlayAll extends Command {
    private static UrlValidator validator = new UrlValidator();

    public CommandPlayAll() {
        super("playall");
    }

    public void execute(MessageReceivedEvent evt, String[] split, String rawContent) {
        if (split.length < 2) {
            sendErrorMessage(evt.getTextChannel(), "Not enough arguments!");
            return;
        }
        if (validator.isValid(split[1])) {
            try {
                String[] data = HTTPUtils.performGetRequest(HTTPUtils.constantURL(split[1]), 16000).trim().split("\n");
                int i = 0;
                Message message = evt.getChannel().sendMessage("Loading " + data.length + " tracks... (this might take a while)").complete();
                for (String s : data) {
                    s = s.trim();
                    if (validator.isValid(s)) {
                        AudioUtils.loadAndPlay(evt.getTextChannel(), s, evt.getMember(), false);
                        try {
                            synchronized (s) {
                                s.wait();
                            }
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                        i++;
                    }
                }
                message.editMessage("Loaded " + i + " tracks! (Out of " + data.length + ")").queue();
            } catch (Exception e) {
                MessageUtils.sendException(e, evt);
            }
        } else {
            MessageUtils.sendMessage("Invalid URL: " + split[1], evt.getTextChannel());
        }
    }

    public String getUsage() {
        return "..play <http://url.to/list/of/audio/files.txt>";
    }

    public String getUsageExample() {
        return "..play https://pastebin.com/raw/eLU4atCw";
    }
}
