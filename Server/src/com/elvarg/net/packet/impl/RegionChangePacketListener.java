package com.elvarg.net.packet.impl;

import com.elvarg.net.packet.Packet;
import com.elvarg.net.packet.PacketListener;
import com.elvarg.world.collision.region.RegionClipping;
import com.elvarg.world.entity.impl.npc.NpcAggression;
import com.elvarg.world.entity.impl.object.ObjectHandler;
import com.elvarg.world.entity.impl.player.Player;
import com.elvarg.world.grounditems.GroundItemManager;


public class RegionChangePacketListener implements PacketListener {

	@Override
	public void handleMessage(Player player, Packet packet) {
		
		if(player == null || player.getHitpoints() <= 0) {
			return;
		}
		
		if(player.isAllowRegionChangePacket()) {
			RegionClipping.loadRegion(player.getPosition().getX(), player.getPosition().getY());
			ObjectHandler.onRegionChange(player);
			GroundItemManager.onRegionChange(player);
			player.getAggressionTolerance().start(NpcAggression.NPC_TOLERANCE_SECONDS); //Every 4 minutes, reset aggression for npcs in the region.
			player.setRegionChange(false).setAllowRegionChangePacket(false);
		}
	}
}
