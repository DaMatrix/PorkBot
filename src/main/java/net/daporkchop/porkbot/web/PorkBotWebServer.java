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
        } else if ((query.fragment() != null && !query.fragment().isEmpty()) || !query.params().isEmpty()) {
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
