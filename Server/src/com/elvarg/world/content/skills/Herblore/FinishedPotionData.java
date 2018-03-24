package com.elvarg.world.content.skills.Herblore;

import java.util.EnumSet;
import java.util.Objects;
import java.util.Optional;

import com.elvarg.world.model.Item;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Sets;

/**
 * Represents all the data regarding a completed potion
 * 
 * @author Vl1 - www.rune-server.org/members/Valii
 * @since March 4th, 2017.
 *
 */
public enum FinishedPotionData {

	ATTACK_POTION(new Item(121), new Item(91), new Item(221), 1, 25),

	ANTIPOISON(new Item(175), new Item(93), new Item(235), 5, 38),

	STRENGTH_POTION(new Item(115), new Item(95), new Item(225), 12, 50),

	RESTORE_POTION(new Item(127), new Item(97), new Item(223), 22, 63),

	ENERGY_POTION(new Item(3010), new Item(97), new Item(1975), 26, 68),

	DEFENCE_POTION(new Item(133), new Item(99), new Item(239), 30, 75),

	AGILITY_POTION(new Item(3034), new Item(3002), new Item(2152), 34, 80),

	COMBAT_POTION(new Item(9741), new Item(97), new Item(9736), 36, 84),

	RAYER_POTION(new Item(139), new Item(99), new Item(231), 38, 88),

	SUMMONING_POTION(new Item(12142), new Item(12181), new Item(12109), 40, 92),

	CRAFTING_POTION(new Item(14840), new Item(14856), new Item(5004), 42, 92),

	SUPER_ATTACK(new Item(145), new Item(101), new Item(221), 45, 100),

	VIAL_OF_STENCH(new Item(18661), new Item(101), new Item(1871), 46, 0),

	FISHING_POTION(new Item(181), new Item(101), new Item(235), 48, 106),

	SUPER_ENERGY(new Item(3018), new Item(103), new Item(2970), 52, 118),

	SUPER_STRENGTH(new Item(157), new Item(105), new Item(225), 55, 125),

	WEAPON_POISON(new Item(187), new Item(105), new Item(241), 60, 138),

	SUPER_RESTORE(new Item(3026), new Item(3004), new Item(223), 63, 143),

	SUPER_DEFENCE(new Item(163), new Item(107), new Item(239), 66, 150),

	ANTIFIRE(new Item(2454), new Item(2483), new Item(241), 69, 158),

	RANGING_POTION(new Item(169), new Item(109), new Item(245), 72, 163),

	MAGIC_POTION(new Item(3042), new Item(2483), new Item(3138), 76, 173),

	ZAMORAK_BREW(new Item(189), new Item(111), new Item(247), 78, 175),

	SARADOMIN_BREW(new Item(6687), new Item(3002), new Item(6693), 81, 180);

	public final static ImmutableSet<FinishedPotionData> POTION_VALUES = Sets
			.immutableEnumSet(EnumSet.allOf(FinishedPotionData.class));

	public static Optional<FinishedPotionData> get(Item item) {
		return POTION_VALUES.stream().filter(Objects::nonNull)
				.filter(potions -> potions.getUnfinishedPotion().getId() == item.getId()).findAny();
	}

	private Item finished, unfinished;

	private Item ingredient;

	private int requirement;

	private int experience;

	private FinishedPotionData(Item finished, Item unfinished, Item ingredient, int requirement, int experience) {
		this.finished = finished;
		this.unfinished = unfinished;
		this.ingredient = ingredient;
		this.requirement = requirement;
		this.experience = experience;
	}

	public int getExperience() {
		return experience;
	}

	public Item getFinishedPotion() {
		return finished;
	}

	public Item getIngredient() {
		return ingredient;
	}

	public int getRequirement() {
		return requirement;
	}

	public Item getUnfinishedPotion() {
		return unfinished;
	}
}