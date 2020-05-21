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

package net.daporkchop.porkbot.util.mcpinger;

import com.google.gson.JsonObject;
import lombok.AccessLevel;
import lombok.NonNull;
import lombok.Setter;
import lombok.ToString;
import lombok.experimental.Accessors;
import lombok.experimental.UtilityClass;
import net.daporkchop.lib.binary.oio.StreamUtil;
import net.daporkchop.lib.minecraft.text.parser.MinecraftFormatParser;
import net.daporkchop.porkbot.PorkBot;
import net.daporkchop.porkbot.util.Constants;
import net.daporkchop.porkbot.util.mcpinger.bedrock.BedrockPing;
import net.daporkchop.porkbot.util.mcpinger.java.JavaPing;
import org.xbill.DNS.Lookup;
import org.xbill.DNS.Record;
import org.xbill.DNS.SRVRecord;
import org.xbill.DNS.TextParseException;
import org.xbill.DNS.Type;

import java.io.IOException;
import java.io.InputStream;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@UtilityClass
public class MCPing {
    private final byte[] FALLBACK_PC_FAVICON;

    private final Pattern MCPE_MOTD_PARSER = Pattern.compile("MCPE;([^;]+);([0-9]+);([^;]+);([0-9]+);([0-9]+)");

    private final ExecutorService EXECUTOR = new ThreadPoolExecutor(
            0, Runtime.getRuntime().availableProcessors() << 4,
            30L, TimeUnit.SECONDS,
            new ArrayBlockingQueue<>(512),
            r -> {
                Thread t = new Thread(r);
                t.setDaemon(true);
                return t;
            });

    static {
        try (InputStream in = PorkBot.class.getResourceAsStream("/defaultfavicon.png")) {
            FALLBACK_PC_FAVICON = StreamUtil.toByteArray(in);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static void main(String... args) throws Exception {
        pingPe("play.2p2e.net", 19132).whenCompleteAsync((pe, e) -> System.out.println(pe));
        pingPc("mc.pepsi.team", 25565).whenCompleteAsync((java, e) -> System.out.println(java));
        query("2b2t.org", 25565).whenCompleteAsync((query, e) -> System.out.println(query));

        Thread.sleep(5000L);
    }

    public void boot() {
        JavaPing.boot();
        BedrockPing.boot();
    }

    public void shutdown() {
        JavaPing.shutdown();
        BedrockPing.shutdown();
    }

    public static CompletableFuture<PE> pingPe(String ip, int port) {
        return BedrockPing.ping(new InetSocketAddress(ip, port))
                .thenApplyAsync(pong -> {
                    Matcher matcher = MCPE_MOTD_PARSER.matcher(new String(pong.getUserData(), StandardCharsets.UTF_8));
                    if (!matcher.find()) {
                        throw new IllegalArgumentException("Invalid response!");
                    }
                    try {
                        return new PE()
                                .motd(MinecraftFormatParser.getDefaultInstance().parse(matcher.group(1)).toRawString())
                                .onlinePlayers(Integer.parseInt(matcher.group(4)))
                                .maxPlayers(Integer.parseInt(matcher.group(5)))
                                .version(matcher.group(3))
                                .protocol(Integer.parseInt(matcher.group(2)))
                                .latency(pong.getPongTime() - pong.getPingTime());
                    } catch (Exception e) {
                        throw new IllegalArgumentException("Invalid response!", e);
                    }
                });
    }

    public static CompletableFuture<Query> query(String ip, int port) {
        CompletableFuture<Query> future = new CompletableFuture<>();

        EXECUTOR.submit(() -> {
            try {
                MCQuery query = new MCQuery(checkAddressForSRV(new InetSocketAddress(ip, port)));

                future.complete(new Query()
                        .motd(MinecraftFormatParser.getDefaultInstance().parse(query.getMOTD()).toRawString())
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
                future.completeExceptionally(e);
            }
        });

        return future;
    }

    public static CompletableFuture<Java> pingPc(String ip, int port) {
        return CompletableFuture.supplyAsync(() -> checkAddressForSRV(new InetSocketAddress(ip, port)))
                .thenComposeAsync(addr -> JavaPing.ping(ip, port, addr))
                .thenApplyAsync(reply -> {
                    try {
                        if (reply != null) {
                            JsonObject desc = reply.getAsJsonObject("description");
                            String text = desc.get("text").getAsString();
                            desc.remove("text");
                            if (desc.size() != 0) {
                                text = Constants.GSON.toJson(desc);
                            }
                            text = MinecraftFormatParser.getDefaultInstance().parse(text).toRawString();

                            return new Java()
                                    .motd(text)
                                    .onlinePlayers(reply.getAsJsonObject("players").get("online").getAsInt())
                                    .maxPlayers(reply.getAsJsonObject("players").get("max").getAsInt())
                                    .version(reply.getAsJsonObject("version").get("name").getAsString())
                                    .protocol(reply.getAsJsonObject("version").get("protocol").getAsInt())
                                    .latency(reply.get("ping").getAsLong())
                                    .favicon(getFavicon(reply));
                        } else {
                            throw new RuntimeException("No response!");
                        }
                    } catch (RuntimeException e) {
                        throw e;
                    } catch (Exception e) {
                        throw new RuntimeException(e);
                    }
                });
    }

    private static byte[] getFavicon(@NonNull JsonObject reply) {
        if (reply.has("favicon") && !reply.get("favicon").getAsString().isEmpty()) {
            String favicon = reply.get("favicon").getAsString().substring("data:image/png;base64,".length()).replace("\n", "");
            return Base64.getDecoder().decode(favicon);
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
        public int onlinePlayers;
        public int maxPlayers;

        public String version;
        public int protocol;

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
        public byte[] favicon;
    }

    /**
     * A generic query, used by both PE and PC
     */
    @ToString(callSuper = true)
    @Setter(AccessLevel.PACKAGE)
    @Accessors(fluent = true, chain = true)
    public static final class Query extends Ping<Query> {
        public String[] playerSample;
        public String plugins;
        public String mapName;
        public String gamemode;
    }
}
