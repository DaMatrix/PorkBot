/*
 * Adapted from the Wizardry License
 *
 * Copyright (c) 2016-2018 DaPorkchop_
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

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import net.daporkchop.porkbot.PorkBot;

import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Queue;
import java.util.TimerTask;
import java.util.UUID;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.function.Consumer;

public class UUIDFetcher {
    private static final double PROFILES_PER_REQUEST = 100;
    private static final String PROFILE_URL = "https://api.mojang.com/profiles/minecraft";
    private static final JsonParser jsonParser = new JsonParser();
    private static final Gson gson = new Gson();
    private static Queue<UUIDRequest> requests = new ConcurrentLinkedQueue<>();

    public static UUID getUUID(String id) {
        return UUID.fromString(id.substring(0, 8) + "-" + id.substring(8, 12) + "-" + id.substring(12, 16) + "-" + id.substring(16, 20) + "-" + id.substring(20, 32));
    }

    public static void run() {
        try {
            ArrayList<UUIDRequest> temp = new ArrayList<>();
            ArrayList<String> jsonArray = new ArrayList<>();
            {
                UUIDRequest request;
                int i = 0;
                while (i++ < PROFILES_PER_REQUEST && (request = requests.poll()) != null)  {
                    jsonArray.add(request.name);
                    temp.add(request);
                }
            }

            String json = HTTPUtils.performPostRequest(new URL(PROFILE_URL), gson.toJson(jsonArray), "application/json");
            JsonArray array = jsonParser.parse(json).getAsJsonArray();
            for (Object profile : array) {
                JsonObject jsonProfile = (JsonObject) profile;
                String id = jsonProfile.get("id").getAsString();
                String name = jsonProfile.get("name").getAsString();
                UUIDRequest[] uuidRequest = getRequestByName(name);
                for (UUIDRequest uuidRequest1 : uuidRequest) {
                    uuidRequest1.uuidCompletable.accept(id);
                    temp.remove(uuidRequest1);
                }
            }
            for (UUIDRequest request : temp) {
                UUIDRequest[] uuidRequest = getRequestByName(request.name);
                for (UUIDRequest uuidRequest1 : uuidRequest) {
                    uuidRequest1.uuidCompletable.accept("11111111-1111-1111-1111-111111111111");
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void init() {
        PorkBot.timer.schedule(new TimerTask() {
            @Override
            public void run() {
                if (requests.isEmpty()) {
                    return;
                }
                UUIDFetcher.run();
            }
        }, 5000, 1200);
    }

    public static void enqueueRequest(String name, Consumer<String> callback) {
        requests.add(new UUIDRequest(name, callback));
    }

    private static UUIDRequest[] getRequestByName(String name) {
        ArrayList<UUIDRequest> arrayList = new ArrayList<>();
        for (UUIDRequest request : requests) {
            if (request.name.equals(name)) {
                arrayList.add(request);
            }
        }

        return arrayList.toArray(new UUIDRequest[arrayList.size()]);
    }

    public static class UUIDRequest {
        public String name;
        public Consumer<String> uuidCompletable;

        public UUIDRequest(String a, Consumer<String> b) {
            name = a;
            uuidCompletable = b;
        }
    }
}
