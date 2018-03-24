package com.elvarg.world.entity.combat.method.impl.specials;

import com.elvarg.world.entity.combat.CombatSpecial;
import com.elvarg.world.entity.combat.CombatType;
import com.elvarg.world.entity.combat.hit.PendingHit;
import com.elvarg.world.entity.combat.method.CombatMethod;
import com.elvarg.world.entity.impl.Character;
import com.elvarg.world.model.Animation;
import com.elvarg.world.model.Graphic;
import com.elvarg.world.model.Priority;

public class SaradominSwordCombatMethod implements CombatMethod {

	private static final Graphic ENMEMY_GRAPHIC = new Graphic(1196, Priority.HIGH);
	private static final Animation ANIMATION = new Animation(1132, Priority.HIGH);
	private static final Graphic GRAPHIC = new Graphic(1213, Priority.HIGH);

	@Override
	public CombatType getCombatType() {
		return CombatType.MELEE;
	}

	@Override
	public PendingHit[] getHits(Character character, Character target) {
		PendingHit qHit = new PendingHit(character, target, this, true, 2, 0);

		/* SARA SWORD SPECIAL FORMULA */
		
		if(!qHit.isAccurate()) {
			qHit.getHits()[1].setDamage(0);
		} else {
			qHit.getHits()[1].setDamage(qHit.getHits()[0].getDamage() + 16);
		}
		
		return new PendingHit[]{qHit};
	}

	@Override
	public boolean canAttack(Character character, Character target) {
		return true;
	}

	@Override
	public void preQueueAdd(Character character, Character target) {
		CombatSpecial.drain(character, CombatSpecial.SARADOMIN_SWORD.getDrainAmount());
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
		hit.getTarget().performGraphic(ENMEMY_GRAPHIC);
	}
}