package com.elvarg.world.content.skills.Prayer;

import java.util.Collections;
import java.util.EnumSet;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;

/**
 * Stores all of the data for Prayer, which contains itemId and experience
 * rates.
 * 
 * @author Dennis
 *
 */
public enum PrayerExperienceTable {
	
	BONES(526, 5),
	BAT_BONES(530, 5),
	WOLF_BONES(2859, 5),
	BIG_BONES(532, 15),
	BABYDRAGON_BONES(534, 30),
	JOGRE_BONE(3125, 15),
	ZOGRE_BONES(4812, 23),
	LONG_BONES(10976, 25),
	CURVED_BONE(10977, 25),
	SHAIKAHAN_BONES(3123, 25),
	DRAGON_BONES(536, 72),
	FAYRG_BONES(4830, 84),
	RAURG_BONES(4832, 96),
	DAGANNOTH_BONES(6729, 125),
	OURG_BONES(14793, 140),
	WYVERN_BONES(6812, 50),
	FROSTDRAGON_BONES(18830, 180),
	LAVA_DRAGON_BONES(11943, 150),
	
	ASHES(592, 20),
	IBANS_ASHES(1502, 500);

	/**
	 * Declares the prayer item identification.
	 */
	private final int prayerItem;
	/**
	 * Declares the set experience for {@link #prayerItem}.
	 */
	private final int experience;

	/**
	 * Gets the {@link #prayerItem}
	 * @return boneId
	 */
	protected final int getPrayerItem() {
		return prayerItem;
	}

	/**
	 * Gets the {@link #experience}.
	 * @return experience
	 */
	protected final int getExperience() {
		return experience;
	}

	/**
	 * The Prayer Experience Table constructor.
	 * @param boneId
	 * @param experience
	 */
	private PrayerExperienceTable(int prayerItem, int experience) {
		this.prayerItem = prayerItem;
		this.experience = experience;
	}

	/**
	 * Stores all of the data in the {@link #PrayerExperienceTable(int, int)} inside the data type shown below.
	 */
	private static final Set<PrayerExperienceTable> prayerItem_set = Collections.unmodifiableSet(EnumSet.allOf(PrayerExperienceTable.class));

	/**
	 * Streams through the {@link #prayerItem_set} data, then filters to find the proper bone id.
	 * @param bone
	 * @return
	 */
	protected final static Optional<PrayerExperienceTable> getPrayerItem(int prayerItem) {
		return prayerItem_set.stream().filter(Objects::nonNull).filter(boneId -> boneId.getPrayerItem() == prayerItem).findAny();
	}
}