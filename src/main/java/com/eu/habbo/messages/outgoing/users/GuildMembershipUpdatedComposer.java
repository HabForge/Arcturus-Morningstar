package com.eu.habbo.messages.outgoing.users;

import com.eu.habbo.habbohotel.guilds.Guild;
import com.eu.habbo.habbohotel.guilds.GuildMember;
import com.eu.habbo.messages.ServerMessage;
import com.eu.habbo.messages.outgoing.MessageComposer;
import com.eu.habbo.messages.outgoing.Outgoing;

public class GuildMembershipUpdatedComposer extends MessageComposer {
    private final Guild guild;
    private final GuildMember guildMember;

    public GuildMembershipUpdatedComposer(Guild guild, GuildMember guildMember) {
        this.guildMember = guildMember;
        this.guild = guild;
    }

    @Override
    protected ServerMessage composeInternal() {
        this.response.init(Outgoing.GuildMembershipUpdated);
        this.response.appendInt(this.guild.getId());
        this.response.appendInt(this.guildMember.getRank().type);
        this.response.appendInt(this.guildMember.getUserId());
        this.response.appendString(this.guildMember.getUsername());
        this.response.appendString(this.guildMember.getLook());
        this.response.appendString(this.guildMember.getRank().type != 0 ? this.guildMember.getJoinDate() + "" : "");
        return this.response;
    }
}