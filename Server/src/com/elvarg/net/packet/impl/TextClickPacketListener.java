package com.elvarg.net.packet.impl;

import com.elvarg.net.packet.Packet;
import com.elvarg.net.packet.PacketListener;
import com.elvarg.world.content.Presetables;
import com.elvarg.world.content.clan.ClanChatManager;
import com.elvarg.world.entity.impl.player.Player;
import com.elvarg.world.model.container.impl.Bank;

public class TextClickPacketListener implements PacketListener {

	@Override
	public void handleMessage(Player player, Packet packet) {
		int frame = packet.readInt();
		int action = packet.readByte();
		
		if(player == null || player.getHitpoints() <= 0) {
			return;
		}
		
		if(Bank.handleButton(player, frame, action)) {
			return;
		}
		if(ClanChatManager.handleButton(player, frame, action)) {
			return;
		}
		if(Presetables.handleButton(player, frame)) {
			return;
		}
	}
}
