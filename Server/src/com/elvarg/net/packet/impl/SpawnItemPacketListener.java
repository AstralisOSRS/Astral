package com.elvarg.net.packet.impl;

import com.elvarg.GameConstants;
import com.elvarg.definitions.ItemDefinition;
import com.elvarg.net.packet.Packet;
import com.elvarg.net.packet.PacketListener;
import com.elvarg.world.entity.impl.player.Player;
import com.elvarg.world.model.Locations.Location;
import com.elvarg.world.model.container.impl.Bank;
import com.elvarg.world.model.syntax.impl.SpawnX;

/**
 * This packet listener reads a item spawn request
 * from the spawn tab.
 * @author Professor Oak
 */

public class SpawnItemPacketListener implements PacketListener {

	@Override
	public void handleMessage(Player player, Packet packet) {
		final int item = packet.readInt();
		final boolean spawnX = packet.readByte() == 1;
		final boolean toBank = packet.readByte() == 1;

		if(player == null || player.getHitpoints() <= 0) {
			return;
		}

		ItemDefinition def = ItemDefinition.forId(item);
		if(def == null) {
			player.getPacketSender().sendMessage("This item is currently unavailable.");
			return;
		}

		//Check if player busy..
		if(player.busy() || player.getLocation() == Location.WILDERNESS) {
			player.getPacketSender().sendMessage("You cannot do that right now.");
			return;
		}

		boolean spawnable = false;
		for(int i : GameConstants.ALLOWED_SPAWNS) {
			if(item == i) {
				spawnable = true;
				break;
			}
		}

		if(!spawnable) {
			player.getPacketSender().sendMessage("This item cannot be spawned.");
			return;
		}

		//Spawn item
		if(!spawnX) {
			spawn(player, item, 1, toBank);
		} else {
			player.setEnterSyntax(new SpawnX(item, toBank));
			player.getPacketSender().sendEnterAmountPrompt("How many "+def.getName()+" would you like to spawn?");
		}
	}

	public static void spawn(Player player, int item, int amount, boolean toBank) {
		ItemDefinition def = ItemDefinition.forId(item);
		
		if(amount > 1000000) {
			amount = 1000000;
			player.getPacketSender().sendMessage("You can only spawn 1 millon of that item in one go.");
		}
		
		//Spawn item.
		if(toBank) {
			player.getBank(Bank.getTabForItem(player, item)).add(item, amount);
		} else {

			if(amount > player.getInventory().getFreeSlots()) {
				if(!def.isStackable() && player.getInventory().contains(item)) {
					amount = player.getInventory().getFreeSlots();
				}
			}

			if(amount > 0) {
				player.getInventory().add(item, amount);
			}
		}

		player.getPacketSender().sendMessage("Spawned "+def.getName()+" to " + (toBank ? ("bank") : ("inventory")) + ".");
	}
}
