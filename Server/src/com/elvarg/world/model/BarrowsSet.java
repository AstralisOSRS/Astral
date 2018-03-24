package com.elvarg.world.model;

import java.util.HashMap;
import java.util.Map;

import com.elvarg.world.entity.impl.player.Player;

public enum BarrowsSet {
	
	GUTHANS_SET(12873, 4724, 4726, 4728, 4730),
	VERACS_SET(12875, 4753, 4755, 4757, 4759),
	TORAGS_SET(12879, 4745, 4747, 4749, 4751),
	AHRIMS_SET(12881, 4708, 4710, 4712, 4714),
	KARILS_SET(12883, 4732, 4734, 4736, 4738),
	DHAROKS_SET(12877, 4716, 4718, 4720, 4722),
	ANCESTRAL_SET(21049, 21018, 21021, 21024);
	
	BarrowsSet(int setId, int... items) {
		this.setId = setId;
		this.items = items;
	}
	
	public int getSetId() {
		return setId;
	}

	public int[] getItems() {
		return items;
	}

	private final int setId;
	private final int[] items;
	
	public static boolean pack(Player player, int itemId) {
		BarrowsSet set = sets.get(itemId);
		if(set == null) {
			return false;
		}
		
		if(player.busy()) {
			player.getPacketSender().sendMessage("You cannot do that right now.");
			return true;
		}
		
		for(int i : set.items) {
			if(!player.getInventory().contains(i)) {
				player.getPacketSender().sendMessage("You do not have enough components to make a set out of this armor.");
				return true;
			}
		}
		
		for(int i : set.items) {
			player.getInventory().delete(i, 1);
		}
		
		player.getInventory().add(set.setId, 1);
		
		player.getPacketSender().sendMessage("You've made a set our of your armor.");
		return true;
	}
	
	private static Map<Integer, BarrowsSet> sets = new HashMap<Integer, BarrowsSet>();

	public static BarrowsSet get(int item) {
		return sets.get(item);
	}
	
	static {
		for (BarrowsSet set : BarrowsSet.values()) {
			for(int i : set.items) {
				sets.put(i, set);
			}
			sets.put(set.getSetId(), set);
		}
	}
}
