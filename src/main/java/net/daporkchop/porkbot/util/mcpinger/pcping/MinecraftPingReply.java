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
package net.daporkchop.porkbot.util.mcpinger.pcping;

import java.util.List;

/**
 * References:
 * http://wiki.vg/Server_List_Ping
 * https://gist.github.com/thinkofdeath/6927216
 */
public class MinecraftPingReply {

    private Description description;
    private Players players;
    private Version version;
    private String favicon;

    /**
     * @return the MOTD
     */
    public Description getDescription() {
        return this.description;
    }

    /**
     * @return @{link Players}
     */
    public Players getPlayers() {
        return this.players;
    }

    /**
     * @return @{link Version}
     */
    public Version getVersion() {
        return this.version;
    }

    /**
     * @return Base64 encoded favicon image
     */
    public String getFavicon() {
        return this.favicon;
    }

    public class Description {
        private String text;

        /**
         * @return Server description text
         */
        public String getText() {
            return this.text;
        }
    }

    public class Players {
        private int max;
        private int online;
        private List<Player> sample;

        /**
         * @return Maximum player count
         */
        public int getMax() {
            return this.max;
        }

        /**
         * @return Online player count
         */
        public int getOnline() {
            return this.online;
        }

        /**
         * @return List of some players (if any) specified by server
         */
        public List<Player> getSample() {
            return this.sample;
        }
    }

    public class Player {
        private String name;
        private String id;

        /**
         * @return Name of player
         */
        public String getName() {
            return this.name;
        }

        /**
         * @return Unknown
         */
        public String getId() {
            return this.id;
        }

    }

    public class Version {
        private String name;
        private int protocol;

        /**
         * @return Version name (ex: 13w41a)
         */
        public String getName() {
            return this.name;
        }

        /**
         * @return Protocol version
         */
        public int getProtocol() {
            return this.protocol;
        }
    }

}
