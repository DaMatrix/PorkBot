package net.daporkchop.porkbot;

import com.google.api.client.http.HttpRequest;
import com.google.api.client.http.HttpRequestInitializer;
import com.google.api.services.youtube.YouTube;
import com.google.api.services.youtube.model.SearchListResponse;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.sedmelluq.discord.lavaplayer.player.AudioLoadResultHandler;
import com.sedmelluq.discord.lavaplayer.player.AudioPlayerManager;
import com.sedmelluq.discord.lavaplayer.player.DefaultAudioPlayerManager;
import com.sedmelluq.discord.lavaplayer.source.AudioSourceManagers;
import com.sedmelluq.discord.lavaplayer.tools.FriendlyException;
import com.sedmelluq.discord.lavaplayer.track.AudioPlaylist;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import net.daporkchop.porkbot.command.Command;
import net.daporkchop.porkbot.command.CommandHelp;
import net.daporkchop.porkbot.command.CommandInvite;
import net.daporkchop.porkbot.command.CommandRegistry;
import net.daporkchop.porkbot.command.base.*;
import net.daporkchop.porkbot.command.base.minecraft.*;
import net.daporkchop.porkbot.command.base.misc.CommandDice;
import net.daporkchop.porkbot.command.base.misc.CommandEmojiID;
import net.daporkchop.porkbot.command.base.misc.CommandInterject;
import net.daporkchop.porkbot.command.base.misc.CommandThonk;
import net.daporkchop.porkbot.command.base.music.*;
import net.daporkchop.porkbot.music.GuildAudioInfo;
import net.daporkchop.porkbot.music.GuildAudioManager;
import net.daporkchop.porkbot.util.Auth;
import net.daporkchop.porkbot.util.HTTPUtils;
import net.daporkchop.porkbot.util.UUIDFetcher;
import net.dv8tion.jda.core.*;
import net.dv8tion.jda.core.entities.*;
import net.dv8tion.jda.core.events.message.MessageReceivedEvent;
import net.dv8tion.jda.core.exceptions.PermissionException;
import net.dv8tion.jda.core.exceptions.RateLimitedException;
import net.dv8tion.jda.core.managers.AudioManager;

import javax.security.auth.login.LoginException;
import java.io.*;
import java.net.URL;
import java.util.*;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;

public class PorkBot {

    public static PorkBot INSTANCE;
    public static Logger logger;
    /**
     * A central Random instance, for random things
     * literally
     * kek
     * eks dee
     */
    public static Random random = new Random(System.currentTimeMillis());
    public JDA jda;

    public YouTube youTube;
    public String devKey;

    public LoadingCache<String, String> videoNameCache = CacheBuilder.newBuilder()
            .maximumSize(5000)
            .expireAfterWrite(1, TimeUnit.DAYS)
            .build(new CacheLoader<String, String>() {
                        public String load(String key) {
                            return searchYoutube(key);
                        }
                    });

    public AudioPlayerManager playerManager;
    public Map<Long, GuildAudioInfo> musicManagers;

    public PorkBot() {
        logger.info("Starting PorkBot...");
        try {
            jda = new JDABuilder(AccountType.BOT)
                    .setToken(getToken())
                    .addEventListener(new PorkListener())
                    .buildBlocking();
        } catch (LoginException e) {
            e.printStackTrace();
            System.exit(0);
        } catch (InterruptedException e) {
            e.printStackTrace();
            System.exit(0);
        } catch (RateLimitedException e) {
            e.printStackTrace();
            System.exit(0);
        }
    }

    public static void main(String[] args) {
        logger = Logger.getLogger("PorkBot");
        INSTANCE = new PorkBot();
        INSTANCE.start();
    }

    public static String getToken() {
        File f = new File(System.getProperty("user.dir") + "/discordtoken.txt");
        String token = "";

        if (!f.exists()) {
            try {
                PrintWriter writer = new PrintWriter(f.getAbsolutePath(), "UTF-8");
                Scanner s = new Scanner(System.in);

                logger.info("Please enter your discord bot token");
                token = s.nextLine();
                writer.println(token);
                logger.info("Successful. Starting...");

                s.close();
                writer.close();
            } catch (FileNotFoundException e) {
                logger.severe("impossible error kek");
                e.printStackTrace();
                System.exit(0);
            } catch (UnsupportedEncodingException e) {
                logger.severe("File encoding not supported!");
                e.printStackTrace();
                System.exit(0);
            }
        } else {
            try {
                Scanner s = new Scanner(f);

                token = s.nextLine();

                s.close();
            } catch (FileNotFoundException e) {
                logger.severe("impossible error kek");
                e.printStackTrace();
                System.exit(0);
            }
        }

        return token.trim();
    }

    /**
     * Sends a message to a channel
     *
     * @param s
     * @param channel
     */
    public static void sendMessage(String s, TextChannel channel) {
        try {
            Thread.sleep(500);
            channel.sendMessage(s).queue();
        } catch (PermissionException | InterruptedException e) {
            //we can't do anything about it
        }
    }

    /**
     * Sends a message to a channel
     *
     * @param builder
     * @param channel
     */
    public static void sendMessage(EmbedBuilder builder, TextChannel channel) {
        try {
            Thread.sleep(500);
            channel.sendMessage(builder.build()).queue();
        } catch (PermissionException | InterruptedException e) {
            //we can't do anything about it
        }
    }

    /**
     * Sends an image with the embed
     *
     * @param builder
     * @param image
     * @param name
     * @param channel
     */
    public static void sendImage(EmbedBuilder builder, byte[] image, String name, TextChannel channel) {
        try {
            channel.sendFile(image, name, new MessageBuilder().setEmbed(builder.build()).build()).queue();
        } catch (PermissionException e) {
            //we can't do anything about it
        }
    }

    /**
     * Sends an excepion using the given MessageReceivedEvent's channel
     *
     * @param e   the exception to print
     * @param evt the channel from this event is used to send the message
     */
    public static void sendException(Exception e, MessageReceivedEvent evt) {
        e.printStackTrace();
        PorkBot.sendMessage("Error running command: `" + evt.getMessage().getRawContent() + "`:\n`" + e.getClass().getCanonicalName() + "`", evt.getTextChannel());
    }

    /**
     * Downloads an image
     *
     * @param address
     * @return
     */
    public static byte[] downloadImage(String address) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        InputStream is = null;
        URL url = null;
        try {
            url = new URL(address);
            is = url.openStream();
            byte[] byteChunk = new byte[4096]; // Or whatever size you want to read in at a time.
            int n;

            while ((n = is.read(byteChunk)) > 0) {
                baos.write(byteChunk, 0, n);
            }
        } catch (IOException e) {
            System.err.printf("Failed while reading bytes from %s: %s", url.toExternalForm(), e.getMessage());
            e.printStackTrace();
            // Perform any other exception handling that's appropriate.
        } finally {
            try {
                if (is != null) {
                    is.close();
                }
            } catch (IOException e) {

            }
        }

        return baos.toByteArray();
    }

    public static String getAuthtoken() {
        File f = new File(System.getProperty("user.dir") + "/authtoken.txt");
        String token = "";

        if (!f.exists()) {
            try {
                PrintWriter writer = new PrintWriter(f.getAbsolutePath(), "UTF-8");
                Scanner s = new Scanner(System.in);

                logger.info("Please enter your auth token for bots.discord.pw");
                token = s.nextLine();
                writer.println(token);
                logger.info("Successful. connecting...");

                s.close();
                writer.close();
            } catch (FileNotFoundException e) {
                logger.severe("impossible error kek");
                e.printStackTrace();
                System.exit(0);
            } catch (UnsupportedEncodingException e) {
                logger.severe("File encoding not supported!");
                e.printStackTrace();
                System.exit(0);
            }
        } else {
            try {
                Scanner s = new Scanner(f);

                token = s.nextLine();

                s.close();
            } catch (FileNotFoundException e) {
                logger.severe("impossible error kek");
                e.printStackTrace();
                System.exit(0);
            }
        }

        return token.trim();
    }

    public static String getDevKey() {
        File f = new File(System.getProperty("user.dir") + "/devkey.txt");
        String token = "";

        if (!f.exists()) {
            try {
                PrintWriter writer = new PrintWriter(f.getAbsolutePath(), "UTF-8");
                Scanner s = new Scanner(System.in);

                logger.info("Please enter your google API key");
                token = s.nextLine();
                writer.println(token);
                logger.info("Successful. connecting...");

                s.close();
                writer.close();
            } catch (FileNotFoundException e) {
                logger.severe("impossible error kek");
                e.printStackTrace();
                System.exit(0);
            } catch (UnsupportedEncodingException e) {
                logger.severe("File encoding not supported!");
                e.printStackTrace();
                System.exit(0);
            }
        } else {
            try {
                Scanner s = new Scanner(f);

                token = s.nextLine();

                s.close();
            } catch (FileNotFoundException e) {
                logger.severe("impossible error kek");
                e.printStackTrace();
                System.exit(0);
            }
        }

        return token.trim();
    }

    public static VoiceChannel connectToFirstVoiceChannel(AudioManager audioManager, Member user, TextChannel channel) {
        VoiceChannel toReturn = null;
        if (!audioManager.isConnected() && !audioManager.isAttemptingToConnect()) {
            if (user.getVoiceState().inVoiceChannel())  {
                audioManager.openAudioConnection(toReturn = user.getVoiceState().getChannel());
            } else {
                sendMessage("You're not in a voice channel! REEEEEEEE", channel);
            }
        } else {
            sendMessage("PorkBot is already connected to a voice channel in this server!!! REEEEEEEEEEEE", channel);
        }

        return toReturn;
    }

    public synchronized GuildAudioInfo getGuildAudioPlayer(Guild guild, boolean createIfNotExists) {
        if (createIfNotExists) {
            long guildId = Long.parseLong(guild.getId());
            GuildAudioInfo musicManager = musicManagers.get(guildId);

            if (musicManager == null) {
                musicManager = new GuildAudioInfo(new GuildAudioManager(playerManager));
                musicManagers.put(guildId, musicManager);
            }

            guild.getAudioManager().setSendingHandler(musicManager.manager.getSendHandler());

            return musicManager;
        } else {
            long guildId = Long.parseLong(guild.getId());
            GuildAudioInfo musicManager = musicManagers.get(guildId);

            return musicManager;
        }
    }

    public void loadAndPlay(final TextChannel channel, final String trackUrl, final Member user) {

        GuildAudioInfo musicManager = getGuildAudioPlayer(channel.getGuild(), true);

        playerManager.loadItemOrdered(musicManager, trackUrl, new AudioLoadResultHandler() {
            @Override
            public void trackLoaded(AudioTrack track) {
                channel.sendMessage("Adding to queue " + track.getInfo().title).queue();

                play(channel.getGuild(), musicManager, track, user, channel);
            }

            @Override
            public void playlistLoaded(AudioPlaylist playlist) {
                AudioTrack firstTrack = playlist.getSelectedTrack();

                if (firstTrack == null) {
                    firstTrack = playlist.getTracks().get(0);
                }

                channel.sendMessage("Adding to queue " + firstTrack.getInfo().title + " (first track of playlist " + playlist.getName() + ")").queue();

                play(channel.getGuild(), musicManager, firstTrack, user, channel);
            }

            @Override
            public void noMatches() {
                channel.sendMessage("Nothing found by " + trackUrl).queue();
            }

            @Override
            public void loadFailed(FriendlyException exception) {
                channel.sendMessage("Could not play: " + exception.getMessage()).queue();
            }
        });
    }

    public void play(Guild guild, GuildAudioInfo musicManager, AudioTrack track, Member user, TextChannel channel) {
        if (!guild.getAudioManager().isConnected()) {
            musicManager.channel = connectToFirstVoiceChannel(guild.getAudioManager(), user, channel);
        }
        musicManager.textChannel = channel;

        musicManager.manager.scheduler.queue(track);
    }

    public void skipTrack(TextChannel channel) {
        GuildAudioInfo musicManager = getGuildAudioPlayer(channel.getGuild(), false);
        if (musicManager == null)   {
            channel.sendMessage("Not playing!").queue();
        } else {
            if (musicManager.manager.scheduler.queue.size() == 0)   {
                musicManager.manager.player.destroy();
                musicManager.channel.getGuild().getAudioManager().closeAudioConnection();
                musicManager.textChannel.sendMessage("Stopped!").queue();
            } else {
                musicManager.manager.scheduler.nextTrack();

                channel.sendMessage("Skipped to next track.").queue();
            }
        }
    }

    public String searchYoutube(String query)   {
        try {
            YouTube.Search.List search = youTube.search().list("id,snippet");
            search.setKey(devKey);
            search.setQ(query);
            search.setType("video");
            search.setFields("items(id/kind,id/videoId,snippet/title,snippet/thumbnails/default/url)");
            search.setMaxResults(1L);
            SearchListResponse searchResponse = search.execute();
            return "https://www.youtube.com/watch?v=" + searchResponse.getItems().get(0).getId().getVideoId();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return "";
    }

    public void start() {
        youTube = new YouTube.Builder(Auth.HTTP_TRANSPORT, Auth.JSON_FACTORY, new HttpRequestInitializer() {
            public void initialize(HttpRequest request) throws IOException {
            }
        }).setApplicationName("youtube-cmdline-search-sample").build();
        devKey = getDevKey();

        jda.getPresence().setGame(Game.of("Say ..help", "https://www.twitch.tv/daporkchop_"));

        this.musicManagers = new HashMap<>();

        this.playerManager = new DefaultAudioPlayerManager();
        AudioSourceManagers.registerRemoteSources(playerManager);
        AudioSourceManagers.registerLocalSource(playerManager);

        UUIDFetcher.init();

        final String authToken = getAuthtoken();

        new Timer().schedule(new TimerTask() {
            @Override
            public void run() {
                try {
                    HTTPUtils.performPostRequestWithAuth(HTTPUtils.constantURL("https://bots.discord.pw/api/bots/287894637165936640/stats"),
                            "{ \"server_count\": " + PorkBot.INSTANCE.jda.getGuilds().size() + " }",
                            "application/json",
                            authToken);
                } catch (Exception e) {
                }
            }
        }, 1000, 120000);

        new Timer().schedule(new TimerTask() {
            @Override
            public void run() {
                Iterator<Map.Entry<Long, GuildAudioInfo>> iterator = musicManagers.entrySet().iterator();
                while (iterator.hasNext()) {
                    Map.Entry<Long, GuildAudioInfo> entry = iterator.next();
                    if (entry.getValue().channel == null) {
                        System.out.println("null channel");
                        continue;
                    }
                    if (entry.getValue().channel.getMembers().size() < 2) { //nobody's in the channel
                        entry.getValue().manager.player.destroy();
                        entry.getValue().channel.getGuild().getAudioManager().closeAudioConnection();
                        iterator.remove();
                    }
                    if (entry.getValue().manager.scheduler.queue.size() == 0 && entry.getValue().manager.player.getPlayingTrack() == null)   {
                        entry.getValue().manager.player.destroy();
                        entry.getValue().channel.getGuild().getAudioManager().closeAudioConnection();
                        iterator.remove();
                    }
                }
            }
        }, 10000, 5000);

        CommandRegistry.registerCommand(new CommandHelp());
        CommandRegistry.registerCommand(new CommandInvite());
        CommandRegistry.registerCommand(new CommandMcUUID());
        CommandRegistry.registerCommand(new CommandSay());
        CommandRegistry.registerCommand(new CommandMcPing());
        CommandRegistry.registerCommand(new CommandPeQuery());
        CommandRegistry.registerCommand(new CommandMcSkin());
        CommandRegistry.registerCommand(new CommandMcAvatar());
        CommandRegistry.registerCommand(new CommandMcHead());
        CommandRegistry.registerCommand(new CommandMcStatus());
        CommandRegistry.registerCommand(new CommandPing());
        CommandRegistry.registerCommand(new CommandMcMOTD());
        CommandRegistry.registerCommand(new CommandMcCount());
        CommandRegistry.registerCommand(new CommandMcIcon());
        CommandRegistry.registerCommand(new CommandMcQuery());
        CommandRegistry.registerCommand(new CommandPePing());
        CommandRegistry.registerCommand(new CommandOfflineUUID());
        CommandRegistry.registerCommand(new CommandBotInfo());
        CommandRegistry.registerCommand(new CommandTest());
        CommandRegistry.registerCommand(new CommandMcLatency());
        CommandRegistry.registerCommand(new CommandMcVersion());
        CommandRegistry.registerCommand(new CommandPeCount());
        CommandRegistry.registerCommand(new CommandPeLatency());
        CommandRegistry.registerCommand(new CommandPeMOTD());
        CommandRegistry.registerCommand(new CommandPeVersion());
        CommandRegistry.registerCommand(new CommandDice());
        CommandRegistry.registerCommand(new CommandEmojiID());
        CommandRegistry.registerCommand(new CommandThonk());
        CommandRegistry.registerCommand(new CommandInterject());
        CommandRegistry.registerCommand(new CommandCommandInfo());
        CommandRegistry.registerCommand(new CommandPlay());
        CommandRegistry.registerCommand(new CommandShuffle());
        CommandRegistry.registerCommand(new CommandQueue());
        CommandRegistry.registerCommand(new CommandStop());
        CommandRegistry.registerCommand(new CommandSkip());

        while (true) { //don't remember why i put this here, but there's got to be a reason :P
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {

            }
        }
    }
}
