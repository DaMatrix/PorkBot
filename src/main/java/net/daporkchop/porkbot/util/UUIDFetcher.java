/*
 * Copyright (c) 2015 Nate Mortensen
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package net.daporkchop.porkbot.util;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;
import java.util.UUID;

public class UUIDFetcher {
    private static final double PROFILES_PER_REQUEST = 100;
    private static final String PROFILE_URL = "https://api.mojang.com/profiles/minecraft";
    private static final JsonParser jsonParser = new JsonParser();
    private static ArrayList<UUIDRequest> requests = new ArrayList<>();

    private static void writeBody(HttpURLConnection connection, String body) throws Exception {
        OutputStream stream = connection.getOutputStream();
        stream.write(body.getBytes());
        stream.flush();
        stream.close();
    }

    private static HttpURLConnection createConnection() throws Exception {
        URL url = new URL(PROFILE_URL);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod("POST");
        connection.setRequestProperty("Content-Type", "application/json");
        connection.setUseCaches(false);
        connection.setDoInput(true);
        connection.setDoOutput(true);
        return connection;
    }

    public static UUID getUUID(String id) {
        return UUID.fromString(id.substring(0, 8) + "-" + id.substring(8, 12) + "-" + id.substring(12, 16) + "-" + id.substring(16, 20) + "-" + id.substring(20, 32));
    }

    public static byte[] toBytes(UUID uuid) {
        ByteBuffer byteBuffer = ByteBuffer.wrap(new byte[16]);
        byteBuffer.putLong(uuid.getMostSignificantBits());
        byteBuffer.putLong(uuid.getLeastSignificantBits());
        return byteBuffer.array();
    }

    public static UUID fromBytes(byte[] array) {
        if (array.length != 16) {
            throw new IllegalArgumentException("Illegal byte array length: " + array.length);
        }
        ByteBuffer byteBuffer = ByteBuffer.wrap(array);
        long mostSignificant = byteBuffer.getLong();
        long leastSignificant = byteBuffer.getLong();
        return new UUID(mostSignificant, leastSignificant);
    }

    public static void run() {
        try {
            String body = "[";
            ArrayList<UUIDRequest> temp = new ArrayList<>();
            for (int i = 0; i < requests.size() && i < 100; i++) {
                UUIDRequest request = requests.get(i);
                body += "\"" + request.name + "\",";
                temp.add(request);
            }
            body = body.substring(0, body.length() - 1) + "]";

            String json = HTTPUtils.performPostRequest(new URL(PROFILE_URL), body, "application/json");
            JsonArray array = (JsonArray) jsonParser.parse(json);
            for (Object profile : array) {
                JsonObject jsonProfile = (JsonObject) profile;
                String id = jsonProfile.get("id").getAsString();
                String name = jsonProfile.get("name").getAsString();
                UUIDRequest[] uuidRequest = getRequestByName(name);
                for (UUIDRequest uuidRequest1 : uuidRequest) {
                    uuidRequest1.uuidCompletable.run(id);
                    requests.remove(uuidRequest1);
                    temp.remove(uuidRequest1);
                }
            }
            for (UUIDRequest request : temp) {
                UUIDRequest[] uuidRequest = getRequestByName(request.name);
                for (UUIDRequest uuidRequest1 : uuidRequest) {
                    requests.remove(uuidRequest1);
                    uuidRequest1.uuidCompletable.run("11111111-1111-1111-1111-111111111111");
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void init() {
        new Timer().schedule(new TimerTask() {
            @Override
            public void run() {
                if (requests.isEmpty()) {
                    return;
                }
                UUIDFetcher.run();
                //requests.clear(); //TODO fix this
            }
        }, 5000, 1000);
    }

    public static void enqeueRequest(String name, StringImplicit completableFuture) {
        requests.add(new UUIDRequest(name, completableFuture));
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
        public StringImplicit uuidCompletable;

        public UUIDRequest(String a, StringImplicit b) {
            name = a;
            uuidCompletable = b;
        }
    }
}
