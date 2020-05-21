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

package net.daporkchop.porkbot.util.mcpinger.bedrock;

import com.nukkitx.network.raknet.RakNetClient;
import com.nukkitx.network.raknet.RakNetPong;
import io.netty.channel.EventLoopGroup;
import lombok.NonNull;
import lombok.experimental.UtilityClass;
import net.daporkchop.lib.network.nettycommon.PorkNettyHelper;
import net.daporkchop.lib.network.nettycommon.eventloopgroup.pool.EventLoopGroupPool;

import java.net.InetSocketAddress;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

/**
 * @author DaPorkchop_
 */
@UtilityClass
public class BedrockPing {
    private final EventLoopGroupPool UDP_GROUP_POOL = PorkNettyHelper.getPoolUDP();
    private final EventLoopGroup UDP_GROUP = UDP_GROUP_POOL.get();
    private final RakNetClient RAKNET_CLIENT = new RakNetClient(new InetSocketAddress(0), UDP_GROUP);

    public void boot() {
        RAKNET_CLIENT.bind();
    }

    public void shutdown() {
        RAKNET_CLIENT.close();
        UDP_GROUP_POOL.release(UDP_GROUP);
    }

    public CompletableFuture<RakNetPong> ping(@NonNull InetSocketAddress address) {
        CompletableFuture<RakNetPong> future = new CompletableFuture<>();

        RAKNET_CLIENT.ping(address, 5L, TimeUnit.SECONDS).whenComplete((pong, ex) -> {
            if (ex != null) {
                future.completeExceptionally(ex);
            } else {
                try {
                    future.complete(pong);
                } catch (Exception e) {
                    future.completeExceptionally(e);
                }
            }
        });

        return future;
    }
}
