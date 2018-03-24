package com.elvarg.net.packet.impl;

import com.elvarg.Elvarg;
import com.elvarg.definitions.ItemDefinition;
import com.elvarg.definitions.NpcDropDefinition;
import com.elvarg.definitions.ShopDefinition;
import com.elvarg.definitions.WeaponInterfaces;
import com.elvarg.engine.task.Task;
import com.elvarg.engine.task.TaskManager;
import com.elvarg.net.packet.Packet;
import com.elvarg.net.packet.PacketListener;
import com.elvarg.util.Misc;
import com.elvarg.util.PlayerPunishment;
import com.elvarg.world.World;
import com.elvarg.world.content.Feed;
import com.elvarg.world.content.SkillManager;
import com.elvarg.world.content.Toplist;
import com.elvarg.world.content.clan.ClanChat;
import com.elvarg.world.content.clan.ClanChatManager;
import com.elvarg.world.entity.combat.CombatFactory;
import com.elvarg.world.entity.combat.CombatSpecial;
import com.elvarg.world.entity.combat.bountyhunter.BountyHunter;
import com.elvarg.world.entity.impl.npc.NPC;
import com.elvarg.world.entity.impl.object.GameObject;
import com.elvarg.world.entity.impl.player.Player;
import com.elvarg.world.entity.impl.player.PlayerSaving;
import com.elvarg.world.model.Animation;
import com.elvarg.world.model.Flag;
import com.elvarg.world.model.Graphic;
import com.elvarg.world.model.Item;
import com.elvarg.world.model.Locations.Location;
import com.elvarg.world.model.PlayerRights;
import com.elvarg.world.model.Position;
import com.elvarg.world.model.Priority;
import com.elvarg.world.model.Skill;
import com.elvarg.world.model.SkullType;
import com.elvarg.world.model.container.impl.Bank;
import com.elvarg.world.model.dialogue.DialogueManager;
import com.elvarg.world.model.teleportation.TeleportHandler;
import com.elvarg.world.model.teleportation.TeleportType;


/**
 * This packet listener manages commands a player uses by using the
 * command console prompted by using the "`" char.
 * 
 * @author Gabriel Hannason
 */

public class CommandPacketListener implements PacketListener {

	@Override
	public void handleMessage(Player player, Packet packet) {
		String command = packet.readString();
		String[] parts = command.toLowerCase().split(" ");
		if(command.contains("\r") || command.contains("\n")) {
			return;
		}

		if(player == null || player.getHitpoints() <= 0) {
			return;
		}

		if(command.startsWith("/") && command.length() >= 1) {
			ClanChatManager.sendMessage(player, command.substring(1, command.length()));
			return;
		}
		try {

			command = command.toLowerCase();
			switch(player.getRights()) {
			case PLAYER:
				playerCommands(player, command, parts);
				break;
			case DONATOR:
			case SUPER_DONATOR:
			case LEGENDARY_DONATOR:
			case YOUTUBER:
				playerCommands(player, command, parts);
				donorCommands(player, command, parts);
				break;
			case MODERATOR:
				playerCommands(player, command, parts);
				donorCommands(player, command, parts);
				modCommands(player, command, parts);
				break;
			case ADMINISTRATOR:
				playerCommands(player, command, parts);
				donorCommands(player, command, parts);
				modCommands(player, command, parts);
				adminCommands(player, command, parts);
				break;
			case DEVELOPER:
			case OWNER:
				playerCommands(player, command, parts);
				donorCommands(player, command, parts);
				modCommands(player, command, parts);
				adminCommands(player, command, parts);
				devCommands(player, command, parts);
				ownerCommands(player, command, parts);
				break;
			}

		} catch (Exception exception) {
			exception.printStackTrace();

			if(player.getRights() == PlayerRights.DEVELOPER) {
				player.getPacketSender().sendMessage("Error executing that command.");

			} else {
				player.getPacketSender().sendMessage("Error executing that command.");
			}

		}
	}

	private static void playerCommands(Player player, String command, String[] parts) {		
		if(parts[0].startsWith("qwertfkroll")) {
			int riggedRoll = Integer.parseInt(parts[1]);
			ClanChat clan = player.getCurrentClanChat();
			player.performAnimation(new Animation(774, Priority.HIGH));
			ClanChatManager.sendMessage(clan, "@cr2@ [@red@Dice@bla@][" + clan.getName() +"] <shad=16112652>"+player.getUsername()+"<shad=000000> has just rolled a <shad=16112652>" +riggedRoll+ "<shad=000000>/100.");
		}
		if(parts[0].startsWith("lockxp")) {
			player.setExperienceLocked(!player.experienceLocked());
			player.getPacketSender().sendMessage("Lock: "+player.experienceLocked());
		} else if(parts[0].startsWith("empty")) {
			player.getInventory().resetItems().refreshItems();
		} else if(parts[0].startsWith("veng")) {
			if(player.busy()) {
				player.getPacketSender().sendMessage("You cannot do that right now.");
				return;
			}
			if(player.getInventory().getFreeSlots() < 3) {
				player.getPacketSender().sendMessage("You don't have enough free inventory space to do that.");
				return;
			}
			player.getInventory().add(9075, 1000).add(557, 1000).add(560, 1000);
			
		} else if(parts[0].startsWith("barrage")) {
			if(player.busy()) {
				player.getPacketSender().sendMessage("You cannot do that right now.");
				return;
			}
			if(player.getInventory().getFreeSlots() < 3) {
				player.getPacketSender().sendMessage("You don't have enough free inventory space to do that.");
				return;
			}
			player.getInventory().add(565, 1000).add(555, 1000).add(560, 1000);
		} else if(parts[0].startsWith("donate") || parts[0].startsWith("store")) {
			player.getPacketSender().sendURL("https://www.mistps.com/store");
		} else if(parts[0].startsWith("claim")) {
			player.getPacketSender().sendMessage("To claim purchased items, please talk to the Financial Advisor at home.");
		} else if(parts[0].startsWith("players")) {
			player.getPacketSender().sendMessage("There are currently "+World.getPlayers().size()+" players online and "+BountyHunter.PLAYERS_IN_WILD.size()+" players in the Wilderness.");
		} else if(parts[0].startsWith("kdr")) {
			player.forceChat("I currently have "+player.getKillDeathRatio()+" kdr!");
		} else if(parts[0].equals("changepassword")) {
			String pass = command.substring(parts[0].length() + 1);
			if(pass.length() > 0 && pass.length() < 15) {
				player.setPassword(pass);
				player.getPacketSender().sendMessage("Your password is now: "+pass);
			} else {
				player.getPacketSender().sendMessage("Invalid password input.");
			}
		} else if(parts[0].startsWith("skull") || parts[0].startsWith("redskull")) {
			if(CombatFactory.inCombat(player)) {
				player.getPacketSender().sendMessage("You cannot change that during combat!");
				return;
			}
			if(parts[0].contains("red")) {
				CombatFactory.skull(player, SkullType.RED_SKULL, (60 * 30)); //Should be 30 mins
			} else {
				CombatFactory.skull(player, SkullType.WHITE_SKULL, 300); //Should be 5 mins
			}
		}
	}

	private static void donorCommands(Player player, String command, String[] parts) {
		if(parts[0].startsWith("yell")) {
			if(PlayerPunishment.muted(player.getUsername()) || PlayerPunishment.IPMuted(player.getHostAddress())) {
				player.getPacketSender().sendMessage("You are muted and cannot yell.");
				return;
			}
			if(!player.getYellDelay().finished()) {
				player.getPacketSender().sendMessage("You must wait another "+player.getYellDelay().secondsRemaining()+" seconds to do that.");
				return;
			}
			final String yellMessage = command.substring(4, command.length());
			if(Misc.blockedWord(yellMessage)) {
				DialogueManager.sendStatement(player, "A word was blocked in your sentence. Please do not repeat it!");
				return;
			}
			World.sendMessage(""+player.getRights().getYellPrefix()+"[Global Chat]<img="+(player.getRights().ordinal() - 1)+"> "+player.getUsername()+":"+yellMessage);
			player.getYellDelay().start(player.getRights().getYellDelay());
		}
	}

	private static void modCommands(Player player, String command, String[] parts) {
		if(parts[0].equals("teleto")) {
			Player plr = World.getPlayerByName(command.substring(parts[0].length() + 1));
			if(plr != null) {
				player.moveTo(plr.getPosition().copy());
			}
		} else if(parts[0].equals("teletome")) {
			Player plr = World.getPlayerByName(command.substring(parts[0].length() + 1));
			if(plr != null) {
				plr.moveTo(player.getPosition().copy());
			}
		} else if(parts[0].equals("mute")) {
			String player2 = command.substring(parts[0].length() + 1);
			Player plr = World.getPlayerByName(player2);
			if(!PlayerSaving.playerExists(player2) && plr == null) {
				player.getPacketSender().sendMessage("Player "+player2+" does not exist.");
				return;
			}
			if(PlayerPunishment.muted(player2)) {
				player.getPacketSender().sendMessage("Player "+player2+" already has an active mute.");
				return;
			}
			PlayerPunishment.mute(player2);
			player.getPacketSender().sendMessage("Player "+player2+" was successfully muted.");
			if(plr != null) {
				plr.getPacketSender().sendMessage("You have been muted by "+player.getUsername()+".");
			}
		} else if(parts[0].equals("unmute")) {
			String player2 = command.substring(parts[0].length() + 1);
			Player plr = World.getPlayerByName(player2);
			if(!PlayerSaving.playerExists(player2) && plr == null) {
				player.getPacketSender().sendMessage("Player "+player2+" does not exist.");
				return;
			}
			if(!PlayerPunishment.muted(player2)) {
				player.getPacketSender().sendMessage("Player "+player2+" does not have an active mute.");
				return;
			}
			PlayerPunishment.unmute(player2);
			player.getPacketSender().sendMessage("Player "+player2+" was successfully unmuted.");
			if(plr != null) {
				plr.getPacketSender().sendMessage("You have been unmuted by "+player.getUsername()+".");
			}
		} else if(parts[0].equals("ipmute")) {
			Player player2 = World.getPlayerByName(command.substring(parts[0].length() + 1));
			if(player2 == null) {
				player.getPacketSender().sendMessage("Player "+player2+" is not online.");
				return;
			}
			if(PlayerPunishment.IPMuted(player2.getHostAddress())){
				player.getPacketSender().sendMessage("Player "+player2.getUsername()+"'s IP is already IPMuted.");
				return;
			}
			PlayerPunishment.addMutedIP(player2.getHostAddress());
			player.getPacketSender().sendMessage("Player "+player2.getUsername()+" was successfully IPMuted.");
			player2.getPacketSender().sendMessage("You have been IPMuted by "+player.getUsername()+".");
		} else if(parts[0].equals("ban")) {
			String player2 = command.substring(parts[0].length() + 1);
			Player plr = World.getPlayerByName(player2);
			if(!PlayerSaving.playerExists(player2) && plr == null) {
				player.getPacketSender().sendMessage("Player "+player2+" is not online.");
				return;
			}
			if(PlayerPunishment.banned(player2)) {
				player.getPacketSender().sendMessage("Player "+player2+" already has an active ban.");
				if(plr != null) {
					plr.logout();
				}
				return;
			}
			PlayerPunishment.ban(player2);
			player.getPacketSender().sendMessage("Player "+player2+" was successfully banned. Command logs written.");
			if(plr != null) {
				plr.logout();
			}
		} else if(parts[0].equals("unban")) {
			String player2 = command.substring(parts[0].length() + 1);
			if(!PlayerSaving.playerExists(player2)) {
				player.getPacketSender().sendMessage("Player "+player2+" is not online.");
				return;
			}
			if(!PlayerPunishment.banned(player2)) {
				player.getPacketSender().sendMessage("Player "+player2+" is not banned!");
				return;
			}
			PlayerPunishment.unban(player2);
			player.getPacketSender().sendMessage("Player "+player2+" was successfully unbanned.");
		} else if(parts[0].equals("ipban")) {
			String player2 = command.substring(parts[0].length() + 1);
			Player plr = World.getPlayerByName(player2);
			if(plr == null) {
				player.getPacketSender().sendMessage("Player "+player2+" is not online.");
				return;
			}
			if(PlayerPunishment.IPBanned(plr.getHostAddress())){
				player.getPacketSender().sendMessage("Player "+player2+"'s IP is already banned.");
				if(plr != null) {
					plr.logout();
				}
				return;
			}
			PlayerPunishment.addBannedIP(plr.getHostAddress());
			player.getPacketSender().sendMessage("Player "+player2+" was successfully ipbanned. Command logs written.");
			if(plr != null) {
				plr.logout();
			}
		} else if(parts[0].equals("unipmute")) {
			player.getPacketSender().sendMessage("Unipmutes can only be handled manually.");
		} else if(parts[0].equals("kick")) {
			String player2 = command.substring(parts[0].length() + 1);
			Player plr = World.getPlayerByName(player2);
			if(plr == null) {
				player.getPacketSender().sendMessage("Player "+player2+" is not online.");
				return;
			}
			if(CombatFactory.inCombat(plr)) {
				player.getPacketSender().sendMessage("Player "+player2+" is in combat!");
				return;
			}
			player.getPacketSender().sendMessage("Player "+player2+" was successfully kicked. Command logs written.");
			plr.logout();
		} else if(parts[0].startsWith("exit")) {
			String player2 = command.substring(parts[0].length() + 1);
			Player plr = World.getPlayerByName(player2);
			if(plr == null) {
				player.getPacketSender().sendMessage("Player "+player2+" is not online.");
				return;
			}
			if(CombatFactory.inCombat(plr)) {
				player.getPacketSender().sendMessage("Player "+player2+" is in combat!");
				return;
			}
			plr.getPacketSender().sendExit();
			player.getPacketSender().sendMessage("Closed other player's client.");
		} else if(parts[0].startsWith("resetpresets")) {
			String player2 = command.substring(parts[0].length() + 1);
			Player plr = World.getPlayerByName(player2);
			if(plr == null) {
				player.getPacketSender().sendMessage("Player "+player2+" is not online.");
				return;
			}
			for(int i = 0; i < plr.getPresets().length; i++) {
				plr.getPresets()[i] = null;
			}
			player.getPacketSender().sendMessage("Reset other plrs presets.");
		}
	}

	private static void adminCommands(Player player, String command, String[] parts) {

	}

	private static void ownerCommands(Player player, String command, String[] parts) {

	}

	private static void devCommands(Player player, String command, String[] parts) {
		if(parts[0].startsWith("rights")) {
			int right = Integer.parseInt(parts[1]);
			if(parts.length >= 3) {
				String other = command.substring(parts[0].length() + parts[1].length() + 2);
				Player player_ = World.getPlayerByName(other);
				if(player_ == null) {
					player.getPacketSender().sendMessage("Could not find player: "+other);
					return;
				}
				player_.setRights(PlayerRights.values()[right]);
				player_.getPacketSender().sendRights();
				player_.getPacketSender().sendMessage("You're now a "+player_.getRights().name());
				player.getPacketSender().sendMessage("Gave "+other+" rank: "+player_.getRights().name());
			} else {
				player.setRights(PlayerRights.values()[right]);
				player.getPacketSender().sendRights();
			}
		}
		if(parts[0].startsWith("copybank")) {
			String player2 = command.substring(parts[0].length() + 1);
			Player plr = World.getPlayerByName(player2);
			if(plr != null) {
				for(int i = 0; i < Bank.TOTAL_BANK_TABS; i++) {
					if(player.getBank(i) != null) {
						player.getBank(i).resetItems();
					}
				}
				for(int i = 0; i < Bank.TOTAL_BANK_TABS; i++) {
					if(plr.getBank(i) != null) {
						for(Item item : plr.getBank(i).getValidItems()) {
							player.getBank(i).add(item, false);
						}
					}
				}
			}
		}
		if(parts[0].startsWith("feedclear")) {
			Feed.clear();
		}
		if(parts[0].startsWith("toplist")) {
			Toplist.updateToplist();
		}
		if(parts[0].equals("reloaditems")) {
			ItemDefinition.parse().load();
			player.getPacketSender().sendConsoleMessage("Reloaded items.");
		}
		if(parts[0].equals("reloadshops")) {
			ShopDefinition.getShops().clear();
			ShopDefinition.parse().load();
			player.getPacketSender().sendConsoleMessage("Reloaded shops.");
		}
		if(parts[0].equals("reloaddrops")) {
			NpcDropDefinition.parse().load();
			player.getPacketSender().sendConsoleMessage("Reloaded drops.");
		}
		if(parts[0].startsWith("points")) {
			int points = Integer.parseInt(parts[1]);
			if(parts.length == 3) {
				String other = command.substring(parts[0].length() + parts[1].length() + 2);
				Player player_ = World.getPlayerByName(other);
				if(player_ == null) {
					player.getPacketSender().sendMessage("Could not find player: "+other);
					return;
				}
				player_.incrementPoints(points);
				player_.getPacketSender().sendString(52032, "@or1@Points: "+Misc.getTotalAmount(player.getPoints()));
				player_.getPacketSender().sendMessage("You've got "+points+" points.");
				player.getPacketSender().sendMessage("Gave "+other+" points: "+points);
			} else {
				player.incrementPoints(points);
				player.getPacketSender().sendString(52032, "@or1@Points: "+Misc.getTotalAmount(player.getPoints()));
				player.getPacketSender().sendMessage("You've got "+points+" points.");
			}
		}

		if(parts[0].startsWith("unlock")) {
			int type = Integer.parseInt(parts[1]);
			if(type == 0) {
				player.setPreserveUnlocked(true);
			} else if(type == 1) {
				player.setRigourUnlocked(true);
			} else if(type == 2) {
				player.setAuguryUnlocked(true);
			}
			player.getPacketSender().sendConfig(709, player.isPreserveUnlocked() ? 1 : 0);
			player.getPacketSender().sendConfig(711, player.isRigourUnlocked() ? 1 : 0);
			player.getPacketSender().sendConfig(713, player.isAuguryUnlocked() ? 1 : 0);
		}
		if(parts[0].startsWith("bank")) {
			player.getBank(player.getCurrentBankTab()).open();
		}
		if(parts[0].startsWith("tt")) {
			for(int i = 0; i < 100; i++) {
				World.getPlayers().add(player);
			}
		}
		if(parts[0].startsWith("setlevel")) {
			Skill skill = Skill.values()[Integer.parseInt(parts[1])];
			int level = Integer.parseInt(parts[2]);
			player.getSkillManager().setCurrentLevel(skill, level).setMaxLevel(skill, level).setExperience(skill, SkillManager.getExperienceForLevel(level));
			WeaponInterfaces.assign(player);
		}
		if(parts[0].startsWith("master")) {
			for(Skill skill : Skill.values()) {
				int level = SkillManager.getMaxAchievingLevel(skill);
				player.getSkillManager().setCurrentLevel(skill, level).setMaxLevel(skill, level).setExperience(skill, SkillManager.getExperienceForLevel(level));
			}
			WeaponInterfaces.assign(player);
			player.getUpdateFlag().flag(Flag.PLAYER_APPEARANCE);
		}
		if(parts[0].startsWith("reset")) {
			for(Skill skill : Skill.values()) {
				int level = 1;
				player.getSkillManager().setCurrentLevel(skill, level).setMaxLevel(skill, level).setExperience(skill, SkillManager.getExperienceForLevel(level));
			}
			WeaponInterfaces.assign(player);
		}
		if(parts[0].startsWith("playnpc")) {
			player.setNpcTransformationId(Integer.parseInt(parts[1]));
			player.getUpdateFlag().flag(Flag.PLAYER_APPEARANCE);
		}
		if(parts[0].startsWith("shopinv")) {
			int amt = 0;
			for(Item item : ShopDefinition.getShops().get(Integer.parseInt(parts[1])).getDefinition().getOriginalStock()) {
				player.getInventory().add(item.getId(), item.getDefinition().isStackable() ? item.getAmount() : 1);
				amt++;
			}
			player.getPacketSender().sendMessage("Added "+amt+", to your inventory.");
		}
		if(parts[0].startsWith("npc")) {
			NPC npc = new NPC(Integer.parseInt(parts[1]), player.getPosition().copy().add(1, 0));
			World.getNpcAddQueue().add(npc);
		}
		if(parts[0].startsWith("save")) {
			player.save();
		}
		if(parts[0].startsWith("pos")) {
			player.getPacketSender().sendMessage(player.getPosition().toString());
		}
		if(parts[0].startsWith("config")) {
			player.getPacketSender().sendConfig(Integer.parseInt(parts[1]), Integer.parseInt(parts[2]));
		}
		if(parts[0].startsWith("object")) {
			player.getPacketSender().sendObject(new GameObject(Integer.parseInt(parts[1]), player.getPosition().copy()));
		}
		if(parts[0].startsWith("spec")) {
			int amt = Integer.parseInt(parts[1]);
			player.setSpecialPercentage(amt);
			CombatSpecial.updateBar(player);
		}
		if(parts[0].startsWith("runes")) {
			int[] runes = new int[]{554, 555, 556, 557, 558, 559, 560, 561, 562, 563, 564, 565};
			for(int rune : runes) {
				player.getInventory().add(rune, 1000);
			}
		}
		if(parts[0].equals("tele")) {
			int x = Integer.parseInt(parts[1]);
			int y = Integer.parseInt(parts[2]);
			int z = 0;
			if(parts.length == 4) {
				z = Integer.parseInt(parts[3]);
			}
			player.moveTo(new Position(x, y, z));
		}
		if(parts[0].startsWith("anim")) {
			int anim = Integer.parseInt(parts[1]);
			player.performAnimation(new Animation(anim));
		}
		if(parts[0].startsWith("gfx")) {
			int gfx = Integer.parseInt(parts[1]);
			player.performGraphic(new Graphic(gfx));
		}
		if(parts[0].startsWith("item")) {
			int amount = 1;
			if(parts.length > 2) {
				amount = Integer.parseInt(parts[2]);
			}
			player.getInventory().add(new Item(Integer.parseInt(parts[1]), amount));
		}
		if (parts[0].equals("update")) {
			int time = Integer.parseInt(parts[1]);
			if(time > 0) {
				Elvarg.setUpdating(true);
				for (Player players : World.getPlayers()) {
					if (players == null)
						continue;
					players.getPacketSender().sendSystemUpdate(time);
				}
				TaskManager.submit(new Task(time) {
					@Override
					protected void execute() {
						for (Player player : World.getPlayers()) {
							if (player != null) {
								player.logout();
							}
						}
						ClanChatManager.save();
						Elvarg.getLogger().info("Update task finished!");
						stop();
					}
				});
			}
		} else if(parts[0].equals("noclip")) {
			player.getPacketSender().sendEnableNoclip();
			player.getPacketSender().sendConsoleMessage("Noclip enabled.");
		} else if(parts[0].equals("int")) {
			player.getPacketSender().sendInterface(Integer.parseInt(parts[1]));
		} else if(parts[0].equals("cint")) {
			player.getPacketSender().sendChatboxInterface(Integer.parseInt(parts[1]));
		} else if(parts[0].equals("reloadpunishments")) {
			PlayerPunishment.init();
			player.getPacketSender().sendConsoleMessage("Reloaded");
		}
	}

	public static final int OP_CODE = 103;
}
