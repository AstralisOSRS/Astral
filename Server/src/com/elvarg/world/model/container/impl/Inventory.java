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

public class Inventory extends ItemContainer {

	/**
	 * The Inventory constructor.
	 * @param player	The player who's inventory is being represented.
	 */
	public Inventory(Player player) {
		super(player);
	}

	@Override
	public int capacity() {
		return 28;
	}

	public void unNoteItem(Item item) {
        int unNotedId = item.getDefinition().getNoteId();
        int originalAmount = item.getAmount();
        if(!getPlayer().getInventory().isFull()) {
            int amountToUnnote = 0;
            if(originalAmount > getPlayer().getInventory().getFreeSlots())
                amountToUnnote = getPlayer().getInventory().getFreeSlots();
            else
                amountToUnnote = originalAmount;
            
            if(amountToUnnote > 0)
                getPlayer().getInventory().delete(item.getId(), amountToUnnote, true);
                getPlayer().getInventory().add(unNotedId, amountToUnnote);
            	  getPlayer().getPacketSender().sendMessage("You withdraw " + (amountToUnnote) + " " + item.getDefinition().getName() + (amountToUnnote > 0 ? "s" : "") + ".");
        } else
            getPlayer().getPacketSender().sendMessage("You don't have enough space in your inventory.");
    }
	
	@Override
	public StackType stackType() {
		return StackType.DEFAULT;
	}

	@Override
	public Inventory refreshItems() {
		getPlayer().getPacketSender().sendItemContainer(this, INTERFACE_ID);
		return this;
	}

	@Override
	public Inventory full() {
		getPlayer().getPacketSender().sendMessage("Not enough space in your inventory.");
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

	public static final int INTERFACE_ID = 3214;
}
