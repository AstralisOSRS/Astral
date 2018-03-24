package com.elvarg.world.content.skills.Herblore;

import java.util.EnumSet;
import java.util.Objects;
import java.util.Optional;

import com.elvarg.world.model.Item;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Sets;

/**
 * Represents all the data regarding an unfinished (unf) potion
 * 
 * @author Vl1 - www.rune-server.org/members/Valii
 * @since March 4th, 2017.
 *
 */
public enum UnfinishedPotionData {

	GUAM_POTION(new Item(91), new Item(249), 1),

	MARRENTILL_POTION(new Item(93), new Item(251), 5),

	TARROMIN_POTION(new Item(95), new Item(253), 12),

	HARRALANDER_POTION(new Item(97), new Item(255), 22),

	RANARR_POTION(new Item(99), new Item(257), 30),

	TOADFLAX_POTION(new Item(3002), new Item(2998), 34),

	SPIRIT_WEED_POTION(new Item(12181), new Item(12172), 40),

	IRIT_POTION(new Item(101), new Item(259), 45),

	WERGALI_POTION(new Item(14856), new Item(14854), 1),

	AVANTOE_POTION(new Item(103), new Item(261), 50),

	KWUARM_POTION(new Item(105), new Item(263), 55),

	SNAPDRAGON_POTION(new Item(3004), new Item(3000), 63),

	CADANTINE_POTION(new Item(107), new Item(265), 66),

	LANTADYME(new Item(2483), new Item(2481), 69),

	DWARF_WEED_POTION(new Item(109), new Item(267), 72),

	TORSTOL_POTION(new Item(111), new Item(269), 78);

	public static final ImmutableSet<UnfinishedPotionData> POTION_VALUES = Sets
			.immutableEnumSet(EnumSet.allOf(UnfinishedPotionData.class));

	public static Optional<UnfinishedPotionData> get(Item item) {
		return POTION_VALUES.stream().filter(Objects::nonNull)
				.filter(potions -> potions.getIngredient().getId() == item.getId()).findAny();
	}

	private Item unfinished;

	private Item ingredient;

	private int requirement;

	private UnfinishedPotionData(Item unfinished, Item ingredient, int requirement) {
		this.unfinished = unfinished;
		this.ingredient = ingredient;
		this.requirement = requirement;
	}

	public Item getUnfinished() {
		return unfinished;
	}

	public Item getIngredient() {
		return ingredient;
	}

	public int getRequirement() {
		return requirement;
	}
}