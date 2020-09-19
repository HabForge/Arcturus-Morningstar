package com.eu.habbo.habbohotel.items.interactions.wired.conditions;

import com.eu.habbo.Emulator;
import com.eu.habbo.habbohotel.bots.Bot;
import com.eu.habbo.habbohotel.items.Item;
import com.eu.habbo.habbohotel.items.interactions.InteractionWiredCondition;
import com.eu.habbo.habbohotel.pets.Pet;
import com.eu.habbo.habbohotel.rooms.Room;
import com.eu.habbo.habbohotel.rooms.RoomTile;
import com.eu.habbo.habbohotel.rooms.RoomUnit;
import com.eu.habbo.habbohotel.users.Habbo;
import com.eu.habbo.habbohotel.users.HabboItem;
import com.eu.habbo.habbohotel.wired.WiredConditionType;
import com.eu.habbo.habbohotel.wired.WiredHandler;
import com.eu.habbo.messages.ClientMessage;
import com.eu.habbo.messages.ServerMessage;
import gnu.trove.map.hash.THashMap;
import gnu.trove.set.hash.THashSet;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collection;
import java.util.Map;

public class WiredConditionNotFurniHaveHabbo extends InteractionWiredCondition {
    public static final WiredConditionType type = WiredConditionType.NOT_FURNI_HAVE_HABBO;

    protected boolean all;
    protected THashSet<HabboItem> items;

    public WiredConditionNotFurniHaveHabbo(ResultSet set, Item baseItem) throws SQLException {
        super(set, baseItem);
        this.items = new THashSet<>();
    }

    public WiredConditionNotFurniHaveHabbo(int id, int userId, Item item, String extradata, int limitedStack, int limitedSells) {
        super(id, userId, item, extradata, limitedStack, limitedSells);
        this.items = new THashSet<>();
    }

    @Override
    public void onPickUp() {
        this.items.clear();
        this.all = false;
    }

    @Override
    public boolean execute(RoomUnit roomUnit, Room room, Object[] stuff) {
        this.refresh();

        if (this.items.isEmpty())
            return true;

        THashMap<HabboItem, THashSet<RoomTile>> tiles = new THashMap<>();
        for (HabboItem item : this.items) {
            tiles.put(item, room.getLayout().getTilesAt(room.getLayout().getTile(item.getX(), item.getY()), item.getBaseItem().getWidth(), item.getBaseItem().getLength(), item.getRotation()));
        }

        Collection<Habbo> habbos = room.getHabbos();
        Collection<Bot> bots = room.getCurrentBots().valueCollection();
        Collection<Pet> pets = room.getCurrentPets().valueCollection();

        for (Map.Entry<HabboItem, THashSet<RoomTile>> set : tiles.entrySet()) {
            if (!habbos.isEmpty()) {
                for (Habbo habbo : habbos) {
                    if (set.getValue().contains(habbo.getRoomUnit().getCurrentLocation())) {
                        return false;
                    }
                }
            }

            if (!bots.isEmpty()) {
                for (Bot bot : bots) {
                    if (set.getValue().contains(bot.getRoomUnit().getCurrentLocation())) {
                        return false;
                    }
                }
            }

            if (!pets.isEmpty()) {
                for (Pet pet : pets) {
                    if (set.getValue().contains(pet.getRoomUnit().getCurrentLocation())) {
                        return false;
                    }
                }
            }

        }

        return true;
    }

    @Override
    public String getWiredData() {
        this.refresh();

        StringBuilder data = new StringBuilder((this.all ? "1" : "0") + ":");

        for (HabboItem item : this.items) {
            data.append(item.getId()).append(";");
        }

        return data.toString();
    }

    @Override
    public void loadWiredData(ResultSet set, Room room) throws SQLException {
        this.items.clear();

        String[] data = set.getString("wired_data").split(":");

        if (data.length >= 1) {
            this.all = (data[0].equals("1"));

            if (data.length == 2) {
                String[] items = data[1].split(";");

                for (String s : items) {
                    HabboItem item = room.getHabboItem(Integer.valueOf(s));

                    if (item != null)
                        this.items.add(item);
                }
            }
        }
    }

    @Override
    public WiredConditionType getType() {
        return type;
    }

    @Override
    public void serializeWiredData(ServerMessage message, Room room) {
        this.refresh();

        message.appendBoolean(false);
        message.appendInt(WiredHandler.MAXIMUM_FURNI_SELECTION);
        message.appendInt(this.items.size());

        for (HabboItem item : this.items)
            message.appendInt(item.getId());

        message.appendInt(this.getBaseItem().getSpriteId());
        message.appendInt(this.getId());
        message.appendString("");
        message.appendInt(1);
        message.appendInt(this.all ? 1 : 0);
        message.appendInt(0);
        message.appendInt(this.getType().code);
        message.appendInt(0);
        message.appendInt(0);
    }

    @Override
    public boolean saveData(ClientMessage packet) {
        packet.readInt();

        packet.readString();

        int count = packet.readInt();
        if (count > Emulator.getConfig().getInt("hotel.wired.furni.selection.count")) return false;

        this.items.clear();

        Room room = Emulator.getGameEnvironment().getRoomManager().getRoom(this.getRoomId());

        if (room != null) {
            for (int i = 0; i < count; i++) {
                HabboItem item = room.getHabboItem(packet.readInt());

                if (item != null)
                    this.items.add(item);
            }

            return true;
        }

        return false;
    }

    private void refresh() {
        THashSet<HabboItem> items = new THashSet<>();

        Room room = Emulator.getGameEnvironment().getRoomManager().getRoom(this.getRoomId());
        if (room == null) {
            items.addAll(this.items);
        } else {
            for (HabboItem item : this.items) {
                if (room.getHabboItem(item.getId()) == null)
                    items.add(item);
            }
        }

        for (HabboItem item : items) {
            this.items.remove(item);
        }
    }
}
