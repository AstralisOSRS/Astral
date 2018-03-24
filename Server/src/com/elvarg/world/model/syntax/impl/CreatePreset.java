package com.elvarg.world.model.syntax.impl;

import com.elvarg.util.Misc;
import com.elvarg.world.content.Presetables;
import com.elvarg.world.entity.impl.player.Player;
import com.elvarg.world.model.Item;
import com.elvarg.world.model.Presetable;
import com.elvarg.world.model.Skill;
import com.elvarg.world.model.syntax.EnterSyntax;

public class CreatePreset implements EnterSyntax {

	private int presetIndex;

	public CreatePreset(final int presetIndex) {
		this.presetIndex = presetIndex;
	}

	@Override
	public void handleSyntax(Player player, String input) {

		player.getPacketSender().sendInterfaceRemoval();

		input = Misc.formatText(input);

		if(!Misc.isValidName(input)) {
			player.getPacketSender().sendMessage("Invalid name for preset.");
			player.setCurrentPreset(null);
			Presetables.open(player);
			return;
		}

		if(player.getPresets()[presetIndex] == null) {

			//Get stats..
			int[] stats = new int[7];
			for(int i = 0; i < stats.length; i++) {
				stats[i] = player.getSkillManager().getMaxLevel(Skill.values()[i]);
			}
			
			Item[] inventory = player.getInventory().copyValidItemsArray();
			Item[] equipment = player.getEquipment().copyValidItemsArray();
			for(Item t : Misc.concat(inventory, equipment)) {
				if(t.getDefinition().isNoted()) {
					player.getPacketSender().sendMessage("You cannot create presets which contain noted items.");
					return;
				}
			}
			player.getPresets()[presetIndex] = new Presetable(input, presetIndex, inventory, equipment, 
					stats, player.getSpellbook(), false);
			player.setCurrentPreset(player.getPresets()[presetIndex]);

			Presetables.open(player);
		}
	}

	@Override
	public void handleSyntax(Player player, int input) {
	}

}
