package com.elvarg.world.model.container.impl;

import com.elvarg.world.entity.impl.player.Player;
import com.elvarg.world.model.Item;
import com.elvarg.world.model.MagicSpellbook;
import com.elvarg.world.model.Locations.Location;
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
    
    public void addBarrageRunes() {
    	if (getPlayer().getLocation() != Location.WILDERNESS && getPlayer().getPouch().isEmpty()) {
    		getPlayer().getPacketSender().sendInterfaceRemoval();
			MagicSpellbook.changeSpellbook(getPlayer(), MagicSpellbook.ANCIENT);
			getPlayer().getPouch().add(565, 100000).add(555, 100000).add(560, 100000);
			getPlayer().getPacketSender().sendMessage("You Fill your pouch with Barrage Runes.");
		} else if (getPlayer().getPouch().contains(565) && getPlayer().getPouch().contains(555) && getPlayer().getPouch().contains(560)) {
			getPlayer().getPacketSender().sendMessage("@red@ You already have Barrage runes inside your runepouch.");
		} else if (getPlayer().getPouch().isFull() && getPlayer().getLocation() != Location.WILDERNESS) {
			getPlayer().getPacketSender().sendMessage("You empty your runepouch.");
			getPlayer().getPouch().resetItems();
		}
    }
    
    public void addTeleBlockRunes() {
    	if (getPlayer().getLocation() != Location.WILDERNESS && getPlayer().getPouch().isEmpty()) {
    		getPlayer().getPacketSender().sendInterfaceRemoval();
			MagicSpellbook.changeSpellbook(getPlayer(), MagicSpellbook.NORMAL);
			getPlayer().getPouch().add(563, 100000).add(562, 100000).add(560, 100000);
			getPlayer().getPacketSender().sendMessage("You Fill your pouch with Teleblock Runes.");
		} else if (getPlayer().getPouch().contains(563) && getPlayer().getPouch().contains(562) && getPlayer().getPouch().contains(560)) {
			getPlayer().getPacketSender().sendMessage("@red@ You already have Teleblock runes inside your runepouch.");
		} else if (getPlayer().getPouch().isFull() && getPlayer().getLocation() != Location.WILDERNESS) {
			getPlayer().getPacketSender().sendMessage("You empty your runepouch.");
			getPlayer().getPouch().resetItems();
		}
    }
    
    public void addVengeanceRunes() {
    	if (getPlayer().getLocation() != Location.WILDERNESS && getPlayer().getPouch().isEmpty()) {
    		getPlayer().getPacketSender().sendInterfaceRemoval();
			MagicSpellbook.changeSpellbook(getPlayer(), MagicSpellbook.LUNAR);
			getPlayer().getPouch().add(9075, 100000).add(557, 100000).add(560, 100000);
			getPlayer().getPacketSender().sendMessage("You Fill your pouch with Vengeance Runes.");
		} else if (getPlayer().getPouch().contains(9075) && getPlayer().getPouch().contains(557) && getPlayer().getPouch().contains(560)) {
			getPlayer().getPacketSender().sendMessage("@red@ You already have Vengeance runes inside your runepouch.");
		} else if (getPlayer().getPouch().isFull() && getPlayer().getLocation() != Location.WILDERNESS) {
			getPlayer().getPacketSender().sendMessage("You empty your runepouch.");
			getPlayer().getPouch().resetItems();
		}
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