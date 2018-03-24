package com.elvarg.net.packet.impl;

import com.elvarg.net.packet.Packet;
import com.elvarg.net.packet.PacketListener;
import com.elvarg.world.entity.impl.player.Player;
import com.elvarg.world.model.PlayerRelations.PrivateChatStatus;

public class ChatSettingsPacketListener implements PacketListener {


	@Override
	public void handleMessage(Player player, Packet packet) {
		@SuppressWarnings("unused")
		int publicMode = packet.readByte();
		
		int privateMode = packet.readByte();
		
		@SuppressWarnings("unused")
		int tradeMode = packet.readByte();
		
		if(player == null || player.getHitpoints() <= 0) {
			return;
		}
		
		/*
		 * Did the player change their private chat status? 
		 * If yes, update status for all friends.
		 */
		
		if(privateMode > PrivateChatStatus.values().length) {
			return;
		}
		
		PrivateChatStatus privateChatStatus = PrivateChatStatus.values()[privateMode];
		if(player.getRelations().getStatus() != privateChatStatus) {
			player.getRelations().setStatus(privateChatStatus, true);
		}
	}
}
