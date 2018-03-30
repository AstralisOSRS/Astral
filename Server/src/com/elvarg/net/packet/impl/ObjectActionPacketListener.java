package com.elvarg.net.packet.impl;

import com.elvarg.Elvarg;
import com.elvarg.definitions.ObjectDefinition;
import com.elvarg.engine.task.TaskManager;
import com.elvarg.engine.task.impl.ForceMovementTask;
import com.elvarg.engine.task.impl.WalkToTask;
import com.elvarg.engine.task.impl.WalkToTask.FinalizedMovementTask;
import com.elvarg.net.packet.Packet;
import com.elvarg.net.packet.PacketConstants;
import com.elvarg.net.packet.PacketListener;
import com.elvarg.world.collision.region.RegionClipping;
import com.elvarg.world.content.Obelisks;
import com.elvarg.world.entity.combat.CombatFactory;
import com.elvarg.world.entity.combat.CombatSpecial;
import com.elvarg.world.entity.combat.CombatType;
import com.elvarg.world.entity.combat.formula.DamageFormulas;
import com.elvarg.world.entity.impl.player.Player;
import com.elvarg.world.model.ForceMovement;
import com.elvarg.world.model.Graphic;
import com.elvarg.world.model.MagicSpellbook;
import com.elvarg.world.model.Position;
import com.elvarg.world.model.Skill;
import com.elvarg.world.model.Locations.Location;
import com.elvarg.world.model.dialogue.DialogueManager;
import com.elvarg.world.model.dialogue.DialogueOptions;

/**
 * This packet listener is called when a player clicked
 * on a game object.
 * 
 * @author relex lawl
 */

public class ObjectActionPacketListener implements PacketListener {

	/**
	 * Handles the first click option on an object.
	 * @param player		The player that clicked on the object.
	 * @param packet		The packet containing the object's information.
	 */
	private static void firstClick(final Player player, Packet packet) {
		final int x = packet.readLEShortA();
		final int id = packet.readUnsignedShort();
		final int y = packet.readUnsignedShortA();
		final Position position = new Position(x, y, player.getPosition().getZ());

		//Make sure object exists...
		if(!RegionClipping.objectExists(id, position)) {
			Elvarg.getLogger().info("Object with id "+id+" does not exist in region.");
			return;
		}

		//Get object definition
		final ObjectDefinition def = ObjectDefinition.forId(id);
		if(def == null) {
			Elvarg.getLogger().info("ObjectDefinition for object "+id+" is null.");
			return;
		}

		//Calculate object size...
		final int size = (def.getSizeX() + def.getSizeY()) - 1;

		//Face object..
		player.setPositionToFace(position);

		player.setWalkToTask(new WalkToTask(player, position, size, new FinalizedMovementTask() {
			@Override
			public void execute() {

				//Wilderness obelisks
				if(player.getLocation() == Location.WILDERNESS) {
					if(Obelisks.activate(id)) {
						return;
					}
				}

				switch(id) {

				case BANK_CHEST:
					if (player.getPosition().getZ() == 0) {
						player.getBank(player.getCurrentBankTab()).open();
					} else {
						player.getPacketSender().sendMessage("Nothing interesting happens.");
					}
					break;
					
				  case MAXHIT_DUMMY:
	                    CombatFactory.getMethod(player).startAnimation(player);
	                    CombatFactory.getMethod(player).getHits(player, player);
	                    if (CombatFactory.getMethod(player).getCombatType().equals(CombatType.MELEE)) {
	                    player.forceChat("My Melee max hit is: " + DamageFormulas.calculateMaxMeleeHit(player));
	                    player.getPacketSender().sendMessage("You Go for your  melee maxhit.");
	                    CombatFactory.getMethod(player).finished(player);
	                }  else if(CombatFactory.getMethod(player).getCombatType().equals(CombatType.RANGED)) {
	                    player.forceChat("My Range max hit is: " + DamageFormulas.calculateMaxRangedHit(player));
	                    player.getPacketSender().sendMessage("You Go for your Range maxhit.");
	                } else if(CombatFactory.getMethod(player).getCombatType().equals(CombatType.MAGIC)) {
	                    player.forceChat("My Magic max hit is: " + DamageFormulas.getMagicMaxhit(player));
	                    player.getPacketSender().sendMessage("You Go for your Magic maxhit.");
	                }
	                    break;
					
				case WILDY_PORTAL:
					player.getPacketSender().sendMessage("You are teleported to level 25 wilderness.");
					player.moveTo(new Position(3199, 3728));
					break;

				case DITCH_PORTAL:
					if (player.getPosition().getZ() == 0) {
					player.getPacketSender().sendMessage("You are teleported to the Wilderness ditch.");
					player.moveTo(new Position(3087, 3520));
					}else {
						player.getPacketSender().sendMessage("Nothing interesting happens.");
					}
					break;

				case WILDERNESS_DITCH:
					if (player.getPosition().getZ() == 0) {
					player.getMovementQueue().reset();
					if(player.getForceMovement() == null && player.getClickDelay().elapsed(2000)) {
						final Position crossDitch = new Position(0, player.getPosition().getY() < 3522 ? 3 : -3);
						TaskManager.submit(new ForceMovementTask(player, 3, new ForceMovement(player.getPosition().copy(), crossDitch, 0, 70, crossDitch.getY() == 3 ? 0 : 2, 6132)));
						player.getClickDelay().reset();
					}
				}else {
					player.getPacketSender().sendMessage("You can't jump over the ditch at Edgepvp.");
				}
				break;

				case MAGICAL_ALTAR:
					if (player.getPosition().getZ() == 0) {
					DialogueManager.start(player, 8);
					player.setDialogueOptions(new DialogueOptions() {
						@Override
						public void handleOption(Player player, int option) {
							switch(option) {
							case 1: //Normal spellbook option
								player.getPacketSender().sendInterfaceRemoval();
								MagicSpellbook.changeSpellbook(player, MagicSpellbook.NORMAL);
								break;
							case 2: //Ancient spellbook option
								player.getPacketSender().sendInterfaceRemoval();
								MagicSpellbook.changeSpellbook(player, MagicSpellbook.ANCIENT);
								break;
							case 3: //Lunar spellbook option
								player.getPacketSender().sendInterfaceRemoval();
								MagicSpellbook.changeSpellbook(player, MagicSpellbook.LUNAR);
								break;
							case 4: //Cancel option
								player.getPacketSender().sendInterfaceRemoval();
								break;
							}
						}
					});
				} else {
					player.getPacketSender().sendMessage("Nothing interesting happens.");
				}
				break;

				case REJUVENATION_POOL:
					if (player.getPosition().getZ() == 0) {
					player.getPacketSender().sendMessage("You feel slightly renewed.");
					player.performGraphic(new Graphic(683));
					player.restart(false);
					} else {
						player.getPacketSender().sendMessage("Nothing interesting happens.");
					}
					break;

				}
			}
		}));
	}

	/**
	 * Handles the second click option on an object.
	 * @param player		The player that clicked on the object.
	 * @param packet		The packet containing the object's information.
	 */
	private static void secondClick(final Player player, Packet packet) {
		final int id = packet.readLEShortA();
		final int y = packet.readLEShort();
		final int x = packet.readUnsignedShortA();
		final Position position = new Position(x, y, player.getPosition().getZ());

		//Make sure object exists...
		if(!RegionClipping.objectExists(id, position)) {
			Elvarg.getLogger().info("Object with id "+id+" does not exist in region.");
			return;
		}

		//Get object definition
		final ObjectDefinition def = ObjectDefinition.forId(id);
		if(def == null) {
			Elvarg.getLogger().info("ObjectDefinition for object "+id+" is null.");
			return;
		}

		//Calculate object size...
		final int size = (def.getSizeX() + def.getSizeY()) - 1;

		//Face object..
		player.setPositionToFace(position);

		player.setWalkToTask(new WalkToTask(player, position, size, new FinalizedMovementTask() {
			public void execute() {
				switch(id) {
				case BANK_CHEST:
				case EDGEVILLE_BANK:
					player.getBank(player.getCurrentBankTab()).open();
					break;
				case MAGICAL_ALTAR:
					if (player.getPosition().getZ() == 0) {
					player.getPacketSender().sendInterfaceRemoval();
					MagicSpellbook.changeSpellbook(player, MagicSpellbook.NORMAL);
					}else {
						player.getPacketSender().sendMessage("Nothing interesting happens.");
					}
					break;
				}
			}
		}));
	}

	/**
	 * Handles the third click option on an object.
	 * @param player		The player that clicked on the object.
	 * @param packet		The packet containing the object's information.
	 */
	private static void thirdClick(Player player, Packet packet) {
		final int x = packet.readLEShort();
		final int y = packet.readShort();
		final int id = packet.readLEShortA();
		final Position position = new Position(x, y, player.getPosition().getZ());

		//Make sure object exists...
		if(!RegionClipping.objectExists(id, position)) {
			Elvarg.getLogger().info("Object with id "+id+" does not exist in region.");
			return;
		}

		//Get object definition
		final ObjectDefinition def = ObjectDefinition.forId(id);
		if(def == null) {
			Elvarg.getLogger().info("ObjectDefinition for object "+id+" is null.");
			return;
		}

		//Calculate object size...
		final int size = (def.getSizeX() + def.getSizeY()) - 1;

		//Face object..
		player.setPositionToFace(position);

		player.setWalkToTask(new WalkToTask(player, position, size, new FinalizedMovementTask() {
			public void execute() {
				switch(id) {
				case MAGICAL_ALTAR:
					if (player.getPosition().getZ() == 0) {
						player.getPacketSender().sendInterfaceRemoval();
						MagicSpellbook.changeSpellbook(player, MagicSpellbook.ANCIENT);
						}else {
							player.getPacketSender().sendMessage("Nothing interesting happens.");
						}
					break;
				}
			}
		}));
	}

	/**
	 * Handles the fourth click option on an object.
	 * @param player		The player that clicked on the object.
	 * @param packet		The packet containing the object's information.
	 */
	private static void fourthClick(Player player, Packet packet) {
		final int x = packet.readLEShortA();
		final int id = packet.readUnsignedShortA();
		final int y = packet.readLEShortA();
		final Position position = new Position(x, y, player.getPosition().getZ());

		//Make sure object exists...
		if(!RegionClipping.objectExists(id, position)) {
			Elvarg.getLogger().info("Object with id "+id+" does not exist in region.");
			return;
		}

		//Get object definition
		final ObjectDefinition def = ObjectDefinition.forId(id);
		if(def == null) {
			Elvarg.getLogger().info("ObjectDefinition for object "+id+" is null.");
			return;
		}

		//Calculate object size...
		final int size = (def.getSizeX() + def.getSizeY()) - 1;

		//Face object..
		player.setPositionToFace(position);

		player.setWalkToTask(new WalkToTask(player, position, size, new FinalizedMovementTask() {
			public void execute() {
				switch(id) {
				case MAGICAL_ALTAR:
					if (player.getPosition().getZ() == 0) {
						player.getPacketSender().sendInterfaceRemoval();
						MagicSpellbook.changeSpellbook(player, MagicSpellbook.LUNAR);
						}else {
							player.getPacketSender().sendMessage("Nothing interesting happens.");
						}
					break;
				}
			}
		}));
	}

	/**
	 * Handles the fifth click option on an object.
	 * @param player		The player that clicked on the object.
	 * @param packet		The packet containing the object's information.
	 */
	private static void fifthClick(final Player player, Packet packet) {

	}

	@Override
	public void handleMessage(Player player, Packet packet) {

		if(player == null || player.getHitpoints() <= 0) {
			return;
		}

		//Make sure we aren't doing something else..
		if(player.busy()) {
			return;
		}

		switch (packet.getOpcode()) {
		case PacketConstants.OBJECT_FIRST_CLICK_OPCODE:
			firstClick(player, packet);
			break;
		case PacketConstants.OBJECT_SECOND_CLICK_OPCODE:
			secondClick(player, packet);
			break;
		case PacketConstants.OBJECT_THIRD_CLICK_OPCODE:
			thirdClick(player, packet);
			break;
		case PacketConstants.OBJECT_FOURTH_CLICK_OPCODE:
			fourthClick(player, packet);
			break;
		case PacketConstants.OBJECT_FIFTH_CLICK_OPCODE:
			fifthClick(player, packet);
			break;
		}
	}

	private static final int MAGICAL_ALTAR = 29150;
	private static final int WILDY_PORTAL = 4152;
	private static final int REJUVENATION_POOL = 29241;
	private static final int DITCH_PORTAL = 4151;
	private static final int EDGEVILLE_BANK = 6943;
	private static final int BANK_CHEST = 2693;
	private static final int WILDERNESS_DITCH = 23271;
	private static final int MAXHIT_DUMMY = 823;
}
