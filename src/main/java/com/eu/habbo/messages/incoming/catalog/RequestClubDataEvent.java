package com.eu.habbo.messages.incoming.catalog;

import com.eu.habbo.messages.incoming.MessageHandler;
import com.eu.habbo.messages.outgoing.unknown.ClubDataComposer;

public class RequestClubDataEvent extends MessageHandler
{
    @Override
    public void handle() throws Exception
    {
        this.client.sendResponse(new ClubDataComposer(this.client.getHabbo(), this.packet.readInt()));
    }
}
