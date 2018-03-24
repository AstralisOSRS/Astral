package com.elvarg.world.content.skills.Prayer;

import com.elvarg.definitions.ItemDefinition;
import com.elvarg.world.entity.impl.player.Player;
import com.elvarg.world.model.*;

/**
 * Standard Prayer Bone & Ash scatter handling.
 * 
 * @author Dennis
 *
 */
public class PrayerItemInteraction {

	/**
	 * Sets the animation for the bone burying.
	 */
	private static final Animation buryAnimation = new Animation(827);

	/**
	 * Sets the animation for Ash scattering.
	 */
	private static final Animation scatteringAnimation = new Animation(2292);

	/**
	 * Checks to see if the bone/ashes is present within the inventory, if so
	 * initiate function and set 1 tick delay (600ms).
	 * 
	 * @param player
	 * @param prayerItem
	 * @return true
	 */
	public static boolean handleInteraction(Player player, int prayerItem) {
		boolean exists = PrayerExperienceTable.getPrayerItem(prayerItem).isPresent();
		if (!exists) {
			return false;
		}
		final PrayerExperienceTable prayerExperienceTable = PrayerExperienceTable.getPrayerItem(prayerItem).get();
		final ItemDefinition itemDefined = ItemDefinition.forId(prayerExperienceTable.getPrayerItem());
		if (prayerItem == prayerExperienceTable.getPrayerItem() && prayerExperienceTable != null) {
			if (!player.getClickDelay().elapsed(600)) {
				return false;
			}
			player.performAnimation(itemDefined.getName().contains("shes") ? scatteringAnimation : buryAnimation);
			player.getSkillManager().addExperience(Skill.PRAYER, prayerExperienceTable.getExperience());
			player.getInventory().delete(new Item(prayerExperienceTable.getPrayerItem()));
			player.getPacketSender().sendMessage(itemDefined.getName().contains("shes") ? "You scatter the " + itemDefined.getName() +" in the air.." : "You bury the " + itemDefined.getName() + " safely in the ground..");
			player.getClickDelay().reset();
		}
		return true;
	}
}