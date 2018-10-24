package net.daporkchop.porkbot.audio;

import com.sedmelluq.discord.lavaplayer.container.*;
import com.sedmelluq.discord.lavaplayer.player.DefaultAudioPlayerManager;
import com.sedmelluq.discord.lavaplayer.source.ProbingAudioSourceManager;
import com.sedmelluq.discord.lavaplayer.source.http.HttpAudioSourceManager;
import com.sedmelluq.discord.lavaplayer.source.http.HttpAudioTrack;
import com.sedmelluq.discord.lavaplayer.tools.FriendlyException;
import com.sedmelluq.discord.lavaplayer.tools.io.*;
import com.sedmelluq.discord.lavaplayer.track.*;
import net.daporkchop.porkbot.util.HTTPUtils;
import net.dv8tion.jda.core.entities.Message;
import org.apache.commons.validator.routines.UrlValidator;
import org.apache.http.HttpStatus;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.impl.client.HttpClientBuilder;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.lang.reflect.Field;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Collectors;

import static com.sedmelluq.discord.lavaplayer.tools.FriendlyException.Severity.COMMON;
import static com.sedmelluq.discord.lavaplayer.tools.FriendlyException.Severity.SUSPICIOUS;
import static com.sedmelluq.discord.lavaplayer.tools.io.HttpClientTools.getHeaderValue;

/**
 * @author DaPorkchop_
 */
public class BetterHttpSourceManager extends HttpAudioSourceManager {
    private static final Field audioTrackInfo_title;

    static {
        Field f = null;
        try {
            f = AudioTrackInfo.class.getDeclaredField("title");
            f.setAccessible(true);
        } catch (Exception e)   {
            throw new RuntimeException(e);
        } finally {
            audioTrackInfo_title = f;
        }
    }

    private static UrlValidator validator = new UrlValidator();

    @Override
    public String getSourceName() {
        return "http";
    }

    @Override
    public AudioItem loadItem(DefaultAudioPlayerManager manager, AudioReference reference) {
        return this.loadItem(manager, reference, true);
    }

    public AudioItem loadItem(DefaultAudioPlayerManager manager, AudioReference reference, boolean allowPlaylist) {
        AudioReference httpReference = getAsHttpReference(reference);
        if (httpReference == null) {
            return null;
        }

        if (allowPlaylist && httpReference.identifier.endsWith(".txt"))  {
            try {
                List<AudioTrack> items = Arrays.stream(HTTPUtils.performGetRequest(HTTPUtils.constantURL(httpReference.identifier), 32000).trim().split("\n"))
                        .map(String::trim)
                        .filter(s -> !s.isEmpty() && !s.contains("~") && validator.isValid(s))
                        .distinct()
                        .map(s -> new AudioReference(s, null))
                        .map(ref -> this.loadItem(manager, ref, false))
                        .filter(o -> o instanceof AudioTrack)
                        .map(AudioTrack.class::cast)
                        .collect(Collectors.toList());
                items.forEach(track -> {
                    if (track.getInfo().title == null || MediaContainerDetection.UNKNOWN_TITLE.equals(track.getInfo().title))  {
                        String[] split = track.getIdentifier().split("/");
                        try {
                            audioTrackInfo_title.set(track.getInfo(), split[split.length - 1].replaceAll("%20", " "));
                        } catch (Exception e)   {
                            throw new RuntimeException(e);
                        }
                    }
                });
                return new BasicAudioPlaylist(httpReference.identifier, items, null, false);
            } catch (IOException e) {
                e.printStackTrace();
                return null;
            }
        }

        return handleLoadResult(detectContainer(httpReference));
    }

    private AudioReference getAsHttpReference(AudioReference reference) {
        if (reference.identifier.startsWith("https://") || reference.identifier.startsWith("http://")) {
            return reference;
        } else if (reference.identifier.startsWith("icy://")) {
            return new AudioReference("http://" + reference.identifier.substring(6), reference.title);
        }
        return null;
    }

    private MediaContainerDetectionResult detectContainer(AudioReference reference) {
        MediaContainerDetectionResult result;

        try (HttpInterface httpInterface = getHttpInterface()) {
            result = detectContainerWithClient(httpInterface, reference);
        } catch (IOException e) {
            throw new FriendlyException("Connecting to the URL failed.", SUSPICIOUS, e);
        }

        return result;
    }

    private MediaContainerDetectionResult detectContainerWithClient(HttpInterface httpInterface, AudioReference reference) throws IOException {
        try (PersistentHttpStream inputStream = new PersistentHttpStream(httpInterface, new URI(reference.identifier), Long.MAX_VALUE)) {
            int statusCode = inputStream.checkStatusCode();
            String redirectUrl = HttpClientTools.getRedirectLocation(reference.identifier, inputStream.getCurrentResponse());

            if (redirectUrl != null) {
                return new MediaContainerDetectionResult(null, new AudioReference(redirectUrl, null));
            } else if (statusCode == HttpStatus.SC_NOT_FOUND) {
                return null;
            } else if (!HttpClientTools.isSuccessWithContent(statusCode)) {
                throw new FriendlyException("That URL is not playable.", COMMON, new IllegalStateException("Status code " + statusCode));
            }

            MediaContainerHints hints = MediaContainerHints.from(getHeaderValue(inputStream.getCurrentResponse(), "Content-Type"), null);
            return MediaContainerDetection.detectContainer(reference, inputStream, hints);
        } catch (URISyntaxException e) {
            throw new FriendlyException("Not a valid URL.", COMMON, e);
        }
    }
}
