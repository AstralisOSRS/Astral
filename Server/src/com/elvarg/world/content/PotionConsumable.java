package com.elvarg.world.content;

import java.util.Optional;

import com.elvarg.world.content.Dueling.DuelRule;
import com.elvarg.world.entity.impl.player.Player;
import com.elvarg.world.model.Animation;
import com.elvarg.world.model.EffectTimer;
import com.elvarg.world.model.Item;
import com.elvarg.world.model.Skill;
import com.elvarg.world.model.dialogue.DialogueManager;

/**
 * The enumerated type managing consumable potion types.
 *
 * @author Ryley Kimmel <ryley.kimmel@live.com>
 * @author lare96 <http://github.com/lare96>
 * @editor Professor oak
 */
public enum PotionConsumable {
	ANTIFIRE_POTIONS(2452, 2454, 2456, 2458) {
		@Override
		public void onEffect(Player player) {
			PotionConsumable.onAntifireEffect(player, 60 * 6);
		}
	},
	ANTIPOISON_POTIONS(2448, 181, 183, 185) {
		@Override
		public void onEffect(Player player) {
			PotionConsumable.onAntipoisonEffect(player, 60 * 6);
		}
	},
	COMBAT_POTIONS(9739, 9741, 9743, 9745) {
		@Override
		public void onEffect(Player player) {
			PotionConsumable.onBasicEffect(player, Skill.ATTACK, BoostType.LOW);
			PotionConsumable.onBasicEffect(player, Skill.STRENGTH, BoostType.LOW);
		}
	},
	MAGIC_POTIONS(3040, 3042, 3044, 3046) {
		@Override
		public void onEffect(Player player) {
			PotionConsumable.onBasicEffect(player, Skill.MAGIC, BoostType.NORMAL);
		}
	},
	SUPER_MAGIC_POTIONS(11726, 11727, 11728, 11729) {
		@Override
		public void onEffect(Player player) {
			PotionConsumable.onBasicEffect(player, Skill.MAGIC, BoostType.SUPER);
		}
	},
	DEFENCE_POTIONS(2432, 133, 135, 137) {
		@Override
		public void onEffect(Player player) {
			PotionConsumable.onBasicEffect(player, Skill.DEFENCE, BoostType.NORMAL);
		}
	},
	STRENGTH_POTIONS(113, 115, 117, 119) {
		@Override
		public void onEffect(Player player) {
			PotionConsumable.onBasicEffect(player, Skill.STRENGTH, BoostType.NORMAL);
		}
	},
	ATTACK_POTIONS(2428, 121, 123, 125) {
		@Override
		public void onEffect(Player player) {
			PotionConsumable.onBasicEffect(player, Skill.ATTACK, BoostType.NORMAL);
		}
	},
	SUPER_DEFENCE_POTIONS(2442, 163, 165, 167) {
		@Override
		public void onEffect(Player player) {
			PotionConsumable.onBasicEffect(player, Skill.DEFENCE, BoostType.SUPER);
		}
	},
	SUPER_ATTACK_POTIONS(2436, 145, 147, 149) {
		@Override
		public void onEffect(Player player) {
			PotionConsumable.onBasicEffect(player, Skill.ATTACK, BoostType.SUPER);
		}
	},
	SUPER_STRENGTH_POTIONS(2440, 157, 159, 161) {
		@Override
		public void onEffect(Player player) {
			PotionConsumable.onBasicEffect(player, Skill.STRENGTH, BoostType.SUPER);
		}
	},
	RANGE_POTIONS(2444, 169, 171, 173) {
		@Override
		public void onEffect(Player player) {
			PotionConsumable.onBasicEffect(player, Skill.RANGED, BoostType.NORMAL);
		}
	},
	SUPER_RANGE_POTIONS(11722, 11723, 11724, 11725) {
		@Override
		public void onEffect(Player player) {
			PotionConsumable.onBasicEffect(player, Skill.RANGED, BoostType.SUPER);
		}
	},
	ZAMORAK_BREW(2450, 189, 191, 193) {
		@Override
		public void onEffect(Player player) {
			PotionConsumable.onZamorakEffect(player);
		}
	},
	SARADOMIN_BREW(6685, 6687, 6689, 6691) {
		@Override
		public void onEffect(Player player) {
			PotionConsumable.onSaradominEffect(player);
		}
	},
	SUPER_RESTORE_POTIONS(3024, 3026, 3028, 3030) {
		@Override
		public void onEffect(Player player) {
			PotionConsumable.onPrayerEffect(player, true);
			PotionConsumable.onRestoreEffect(player);
		}
	},
	
	SANFEW_SERUM_POTIONS(10925, 10927, 10929, 10931) {
		@Override
		 public void onEffect(Player player) {
            PotionConsumable.onPrayerEffect(player, true);
            PotionConsumable.onRestoreEffect(player);
            PotionConsumable.onAntipoisonEffect(player, 60 * 2);
		}
	},
	
	PRAYER_POTIONS(2434, 139, 141, 143) {
		@Override
		public void onEffect(Player player) {
			PotionConsumable.onPrayerEffect(player, false);
			
		}
	},
	
	SUPER_COMBAT_POTION(12695, 12697, 12699, 12701) {
		@Override
		public void onEffect(Player player) {
			PotionConsumable.onBasicEffect(player, Skill.STRENGTH, BoostType.SUPER);
			PotionConsumable.onBasicEffect(player, Skill.ATTACK, BoostType.SUPER);
			PotionConsumable.onBasicEffect(player, Skill.DEFENCE, BoostType.SUPER);
		}
	},

	;

	/**
	 * The default item representing the final potion dose.
	 */
	private static final int VIAL = 229;

	/**
	 * The identifiers which represent this potion type.
	 */
	private final int[] ids;

	/**
	 * Create a new {@link PotionConsumable}.
	 *
	 * @param ids
	 *            the identifiers which represent this potion type.
	 */
	private PotionConsumable(int... ids) {
		this.ids = ids;
	}

	/**
	 * Attempts to consume {@code item} in {@code slot} for {@code player}.
	 *
	 * @param player
	 *            the player attempting to consume the item.
	 * @param item
	 *            the item being consumed by the player.
	 * @param slot
	 *            the slot the player is consuming from.
	 * @return {@code true} if the item was consumed, {@code false} otherwise.
	 */
	public static boolean consume(Player player, int item, int slot) {
		Optional<PotionConsumable> potion = forId(item);
		// TODO: Check duel rule for no potions.
		if (!potion.isPresent()) {
			return false;
		}
		
		//Duel, disabled drinks?
		if(player.getDueling().inDuel() && player.getDueling().getRules()[DuelRule.NO_POTIONS.ordinal()]) {
			DialogueManager.sendStatement(player, "Drinks have been disabled in this duel!");
			return true;
		}
		
		//Stun
		if(player.getCombat().isStunned()) {
			player.getPacketSender().sendMessage("You're currently stunned and cannot drink.");
			return true;
		}
		
		if(player.getPotionTimer().elapsed(1200)) {
			player.getPacketSender().sendInterfaceRemoval();
			player.getCombat().reset();
			player.getPacketSender().sendMessage("You Drink the " + potion.get().name().replace("_", " ").toLowerCase());
			player.performAnimation(new Animation(829));
			player.getPotionTimer().reset();
			player.getInventory().setItem(slot, getReplacementItem(item)).refreshItems();
			potion.get().onEffect(player);
		}
		return true;
	}

	/**
	 * Retrieves the replacement item for {@code item}.
	 *
	 * @param item
	 *            the item to retrieve the replacement item for.
	 * @return the replacement item wrapped in an optional, or an empty optional
	 *         if no replacement item is available.
	 */
	private static Item getReplacementItem(int item) {
		Optional<PotionConsumable> potion = forId(item);
		if (potion.isPresent()) {
			int length = potion.get().getIds().length;
			for (int index = 0; index < length; index++) {
				if (potion.get().getIds()[index] == item && index + 1 < length) {
					return new Item(potion.get().getIds()[index + 1]);
				}
			}
		}
		return new Item(VIAL);
	}

	/**
	 * Retrieves the potion consumable element for {@code id}.
	 *
	 * @param id
	 *            the id that the potion consumable is attached to.
	 * @return the potion consumable wrapped in an optional, or an empty
	 *         optional if no potion consumable was found.
	 */
	private static Optional<PotionConsumable> forId(int id) {
		for (PotionConsumable potion : PotionConsumable.values()) {
			for (int potionId : potion.getIds()) {
				if (id == potionId) {
					return Optional.of(potion);
				}
			}
		}
		return Optional.empty();
	}

	/**
	 * The method executed when this potion type activated.
	 *
	 * @param player
	 *            the player to execute this effect for.
	 */
	public abstract void onEffect(Player player);

	/**
	 * Gets the identifiers which represent this potion type.
	 *
	 * @return the identifiers for this potion.
	 */
	public final int[] getIds() {
		return ids;
	}


	/**
	 * The method that executes the Saradomin brew action.
	 *
	 * @param player
	 *            the player to do this action for.
	 */
	private static void onSaradominEffect(Player player) {

		player.getSkillManager().increaseCurrentLevelMax(Skill.DEFENCE, (int) Math.floor(2 + (0.120 * 
				player.getSkillManager().getMaxLevel(Skill.DEFENCE))));

		player.getSkillManager().increaseCurrentLevelMax(Skill.HITPOINTS, (int) Math.floor(2 + (0.15 * 
				player.getSkillManager().getMaxLevel(Skill.HITPOINTS))));

		player.getSkillManager().decreaseCurrentLevel(Skill.ATTACK, (int) Math.floor(0.10 * 
				player.getSkillManager().getMaxLevel(Skill.ATTACK)), -1);

		player.getSkillManager().decreaseCurrentLevel(Skill.STRENGTH, (int) Math.floor(0.10 * 
				player.getSkillManager().getCurrentLevel(Skill.STRENGTH)), -1);

		player.getSkillManager().decreaseCurrentLevel(Skill.MAGIC, (int) Math.floor(0.10 * 
				player.getSkillManager().getCurrentLevel(Skill.MAGIC)), -1);

		player.getSkillManager().decreaseCurrentLevel(Skill.RANGED, (int) Math.floor(0.10 * 
				player.getSkillManager().getCurrentLevel(Skill.RANGED)), -1);
	}

	/**
	 * The method that executes the Zamorak brew action.
	 *
	 * @param player
	 *            the player to do this action for.
	 */
	private static void onZamorakEffect(Player player) {

		player.getSkillManager().increaseCurrentLevelMax(Skill.ATTACK, (int) Math.floor(2 + (0.20 * 
				player.getSkillManager().getMaxLevel(Skill.ATTACK))));

		player.getSkillManager().increaseCurrentLevelMax(Skill.STRENGTH, (int) Math.floor(2 + (0.12 * 
				player.getSkillManager().getMaxLevel(Skill.STRENGTH))));

		player.getSkillManager().decreaseCurrentLevel(Skill.DEFENCE, (int) Math.floor(2 + (0.10 *  
				player.getSkillManager().getMaxLevel(Skill.DEFENCE))), -1);

		player.getSkillManager().decreaseCurrentLevel(Skill.HITPOINTS, (int) Math.floor(2 + (0.10 *   
				player.getSkillManager().getCurrentLevel(Skill.HITPOINTS))), 1);

		player.getSkillManager().increaseCurrentLevel(Skill.PRAYER, (int) Math.floor(0.10 * player.getSkillManager().getMaxLevel(Skill.PRAYER)), 
				player.getSkillManager().getMaxLevel(Skill.PRAYER));

	}


	/**
	 * The method that executes the prayer potion action.
	 *
	 * @param player
	 *            the player to do this action for.
	 * @param restorePotion
	 *            {@code true} if this potion is a restore potion, {@code false}
	 *            otherwise.
	 */
	private static void onPrayerEffect(Player player, boolean restorePotion) {
		int maxLevel = player.getSkillManager().getMaxLevel(Skill.PRAYER);
		double min = (int) Math.floor((restorePotion ? 8 : 7) + (maxLevel / 4));
		player.getSkillManager().increaseCurrentLevel(Skill.PRAYER, (int) min, maxLevel);
	}

	/**
	 * The method that executes the restore potion action.
	 *
	 * @param player
	 *            the player to do this action for.
	 */
	private static void onRestoreEffect(Player player) {
		for (int index = 0; index <= 6; index++) {
			Skill skill = Skill.values()[index];
			if ((skill == Skill.PRAYER) || (skill == Skill.HITPOINTS)) {
				continue;
			}
			int maxLevel = player.getSkillManager().getMaxLevel(skill);
			int currLevel = player.getSkillManager().getCurrentLevel(skill);
			if(currLevel < maxLevel) {
				player.getSkillManager().increaseCurrentLevel(skill, (int) Math.floor(8 + (maxLevel / 4)), maxLevel);
			}
		}
	}
	

	/**
	 * The method that executes the basic effect potion action that will
	 * increment the level of {@code skill}.
	 *
	 * @param player
	 *            the player to do this action for.
	 */
	private static void onBasicEffect(Player player, Skill skill, BoostType type) {
		int maxLevel = player.getSkillManager().getMaxLevel(skill);
		int boostLevel = Math.round(maxLevel * type.getAmount());
		if (type == BoostType.LOW) {
			boostLevel += 3;
		}
		int cap = maxLevel + boostLevel;
		if(maxLevel + boostLevel > player.getSkillManager().getCurrentLevel(skill)) {
			player.getSkillManager().increaseCurrentLevel(skill, boostLevel, cap);
		}
	}

	/**
	 * The method that executes the anti-fire potion action.
	 *
	 * @param player
	 *            the player to do this action for.
	 */
	private static void onAntifireEffect(Player player, int seconds) {
		player.getCombat().getFireImmunityTimer().start(seconds);
		player.getPacketSender().sendEffectTimer(seconds, EffectTimer.ANTIFIRE);
	}


	/**
	 * The method that executes the anti-poison potion action.
	 *
	 * @param player
	 *            the player to do this action for.
	 */
	private static void onAntipoisonEffect(Player player, int seconds) {
		player.getCombat().getPoisonImmunityTimer().start(seconds);
		//player.getPacketSender().sendMessage("You are now immune to poison for another "+seconds+" seconds.");
	}

	/**
	 * The enumerated type whose elements represent the boost types for potions.
	 *
	 * @author Ryley Kimmel <ryley.kimmel@live.com>
	 * @author lare96 <http://github.com/lare96>
	 */
	private enum BoostType {
		LOW(.10F),
		NORMAL(.13F),
		SUPER(.19F);

		/**
		 * The amount this type will boost by.
		 */
		private final float amount;

		/**
		 * Creates a new {@link BoostType}.
		 *
		 * @param boostAmount
		 *            the amount this type will boost by.
		 */
		private BoostType(float boostAmount) {
			this.amount = boostAmount;
		}

		/**
		 * Gets the amount this type will boost by.
		 *
		 * @return the boost amount.
		 */
		public final float getAmount() {
			return amount;
		}
	}
}
