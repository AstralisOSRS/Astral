package com.elvarg.net.packet.impl;

import com.elvarg.Elvarg;
import com.elvarg.GameConstants;
import com.elvarg.definitions.WeaponInterfaces;
import com.elvarg.net.packet.Packet;
import com.elvarg.net.packet.PacketConstants;
import com.elvarg.net.packet.PacketListener;
import com.elvarg.util.Misc;
import com.elvarg.world.content.Dueling.DuelRule;
import com.elvarg.world.entity.combat.CombatSpecial;
import com.elvarg.world.entity.combat.magic.Autocasting;
import com.elvarg.world.entity.combat.magic.CombatSpells;
import com.elvarg.world.entity.impl.player.Player;
import com.elvarg.world.model.Flag;
import com.elvarg.world.model.Item;
import com.elvarg.world.model.Skill;
import com.elvarg.world.model.container.impl.Equipment;
import com.elvarg.world.model.container.impl.Inventory;
import com.elvarg.world.model.dialogue.DialogueManager;
import com.elvarg.world.model.equipment.BonusManager;

/**
 * This packet listener manages the equip action a player
 * executes when wielding or equipping an item.
 * 
 * @author relex lawl
 */

public class EquipPacketListener implements PacketListener {

	@Override
	public void handleMessage(Player player, Packet packet) {

		int id = packet.readShort();
		int slot = packet.readShortA();
		int interfaceId = packet.readShortA();


		if(player == null || player.getHitpoints() <= 0) {
			return;
		}

		if(player.getInterfaceId() > 0 && player.getInterfaceId() != Equipment.EQUIPMENT_SCREEN_INTERFACE_ID) {
			player.getPacketSender().sendInterfaceRemoval();
		}

		switch (interfaceId) {
		case Inventory.INTERFACE_ID:
			/*
			 * Making sure slot is valid.
			 */
			if (slot >= 0 && slot <= 28) {
				Item item = player.getInventory().getItems()[slot].copy();
				if(item.getId() != id) {
					return;
				}

				/*
				 * Making sure item exists and that id is consistent.
				 */
				if (item != null && id == item.getId()) {

					//Check if player can wield the item..
					if(item.getDefinition().getRequirements() != null) {
						for (Skill skill : Skill.values()) {
							if (item.getDefinition().getRequirements()[skill.ordinal()] > player.getSkillManager().getMaxLevel(skill)) {
								StringBuilder vowel = new StringBuilder();
								if (skill.getName().startsWith("a") || skill.getName().startsWith("e") || skill.getName().startsWith("i") || skill.getName().startsWith("o") || skill.getName().startsWith("u")) {
									vowel.append("an ");
								} else {
									vowel.append("a ");
								}
								player.getPacketSender().sendMessage("You need " + vowel.toString() + Misc.formatText(skill.getName()) + " level of at least " + item.getDefinition().getRequirements()[skill.ordinal()] + " to wear this.");
								return;
							}
						}
					}

					//Check if it's a proper equipable item!
					int equipmentSlot = item.getDefinition().getEquipmentSlot();

					//Item hasn't been added yet?
					if(equipmentSlot == -1) {
						Elvarg.getLogger().info("Attempting to equip item "+item.getId()+" which has no definition.");
						return;
					}

					//Duel, locked weapon?
					if(player.getDueling().inDuel()) {
						for(int i = 11; i < player.getDueling().getRules().length; i++) {
							if(player.getDueling().getRules()[i]) {
								DuelRule duelRule = DuelRule.forId(i);
								if(equipmentSlot == duelRule.getEquipmentSlot() || duelRule == DuelRule.NO_SHIELD && item.getDefinition().isDoubleHanded()) {
									DialogueManager.sendStatement(player, "The rules that were set do not allow this item to be equipped.");
									return;
								}
							}
						}
						if(equipmentSlot == Equipment.WEAPON_SLOT || item.getDefinition().isDoubleHanded()) {
							if(player.getDueling().getRules()[DuelRule.LOCK_WEAPON.ordinal()]) {
								DialogueManager.sendStatement(player, "Weapons have been locked in this duel!");
								return;
							}
						}
					}

					Item equipItem = player.getEquipment().forSlot(equipmentSlot).copy();
					if (equipItem.getDefinition().isStackable() && equipItem.getId() == item.getId()) {
						int amount = equipItem.getAmount() + item.getAmount() <= Integer.MAX_VALUE ? equipItem.getAmount() + item.getAmount() : Integer.MAX_VALUE;
						player.getInventory().delete(item, false);
						player.getEquipment().getItems()[equipmentSlot].setAmount(amount);
						equipItem.setAmount(amount);
					} else {
						if (item.getDefinition().isDoubleHanded() && item.getDefinition().getEquipmentSlot() == Equipment.WEAPON_SLOT) {

							int slotsRequired = player.getEquipment().isSlotOccupied(Equipment.SHIELD_SLOT) && player.getEquipment().isSlotOccupied(Equipment.WEAPON_SLOT) ? 1 : 0;
							if (player.getInventory().getFreeSlots() < slotsRequired) {
								player.getInventory().full();
								return;
							}

							Item shield = player.getEquipment().getItems()[Equipment.SHIELD_SLOT];
							Item weapon = player.getEquipment().getItems()[Equipment.WEAPON_SLOT];
							player.getEquipment().set(Equipment.SHIELD_SLOT, new Item(-1, 0));
							player.getInventory().delete(item);
							player.getEquipment().set(equipmentSlot, item);


							if (weapon.getId() != -1) {
								player.getInventory().setItem(slot, weapon);
							}

							if (shield.getId() != -1) {
								player.getInventory().add(shield);
							}
							
						} else if (equipmentSlot == Equipment.SHIELD_SLOT && player.getEquipment().getItems()[Equipment.WEAPON_SLOT].getDefinition().isDoubleHanded()) {
							player.getInventory().setItem(slot, player.getEquipment().getItems()[Equipment.WEAPON_SLOT]);
							player.getEquipment().setItem(Equipment.WEAPON_SLOT, new Item(-1));
							player.getEquipment().setItem(Equipment.SHIELD_SLOT, item);
							resetWeapon(player);
						} else {
							if (item.getDefinition().getEquipmentSlot() == equipItem.getDefinition().getEquipmentSlot() && equipItem.getId() != -1) {
								if(player.getInventory().contains(equipItem.getId())) {
									player.getInventory().delete(item, false);
									player.getInventory().add(equipItem, false);
								} else {
									player.getInventory().setItem(slot, equipItem);
								}
								player.getEquipment().setItem(equipmentSlot, item);
							} else {
								player.getInventory().setItem(slot, new Item(-1, 0));
								player.getEquipment().setItem(item.getDefinition().getEquipmentSlot(), item);
							}
						}
					}

					//Refresh inventory
					if(GameConstants.QUEUE_SWITCHING_REFRESH) {
						player.setUpdateInventory(true);
					} else {
						player.getInventory().refreshItems();
					}
					
					if (equipmentSlot == Equipment.WEAPON_SLOT) {
						resetWeapon(player);
					}

					if(player.getEquipment().get(Equipment.WEAPON_SLOT).getId() != 4153 || player.getEquipment().get(Equipment.WEAPON_SLOT).getId() != 12848 || player.getEquipment().get(Equipment.WEAPON_SLOT).getId() != 13652) {
						player.getCombat().reset();
					}

					BonusManager.update(player);
					player.getEquipment().refreshItems();

					if(equipmentSlot == Equipment.WEAPON_SLOT) {
						player.setQueuedAppearanceUpdate(true); //1 ticking
					} else {
						player.getUpdateFlag().flag(Flag.PLAYER_APPEARANCE);
					}
				}
			}
			break;
		}
	}

	public static void resetWeapon(Player player) {
		player.setSpecialActivated(false);
		WeaponInterfaces.assign(player);
		Item weapon = player.getEquipment().getItems()[Equipment.WEAPON_SLOT];

		//Check if player is using a trident. If so, set autocast to respective spell..
		if(weapon.getId() == 11905) {
			Autocasting.setAutocast(player, CombatSpells.TRIDENT_OF_THE_SEAS.getSpell());
		} else if(weapon.getId() == 12899) {
			Autocasting.setAutocast(player, CombatSpells.TRIDENT_OF_THE_SWAMP.getSpell());
		} else {
			//Otherwise always reset autocast when switching weapon
			if(player.getCombat().getAutocastSpell() != null) {
				Autocasting.setAutocast(player, null);
				player.getPacketSender().sendMessage("Autocast spell cleared.");
			}
		}
	}
}