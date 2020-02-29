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

package net.daporkchop.porkbot.web;

import lombok.NonNull;
import net.daporkchop.lib.http.HttpMethod;
import net.daporkchop.lib.http.impl.netty.server.NettyHttpServer;
import net.daporkchop.lib.http.message.Message;
import net.daporkchop.lib.http.request.query.Query;
import net.daporkchop.lib.http.server.HttpServer;
import net.daporkchop.lib.http.server.ResponseBuilder;
import net.daporkchop.lib.http.server.handle.ServerHandler;
import net.daporkchop.lib.http.util.StatusCodes;
import net.daporkchop.lib.logging.Logging;
import net.daporkchop.lib.network.nettycommon.PorkNettyHelper;

import java.net.InetSocketAddress;
import java.util.HashMap;
import java.util.Map;

/**
 * @author DaPorkchop_
 */
public final class PorkBotWebServer implements ServerHandler {
    public static final int PORT = 8081;

    private final HttpServer server;

    private final Map<String, ApiMethod> methods = new HashMap<>();

    public PorkBotWebServer() {
        (this.server = new NettyHttpServer(PorkNettyHelper.getPoolTCP(), Logging.logger))
                .handler(this)
                .bind(new InetSocketAddress(PORT)).syncUninterruptibly();
    }

    public void shutdown()  {
        this.server.close();
    }

    public void register(@NonNull String base, @NonNull ApiMethod method) {
        if (this.methods.putIfAbsent(base, method) != null) {
            throw new IllegalStateException("URL base already registered: " + base);
        }
    }

    @Override
    public int maxBodySize() {
        return 0;
    }

    @Override
    public void handleQuery(@NonNull Query query) throws Exception {
        if (query.method() != HttpMethod.GET) {
            throw StatusCodes.METHOD_NOT_ALLOWED.exception();
        } else if ((query.fragment() != null && !query.fragment().isEmpty())) {
            throw StatusCodes.BAD_REQUEST.exception();
        }
    }

    @Override
    public void handle(@NonNull Query query, @NonNull Message message, @NonNull ResponseBuilder response) throws Exception {
        String path = query.path();
        for (Map.Entry<String, ApiMethod> entry : this.methods.entrySet()) {
            if (path.startsWith(entry.getKey())) {
                entry.getValue().handle(path, response);
                return;
            }
        }

        throw StatusCodes.NOT_FOUND.exception();
    }
}
