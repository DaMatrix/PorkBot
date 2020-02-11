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

package net.daporkchop.porkbot.util.mcpinger;

import com.google.gson.JsonObject;
import com.nukkitx.network.raknet.RakNetClient;
import com.nukkitx.network.raknet.RakNetPong;
import lombok.AccessLevel;
import lombok.NonNull;
import lombok.Setter;
import lombok.ToString;
import lombok.experimental.Accessors;
import net.daporkchop.lib.minecraft.text.MCTextEncoder;
import net.daporkchop.lib.minecraft.text.MCTextType;
import net.daporkchop.lib.minecraft.text.parser.MinecraftFormatParser;
import net.daporkchop.porkbot.util.Constants;
import net.daporkchop.porkbot.util.mcpinger.pcping.MinecraftPing;
import org.xbill.DNS.Lookup;
import org.xbill.DNS.Record;
import org.xbill.DNS.SRVRecord;
import org.xbill.DNS.TextParseException;
import org.xbill.DNS.Type;

import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * A library for interacting with DaPorkchop_'s mcping api
 */
public abstract class MCPing {
    private static final String       FALLBACK_PC_FAVICON = "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";
    private static final RakNetClient RAKNET_CLIENT       = new RakNetClient(new InetSocketAddress(0));
    private static final Pattern      MCPE_MOTD_PARSER    = Pattern.compile("MCPE;([^;]+);([0-9]+);([^;]+);([0-9]+);([0-9]+)");
    private static final Pattern FAVICON_EXTRACTOR = Pattern.compile(",(\\S+)");

    private static final ExecutorService EXECUTOR = new ThreadPoolExecutor(
            0, Runtime.getRuntime().availableProcessors() << 4,
            30L, TimeUnit.SECONDS,
            new ArrayBlockingQueue<>(512),
            r -> {
                Thread t = new Thread(r);
                t.setDaemon(true);
                return t;
            }
    );

    static {
        RAKNET_CLIENT.bind();
        Runtime.getRuntime().addShutdownHook(new Thread(RAKNET_CLIENT::close));
    }

    public static void main(String... args) throws Exception {
        pingPe("play.2p2e.net", 19132).whenComplete((pe, e) -> System.out.println(pe));
        pingPc("mc.pepsi.team", 25565).whenComplete((java, e) -> System.out.println(java));
        query("2b2t.org", 25565).whenComplete((query, e) -> System.out.println(query));

        Thread.sleep(5000L);
    }

    private static PE parsePE(@NonNull RakNetPong pong) {
        Matcher matcher = MCPE_MOTD_PARSER.matcher(new String(pong.getUserData(), StandardCharsets.UTF_8));
        if (!matcher.find()) {
            throw new IllegalArgumentException("Invalid response!");
        }
        try {
            return new PE()
                    .motd(matcher.group(1))
                    .onlinePlayers(Integer.parseInt(matcher.group(4)))
                    .maxPlayers(Integer.parseInt(matcher.group(5)))
                    .version(matcher.group(3))
                    .protocol(Integer.parseInt(matcher.group(2)))
                    .latency(pong.getPongTime() - pong.getPingTime());
        } catch (Exception e) {
            throw new IllegalArgumentException("Invalid response!");
        }
    }

    public static CompletableFuture<PE> pingPe(String ip, int port) {
        CompletableFuture<PE> future = new CompletableFuture<>();

        RAKNET_CLIENT.ping(new InetSocketAddress(ip, port), 5L, TimeUnit.SECONDS).whenComplete((pong, ex) -> {
            if (ex != null) {
                future.completeExceptionally(ex);
            } else {
                try {
                    future.complete(parsePE(pong));
                } catch (Exception e) {
                    future.completeExceptionally(e);
                }
            }
        });

        return future;
    }

    public static CompletableFuture<Query> query(String ip, int port) {
        CompletableFuture<Query> future = new CompletableFuture<>();

        EXECUTOR.submit(() -> {
            try {
                InetSocketAddress address = checkAddressForSRV(new InetSocketAddress(ip, port));
                try {
                    MCQuery query = new MCQuery(address);

                    future.complete(new Query()
                            .motd(query.getMOTD())
                            .onlinePlayers(query.getOnlinePlayers())
                            .maxPlayers(query.getMaxPlayers())
                            .version(query.values.getOrDefault("version", "Unknown"))
                            .protocol(-1)
                            .latency(-1L)
                            .playerSample(query.onlineUsernames)
                            .plugins(query.getPlugins())
                            .mapName(query.getMapName())
                            .gamemode(query.getGameMode()));
                } catch (Exception e) {
                    future.complete(null);
                }
            } catch (Exception e) {
                future.completeExceptionally(e);
            }
        });

        return future;
    }

    public static CompletableFuture<Java> pingPc(String ip, int port) {
        CompletableFuture<Java> future = new CompletableFuture<>();

        EXECUTOR.submit(() -> {
            try {
                InetSocketAddress address = checkAddressForSRV(new InetSocketAddress(ip, port));

                JsonObject reply = MinecraftPing.getPing(address);

                if (reply != null) {
                    JsonObject desc = reply.getAsJsonObject("description");
                    String text = desc.get("text").getAsString();
                    desc.remove("text");
                    if (desc.size() != 0)   {
                        text = MCTextEncoder.encode(MCTextType.LEGACY, MinecraftFormatParser.getDefaultInstance().parse(Constants.GSON.toJson(desc)));
                    }

                    future.complete(new Java()
                            .motd(text)
                            .onlinePlayers(reply.getAsJsonObject("players").get("online").getAsInt())
                            .maxPlayers(reply.getAsJsonObject("players").get("max").getAsInt())
                            .version(reply.getAsJsonObject("version").get("name").getAsString())
                            .protocol(reply.getAsJsonObject("version").get("protocol").getAsInt())
                            .latency(reply.get("ping").getAsLong())
                            .favicon(getFavicon(reply)));
                } else {
                    throw new RuntimeException("No response!");
                }
            } catch (Exception e) {
                future.completeExceptionally(e);
            }
        });

        return future;
    }

    private static String getFavicon(@NonNull JsonObject reply) {
        if (reply.has("favicon") && !reply.get("favicon").getAsString().isEmpty())  {
            Matcher matcher = FAVICON_EXTRACTOR.matcher(reply.get("favicon").getAsString());
            if (matcher.find()) {
                return matcher.group(1);
            } else {
                throw new IllegalArgumentException("Invalid favicon!");
            }
        } else {
            return FALLBACK_PC_FAVICON;
        }
    }

    public static InetSocketAddress checkAddressForSRV(InetSocketAddress address) {
        try {
            Record[] records = new Lookup(String.format("_minecraft._tcp.%s", address.getHostName()), Type.SRV).run();
            if (records != null) {
                //TODO: actually use the SRV balancing stuff
                /*for (Record record : records) {
                    SRVRecord srv = (SRVRecord) record;
                    return new InetSocketAddress(srv.getTarget().toString(), srv.getPort());
                }*/
                SRVRecord srv = (SRVRecord) records[0];
                return new InetSocketAddress(srv.getTarget().toString(), srv.getPort());
            }
        } catch (TextParseException e1) {
            e1.printStackTrace();
        }
        return address;
    }

    /**
     * Represents a ping. All pings extend this.
     */
    @SuppressWarnings("unchecked")
    @ToString
    protected static abstract class Ping<P extends Ping<P>> {
        public String motd;
        public int    onlinePlayers;
        public int    maxPlayers;

        public String version;
        public int    protocol;

        public long latency;

        P motd(@NonNull String motd) {
            this.motd = motd;
            return (P) this;
        }

        P onlinePlayers(int onlinePlayers) {
            this.onlinePlayers = onlinePlayers;
            return (P) this;
        }

        P maxPlayers(int maxPlayers) {
            this.maxPlayers = maxPlayers;
            return (P) this;
        }

        P version(@NonNull String version) {
            this.version = version;
            return (P) this;
        }

        P protocol(int protocol) {
            this.protocol = protocol;
            return (P) this;
        }

        P latency(long latency) {
            this.latency = latency;
            return (P) this;
        }
    }

    /**
     * A Minecraft: Pocket Edition ping.
     */
    @ToString(callSuper = true)
    @Setter(AccessLevel.PACKAGE)
    @Accessors(fluent = true, chain = true)
    public static final class PE extends Ping<PE> {
    }

    /**
     * Represents a MCPC ping
     */
    @ToString(callSuper = true)
    @Setter(AccessLevel.PACKAGE)
    @Accessors(fluent = true, chain = true)
    public static final class Java extends Ping<Java> {
        public String favicon;
    }

    /**
     * A generic query, used by both PE and PC
     */
    @ToString(callSuper = true)
    @Setter(AccessLevel.PACKAGE)
    @Accessors(fluent = true, chain = true)
    public static final class Query extends Ping<Query> {
        public String[] playerSample;
        public String   plugins;
        public String   mapName;
        public String   gamemode;
    }
}
