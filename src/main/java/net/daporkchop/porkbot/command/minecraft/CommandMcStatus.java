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

package net.daporkchop.porkbot.command.minecraft;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import net.daporkchop.porkbot.command.Command;
import net.daporkchop.porkbot.util.HTTPUtils;
import net.daporkchop.porkbot.util.MessageUtils;
import net.dv8tion.jda.core.EmbedBuilder;
import net.dv8tion.jda.core.events.message.MessageReceivedEvent;

import java.awt.*;
import java.io.IOException;
import java.util.Map;

public class CommandMcStatus extends Command {

    public CommandMcStatus() {
        super("mcstatus");
    }

    @Override
    public void execute(MessageReceivedEvent evt, String[] args, String message) {
        String s;
        try {
            s = HTTPUtils.performGetRequest(HTTPUtils.constantURL("https://mcapi.ca/mcstatus"), Integer.MAX_VALUE);
        } catch (IOException e) {
            e.printStackTrace();
            MessageUtils.sendMessage("Error getting server info: `java.io.IOException`", evt.getTextChannel());
            return;
        }

        try {
            JsonObject json = (new JsonParser()).parse(s).getAsJsonObject();

            EmbedBuilder builder = new EmbedBuilder();

            builder.setTitle("**Mojang status:**", "https://mojang.com");

            boolean isAllOnline = true;
            int onlineCount = 0;

            for (Map.Entry<String, JsonElement> entry : json.entrySet()) {
                if (entry.getValue().getAsJsonObject().get("status").getAsString().equals("Online")) {
                    onlineCount++;
                    builder.addField(entry.getKey(), "*Online*", true);
                } else {
                    isAllOnline = false;
                    builder.addField(entry.getKey(), "*Offline*", true);
                }
            }

            if (isAllOnline) {
                //everything is online
                builder.setColor(Color.GREEN);
            } else {
                if (onlineCount > 0) {
                    //some stuff is online
                    builder.setColor(Color.ORANGE);
                } else {
                    //nothing is online
                    builder.setColor(Color.RED);
                }
            }

            MessageUtils.sendMessage(builder, evt.getTextChannel());
        } catch (IllegalStateException e) {
            MessageUtils.sendMessage("Unable to parse minecraft status!", evt.getTextChannel());
            return;
        }
    }

    @Override
    public String getUsage() {
        return "..mcstatus";
    }

    @Override
    public String getUsageExample() {
        return "..mcstatus";
    }
}
