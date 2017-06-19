package net.daporkchop.porkbot.util.mcpinger;

import ch.jamiete.mcping.MinecraftPing;
import ch.jamiete.mcping.MinecraftPingReply;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import net.daporkchop.porkbot.util.HTTPUtils;

import java.net.InetAddress;
import java.util.GregorianCalendar;

/**
 * A library for interacting with DaPorkchop_'s mcping api
 */
public abstract class MCPing {
    /**
     * all urls start with this
     */
    public static final String BASE_URL = "http://repo.daporkchop.tk/PorkBot-MCQuery/";

    /**
     * Pings a PE server and returns a ping
     *
     * @param ip   the server's ip
     * @param port the server's port
     * @return a filled PePing
     */
    public static PePing pingPe(String ip, int port) {
        try {
            String s = HTTPUtils.performGetRequest(HTTPUtils.constantURL(BASE_URL + "pepinger.php?ip=" + ip + "&port=" + port));

            JsonObject json = new JsonParser().parse(s).getAsJsonObject();

            if (json.get("status").getAsBoolean()) {
                if (json.get("old").getAsBoolean()) {
                    return new PePing(true, true, null, null, null, null, 0, false, null);
                } else {
                    return new PePing(true, false, json.get("motd").getAsString(), json.get("players").getAsString(), json.get("ping").getAsString(), json.get("version").getAsString(), json.get("protocol").getAsInt(), false, null);
                }
            } else {
                return new PePing(false, false, null, null, null, null, 0, false, null);
            }
        } catch (Exception e) {
            return new PePing(false, false, null, null, null, null, 0, true, e);
        }
    }

    /**
     * Queries a Minecraft server and pings it, returning info
     *
     * @param ip   the server's ip
     * @param port the server's port
     * @param pe   whether or not the server is a Pocket Edition server
     * @return a filled Query
     */
    public static Query query(String ip, int port, boolean pe) {
        try {
            String s = HTTPUtils.performGetRequest(HTTPUtils.constantURL(BASE_URL + "query.php?ip=" + ip + "&port=" + port + (pe ? "&pe=true" : "")));

            JsonObject json = new JsonParser().parse(s).getAsJsonObject();

            if (!pe) {
                s = HTTPUtils.performGetRequest(HTTPUtils.constantURL("https://mcapi.ca/query/" + ip + ":" + port + "/motd"));
            }

            if (json.get("status").getAsBoolean()) {
                if (json.get("noquery").getAsBoolean()) {
                    return new Query(true, true, null, null, null, null, 0, null, null, false, null);
                } else {
                    return new Query(true, false, (pe ? json.get("motd").getAsString() : new JsonParser().parse(s).getAsJsonObject().get("motd").getAsString()), json.get("players").getAsString(), json.get("ping").getAsString(), json.get("version").getAsString(), json.get("protocol").getAsInt(), json.get("playersample").getAsString(), json.get("plugins").getAsString(), false, null);
                }
            } else {
                return new Query(false, false, null, null, null, null, 0, null, null, false, null);
            }
        } catch (Exception e) {
            return new Query(false, false, null, null, null, null, 0, null, null, true, e);
        }
    }

    /**
     * Ping a Minecraft PC server, returning info
     *
     * @param ip   the server's ip
     * @param port the server's port
     * @return a filled McPing
     */
    public static McPing pingPc(String ip, int port) {
        try {
            MinecraftPingReply reply = MinecraftPing.getPing(ip, port);

            if (reply != null) {
                return new McPing(true, reply.getDescription().getText(), reply.getPlayers().getOnline() + "/" + reply.getPlayers().getMax(), reply.getVersion().getProtocol(), reply.getVersion().getName(), getPingToIP(ip), false, null);
            } else {
                return new McPing(false, null, null, 0, null, null, false, null);
            }
        } catch (Exception e) {
            return new McPing(false, null, null, 0, null, null, true, e);
        }
    }

    /**
     * pings an ip address
     *
     * @param ipToPingNowPleaseGiveThisWhyIsThisFieldDescriptorSoLongLolEksDee idk lol
     * @return the ping
     */
    public static String getPingToIP(String ipToPingNowPleaseGiveThisWhyIsThisFieldDescriptorSoLongLolEksDee) {
        try {
            InetAddress inet = InetAddress.getByName(ipToPingNowPleaseGiveThisWhyIsThisFieldDescriptorSoLongLolEksDee);

            System.out.println("Sending Ping Request to " + ipToPingNowPleaseGiveThisWhyIsThisFieldDescriptorSoLongLolEksDee);

            long finish = 0;
            long start = new GregorianCalendar().getTimeInMillis();

            if (inet.isReachable(5000)) {
                finish = new GregorianCalendar().getTimeInMillis();
                return finish - start + " ms";
            } else {
                return "-1 ms";
            }
        } catch (Exception e) {
            System.out.println("Exception:" + e.getMessage());
        }

        return "-1 ms";
    }

    /**
     * Represents a ping. All pings extend this.
     */
    protected static abstract class Ping {
        public boolean status;
        public boolean errored;
        public Exception error;

        public Ping(boolean isOnline, boolean errored, Exception error) {
            this.status = isOnline;
            this.errored = errored;
            this.error = error;
        }
    }

    /**
     * A Minecraft: Pocket Edition ping.
     */
    public static class PePing extends Ping {
        public boolean old;
        public String motd;
        public String players;
        public String ping;
        public String version;
        public int protocol;

        public PePing(boolean isOnline, boolean old, String motd, String players, String ping, String version, int protocol, boolean errored, Exception error) {
            super(isOnline, errored, error);
            this.old = old;
            this.players = players;
            this.ping = ping;
            this.version = version;
            this.protocol = protocol;
            this.motd = motd;
        }
    }

    /**
     * A generic query, used by both PE and PC
     */
    public static class Query extends Ping {
        public boolean noQuery;
        public String motd;
        public String players;
        public String ping;
        public String version;
        public int protocol;
        public String playerSample;
        public String plugins;

        public Query(boolean isOnline, boolean noQuery, String motd, String players, String ping, String version, int protocol, String playerSample, String plugins, boolean errored, Exception error) {
            super(isOnline, errored, error);
            this.noQuery = noQuery;
            this.players = players;
            this.ping = ping;
            this.version = version;
            this.protocol = protocol;
            this.playerSample = playerSample;
            this.plugins = plugins;
            this.motd = motd;
        }
    }

    /**
     * Represents a MCPC ping
     */
    public static class McPing extends Ping {
        public boolean old;
        public String motd;
        public String players;
        public int protocol;
        public String version;
        public String ping;

        public McPing(boolean isOnline, String motd, String players, int protocol, String version, String ping, boolean errored, Exception error) {
            super(isOnline, errored, error);
            this.motd = motd;
            this.old = old;
            this.players = players;
            this.protocol = protocol;
            this.version = version;
            this.ping = ping;
        }
    }
}
