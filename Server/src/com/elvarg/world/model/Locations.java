package com.elvarg.world.model;

import com.elvarg.world.content.Dueling.DuelState;
import com.elvarg.world.entity.Entity;
import com.elvarg.world.entity.combat.CombatFactory;
import com.elvarg.world.entity.combat.bountyhunter.BountyHunter;
import com.elvarg.world.entity.impl.Character;
import com.elvarg.world.entity.impl.npc.NPC;
import com.elvarg.world.entity.impl.player.Player;
import com.elvarg.world.model.dialogue.DialogueManager;

/**
 * Fixed issues with duel arena and attack option appear out of combat areas, etc..
 * @author Dennis
 *
 */
public class Locations {

	public static void login(Player player) {
		player.setLocation(Location.getLocation(player));
		player.getLocation().login(player);
		player.getLocation().enter(player);
	}

	public static void logout(Player player) {
		player.getLocation().logout(player);
		player.getLocation().leave(player);
	}

	public enum Location {
		WILDERNESS(new int[]{2940, 3392, 2986, 3012, 3650, 3653, 3012, 3059, 3008, 3070, 2250, 2295, 2760,
                2805, 2830, 2885, 2505, 2550},
        new int[]{3525, 3968, 10338, 10366, 3457, 3472, 10303, 10351, 10235, 10300, 4675, 4729,
                10120, 10180, 10105, 10150, 4760, 4795}, new int[]{0},false, true,
				true, true, true, true) {
			@Override
			public void process(Player player) {
				int y = player.getPosition().getY();
				player.setWildernessLevel(((((y > 6400 ? y - 6400 : y) - 3520) / 8)+1));
				player.getPacketSender().sendString(23321, "Level: "+player.getWildernessLevel());
			}

			@Override
			public void leave(Player player) {
				BountyHunter.onLeave(player);
			}

			@Override
			public void login(Player player) {
				enter(player);
			}

			@Override
			public void enter(Player player) {
				BountyHunter.onEnter(player);
			}

			@Override
			public boolean canTeleport(Player player) {
				if(player.getWildernessLevel() > 20 && player.getRights() != PlayerRights.DEVELOPER) {
					player.getPacketSender().sendMessage("Teleport spells are blocked in this level of Wilderness.");
					player.getPacketSender().sendMessage("You must be below level 20 of Wilderness to use teleportation spells.");
					return false;
				}
				return true;
			}

			@Override
			public boolean canAttack(Player attacker, Player target) {
				int combatDifference = CombatFactory.combatLevelDifference(attacker.getSkillManager().getCombatLevel(), target.getSkillManager().getCombatLevel());
				if (combatDifference > attacker.getWildernessLevel() || combatDifference > target.getWildernessLevel()) {
					attacker.getPacketSender().sendMessage("Your combat level difference is too great to attack that player here.").sendMessage("Move deeper into the wilderness first.");
					attacker.getMovementQueue().reset();
					return false;
				}
				if(target.getLocation() != Location.WILDERNESS) {
					attacker.getPacketSender().sendMessage("That player cannot be attacked, because they are not in the Wilderness.");
					attacker.getMovementQueue().reset();
					return false;
				}
				return true;
			}
		},
		EDGE_PVP(new int[]{3070, 3106, 3077, 3099, 3099, 3144, 3140, 3099, 3090, 3068, 3067, 3090, 3098, 3067, 3067, 3098},
		         new int[]{3446, 3487, 3486, 3446, 3488, 3520, 3488, 3520, 3488, 3499, 3488, 3499, 3500, 3520, 3500, 3520}, new int[]{20},false, true,
						true, true, true, true) {
					@Override
			public void process(Player player) {
				int combatleast = player.getSkillManager().getCombatLevel()-player.getWildernessLevel();
				player.setWildernessLevel(12);
				player.getPacketSender().sendString(23321, "PVP: "+player.getSkillManager().getCombatLevel()+"-"+combatleast);
			}

			@Override
			public void leave(Player player) {
				player.getPacketSender().sendInterfaceRemoval();
				player.setWildernessLevel(0);
			}

			@Override
			public void login(Player player) {
				enter(player);
			}

			@Override
			public void enter(Player player) {
				//BountyHunter.onEnter(player);
				player.getPacketSender().sendWalkableInterface(25400);
				player.getPacketSender().sendInteractionOption("Attack", 2, true);
				player.getPacketSender().sendInteractionOption("null", 1, false); //Remove challenge option
			}

			@Override
			public boolean canTeleport(Player player) {
				return true;
			}

			@Override
			public boolean canAttack(Player attacker, Player target) {
				int combatDifference = CombatFactory.combatLevelDifference(attacker.getSkillManager().getCombatLevel(), target.getSkillManager().getCombatLevel());
				if (combatDifference > attacker.getWildernessLevel() || combatDifference > target.getWildernessLevel()) {
					attacker.getPacketSender().sendMessage("Your combat level difference is too great to attack that player here.").sendMessage("Move deeper into the wilderness first.");
					attacker.getMovementQueue().reset();
					return false;
				}
				if(target.getLocation() != Location.EDGE_PVP) {
					attacker.getPacketSender().sendMessage("That player cannot be attacked, because they are not outside of the bank..");
					attacker.getMovementQueue().reset();
					return false;
				}
				return true;
			}
		},
		DUEL_ARENA(new int[]{3322, 3394, 3311, 3323, 3331, 3391}, new int[]{3195, 3291, 3223, 3248, 3242, 3260}, new int[]{0}, false, true, true, true, true, true) {
			@Override
			public void process(Player p) {
				p.getDueling().process();
			}
			@Override
			public boolean canAttack(Player attacker, Player target) {
				if(attacker.getDueling().getState() == DuelState.IN_DUEL) {
					if(target.getDueling().getState() == DuelState.IN_DUEL) {
						return true;
					}
				} else if(attacker.getDueling().getState() == DuelState.STARTING_DUEL
						|| target.getDueling().getState() == DuelState.STARTING_DUEL) {
					DialogueManager.sendStatement(attacker, "The duel hasn't started yet!");
					return false;
				}
				return false;
			}
			@Override
			public boolean canTeleport(Player player) {
				if(player.getDueling().inDuel()) {
					DialogueManager.sendStatement(player, "You cannot teleport in a duel!");
					return false;
				}
				return true;
			}
		},
		DEFAULT(null, null, null, false, true, true, true, true, true) {
			@Override
			public void process(Player p) {
				// removes the attack option when leaving a combat area
				p.getPacketSender().sendInteractionOption("null", 2, true);
			}
        };

		private Location(int[] x, int[] y, int[] z, boolean multi, boolean summonAllowed, boolean followingAllowed, boolean cannonAllowed, boolean firemakingAllowed, boolean aidingAllowed) {
			this.x = x;
			this.y = y;
			this.z = z;
			this.multi = multi;
			this.summonAllowed = summonAllowed;
			this.followingAllowed = followingAllowed;
			this.cannonAllowed = cannonAllowed;
			this.firemakingAllowed = firemakingAllowed;
			this.aidingAllowed = aidingAllowed;
		}

		private int[] x, y, z;
		private boolean multi;
		private boolean summonAllowed;
		private boolean followingAllowed;
		private boolean cannonAllowed;
		private boolean firemakingAllowed;
		private boolean aidingAllowed;

		public int[] getX() {
			return x;
		}

		private int[] getZ() {
		return z;
	}
		public int[] getY() {
			return y;
		}

        public static boolean inMulti(Character character) {
            int tileX = character.getPosition().getX(), tileY = character.getPosition().getY();
            boolean agilityWild = tileX >= 2989 && tileX <= 3009 && tileY >= 3916 && tileY <= 3966;
            if (character.getLocation() == WILDERNESS) {
                if (tileX >= 3250 && tileX <= 3302 && tileY >= 3905 && tileY <= 3925 || tileX >= 3020 
                		&& tileX <= 3055 && tileY >= 3684 && tileY <= 3711
                        || tileX >= 3150 && tileX <= 3195 && tileY >= 2958 && tileY <= 3003
                        || tileX >= 3645 && tileX <= 3715 && tileY >= 3454 && tileY <= 3550
                        || tileX > 3193 && tileX < 3279 && tileY > 9272 && tileY < 9343
                        || tileX >= 3136 && tileX <= 3327 && tileY >= 3519 && tileY <= 3607
                        || tileX >= 3125 && tileX <= 3327 && tileY >= 3648 && tileY <= 3860
                        || tileX >= 3200 && tileX <= 3390 && tileY >= 3840 && tileY <= 3967
                        || tileX >= 2992 && tileX <= 3007 && tileY >= 3912 && tileY <= 3967 && !agilityWild
                        || tileX >= 2946 && tileX <= 2959 && tileY >= 3816 && tileY <= 3831
                        || tileX >= 3008 && tileX <= 3199 && tileY >= 3856 && tileY <= 3903
                        || tileX >= 3008 && tileX <= 3071 && tileY >= 3600 && tileY <= 3711
                        || tileX >= 3072 && tileX <= 3327 && tileY >= 3608 && tileY <= 3647
                        || tileX >= 2624 && tileX <= 2690 && tileY >= 2550 && tileY <= 2619
                        || tileX >= 2371 && tileX <= 2422 && tileY >= 5062 && tileY <= 5117
                        || tileX >= 2896 && tileX <= 2927 && tileY >= 3595 && tileY <= 3630
                        || tileX >= 2892 && tileX <= 2932 && tileY >= 4435 && tileY <= 4464
                        || tileX >= 2256 && tileX <= 2287 && tileY >= 4680 && tileY <= 4711
                        || tileX >= 2250 && tileX <= 2295 && tileY >= 4675 && tileY <= 4729 // in
                        // kbd
                        || tileX >= 2516 && tileX <= 2595 && tileY >= 4926 && tileY <= 5003 // gwd
                        || tileX >= 3008 && tileX <= 3070 && tileY >= 10235 && tileY <= 10300 // kbd
                        // poison
                        // spiders
                        || tileX >= 2560 && tileX <= 2630 && tileY >= 5710 && tileY <= 5753
                        || tileX >= 2505 && tileX <= 2550 && tileY >= 4760 && tileY <= 4795)
                    return true;
            } else {
                if (character.getLocation() != WILDERNESS) {
                    // rock crabs
                    if (tileX >= 2656 && tileX <= 2732 && tileY >= 3711 && tileY <= 3742
                            || tileX >= 2885 && tileX <= 2920 && tileY >= 4375 && tileY <= 4415
                            //tds
                            || tileX >= 2570 && tileX <= 2620 && tileY >= 5701 && tileY <= 5750) {
                        return true;
                    }
                }
            }
            return character.getLocation().multi;
        }

		public boolean isSummoningAllowed() {
			return summonAllowed;
		}

		public boolean isFollowingAllowed() {
			return followingAllowed;
		}

		public boolean isCannonAllowed() {
			return cannonAllowed;
		}

		public boolean isFiremakingAllowed() {
			return firemakingAllowed;
		}

		public boolean isAidingAllowed() {
			return aidingAllowed;
		}

        public static Location getLocation(Entity gc) {
            for (Location location : Location.values()) {
                if (location != Location.DEFAULT)
                    if (inLocation(gc, location))
                        return location;
            }
            return Location.DEFAULT;
        }

        public static boolean inLocation(Entity entity, Location location) {
        	return location == Location.DEFAULT ? getLocation(entity) == Location.DEFAULT ? true : false : inLocation(entity.getPosition().getX(), entity.getPosition().getY(), entity.getPosition().getZ(), location); 
        }

        public static boolean inLocation(int x, int y, int z, Location location) {
            int checks = location.getX().length - 1;
            for (int i = 0; i <= checks; i += 2) {
                if (x >= location.getX()[i] && x <= location.getX()[i + 1]) {
                    if (y >= location.getY()[i] && y <= location.getY()[i + 1]) {
                    	if (z >= location.getZ()[0] && z <= location.getZ()[0]) {
                        return true;
                    	}
                    }
                }
            }
            return false;
        }

		public void process(Player player) { }

		public boolean canTeleport(Player player) {
			return true;
		}

		public void login(Player player) { }

		public void enter(Player player) { }

		public void leave(Player player) { }

		public void logout(Player player) { }

		public void onDeath(Player player) { }

		public boolean handleKilledNPC(Player killer, NPC npc) {
			return false;
		}

		public boolean canAttack(Player attacker, Player target) {
			return false;
		}
	}

	public static void onTick(Character character) {
        Location newLocation = Location.getLocation(character);
        if (character.getLocation() == newLocation) {
            if (character.isPlayer()) {
                Player player = (Player) character;
                character.getLocation().process(player);
                if (Location.inMulti(player)) {
                    if (player.getMultiIcon() != 1) {
                        player.getPacketSender().sendMultiIcon(1);
                    }
                } else if (player.getMultiIcon() == 1) {
                    player.getPacketSender().sendMultiIcon(0);
                }
            }
        } else {
            Location previousLocation = character.getLocation();
            if (character.isPlayer()) {
                Player player = (Player) character;
                if (player.getMultiIcon() > 0)
                    player.getPacketSender().sendMultiIcon(0);
                if (player.getWalkableInterfaceId() > 0 && player.getWalkableInterfaceId() != 37400
                        && player.getWalkableInterfaceId() != 50000)
                    player.getPacketSender().sendWalkableInterface(-1);
                if (player.getPlayerInteractingOption() != PlayerInteractingOption.NONE)
                    player.getPacketSender().sendInteractionOption("null", 2, true);
                	player.getPacketSender().sendInteractionOption("null", 1, true);
            }
            character.setLocation(newLocation);
            if (character.isPlayer()) {
                previousLocation.leave(((Player) character));
                character.getLocation().enter(((Player) character));
            }
        }
	}
}