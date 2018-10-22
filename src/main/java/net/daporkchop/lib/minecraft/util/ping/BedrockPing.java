/*
 * Adapted from the Wizardry License
 *
 * Copyright (c) 2018-2018 DaPorkchop_ and contributors
 *
 * Permission is hereby granted to any persons and/or organizations using this software to copy, modify, merge, publish, and distribute it. Said persons and/or organizations are not allowed to use the software or any derivatives of the work for commercial use or any other means to generate income, nor are they allowed to claim this software as their own.
 *
 * The persons and/or organizations are also disallowed from sub-licensing and/or trademarking this software without explicit permission from DaPorkchop_.
 *
 * Any persons and/or organizations using this software must disclose their source code and have it publicly available, include this license, provide sufficient credit to the original authors of the project (IE: DaPorkchop_), as well as provide a link to the original project.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NON INFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 *
 */

package net.daporkchop.lib.minecraft.util.ping;

import io.gomint.jraknet.ClientSocket;
import io.gomint.jraknet.SocketEvent;
import lombok.NonNull;
import net.daporkchop.lib.minecraft.api.PingData;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.net.SocketException;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

/**
 * @author DaPorkchop_
 */
public class BedrockPing {
    private static final ClientSocket socket;
    private static final Map<SocketAddress, CompletableFuture<PingData>> queuedPings = new ConcurrentHashMap<>();

    static {
        try {
            socket = new ClientSocket();
            socket.setMojangModificationEnabled(true);
            socket.setEventHandler((sock, event) -> {
                if (event.getType() == SocketEvent.Type.UNCONNECTED_PONG) {
                    SocketEvent.PingPongInfo info = event.getPingPongInfo();
                    SocketAddress remoteAddress = info.getAddress();
                    CompletableFuture<PingData> future = queuedPings.remove(remoteAddress);
                    if (future != null) {
                        String[] split = info.getMotd().split(";");
                        //System.out.println(info.getMotd());
                        if (split.length != 9 || !"MCPE".equals(split[0])) {
                            throw new IllegalStateException("Invalid MOTD: " + info.getMotd() + "(semicolon count:" + (split.length - 1) + ')');
                        }
                        int protocol = Integer.parseInt(split[2]);
                        String version = split[3];
                        int online = Integer.parseInt(split[4]);
                        int max = Integer.parseInt(split[5]);
                        future.complete(new PingData(split[1].replaceAll(String.valueOf('\uFFFD'), String.valueOf('§')), protocol, version, (info.getPongTime() - info.getPingTime()) >> 1L, online, max));
                    }
                }
            });
            socket.initialize();
        } catch (SocketException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Pings a Minecraft: Bedrock edition server
     *
     * @param server the address of the server to ping
     * @return the server's ping data, or null if the connection timed out
     */
    public static PingData ping(@NonNull InetSocketAddress server) {
        try {
            CompletableFuture<PingData> future = queuedPings.computeIfAbsent(server, address -> {
                socket.ping((InetSocketAddress) address);
                return new CompletableFuture<>();
            });
            return future.get(10L, TimeUnit.SECONDS);
        } catch (TimeoutException e) {
            return null;
        } catch (ExecutionException
                | InterruptedException e) {
            throw new RuntimeException(e);
        } finally {
            queuedPings.remove(server);
        }
    }
}
