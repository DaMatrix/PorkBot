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
import net.dv8tion.jda.core.OnlineStatus;
import net.dv8tion.jda.core.Permission;
import net.dv8tion.jda.core.entities.*;
import net.dv8tion.jda.core.utils.Checks;
import net.dv8tion.jda.core.utils.PermissionUtil;

import java.awt.*;
import java.time.OffsetDateTime;
import java.util.*;
import java.util.List;

public class MemberImpl implements Member {
    private final GuildImpl guild;
    private final User user;
    private final HashSet<Role> roles = new HashSet<>();
    private final GuildVoiceState voiceState;

    private String nickname;
    //Not needed by porkbot: private OffsetDateTime joinDate;
    //Not needed by porkbot: private Game game;
    //Not needed by porkbot: private OnlineStatus onlineStatus = OnlineStatus.OFFLINE;

    public MemberImpl(GuildImpl guild, User user) {
        this.guild = guild;
        this.user = user;
        this.voiceState = new GuildVoiceStateImpl(guild, this);
    }

    @Override
    public User getUser() {
        return user;
    }

    @Override
    public Guild getGuild() {
        return guild;
    }

    @Override
    public JDA getJDA() {
        return user.getJDA();
    }

    @Override
    public OffsetDateTime getJoinDate() {
        //Not needed by porkbot: return joinDate;
        return null;
    }

    public MemberImpl setJoinDate(OffsetDateTime joinDate) {
        //Not needed by porkbot: this.joinDate = joinDate;
        return this;
    }

    @Override
    public GuildVoiceState getVoiceState() {
        return voiceState;
    }

    @Override
    public Game getGame() {
        //Not needed by porkbot: return game;
        return null;
    }

    public MemberImpl setGame(Game game) {
        //Not needed by porkbot: this.game = game;
        return this;
    }

    @Override
    public OnlineStatus getOnlineStatus() {
        //Not needed by porkbot: return onlineStatus;
        return OnlineStatus.ONLINE;
    }

    public MemberImpl setOnlineStatus(OnlineStatus onlineStatus) {
        //Not needed by porkbot: this.onlineStatus = onlineStatus;
        return this;
    }

    @Override
    public String getNickname() {
        return nickname;
    }

    public MemberImpl setNickname(String nickname) {
        this.nickname = nickname;
        return this;
    }

    @Override
    public String getEffectiveName() {
        return nickname != null ? nickname : user.getName();
    }

    @Override
    public List<Role> getRoles() {
        List<Role> roleList = new ArrayList<>(roles);
        roleList.sort(Comparator.reverseOrder());

        return Collections.unmodifiableList(roleList);
        //return Sickhackery.notSavingRoleList;
    }

    @Override
    public Color getColor() {
        for (Role r : getRoles()) {
            if (r.getColor() != null)
                return r.getColor();
        }
        return null;
    }

    @Override
    public List<Permission> getPermissions() {
        return Collections.unmodifiableList(
                Permission.getPermissions(
                        PermissionUtil.getEffectivePermission(this)));
    }

    @Override
    public List<Permission> getPermissions(Channel channel) {
        if (!guild.equals(channel.getGuild()))
            throw new IllegalArgumentException("Provided channel is not in the same guild as this member!");

        return Collections.unmodifiableList(
                Permission.getPermissions(
                        PermissionUtil.getEffectivePermission(channel, this)));
    }

    @Override
    public boolean hasPermission(Permission... permissions) {
        return PermissionUtil.checkPermission(this, permissions);
    }

    @Override
    public boolean hasPermission(Collection<Permission> permissions) {
        Checks.notNull(permissions, "Permission Collection");

        return hasPermission(permissions.toArray(new Permission[permissions.size()]));
    }

    @Override
    public boolean hasPermission(Channel channel, Permission... permissions) {
        return PermissionUtil.checkPermission(channel, this, permissions);
    }

    @Override
    public boolean hasPermission(Channel channel, Collection<Permission> permissions) {
        Checks.notNull(permissions, "Permission Collection");

        return hasPermission(channel, permissions.toArray(new Permission[permissions.size()]));
    }

    @Override
    public boolean canInteract(Member member) {
        return PermissionUtil.canInteract(this, member);
    }

    @Override
    public boolean canInteract(Role role) {
        return PermissionUtil.canInteract(this, role);
    }

    @Override
    public boolean canInteract(Emote emote) {
        return PermissionUtil.canInteract(this, emote);
    }

    @Override
    public boolean isOwner() {
        return this.equals(guild.getOwner());
    }

    public Set<Role> getRoleSet() {
        return roles;
        //Not needed by porkbot: return Sickhackery.notSavingRoleSet;
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof Member))
            return false;

        Member oMember = (Member) o;
        return this == oMember || (oMember.getUser().equals(user) && oMember.getGuild().equals(guild));
    }

    @Override
    public int hashCode() {
        return (guild.getId() + user.getId()).hashCode();
    }

    @Override
    public String toString() {
        return "MB:" + getEffectiveName() + '(' + user.toString() + " / " + guild.toString() + ')';
    }

    @Override
    public String getAsMention() {
        return nickname == null ? user.getAsMention() : "<@!" + user.getIdLong() + '>';
    }
}
