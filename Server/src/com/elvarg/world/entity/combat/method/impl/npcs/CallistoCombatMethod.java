package com.elvarg.world.entity.combat.method.impl.npcs;

import com.elvarg.engine.task.TaskManager;
import com.elvarg.engine.task.impl.ForceMovementTask;
import com.elvarg.util.Misc;
import com.elvarg.world.entity.combat.CombatFactory;
import com.elvarg.world.entity.combat.CombatType;
import com.elvarg.world.entity.combat.hit.PendingHit;
import com.elvarg.world.entity.combat.method.CombatMethod;
import com.elvarg.world.entity.impl.Character;
import com.elvarg.world.entity.impl.player.Player;
import com.elvarg.world.model.Animation;
import com.elvarg.world.model.ForceMovement;
import com.elvarg.world.model.Graphic;
import com.elvarg.world.model.GraphicHeight;
import com.elvarg.world.model.Position;
import com.elvarg.world.model.Projectile;
import com.elvarg.world.model.SecondsTimer;

/**
 * Handles Callisto's combat.
 * @author Professor Oak
 */
public class CallistoCombatMethod implements CombatMethod {

	private static final Animation MELEE_ATTACK_ANIMATION = new Animation(4925);
	private static final Graphic END_PROJECTILE_GRAPHIC = new Graphic(359, GraphicHeight.HIGH);

	private SecondsTimer comboTimer = new SecondsTimer();
	private CombatType currentAttackType = CombatType.MELEE;

	@Override
	public CombatType getCombatType() {
		return currentAttackType;
	}

	@Override
	public boolean canAttack(Character character, Character target) {
		return true;
	}

	@Override
	public PendingHit[] getHits(Character character, Character target) {
		return new PendingHit[]{new PendingHit(character, target, this, true, 2)};
	}

	@Override
	public void preQueueAdd(Character character, Character target) {
		if(currentAttackType == CombatType.MAGIC) {
			new Projectile(character, target, 395, 40, 60, 31, 43, 0).sendProjectile();
		}
	}

	@Override
	public int getAttackSpeed(Character character) {
		return character.getBaseAttackSpeed();
	}

	@Override
	public int getAttackDistance(Character character) {
		return 4;
	}

	@Override
	public void startAnimation(Character character) {
		character.performAnimation(MELEE_ATTACK_ANIMATION);
	}

	@Override
	public void finished(Character character) {

		currentAttackType = CombatType.MELEE;

		//Switch attack to magic randomly
		if(comboTimer.finished()) {
			if(Misc.getRandom(10) <= 2) {
				comboTimer.start(5);
				currentAttackType = CombatType.MAGIC;
				character.getCombat().setDisregardDelay(true);
				character.getCombat().doCombat();
			}
		}
	}

	@Override
	public void handleAfterHitEffects(PendingHit hit) {
		if(hit.getTarget() == null || !hit.getTarget().isPlayer()) {
			return;
		}

		final Player player = hit.getTarget().getAsPlayer();

		if(currentAttackType == CombatType.MAGIC) {
			player.performGraphic(END_PROJECTILE_GRAPHIC);
		}

		//Stun player 15% chance
		if(!player.getCombat().isStunned() && Misc.getRandom(100) <= 10) {
			player.performAnimation(new Animation(3131));
			final Position toKnock = new Position(player.getPosition().getX() > 3325 ? -3 : 1 + Misc.getRandom(2), player.getPosition().getY() > 3834 && player.getPosition().getY() < 3843 ? 3 : -3);
			TaskManager.submit(new ForceMovementTask(player, 3, new ForceMovement(player.getPosition().copy(), toKnock, 0, 15, 0, 0)));
			CombatFactory.stun(player, 4);
		}
	}
}
