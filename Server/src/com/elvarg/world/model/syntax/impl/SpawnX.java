package com.elvarg.world.model.syntax.impl;

import com.elvarg.net.packet.impl.SpawnItemPacketListener;
import com.elvarg.world.entity.impl.player.Player;
import com.elvarg.world.model.syntax.EnterSyntax;

public class SpawnX implements EnterSyntax {
	
	private int item_id;
	private boolean toBank;
	
	public SpawnX(int item_id, boolean toBank) {
		this.item_id = item_id;
		this.toBank = toBank;
	}

	@Override
	public void handleSyntax(Player player, String input) {		
	}

	@Override
	public void handleSyntax(Player player, int input) {
		if(input <= 0 || input > 100000) {
			player.getPacketSender().sendMessage("You can spawn a minimum of 0 and a maximum of 100,000");
			return;
		}
		SpawnItemPacketListener.spawn(player, item_id, input, toBank);
	}
}
