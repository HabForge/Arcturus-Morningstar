package com.eu.habbo.messages.outgoing.camera;

import com.eu.habbo.messages.ServerMessage;
import com.eu.habbo.messages.outgoing.MessageComposer;
import com.eu.habbo.messages.outgoing.Outgoing;

public class CameraRoomThumbnailSavedComposer extends MessageComposer
{
    @Override
    public ServerMessage compose()
    {
        this.response.init(Outgoing.CameraRoomThumbnailSavedComposer);
        return this.response;
    }
}