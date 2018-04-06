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

package net.daporkchop.porkbot.util;

import net.dv8tion.jda.core.JDA;
import net.dv8tion.jda.core.entities.Game;
import net.dv8tion.jda.core.entities.Guild;
import net.dv8tion.jda.core.managers.AudioManager;

import java.util.ArrayList;
import java.util.List;

public class ShardUtils {
    public static List<Guild> guilds = new ArrayList<>();
    private static List<JDA> shards = null;

    public static void setShards(List<JDA> shards) {
        if (shards != null) {
            ShardUtils.shards = shards;
        }
        ArrayList<Guild> guilds = new ArrayList<>();
        for (JDA jda : shards) {
            guilds.addAll(jda.getGuilds());
        }
        ShardUtils.guilds = guilds;
    }

    public static void setGame(Game game) {
        for (JDA jda : shards) {
            jda.getPresence().setGame(game);
        }
    }

    public static int getGuildCount() {
        int i = 0;
        for (JDA jda : shards) {
            i += jda.getGuilds().size();
        }
        return i;
    }

    public static int getUserCount() {
        int i = 0;
        for (JDA jda : shards) {
            i += jda.getUsers().size();
        }
        return i;
    }

    public static void shutdown() {
        for (JDA jda : shards) {
            jda.shutdownNow();
        }
    }

    /**
     * WARNING: DO NOT USE FREQUENTLY!
     */
    public static List<AudioManager> getConnectedVoice() {
        List<AudioManager> list = new ArrayList<>();
        for (JDA jda : shards) {
            for (AudioManager manager : jda.getAudioManagers()) {
                if (manager.isConnected()) {
                    list.add(manager);
                }
            }
        }
        return list;
    }
}
