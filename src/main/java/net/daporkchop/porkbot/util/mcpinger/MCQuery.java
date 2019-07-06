/*
 * Adapted from the Wizardry License
 *
 * Copyright (c) 2016-2019 DaPorkchop_
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

package net.daporkchop.porkbot.util.mcpinger;

import org.xbill.DNS.Lookup;
import org.xbill.DNS.Record;
import org.xbill.DNS.SRVRecord;
import org.xbill.DNS.TextParseException;
import org.xbill.DNS.Type;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketException;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Send a query to a given minecraft server and store any metadata and the
 * player list.
 *
 * @author Ryan Shaw, Jonas Konrad
 */
public class MCQuery {
    private static final DatagramSocket SOCKET;

    static {
        DatagramSocket s = null;
        try {
            s = new DatagramSocket();
            s.setSoTimeout(5000);
        } catch (SocketException e) {
            throw new RuntimeException(e);
        } finally {
            SOCKET = s;
        }
        Runtime.getRuntime().addShutdownHook(new Thread(SOCKET::close));
    }

    private static void sendPacket(InetSocketAddress targetAddress, byte[] data) throws IOException {
        SOCKET.send(new DatagramPacket(data, data.length, targetAddress.getAddress(), targetAddress.getPort()));
    }

    /*private static void sendPacket(InetSocketAddress targetAddress, int... data) throws IOException {
        final byte[] d = new byte[data.length];
        int i = 0;
        for (int j : data) {
            d[i++] = (byte) (j & 0xff);
        }
        sendPacket(targetAddress, d);
    }*/

    private static DatagramPacket receivePacket(byte[] buffer) throws IOException {
        DatagramPacket dp = new DatagramPacket(buffer, buffer.length);
        SOCKET.receive(dp);
        return dp;
    }

    private static String readString(byte[] array, AtomicInteger cursor) {
        final int startPosition = cursor.incrementAndGet();
        for (; cursor.get() < array.length && array[cursor.get()] != 0; cursor.incrementAndGet()) {
            ;
        }
        return new String(array, startPosition, cursor.get() - startPosition, StandardCharsets.UTF_8);
    }

    /**
     * The target address and port
     */
    public InetSocketAddress   address;
    /**
     * <code>null</code> if no successful request has been sent, otherwise a Map
     * containing any metadata received except the player list
     */
    public Map<String, String> values;
    /**
     * <code>null</code> if no successful request has been sent, otherwise an
     * array containing all online player usernames
     */
    public String[]            onlineUsernames;

    public MCQuery(InetSocketAddress address) throws IOException {
        this.address = address;

        this.sendQueryRequest();
    }

    /**
     * Get the additional values if the Query has been sent
     *
     * @return The data
     * @throws IllegalStateException if the query has not been sent yet or there has been an error
     */
    public Map<String, String> getValues() {
        if (values == null) {
            throw new IllegalStateException("Query has not been sent yet!");
        } else {
            return values;
        }
    }

    /**
     * Get the online usernames if the Query has been sent
     *
     * @return The username array
     * @throws IllegalStateException if the query has not been sent yet or there has been an error
     */
    public String[] getOnlineUsernames() {
        if (onlineUsernames == null) {
            throw new IllegalStateException("Query has not been sent yet!");
        } else {
            return onlineUsernames;
        }
    }

    /**
     * Request the UDP query
     *
     * @throws IOException if anything goes wrong during the request
     */
    private void sendQueryRequest() throws IOException {
        final byte[] receiveData = new byte[10240];
        sendPacket(this.address, new byte[]{
                (byte) 0xFE,
                (byte) 0xFD,
                (byte) 0x09,
                (byte) 0x01,
                (byte) 0x01,
                (byte) 0x01,
                (byte) 0x01
        });
        final int challengeInteger;
        {
            receivePacket(receiveData);
            byte byte1 = -1;
            int i = 0;
            byte[] buffer = new byte[11];
            for (int count = 5; (byte1 = receiveData[count++]) != 0; ) {
                buffer[i++] = byte1;
            }
            challengeInteger = Integer.parseInt(new String(buffer, StandardCharsets.UTF_8).trim());
        }
        sendPacket(this.address, new byte[] {
                (byte) 0xFE,
                (byte) 0xFD,
                (byte) 0x00,
                (byte) 0x01,
                (byte) 0x01,
                (byte) 0x01,
                (byte) 0x01,
                (byte) ((challengeInteger >> 24) & 0xFF),
                (byte) ((challengeInteger >> 16) & 0xFF),
                (byte) ((challengeInteger >> 8) & 0xFF),
                (byte) (challengeInteger & 0xFF),
                (byte) 0x00,
                (byte) 0x00,
                (byte) 0x00,
                (byte) 0x00
        });

        final int length = receivePacket(receiveData).getLength();
        values = new HashMap<>();
        final AtomicInteger cursor = new AtomicInteger(5);
        while (cursor.get() < length) {
            String s = readString(receiveData, cursor);
            if (s.length() == 0) {
                break;
            } else {
                values.put(s, readString(receiveData, cursor).replace('\ufffd', '\u00A7')); //i don't know why color codes get broken by query, but so it is
            }
        }
        readString(receiveData, cursor);
        final Set<String> players = new HashSet<>();
        while (cursor.get() < length) {
            final String name = readString(receiveData, cursor);
            if (name.length() > 0) {
                players.add(name);
            }
        }
        onlineUsernames = players.toArray(new String[players.size()]);
    }

    public String getMOTD() {
        return values.getOrDefault("hostname", "A Minecraft server");
    }

    public int getOnlinePlayers() {
        return Integer.parseInt(values.getOrDefault("numplayers", "0"));
    }

    public int getMaxPlayers() {
        return Integer.parseInt(values.getOrDefault("maxplayers", "20"));
    }

    public String getPlugins() {
        return values.getOrDefault("plugins", "");
    }

    public String getMapName() {
        return values.getOrDefault("map", "New World");
    }

    public String getGameMode() {
        return values.getOrDefault("gametype", "SMP");
    }
}
