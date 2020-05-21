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
import com.google.gson.JsonParser;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import net.daporkchop.porkbot.PorkBot;

import java.nio.charset.StandardCharsets;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

/**
 * @author DaPorkchop_
 */
@RequiredArgsConstructor
public class MCChannelHandler extends ChannelInboundHandlerAdapter {
    @NonNull
    protected final String host;
    protected final int port;
    @NonNull
    protected final CompletableFuture<JsonObject> future;

    protected JsonObject obj;

    @Override
    public void channelRegistered(ChannelHandlerContext ctx) throws Exception {
        //close channel after 5 seconds
        PorkBot.SCHEDULED_EXECUTOR.schedule((Runnable) ctx.channel()::close, 5L, TimeUnit.SECONDS);

        super.channelRegistered(ctx);
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        ByteBuf buf = ctx.channel().alloc().ioBuffer();

        //handshake packet
        byte[] hostUTF = this.host.getBytes(StandardCharsets.UTF_8);
        int handshakeLength = 1 + MCPacketFramer.varIntLength(340) + MCPacketFramer.varIntLength(hostUTF.length) + hostUTF.length + 2 + 1;
        MCPacketFramer.writeVarInt(buf, handshakeLength);
        buf.writeByte(0); //packet id
        MCPacketFramer.writeVarInt(buf, 340); //protocol version
        MCPacketFramer.writeVarInt(buf, hostUTF.length);
        buf.writeBytes(hostUTF); //host
        buf.writeShort(this.port); //port
        buf.writeByte(1); //next state: status

        //request packet
        buf.writeByte(1);
        buf.writeByte(0); //packet id

        ctx.channel().writeAndFlush(buf, ctx.voidPromise());

        super.channelActive(ctx);
    }

    @Override
    public void channelUnregistered(ChannelHandlerContext ctx) throws Exception {
        this.future.completeExceptionally(JavaPing.NO_RESPONSE); //this will do nothing if it completed successfully

        super.channelUnregistered(ctx);
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        this.future.completeExceptionally(cause);
        ctx.channel().close();
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object in) throws Exception {
        try {
            ByteBuf msg = (ByteBuf) in;
            try {
                switch (MCPacketFramer.readVarInt(msg)) {
                    case 0: { //response
                        String json = msg.readCharSequence(MCPacketFramer.readVarInt(msg), StandardCharsets.UTF_8).toString();
                        this.obj = new JsonParser().parse(json).getAsJsonObject();

                        ctx.channel().writeAndFlush(ctx.alloc().ioBuffer() //ping
                                .writeByte(9) //length
                                .writeByte(1) //id
                                .writeLong(System.currentTimeMillis())); //time
                    }
                    break;
                    case 1: { //pong
                        this.obj.addProperty("ping", (System.currentTimeMillis() - msg.readLong()) >> 1L);
                        ctx.channel().close();
                        this.future.complete(this.obj);
                    }
                    break;
                    default:
                        throw new IllegalStateException("Invalid packet ID!");
                }
            } finally {
                msg.release();
            }
        } catch (Throwable t) {
            ctx.channel().close();
            this.future.completeExceptionally(t);
        }
    }
}
