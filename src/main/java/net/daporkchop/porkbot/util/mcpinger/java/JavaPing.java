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

package net.daporkchop.porkbot.util.mcpinger.java;

import com.google.gson.JsonObject;
import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.UnpooledByteBufAllocator;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.group.ChannelGroup;
import io.netty.channel.group.DefaultChannelGroup;
import lombok.experimental.UtilityClass;
import net.daporkchop.lib.network.nettycommon.PorkNettyHelper;
import net.daporkchop.lib.network.nettycommon.eventloopgroup.pool.EventLoopGroupPool;

import java.net.InetSocketAddress;
import java.util.concurrent.CompletableFuture;

/**
 * @author DaPorkchop_
 */
@UtilityClass
public class JavaPing {
    public final RuntimeException NO_RESPONSE = new RuntimeException("no response!");

    private final   EventLoopGroupPool TCP_GROUP_POOL = PorkNettyHelper.getPoolTCP();
    private final   EventLoopGroup     TCP_GROUP      = TCP_GROUP_POOL.get();
    private final Bootstrap          BOOTSTRAP      = new Bootstrap().group(TCP_GROUP)
            .option(ChannelOption.TCP_NODELAY, true)
            .option(ChannelOption.ALLOCATOR, UnpooledByteBufAllocator.DEFAULT)
            .channelFactory(TCP_GROUP_POOL.transport().channelFactorySocketClient());

    final ChannelGroup CHANNELS = new DefaultChannelGroup(TCP_GROUP.next(), true);

    public void boot()  {
    }

    public void shutdown()  {
        CHANNELS.close();
        TCP_GROUP_POOL.release(TCP_GROUP);
    }

    public CompletableFuture<JsonObject> ping(String host, int port, InetSocketAddress address)    {
        CompletableFuture<JsonObject> future = new CompletableFuture<>();
        BOOTSTRAP.clone()
                .handler(new MCChannelInitializer(host, port, future))
                .connect(address);

        return future;
    }
}
