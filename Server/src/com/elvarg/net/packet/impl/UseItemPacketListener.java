package com.elvarg.net.packet.impl;

import com.elvarg.net.packet.Packet;
import com.elvarg.net.packet.PacketConstants;
import com.elvarg.net.packet.PacketListener;
import com.elvarg.world.World;
import com.elvarg.world.collision.region.RegionClipping;
import com.elvarg.world.entity.combat.CombatFactory;
import com.elvarg.world.entity.impl.npc.NPC;
import com.elvarg.world.entity.impl.object.GameObject;
import com.elvarg.world.entity.impl.player.Player;
import com.elvarg.world.model.Item;
import com.elvarg.world.model.Position;


public class UseItemPacketListener implements PacketListener {

	private static void itemOnItem(Player player, Packet packet) {
		int usedWithSlot = packet.readUnsignedShort();
		int itemUsedSlot = packet.readUnsignedShortA();
		if (usedWithSlot < 0 || itemUsedSlot < 0
				|| itemUsedSlot > player.getInventory().capacity()
				|| usedWithSlot > player.getInventory().capacity())
			return;
		Item used = player.getInventory().getItems()[itemUsedSlot];
		Item usedWith = player.getInventory().getItems()[usedWithSlot];

		//Granite clamp on Granite maul
		if(used.getId() == 12849 || usedWith.getId() == 12849) {
			if(used.getId() == 4153 || usedWith.getId() == 4153) {
				if(player.busy() || CombatFactory.inCombat(player)) {
					player.getPacketSender().sendMessage("You cannot do that right now.");
					return;
				}
				if(player.getInventory().contains(4153)) {
					player.getInventory().delete(4153, 1).delete(12849, 1).add(12848, 1);
					player.getPacketSender().sendMessage("You attach your Granite clamp onto the maul..");
				}
				return;
			}
		}
		//Blowpipe reload
		else if(used.getId() == 12926 || usedWith.getId() == 12926) {
			int reload = used.getId() == 12926 ? usedWith.getId() : used.getId();
			//Zulrah scales
			if(reload == 12934) {
				final int amount = player.getInventory().getAmount(12934);
				player.incrementBlowpipeScales(amount);
				player.getInventory().delete(12934, amount);
				player.getPacketSender().sendMessage("You now have "+player.getBlowpipeScales()+" Zulrah scales in your blowpipe.");
			} else {
				player.getPacketSender().sendMessage("You cannot load the blowpipe with that!");
			}
		}
	}

	private static void itemOnNpc(final Player player, Packet packet) {
		final int id = packet.readShortA();
		final int index = packet.readShortA();
		final int slot = packet.readLEShort();
		if(index < 0 || index > World.getNpcs().capacity()) {
			return;
		}
		if(slot < 0 || slot > player.getInventory().getItems().length) {
			return;
		}
		NPC npc = World.getNpcs().get(index);
		if(npc == null) {
			return;
		}
		if(player.getInventory().getItems()[slot].getId() != id) {
			return;
		}
		switch(id) {

		}
	}

	@SuppressWarnings("unused")
	private static void itemOnObject(Player player, Packet packet) {
		int interfaceType = packet.readShort();
		final int objectId = packet.readShort();
		final int objectY = packet.readLEShortA();
		final int itemSlot = packet.readLEShort();
		final int objectX = packet.readLEShortA();
		final int itemId = packet.readShort();
		if (itemSlot < 0 || itemSlot > player.getInventory().capacity())
			return;
		final Item item = player.getInventory().getItems()[itemSlot];
		if (item == null)
			return;
		final GameObject gameObject = new GameObject(objectId, new Position(
				objectX, objectY, player.getPosition().getZ()));
		if(!RegionClipping.objectExists(gameObject)) {
			//	player.getPacketSender().sendMessage("An error occured. Error code: "+id).sendMessage("Please report the error to a staff member.");
			return;
		}
		if (item.getDefinition().isNoted() && gameObject.getDefinition().getName().toLowerCase().contains("bank")) {
            player.getInventory().unNoteItem(item);
            return;
       }
	}

	@SuppressWarnings("unused")
	private static void itemOnPlayer(Player player, Packet packet) {
		int interfaceId = packet.readUnsignedShortA();
		int targetIndex = packet.readUnsignedShort();
		int itemId = packet.readUnsignedShort();
		int slot = packet.readLEShort();
		if (slot < 0 || slot > player.getInventory().capacity() || targetIndex > World.getPlayers().capacity())
			return;
		Player target = World.getPlayers().get(targetIndex);
		if(target == null) {
			return;
		}
	}


	@Override
	public void handleMessage(Player player, Packet packet) {
		if (player.getHitpoints() <= 0)
			return;
		switch (packet.getOpcode()) {
		case PacketConstants.ITEM_ON_ITEM:
			itemOnItem(player, packet);
			break;
		case PacketConstants.ITEM_ON_OBJECT:
			itemOnObject(player, packet);
			break;
		case PacketConstants.ITEM_ON_GROUND_ITEM:
			// TODO
			break;
		case PacketConstants.ITEM_ON_NPC:
			itemOnNpc(player, packet);
			break;
		case PacketConstants.ITEM_ON_PLAYER:
			itemOnPlayer(player, packet);
			break;
		}
	}
}