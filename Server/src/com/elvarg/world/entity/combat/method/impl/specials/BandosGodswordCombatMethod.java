package com.elvarg.world.entity.combat.method.impl.specials;

import com.elvarg.util.Misc;
import com.elvarg.world.entity.combat.CombatSpecial;
import com.elvarg.world.entity.combat.CombatType;
import com.elvarg.world.entity.combat.hit.PendingHit;
import com.elvarg.world.entity.combat.method.CombatMethod;
import com.elvarg.world.entity.impl.Character;
import com.elvarg.world.entity.impl.player.Player;
import com.elvarg.world.model.Animation;
import com.elvarg.world.model.Graphic;
import com.elvarg.world.model.Priority;
import com.elvarg.world.model.Skill;

public class BandosGodswordCombatMethod implements CombatMethod {

	private static final Animation ANIMATION = new Animation(7060, Priority.HIGH);
	private static final Graphic GRAPHIC = new Graphic(1212, Priority.HIGH);
	
	@Override
	public CombatType getCombatType() {
		return CombatType.MELEE;
	}

	@Override
	public PendingHit[] getHits(Character character, Character target) {
		return new PendingHit[]{new PendingHit(character, target, this, true, 0)};
	}

	@Override
	public boolean canAttack(Character character, Character target) {
		return true;
	}

	@Override
	public void preQueueAdd(Character character, Character target) {
		CombatSpecial.drain(character, CombatSpecial.BANDOS_GODSWORD.getDrainAmount());
	}

	@Override
	public int getAttackSpeed(Character character) {
		return character.getBaseAttackSpeed();
	}

	@Override
	public int getAttackDistance(Character character) {
		return 1;
	}

	@Override
	public void startAnimation(Character character) {
		character.performAnimation(ANIMATION);
		character.performGraphic(GRAPHIC);
	}

	@Override
	public void finished(Character character) {

	}

	@Override
	public void handleAfterHitEffects(PendingHit hit) {
		if(hit.isAccurate() && hit.getTarget().isPlayer()) {
			int skillDrain = 1;
			int damageDrain = (int) (hit.getTotalDamage() * 0.1);
			if(damageDrain < 0)
				return;
			Player player = hit.getAttacker().getAsPlayer();
			Player target = hit.getTarget().getAsPlayer();
			final Skill skill = Skill.values()[skillDrain];
			target.getSkillManager().setCurrentLevel(skill, player.getSkillManager().getCurrentLevel(skill) - damageDrain);
			if(target.getSkillManager().getCurrentLevel(skill) < 1)
				target.getSkillManager().setCurrentLevel(skill, 1);
			player.getPacketSender().sendMessage("You've drained "+target.getUsername()+"'s "+Misc.formatText(Skill.values()[skillDrain].toString().toLowerCase())+" level by "+damageDrain+".");
			target.getPacketSender().sendMessage("Your "+skill.getName()+" level has been drained.");
		}
	}
}