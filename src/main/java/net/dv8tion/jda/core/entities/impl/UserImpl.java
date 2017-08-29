/*
 * Adapted from the Wizardry License
 *
 * Copyright (c) 2016 DaPorkchop_
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

package net.dv8tion.jda.core.entities.impl;

import net.dv8tion.jda.core.JDA;
import net.dv8tion.jda.core.entities.Guild;
import net.dv8tion.jda.core.entities.PrivateChannel;
import net.dv8tion.jda.core.entities.User;
import net.dv8tion.jda.core.requests.Request;
import net.dv8tion.jda.core.requests.Response;
import net.dv8tion.jda.core.requests.RestAction;
import net.dv8tion.jda.core.requests.Route;
import net.dv8tion.jda.core.utils.MiscUtil;
import org.json.JSONObject;

import java.util.FormattableFlags;
import java.util.Formatter;
import java.util.List;

public class UserImpl implements User {
    protected final long id;
    protected final JDAImpl api;

    protected short discriminator;
    protected String name;
    //Not needed by porkbot: protected String avatarId;
    protected PrivateChannel privateChannel;
    protected boolean bot;
    protected boolean fake = false;

    public UserImpl(long id, JDAImpl api) {
        this.id = id;
        this.api = api;
    }

    @Override
    public String getName() {
        return name;
    }

    public UserImpl setName(String name) {
        this.name = name;
        return this;
    }

    @Override
    public String getDiscriminator() {
        return String.format("%04d", discriminator);
    }

    public UserImpl setDiscriminator(String discriminator) {
        this.discriminator = Short.parseShort(discriminator);
        return this;
    }

    @Override
    public String getAvatarId() {
        //Not needed by porkbot: return avatarId;
        return null;
    }

    public UserImpl setAvatarId(String avatarId) {
        //Not needed by porkbot: this.avatarId = avatarId;
        return this;
    }

    @Override
    public String getAvatarUrl() {
        //Not needed by porkbot: return getAvatarId() == null ? null : "https://cdn.discordapp.com/avatars/" + getId() + "/" + getAvatarId()
        //Not needed by porkbot: + (getAvatarId().startsWith("a_") ? ".gif" : ".png");
        return null;
    }

    @Override
    public String getDefaultAvatarId() {
        return DefaultAvatar.values()[Integer.parseInt(getDiscriminator()) % DefaultAvatar.values().length].toString();
    }

    @Override
    public String getDefaultAvatarUrl() {
        return "https://discordapp.com/assets/" + getDefaultAvatarId() + ".png";
    }

    @Override
    public String getEffectiveAvatarUrl() {
        return getAvatarUrl() == null ? getDefaultAvatarUrl() : getAvatarUrl();
    }

    @Override
    public boolean hasPrivateChannel() {
        return privateChannel != null;
    }

    @Override
    public RestAction<PrivateChannel> openPrivateChannel() {
        if (privateChannel != null)
            return new RestAction.EmptyRestAction<>(getJDA(), privateChannel);

        if (fake)
            throw new IllegalStateException("Cannot open a PrivateChannel with a Fake user.");

        Route.CompiledRoute route = Route.Self.CREATE_PRIVATE_CHANNEL.compile();
        JSONObject body = new JSONObject().put("recipient_id", getId());
        return new RestAction<PrivateChannel>(api, route, body) {
            @Override
            protected void handleResponse(Response response, Request<PrivateChannel> request) {
                if (response.isOk()) {
                    PrivateChannel priv = api.getEntityBuilder().createPrivateChannel(response.getObject());
                    UserImpl.this.privateChannel = priv;
                    request.onSuccess(priv);
                } else {
                    request.onFailure(response);
                }
            }
        };
    }

    @Override
    public List<Guild> getMutualGuilds() {
        return getJDA().getMutualGuilds(this);
    }

    public PrivateChannel getPrivateChannel() {
        if (!hasPrivateChannel())
            throw new IllegalStateException("There is no PrivateChannel for this user yet! Use User#openPrivateChannel() first!");

        return privateChannel;
    }

    public UserImpl setPrivateChannel(PrivateChannel privateChannel) {
        this.privateChannel = privateChannel;
        return this;
    }

    @Override
    public boolean isBot() {
        return bot;
    }

    public UserImpl setBot(boolean bot) {
        this.bot = bot;
        return this;
    }

    @Override
    public JDA getJDA() {
        return api;
    }

    @Override
    public String getAsMention() {
        return "<@" + id + '>';
    }

    // -- Setters --

    @Override
    public long getIdLong() {
        return id;
    }

    @Override
    public boolean isFake() {
        return fake;
    }

    public UserImpl setFake(boolean fake) {
        this.fake = fake;
        return this;
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof UserImpl))
            return false;
        UserImpl oUser = (UserImpl) o;
        return this == oUser || this.id == oUser.id;
    }

    @Override
    public int hashCode() {
        return Long.hashCode(id);
    }

    @Override
    public String toString() {
        return "U:" + getName() + '(' + id + ')';
    }

    @Override
    public void formatTo(Formatter formatter, int flags, int width, int precision) {
        boolean alt = (flags & FormattableFlags.ALTERNATE) == FormattableFlags.ALTERNATE;
        boolean upper = (flags & FormattableFlags.UPPERCASE) == FormattableFlags.UPPERCASE;
        boolean leftJustified = (flags & FormattableFlags.LEFT_JUSTIFY) == FormattableFlags.LEFT_JUSTIFY;

        String out;
        if (!alt)
            out = getAsMention();
        else if (upper)
            out = String.format(formatter.locale(), "%S#%s", getName(), getDiscriminator());
        else
            out = String.format(formatter.locale(), "%s#%s", getName(), getDiscriminator());

        MiscUtil.appendTo(formatter, width, precision, leftJustified, out);
    }

    public enum DefaultAvatar {
        BLURPLE("6debd47ed13483642cf09e832ed0bc1b"),
        GREY("322c936a8c8be1b803cd94861bdfa868"),
        GREEN("dd4dbc0016779df1378e7812eabaa04d"),
        ORANGE("0e291f67c9274a1abdddeb3fd919cbaa"),
        RED("1cbd08c76f8af6dddce02c5138971129");

        private final String text;

        DefaultAvatar(String text) {
            this.text = text;
        }

        @Override
        public String toString() {
            return text;
        }
    }
}
