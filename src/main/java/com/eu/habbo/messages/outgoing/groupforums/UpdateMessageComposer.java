package com.eu.habbo.messages.outgoing.groupforums;

import com.eu.habbo.habbohotel.guilds.forums.ForumThreadComment;
import com.eu.habbo.messages.ServerMessage;
import com.eu.habbo.messages.outgoing.MessageComposer;
import com.eu.habbo.messages.outgoing.Outgoing;

public class UpdateMessageComposer extends MessageComposer {
    public final int guildId;
    public final int threadId;
    public final ForumThreadComment comment;

    public UpdateMessageComposer(int guildId, int threadId, ForumThreadComment comment) {
        this.guildId = guildId;
        this.threadId = threadId;
        this.comment = comment;
    }

    @Override
    protected ServerMessage composeInternal() {
        this.response.init(Outgoing.UpdateMessage);

        this.response.appendInt(this.guildId); //guild_id
        this.response.appendInt(this.threadId); //thread_id
        this.comment.serialize(this.response); //Comment

        return this.response;
    }
}