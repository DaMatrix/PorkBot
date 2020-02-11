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
package net.daporkchop.porkbot.util;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;

import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.StringJoiner;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;
import java.util.stream.Collector;
import java.util.stream.StreamSupport;

public class UUIDFetcher extends Thread {
    private static final int                                       PROFILES_PER_REQUEST = 10;
    private static final URL                                       PROFILE_URL          = HTTPUtils.constantURL("https://api.mojang.com/profiles/minecraft");
    private static final JsonParser                                jsonParser           = new JsonParser();
    private static final Cache<String, String>                     CACHE                = CacheBuilder.newBuilder()
            .maximumSize(5000L)
            .expireAfterAccess(1L, TimeUnit.DAYS)
            .build();
    private static final Map<String, Collection<Consumer<String>>> PENDING              = new HashMap<>();
    private static final String                                    EMPTY_UUID           = "8667ba71b85a4004af54457a9734eed7";

    public static UUID getUUID(String id) {
        return UUID.fromString(id.substring(0, 8) + "-" + id.substring(8, 12) + "-" + id.substring(12, 16) + "-" + id.substring(16, 20) + "-" + id.substring(20, 32));
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
                    StreamSupport.stream(jsonParser.parse(HTTPUtils.performPostRequest(
                            PROFILE_URL,
                            nameBuf.stream().collect(Collector.of(() -> new StringJoiner("\",\"", "[\"", "\"]"), StringJoiner::add, StringJoiner::merge, StringJoiner::toString)),
                            "application/json"
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
