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
package net.daporkchop.porkbot.util;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.gson.JsonElement;
import lombok.NonNull;
import net.daporkchop.lib.common.misc.string.PUnsafeStrings;
import net.daporkchop.lib.common.pool.handle.Handle;
import net.daporkchop.lib.common.util.PorkUtil;
import net.daporkchop.lib.http.Http;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

public final class UUIDFetcher extends Thread {
    private static final int PROFILES_PER_REQUEST = 10;
    private static final String PROFILE_URL = "https://api.mojang.com/profiles/minecraft";
    private static final String EMPTY_UUID = "8667ba71b85a4004af54457a9734eed7";

    private static final Cache<String, String> CACHE = CacheBuilder.newBuilder()
            .maximumSize(5000L)
            .expireAfterWrite(1L, TimeUnit.DAYS)
            .build();

    private static final Map<String, Collection<Consumer<String>>> PENDING = new HashMap<>();

    public static UUID getUUID(@NonNull String id) {
        if (id.length() == 32) {
            char[] old = PUnsafeStrings.unwrap(id);
            try (Handle<StringBuilder> handle = PorkUtil.STRINGBUILDER_POOL.get()) {
                StringBuilder builder = handle.value();
                builder.setLength(0);

                builder.append(old, 0, 8).append('-')
                        .append(old, 8, 4).append('-')
                        .append(old, 12, 4).append('-')
                        .append(old, 16, 4).append('-')
                        .append(old, 20, 12);

                return UUID.fromString(builder.toString());
            }
        } else if (id.length() == 32 + 4) {
            return UUID.fromString(id);
        } else {
            throw new IllegalArgumentException(id);
        }
    }

    public static void enqueueRequest(String name, Consumer<String> callback) {
        String uuid;
        synchronized (PENDING) {
            uuid = CACHE.getIfPresent(name);
            if (uuid == null) {
                PENDING.computeIfAbsent(name, s -> new LinkedList<>()).add(callback);
                return;
            }
        }
        callback.accept(uuid == EMPTY_UUID ? null : uuid);
    }

    public static void init() {
        new UUIDFetcher();
    }

    private UUIDFetcher() {
        this.setDaemon(true);
        this.start();
    }

    @Override
    public void run() {
        List<String> nameBuf = new ArrayList<>(PROFILES_PER_REQUEST);
        while (true) {
            try {
                Thread.sleep(1250L);
                synchronized (PENDING) {
                    if (PENDING.isEmpty()) {
                        continue;
                    }
                    for (Iterator<String> i = PENDING.keySet().iterator(); i.hasNext() && nameBuf.size() < PROFILES_PER_REQUEST; ) {
                        nameBuf.add(i.next());
                    }
                    StreamSupport.stream(Constants.JSON_PARSER.parse(Http.postJsonString(
                            PROFILE_URL,
                            nameBuf.stream().collect(Collectors.joining("\",\"", "[\"", "\"]"))
                    )).getAsJsonArray().spliterator(), false)
                            .map(JsonElement::getAsJsonObject)
                            .forEach(obj -> {
                                String name = obj.get("name").getAsString();
                                String uuid = obj.get("id").getAsString();
                                CACHE.put(name, uuid);
                                Collection<Consumer<String>> callbacks = PENDING.remove(name);
                                if (callbacks == null) {
                                    System.out.printf("Warning: Duplicate callback for name: \"%s\"\n", name);
                                } else {
                                    for (Consumer<String> callback : callbacks) {
                                        try {
                                            callback.accept(uuid);
                                        } catch (Exception e) {
                                            e.printStackTrace();
                                        }
                                    }
                                }
                            });
                }
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                for (String name : nameBuf) {
                    if (PENDING.containsKey(name)) {
                        CACHE.put(name, EMPTY_UUID); //fallback to steve
                        for (Consumer<String> callback : PENDING.remove(name)) {
                            callback.accept(null); //fallback to steve
                        }
                    }
                }
                nameBuf.clear();
            }
        }
    }
}
