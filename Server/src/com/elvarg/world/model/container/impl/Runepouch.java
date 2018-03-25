package com.elvarg.world.model.container.impl;

import java.util.Optional;

import com.elvarg.world.entity.impl.player.Player;
import com.elvarg.world.model.Item;
import com.elvarg.world.model.container.ItemContainer;
import com.elvarg.world.model.container.StackType;

/**
 * Represents a player's inventory item container.
 * 
 * @author relex lawl
 */

public class Runepouch extends ItemContainer {

    /**
     * The Inventory constructor.
     * @param player    The player who's inventory is being represented.
     */
    public Runepouch(Player player) {
        super(player);
    }

    @Override
    public int capacity() {
        return 3;
    }

    @Override
    public StackType stackType() {
        return StackType.DEFAULT;
    }

    @Override
    public Runepouch refreshItems() {
        getPlayer().getPacketSender().sendItemContainer(this, INTERFACE_ID);
        return this;
    }

    @Override
    public Runepouch full() {
        getPlayer().getPacketSender().sendMessage("Not enough space in your RunePouch.");
        return this;
    }

    /**
     * Adds a set of items into the inventory.
     *
     * @param item
     * the set of items to add.
     */
    public void addItemSet(Item[] item) {
        for (Item addItem : item) {
            if (addItem == null) {
                continue;
            }
            add(addItem);
        }
    }

    public static final int INTERFACE_ID = 32900;
}