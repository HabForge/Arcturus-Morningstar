package com.eu.habbo.plugin.events.inventory;

import com.eu.habbo.habbohotel.users.HabboInventory;
import com.eu.habbo.habbohotel.users.HabboItem;

public class InventoryItemAddedEvent extends InventoryItemEvent
{
    public InventoryItemAddedEvent(HabboInventory inventory, HabboItem item)
    {
        super(inventory, item);
    }
}
