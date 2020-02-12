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

package net.daporkchop.porkbot.command.audio;

import net.daporkchop.lib.common.cache.Cache;
import net.daporkchop.lib.common.util.PorkUtil;
import net.daporkchop.porkbot.audio.PorkAudio;
import net.daporkchop.porkbot.audio.SearchPlatform;
import net.daporkchop.porkbot.command.Command;
import net.daporkchop.porkbot.util.Constants;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.VoiceChannel;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author DaPorkchop_
 */
public class CommandPlay extends Command {
    private static final Pattern        URL_PATTERN               = Pattern.compile('^' + Pattern.quote(Constants.COMMAND_PREFIX) + "play ((?>https?|ftp)://[0-9a-zA-Z-._~:/?#\\[\\]@!$&'()*+,;=%]+)");
    private static final Cache<Matcher> URL_PATTERN_MATCHER_CACHE = Cache.soft(() -> URL_PATTERN.matcher(""));

    private static final Pattern        SEARCH_PATTERN               = Pattern.compile('^' + Pattern.quote(Constants.COMMAND_PREFIX) + "play (?>(?i)(" + String.join("|", SearchPlatform.getAllPlatformNamesAndAliases()) + ")(?-i) )?(.+)");
    private static final Cache<Matcher> SEARCH_PATTERN_MATCHER_CACHE = Cache.soft(() -> SEARCH_PATTERN.matcher(""));

    public CommandPlay() {
        super("play");
    }

    @Override
    public void execute(GuildMessageReceivedEvent evt, String[] args, String rawContent) {
        Matcher matcher;
        VoiceChannel dstChannel;

        if (args.length < 2 || args[1].isEmpty()) {
            this.sendErrorMessage(evt.getChannel(), "No track URL or search terms given!");
        } else if ((dstChannel = evt.getMember().getVoiceState().getChannel()) == null) {
            evt.getChannel().sendMessage("You must be in a voice channel to play audio!").queue();
        } else if ((matcher = URL_PATTERN_MATCHER_CACHE.get().reset(rawContent)).matches()) {
            PorkAudio.addTrackByURL(evt.getGuild(), evt.getChannel(), evt.getMember(), matcher.group(1), dstChannel);

            if (evt.getGuild().getMember(evt.getJDA().getSelfUser()).hasPermission(evt.getChannel(), Permission.MESSAGE_MANAGE)) {
                evt.getMessage().suppressEmbeds(true).queue();
            }
        } else if ((matcher = SEARCH_PATTERN_MATCHER_CACHE.get()).reset(rawContent).matches()) {
            SearchPlatform platform = SearchPlatform.from(PorkUtil.fallbackIfNull(matcher.group(1), SearchPlatform.YOUTUBE.name()));
            if (platform == null)   {
                evt.getChannel().sendMessage("Unknown platform: `" + matcher.group(1) + '`').queue();
            } else {
                PorkAudio.addTrackBySearch(evt.getGuild(), evt.getChannel(), evt.getMember(), platform, matcher.group(2), dstChannel);
            }
        } else {
            throw new IllegalStateException();
        }

    }

    @Override
    public String getUsage() {
        return "..play <url or YouTube search terms>";
    }

    @Override
    public String getUsageExample() {
        return "..play https://cloud.daporkchop.net/random/music/DBOYD/DBOYD%20-%20Lazy%20Dayze.mp3";
    }
}
