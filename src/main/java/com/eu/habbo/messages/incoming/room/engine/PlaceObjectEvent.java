package com.eu.habbo.messages.incoming.room.engine;

import com.eu.habbo.habbohotel.items.FurnitureType;
import com.eu.habbo.habbohotel.items.interactions.*;
import com.eu.habbo.habbohotel.modtool.ScripterManager;
import com.eu.habbo.habbohotel.rooms.FurnitureMovementError;
import com.eu.habbo.habbohotel.rooms.Room;
import com.eu.habbo.habbohotel.rooms.RoomLayout;
import com.eu.habbo.habbohotel.rooms.RoomTile;
import com.eu.habbo.habbohotel.users.HabboItem;
import com.eu.habbo.messages.incoming.MessageHandler;
import com.eu.habbo.habbohotel.notifications.BubbleAlertKeys;
import com.eu.habbo.messages.outgoing.inventory.furni.FurniListRemoveComposer;
import com.eu.habbo.messages.outgoing.notifications.NotificationDialogComposer;

public class PlaceObjectEvent extends MessageHandler {
    @Override
    public void handle() throws Exception {
        String[] values = this.packet.readString().split(" ");

        int itemId = -1;

        if (values.length != 0) itemId = Math.abs(Integer.parseInt(values[0]));

        if (!this.client.getHabbo().getRoomUnit().isInRoom()) {
            this.client.sendResponse(new NotificationDialogComposer(BubbleAlertKeys.FURNITURE_PLACEMENT_ERROR.key, FurnitureMovementError.NO_RIGHTS.errorCode));
            return;
        }

        Room room = this.client.getHabbo().getHabboInfo().getCurrentRoom();
        if (room == null) {
            return;
        }

        HabboItem rentSpace = null;
        if (this.client.getHabbo().getHabboStats().isRentingSpace()) {
            rentSpace = room.getHabboItem(this.client.getHabbo().getHabboStats().rentedItemId);
        }

        HabboItem item = this.client.getHabbo().getInventory().getItemsComponent().getHabboItem(itemId);

        if (item == null || item.getBaseItem().getInteractionType().getType() == InteractionPostIt.class)
            return;

        if (room.getId() != item.getRoomId() && item.getRoomId() != 0)
            return;

        //TODO move this to canStackAt() though find a way to handle the different bubble alert keys
        if (item instanceof InteractionMoodLight && !room.getRoomSpecialTypes().getItemsOfType(InteractionMoodLight.class).isEmpty()) {
            this.client.sendResponse(new NotificationDialogComposer(BubbleAlertKeys.FURNITURE_PLACEMENT_ERROR.key, FurnitureMovementError.MAX_DIMMERS.errorCode));
            return;
        }
        if (item instanceof InteractionJukeBox && !room.getRoomSpecialTypes().getItemsOfType(InteractionJukeBox.class).isEmpty()) {
            this.client.sendResponse(new NotificationDialogComposer(BubbleAlertKeys.FURNITURE_PLACEMENT_ERROR.key, FurnitureMovementError.MAX_SOUNDFURNI.errorCode));
            return;
        }

        if (item.getBaseItem().getType() == FurnitureType.FLOOR) {
            short x = Short.parseShort(values[1]);
            short y = Short.parseShort(values[2]);
            int rotation = Integer.parseInt(values[3]);

            RoomTile tile = room.getLayout().getTile(x, y);

            if(tile == null)
            {
                String userName  = this.client.getHabbo().getHabboInfo().getUsername();
                int roomId = room.getId();
                ScripterManager.scripterDetected(this.client, "User [" + userName + "] tried to place a furni with itemId [" + itemId + "] at a tile which is not existing in room [" + roomId + "], tile: [" + x + "," + y + "]");
                return;
            }

            HabboItem buildArea = null;
            for (HabboItem area : room.getRoomSpecialTypes().getItemsOfType(InteractionBuildArea.class)) {
                if (((InteractionBuildArea) area).inSquare(tile)) {
                    buildArea = area;
                }
            }

            if ((rentSpace != null || buildArea != null) && !room.hasRights(this.client.getHabbo())) {
                if (item instanceof InteractionRoller ||
                        item instanceof InteractionStackHelper ||
                        item instanceof InteractionWired ||
                        item instanceof InteractionBackgroundToner ||
                        item instanceof InteractionRoomAds ||
                        item instanceof InteractionCannon ||
                        item instanceof InteractionPuzzleBox ||
                        item.getBaseItem().getType() == FurnitureType.WALL) {
                    this.client.sendResponse(new NotificationDialogComposer(BubbleAlertKeys.FURNITURE_PLACEMENT_ERROR.key, FurnitureMovementError.NO_RIGHTS.errorCode));
                    return;
                }
                if (rentSpace != null && !RoomLayout.squareInSquare(RoomLayout.getRectangle(rentSpace.getX(), rentSpace.getY(), rentSpace.getBaseItem().getWidth(), rentSpace.getBaseItem().getLength(), rentSpace.getRotation()), RoomLayout.getRectangle(x, y, item.getBaseItem().getWidth(), item.getBaseItem().getLength(), rotation))) {
                    this.client.sendResponse(new NotificationDialogComposer(BubbleAlertKeys.FURNITURE_PLACEMENT_ERROR.key, FurnitureMovementError.NO_RIGHTS.errorCode));
                    return;
                }
            }
            FurnitureMovementError error = room.canPlaceFurnitureAt(item, this.client.getHabbo(), tile, rotation);

            if (!error.equals(FurnitureMovementError.NONE)) {
                this.client.sendResponse(new NotificationDialogComposer(BubbleAlertKeys.FURNITURE_PLACEMENT_ERROR.key, error.errorCode));
                return;
            }

            error = room.placeFloorFurniAt(item, tile, rotation, this.client.getHabbo());
            if (!error.equals(FurnitureMovementError.NONE)) {
                this.client.sendResponse(new NotificationDialogComposer(BubbleAlertKeys.FURNITURE_PLACEMENT_ERROR.key, error.errorCode));
                return;
            }
        } else {
            FurnitureMovementError error = room.placeWallFurniAt(item, values[1] + " " + values[2] + " " + values[3], this.client.getHabbo());
            if (!error.equals(FurnitureMovementError.NONE)) {
                this.client.sendResponse(new NotificationDialogComposer(BubbleAlertKeys.FURNITURE_PLACEMENT_ERROR.key, error.errorCode));
                return;
            }
        }

        this.client.sendResponse(new FurniListRemoveComposer(item.getGiftAdjustedId()));
        this.client.getHabbo().getInventory().getItemsComponent().removeHabboItem(item.getId());
        item.setFromGift(false);
    }
}
