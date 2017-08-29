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

import gnu.trove.map.TLongObjectMap;
import net.dv8tion.jda.client.requests.restaction.pagination.MentionPaginationAction;
import net.dv8tion.jda.core.AccountType;
import net.dv8tion.jda.core.Permission;
import net.dv8tion.jda.core.Region;
import net.dv8tion.jda.core.entities.*;
import net.dv8tion.jda.core.exceptions.AccountTypeException;
import net.dv8tion.jda.core.exceptions.GuildUnavailableException;
import net.dv8tion.jda.core.exceptions.InsufficientPermissionException;
import net.dv8tion.jda.core.exceptions.PermissionException;
import net.dv8tion.jda.core.managers.AudioManager;
import net.dv8tion.jda.core.managers.GuildController;
import net.dv8tion.jda.core.managers.GuildManager;
import net.dv8tion.jda.core.managers.GuildManagerUpdatable;
import net.dv8tion.jda.core.managers.impl.AudioManagerImpl;
import net.dv8tion.jda.core.requests.Request;
import net.dv8tion.jda.core.requests.Response;
import net.dv8tion.jda.core.requests.RestAction;
import net.dv8tion.jda.core.requests.Route;
import net.dv8tion.jda.core.requests.restaction.pagination.AuditLogPaginationAction;
import net.dv8tion.jda.core.utils.Checks;
import net.dv8tion.jda.core.utils.Helpers;
import net.dv8tion.jda.core.utils.MiscUtil;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.time.OffsetDateTime;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;

public class GuildImpl implements Guild {
    private final long id;
    private final JDAImpl api;
    private final TLongObjectMap<TextChannel> textChannels = MiscUtil.newLongMap();
    private final TLongObjectMap<VoiceChannel> voiceChannels = MiscUtil.newLongMap();
    private final TLongObjectMap<Member> members = MiscUtil.newLongMap();
    private final TLongObjectMap<Role> roles = MiscUtil.newLongMap();
    private final TLongObjectMap<Emote> emotes = MiscUtil.newLongMap();

    private final TLongObjectMap<JSONObject> cachedPresences = MiscUtil.newLongMap();

    private final Object mngLock = new Object();
    private volatile GuildManager manager;
    private volatile GuildManagerUpdatable managerUpdatable;
    private volatile GuildController controller;

    private Member owner;
    private String name;
    //Not needed by porkbot: private String iconId;
    private String splashId;
    private Region region;
    private TextChannel publicChannel;
    private VoiceChannel afkChannel;
    private Role publicRole;
    private VerificationLevel verificationLevel;
    private NotificationLevel defaultNotificationLevel;
    private MFALevel mfaLevel;
    private ExplicitContentLevel explicitContentLevel;
    private Timeout afkTimeout;
    private boolean available;
    private boolean canSendVerification = false;

    public GuildImpl(JDAImpl api, long id) {
        this.id = id;
        this.api = api;
    }

    @Override
    public String getName() {
        return name;
    }

    public GuildImpl setName(String name) {
        this.name = name;
        return this;
    }

    @Override
    public String getIconId() {
        //Not needed by porkbot: return iconId;
        return "";
    }

    public GuildImpl setIconId(String iconId) {
        //Not needed by porkbot: this.iconId = iconId;
        return this;
    }

    @Override
    public String getIconUrl() {
        //Not needed by porkbot: return iconId == null ? null : "https://cdn.discordapp.com/icons/" + id + "/" + iconId + ".jpg";
        return "";
    }

    @Override
    public String getSplashId() {
        return splashId;
    }

    public GuildImpl setSplashId(String splashId) {
        this.splashId = splashId;
        return this;
    }

    @Override
    public String getSplashUrl() {
        return splashId == null ? null : "https://cdn.discordapp.com/splashes/" + id + "/" + splashId + ".jpg";
    }

    @Override
    public VoiceChannel getAfkChannel() {
        return afkChannel;
    }

    public GuildImpl setAfkChannel(VoiceChannel afkChannel) {
        this.afkChannel = afkChannel;
        return this;
    }

    @Override
    public RestAction<List<Webhook>> getWebhooks() {
        if (!getSelfMember().hasPermission(Permission.MANAGE_WEBHOOKS))
            throw new InsufficientPermissionException(Permission.MANAGE_WEBHOOKS);

        Route.CompiledRoute route = Route.Guilds.GET_WEBHOOKS.compile(getId());

        return new RestAction<List<Webhook>>(api, route) {
            @Override
            protected void handleResponse(Response response, Request<List<Webhook>> request) {
                if (!response.isOk()) {
                    request.onFailure(response);
                    return;
                }

                List<Webhook> webhooks = new LinkedList<>();
                JSONArray array = response.getArray();
                EntityBuilder builder = api.getEntityBuilder();

                for (Object object : array) {
                    try {
                        webhooks.add(builder.createWebhook((JSONObject) object));
                    } catch (JSONException | NullPointerException e) {
                        JDAImpl.LOG.log(e);
                    }
                }

                request.onSuccess(webhooks);
            }
        };
    }

    @Override
    public Member getOwner() {
        return owner;
    }

    public GuildImpl setOwner(Member owner) {
        this.owner = owner;
        return this;
    }

    @Override
    public Timeout getAfkTimeout() {
        return afkTimeout;
    }

    public GuildImpl setAfkTimeout(Timeout afkTimeout) {
        this.afkTimeout = afkTimeout;
        return this;
    }

    @Override
    public Region getRegion() {
        return region;
    }

    public GuildImpl setRegion(Region region) {
        this.region = region;
        return this;
    }

    @Override
    public boolean isMember(User user) {
        return members.containsKey(user.getIdLong());
    }

    @Override
    public Member getSelfMember() {
        return getMember(getJDA().getSelfUser());
    }

    @Override
    public Member getMember(User user) {
        return getMemberById(user.getIdLong());
    }

    @Override
    public Member getMemberById(String userId) {
        return members.get(MiscUtil.parseSnowflake(userId));
    }

    @Override
    public Member getMemberById(long userId) {
        return members.get(userId);
    }

    @Override
    public List<Member> getMembers() {
        return Collections.unmodifiableList(new ArrayList<>(members.valueCollection()));
    }

    @Override
    public List<Member> getMembersByName(String name, boolean ignoreCase) {
        Checks.notNull(name, "name");
        return Collections.unmodifiableList(members.valueCollection().stream()
                .filter(m ->
                        ignoreCase
                                ? name.equalsIgnoreCase(m.getUser().getName())
                                : name.equals(m.getUser().getName()))
                .collect(Collectors.toList()));
    }

    @Override
    public List<Member> getMembersByNickname(String nickname, boolean ignoreCase) {
        Checks.notNull(nickname, "nickname");
        return Collections.unmodifiableList(members.valueCollection().stream()
                .filter(m ->
                        ignoreCase
                                ? nickname.equalsIgnoreCase(m.getNickname())
                                : nickname.equals(m.getNickname()))
                .collect(Collectors.toList()));
    }

    @Override
    public List<Member> getMembersByEffectiveName(String name, boolean ignoreCase) {
        Checks.notNull(name, "name");
        return Collections.unmodifiableList(members.valueCollection().stream()
                .filter(m ->
                        ignoreCase
                                ? name.equalsIgnoreCase(m.getEffectiveName())
                                : name.equals(m.getEffectiveName()))
                .collect(Collectors.toList()));
    }

    @Override
    public List<Member> getMembersWithRoles(Role... roles) {
        Checks.notNull(roles, "roles");
        return getMembersWithRoles(Arrays.asList(roles));
    }

    @Override
    public List<Member> getMembersWithRoles(Collection<Role> roles) {
        Checks.notNull(roles, "roles");
        for (Role r : roles) {
            Checks.notNull(r, "Role provided in collection");
            if (!r.getGuild().equals(this))
                throw new IllegalArgumentException("Role provided was from a different Guild! Role: " + r);
        }

        return Collections.unmodifiableList(members.valueCollection().stream()
                .filter(m -> m.getRoles().containsAll(roles))
                .collect(Collectors.toList()));
    }

    @Override
    public TextChannel getTextChannelById(String id) {
        return textChannels.get(MiscUtil.parseSnowflake(id));
    }

    @Override
    public TextChannel getTextChannelById(long id) {
        return textChannels.get(id);
    }

    @Override
    public List<TextChannel> getTextChannelsByName(String name, boolean ignoreCase) {
        Checks.notNull(name, "name");
        return Collections.unmodifiableList(textChannels.valueCollection().stream()
                .filter(tc ->
                        ignoreCase
                                ? name.equalsIgnoreCase(tc.getName())
                                : name.equals(tc.getName()))
                .collect(Collectors.toList()));
    }

    @Override
    public List<TextChannel> getTextChannels() {
        ArrayList<TextChannel> channels = new ArrayList<>(textChannels.valueCollection());
        channels.sort(Comparator.reverseOrder());
        return Collections.unmodifiableList(channels);
    }

    @Override
    public VoiceChannel getVoiceChannelById(String id) {
        return voiceChannels.get(MiscUtil.parseSnowflake(id));
    }

    @Override
    public VoiceChannel getVoiceChannelById(long id) {
        return voiceChannels.get(id);
    }

    @Override
    public List<VoiceChannel> getVoiceChannelsByName(String name, boolean ignoreCase) {
        Checks.notNull(name, "name");
        return Collections.unmodifiableList(voiceChannels.valueCollection().stream()
                .filter(vc ->
                        ignoreCase
                                ? name.equalsIgnoreCase(vc.getName())
                                : name.equals(vc.getName()))
                .collect(Collectors.toList()));
    }

    @Override
    public List<VoiceChannel> getVoiceChannels() {
        List<VoiceChannel> channels = new ArrayList<>(voiceChannels.valueCollection());
        channels.sort(Comparator.reverseOrder());
        return Collections.unmodifiableList(channels);
    }

    @Override
    public Role getRoleById(String id) {
        return roles.get(MiscUtil.parseSnowflake(id));
    }

    @Override
    public Role getRoleById(long id) {
        return roles.get(id);
    }

    @Override
    public List<Role> getRoles() {
        List<Role> list = new ArrayList<>(roles.valueCollection());
        list.sort(Comparator.reverseOrder());
        return Collections.unmodifiableList(list);
    }

    @Override
    public List<Role> getRolesByName(String name, boolean ignoreCase) {
        Checks.notNull(name, "name");
        return Collections.unmodifiableList(roles.valueCollection().stream()
                .filter(r ->
                        ignoreCase
                                ? name.equalsIgnoreCase(r.getName())
                                : name.equals(r.getName()))
                .collect(Collectors.toList()));
    }

    @Override
    public Emote getEmoteById(String id) {
        return emotes.get(MiscUtil.parseSnowflake(id));
    }

    @Override
    public Emote getEmoteById(long id) {
        return emotes.get(id);
    }

    @Override
    public List<Emote> getEmotes() {
        return Collections.unmodifiableList(new LinkedList<>(emotes.valueCollection()));
    }

    @Override
    public List<Emote> getEmotesByName(String name, boolean ignoreCase) {
        Checks.notNull(name, "name");
        return Collections.unmodifiableList(emotes.valueCollection().parallelStream()
                .filter(e ->
                        ignoreCase
                                ? Helpers.equalsIgnoreCase(e.getName(), name)
                                : Objects.equals(e.getName(), name))
                .collect(Collectors.toList()));
    }

    @Override
    public RestAction<List<User>> getBans() {
        if (!isAvailable())
            throw new GuildUnavailableException();
        if (!getSelfMember().hasPermission(Permission.BAN_MEMBERS))
            throw new InsufficientPermissionException(Permission.BAN_MEMBERS);

        Route.CompiledRoute route = Route.Guilds.GET_BANS.compile(getId());
        return new RestAction<List<User>>(getJDA(), route) {
            @Override
            protected void handleResponse(Response response, Request<List<User>> request) {
                if (!response.isOk()) {
                    request.onFailure(response);
                    return;
                }

                EntityBuilder builder = api.getEntityBuilder();
                List<User> bans = new LinkedList<>();
                JSONArray bannedArr = response.getArray();

                for (int i = 0; i < bannedArr.length(); i++) {
                    JSONObject user = bannedArr.getJSONObject(i).getJSONObject("user");
                    bans.add(builder.createFakeUser(user, false));
                }
                request.onSuccess(Collections.unmodifiableList(bans));
            }
        };
    }

    @Override
    public RestAction<Integer> getPrunableMemberCount(int days) {
        if (!isAvailable())
            throw new GuildUnavailableException();
        if (!getSelfMember().hasPermission(Permission.KICK_MEMBERS))
            throw new InsufficientPermissionException(Permission.KICK_MEMBERS);

        if (days < 1)
            throw new IllegalArgumentException("Days amount must be at minimum 1 day.");

        Route.CompiledRoute route = Route.Guilds.PRUNABLE_COUNT.compile(getId()).withQueryParams("days", Integer.toString(days));
        return new RestAction<Integer>(getJDA(), route) {
            @Override
            protected void handleResponse(Response response, Request<Integer> request) {
                if (response.isOk())
                    request.onSuccess(response.getObject().getInt("pruned"));
                else
                    request.onFailure(response);
            }
        };
    }

    @Override
    public Role getPublicRole() {
        return publicRole;
    }

    public GuildImpl setPublicRole(Role publicRole) {
        this.publicRole = publicRole;
        return this;
    }

    @Override
    public TextChannel getPublicChannel() {
        return publicChannel;
    }

    public GuildImpl setPublicChannel(TextChannel publicChannel) {
        this.publicChannel = publicChannel;
        return this;
    }

    @Override
    public GuildManager getManager() {
        GuildManager mng = manager;
        if (mng == null) {
            synchronized (mngLock) {
                mng = manager;
                if (mng == null)
                    mng = manager = new GuildManager(this);
            }
        }
        return mng;
    }

    @Override
    public GuildManagerUpdatable getManagerUpdatable() {
        GuildManagerUpdatable mng = managerUpdatable;
        if (mng == null) {
            synchronized (mngLock) {
                mng = managerUpdatable;
                if (mng == null)
                    mng = managerUpdatable = new GuildManagerUpdatable(this);
            }
        }
        return mng;
    }

    @Override
    public GuildController getController() {
        GuildController ctrl = controller;
        if (ctrl == null) {
            synchronized (mngLock) {
                ctrl = controller;
                if (ctrl == null)
                    ctrl = controller = new GuildController(this);
            }
        }
        return ctrl;
    }

    @Override
    public MentionPaginationAction getRecentMentions() {
        if (getJDA().getAccountType() != AccountType.CLIENT)
            throw new AccountTypeException(AccountType.CLIENT);
        return getJDA().asClient().getRecentMentions(this);
    }

    @Override
    public AuditLogPaginationAction getAuditLogs() {
        return new AuditLogPaginationAction(this);
    }

    @Override
    public RestAction<Void> leave() {
        if (owner.equals(getSelfMember()))
            throw new IllegalStateException("Cannot leave a guild that you are the owner of! Transfer guild ownership first!");

        Route.CompiledRoute route = Route.Self.LEAVE_GUILD.compile(getId());
        return new RestAction<Void>(api, route) {
            @Override
            protected void handleResponse(Response response, Request<Void> request) {
                if (response.isOk())
                    request.onSuccess(null);
                else
                    request.onFailure(response);
            }
        };
    }

    @Override
    public RestAction<Void> delete() {
        if (api.getSelfUser().isMfaEnabled())
            throw new IllegalStateException("Cannot delete a guild without providing MFA code. Use Guild#delete(String)");

        return delete(null);
    }

    @Override
    public RestAction<Void> delete(String mfaCode) {
        if (!owner.equals(getSelfMember()))
            throw new PermissionException("Cannot delete a guild that you do not own!");

        JSONObject mfaBody = null;
        if (api.getSelfUser().isMfaEnabled()) {
            Checks.notEmpty(mfaCode, "Provided MultiFactor Auth code");
            mfaBody = new JSONObject().put("code", mfaCode);
        }

        Route.CompiledRoute route = Route.Guilds.DELETE_GUILD.compile(getId());
        return new RestAction<Void>(api, route, mfaBody) {
            @Override
            protected void handleResponse(Response response, Request<Void> request) {
                if (response.isOk())
                    request.onSuccess(null);
                else
                    request.onFailure(response);
            }
        };
    }

    @Override
    public AudioManager getAudioManager() {
        if (!api.isAudioEnabled())
            throw new IllegalStateException("Audio is disabled. Cannot retrieve an AudioManager while audio is disabled.");

        final TLongObjectMap<AudioManagerImpl> managerMap = api.getAudioManagerMap();
        AudioManagerImpl mng = managerMap.get(id);
        if (mng == null) {
            // No previous manager found -> create one
            synchronized (managerMap) {
                mng = managerMap.get(id);
                if (mng == null) {
                    mng = new AudioManagerImpl(this);
                    managerMap.put(id, mng);
                }
            }
        }
        // set guild again to make sure the manager references this instance! Avoiding invalid member cache
        mng.setGuild(this);
        return mng;
    }

    // ---- Setters -----

    @Override
    public JDAImpl getJDA() {
        return api;
    }

    @Override
    public List<GuildVoiceState> getVoiceStates() {
        return Collections.unmodifiableList(
                members.valueCollection().stream().map(Member::getVoiceState).collect(Collectors.toList()));
    }

    @Override
    public VerificationLevel getVerificationLevel() {
        return verificationLevel;
    }

    public GuildImpl setVerificationLevel(VerificationLevel level) {
        this.verificationLevel = level;
        this.canSendVerification = false;   //recalc on next send
        return this;
    }

    @Override
    public NotificationLevel getDefaultNotificationLevel() {
        return defaultNotificationLevel;
    }

    public GuildImpl setDefaultNotificationLevel(NotificationLevel level) {
        this.defaultNotificationLevel = level;
        return this;
    }

    @Override
    public MFALevel getRequiredMFALevel() {
        return mfaLevel;
    }

    public GuildImpl setRequiredMFALevel(MFALevel level) {
        this.mfaLevel = level;
        return this;
    }

    @Override
    public ExplicitContentLevel getExplicitContentLevel() {
        return explicitContentLevel;
    }

    public GuildImpl setExplicitContentLevel(ExplicitContentLevel level) {
        this.explicitContentLevel = level;
        return this;
    }

    @Override
    public boolean checkVerification() {
        if (api.getAccountType() == AccountType.BOT)
            return true;
        if (canSendVerification)
            return true;
        switch (verificationLevel) {
            case HIGH:
                if (ChronoUnit.MINUTES.between(getSelfMember().getJoinDate(), OffsetDateTime.now()) < 10)
                    break;
            case MEDIUM:
                if (ChronoUnit.MINUTES.between(MiscUtil.getCreationTime(api.getSelfUser()), OffsetDateTime.now()) < 5)
                    break;
            case LOW:
                if (!api.getSelfUser().isVerified())
                    break;
            case NONE:
                canSendVerification = true;
                return true;
        }
        return false;
    }

    @Override
    public boolean isAvailable() {
        return available;
    }

    public GuildImpl setAvailable(boolean available) {
        this.available = available;
        return this;
    }

    @Override
    public long getIdLong() {
        return id;
    }

    // -- Map getters --

    public TLongObjectMap<TextChannel> getTextChannelsMap() {
        return textChannels;
    }

    public TLongObjectMap<VoiceChannel> getVoiceChannelMap() {
        return voiceChannels;
    }

    public TLongObjectMap<Member> getMembersMap() {
        return members;
    }

    public TLongObjectMap<Role> getRolesMap() {
        return roles;
    }

    public TLongObjectMap<JSONObject> getCachedPresenceMap() {
        return cachedPresences;
    }

    public TLongObjectMap<Emote> getEmoteMap() {
        return emotes;
    }


    // -- Object overrides --

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof GuildImpl))
            return false;
        GuildImpl oGuild = (GuildImpl) o;
        return this == oGuild || this.id == oGuild.id;
    }

    @Override
    public int hashCode() {
        return Long.hashCode(id);
    }

    @Override
    public String toString() {
        return "G:" + getName() + '(' + id + ')';
    }

    @Override
    public RestAction<List<Invite>> getInvites() {
        if (!this.getSelfMember().hasPermission(Permission.MANAGE_SERVER))
            throw new InsufficientPermissionException(Permission.MANAGE_SERVER);

        final Route.CompiledRoute route = Route.Invites.GET_GUILD_INVITES.compile(getId());

        return new RestAction<List<Invite>>(api, route) {
            @Override
            protected void handleResponse(final Response response, final Request<List<Invite>> request) {
                if (response.isOk()) {
                    EntityBuilder entityBuilder = this.api.getEntityBuilder();
                    JSONArray array = response.getArray();
                    List<Invite> invites = new ArrayList<>(array.length());
                    for (int i = 0; i < array.length(); i++) {
                        invites.add(entityBuilder.createInvite(array.getJSONObject(i)));
                    }
                    request.onSuccess(invites);
                } else {
                    request.onFailure(response);
                }
            }
        };
    }

}
