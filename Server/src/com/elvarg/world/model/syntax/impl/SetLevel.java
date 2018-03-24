package com.elvarg.world.model.syntax.impl;

import com.elvarg.world.entity.impl.player.Player;
import com.elvarg.world.model.Skill;
import com.elvarg.world.model.syntax.EnterSyntax;

public class SetLevel implements EnterSyntax {
	
	private Skill skill;
	public SetLevel(Skill skill) {
		this.skill = skill;
	}

	@Override
	public void handleSyntax(Player player, String input) {
		
	}

	@Override
	public void handleSyntax(Player player, int input) {
		if(input <= 0 || input > 99) {
			player.getPacketSender().sendMessage("Invalid syntax. Please enter a level in the range of 1-99.");
			return;
		}
		player.getSkillManager().setLevel(skill, input);
	}
}
