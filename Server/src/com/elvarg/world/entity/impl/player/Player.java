
package com.elvarg.world.entity.impl.player;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import com.elvarg.GameConstants;
import com.elvarg.definitions.ItemDefinition;
import com.elvarg.definitions.WeaponInterfaces;
import com.elvarg.engine.task.TaskManager;
import com.elvarg.engine.task.impl.CombatPoisonEffect;
import com.elvarg.engine.task.impl.PlayerDeathTask;
import com.elvarg.engine.task.impl.RestoreSpecialAttackTask;
import com.elvarg.engine.task.impl.WalkToTask;
import com.elvarg.net.PlayerSession;
import com.elvarg.net.SessionState;
import com.elvarg.net.packet.PacketSender;
import com.elvarg.util.FrameUpdater;
import com.elvarg.util.Misc;
import com.elvarg.util.Stopwatch;
import com.elvarg.world.World;
import com.elvarg.world.content.BossPets;
import com.elvarg.world.content.Dueling;
import com.elvarg.world.content.Feed;
import com.elvarg.world.content.PrayerHandler;
import com.elvarg.world.content.PrayerHandler.PrayerData;
import com.elvarg.world.content.Presetables;
import com.elvarg.world.content.QuickPrayers;
import com.elvarg.world.content.SkillManager;
import com.elvarg.world.content.Toplist;
import com.elvarg.world.content.Trading;
import com.elvarg.world.content.clan.ClanChat;
import com.elvarg.world.content.clan.ClanChatManager;
import com.elvarg.world.entity.combat.CombatFactory;
import com.elvarg.world.entity.combat.CombatSpecial;
import com.elvarg.world.entity.combat.CombatType;
import com.elvarg.world.entity.combat.bountyhunter.BountyHunter;
import com.elvarg.world.entity.combat.magic.Autocasting;
import com.elvarg.world.entity.impl.Character;
import com.elvarg.world.entity.impl.npc.NPC;
import com.elvarg.world.entity.impl.npc.NpcAggression;
import com.elvarg.world.model.Animation;
import com.elvarg.world.model.Appearance;
import com.elvarg.world.model.BossPet;
import com.elvarg.world.model.ChatMessage;
import com.elvarg.world.model.EffectTimer;
import com.elvarg.world.model.Flag;
import com.elvarg.world.model.ForceMovement;
import com.elvarg.world.model.Item;
import com.elvarg.world.model.Locations;
import com.elvarg.world.model.MagicSpellbook;
import com.elvarg.world.model.PlayerInteractingOption;
import com.elvarg.world.model.PlayerRelations;
import com.elvarg.world.model.PlayerRights;
import com.elvarg.world.model.PlayerStatus;
import com.elvarg.world.model.Presetable;
import com.elvarg.world.model.SecondsTimer;
import com.elvarg.world.model.Skill;
import com.elvarg.world.model.SkullType;
import com.elvarg.world.model.container.impl.Bank;
import com.elvarg.world.model.container.impl.Equipment;
import com.elvarg.world.model.container.impl.Inventory;
import com.elvarg.world.model.container.impl.PriceChecker;
import com.elvarg.world.model.container.impl.Shop;
import com.elvarg.world.model.dialogue.Dialogue;
import com.elvarg.world.model.dialogue.DialogueOptions;
import com.elvarg.world.model.equipment.BonusManager;
import com.elvarg.world.model.movement.MovementStatus;
import com.elvarg.world.model.syntax.EnterSyntax;


public class Player extends Character {

	public Player(PlayerSession playerIO) {
		super(GameConstants.DEFAULT_POSITION.copy());
		this.session = playerIO;
	}

	@Override
	public void appendDeath() {
		if(!isDying) {
			isDying = true;
			TaskManager.submit(new PlayerDeathTask(this));
		}
	}

	@Override
	public int getHitpoints() {
		return getSkillManager().getCurrentLevel(Skill.HITPOINTS);
	}

	@Override
	public int getAttackAnim() {
		int anim = getCombat().getFightType().getAnimation();
		return anim;
	}


	@Override
	public int getBlockAnim() {
		final Item shield = getEquipment().getItems()[Equipment.SHIELD_SLOT];
		final Item weapon = getEquipment().getItems()[Equipment.WEAPON_SLOT];
		ItemDefinition definition = shield.getId() > 0 ? shield.getDefinition() : weapon.getDefinition();
		return definition.getBlockAnim();
	}

	@Override
	public Character setHitpoints(int hitpoints) {
		if(isDying) {
			return this;
		}

		skillManager.setCurrentLevel(Skill.HITPOINTS, hitpoints);
		packetSender.sendSkill(Skill.HITPOINTS);
		if(getHitpoints() <= 0 && !isDying)
			appendDeath();
		return this;
	}

	@Override
	public void heal(int amount) {
		int level = skillManager.getMaxLevel(Skill.HITPOINTS);
		if ((skillManager.getCurrentLevel(Skill.HITPOINTS) + amount) >= level) {
			setHitpoints(level);
		} else {
			setHitpoints(skillManager.getCurrentLevel(Skill.HITPOINTS) + amount);
		}
	}

	@Override
	public int getBaseAttack(CombatType type) {
		if (type == CombatType.RANGED)
			return skillManager.getCurrentLevel(Skill.RANGED);
		else if (type == CombatType.MAGIC)
			return skillManager.getCurrentLevel(Skill.MAGIC);
		return skillManager.getCurrentLevel(Skill.ATTACK);
	}

	@Override
	public int getBaseDefence(CombatType type) {
		if (type == CombatType.MAGIC)
			return skillManager.getCurrentLevel(Skill.MAGIC);
		return skillManager.getCurrentLevel(Skill.DEFENCE);
	}

	@Override
	public int getBaseAttackSpeed() {

		//Gets attack speed for player's weapon
		//If player is using magic, attack speed is
		//Calculated in the MagicCombatMethod class.

		int speed = getCombat().getWeapon().getSpeed();

		if(getCombat().getFightType().toString().toLowerCase().contains("rapid")) {
			speed--;
		}

		return speed;
	}

	@Override
	public boolean isPlayer() {
		return true;
	}

	@Override
	public boolean equals(Object o) {
		if (!(o instanceof Player)) {
			return false;
		}

		Player p = (Player) o;
		return p.getIndex() == getIndex() || p.getUsername().equals(username);
	}

	@Override
	public int getSize() {
		return 1;
	}

	public void onTick() {
		//Process incoming packets...
		getSession().handleQueuedPackets(false);

		//Process walking queue..
		getMovementQueue().onTick();
		
		//Process walk to task..
		if(walkToTask != null) {
			walkToTask.onTick();
		}
		
		//Process aggression
		NpcAggression.onTick(this);
		
		//Process combat
		getCombat().onTick();

		//Process Bounty Hunter
		BountyHunter.onTick(this);

		//Process locations
		Locations.onTick(this);

		//Updates inventory if an update
		//has been requested
		if(isUpdateInventory()) {
			getInventory().refreshItems();
			setUpdateInventory(false);
		}
		
		//Updates appearance if an update
		//has been requested
		//or if skull timer hits 0.
		if(queuedAppearanceUpdate() || getAndDecrementSkullTimer() == 0) {
			getUpdateFlag().flag(Flag.PLAYER_APPEARANCE);
			setQueuedAppearanceUpdate(false);
		}
		
		/**
		 * Decrease boosted stats
		 * Increase lowered stats
		 */
		if(getHitpoints() > 0) {
			if(increaseStats.finished() || decreaseStats.secondsElapsed() >= (PrayerHandler.isActivated(this, PrayerHandler.PRESERVE) ? 72 : 60)) {
				for(Skill skill : Skill.values()) {
					int current = getSkillManager().getCurrentLevel(skill);
					int max = getSkillManager().getMaxLevel(skill);

					//Should lowered stats be increased?
					if(current < max) {
						if(increaseStats.finished()) {
							int restoreRate = 1;

							//Rapid restore effect - 2x restore rate for all stats except hp/prayer
							//Rapid heal - 2x restore rate for hitpoints
							if(skill != Skill.HITPOINTS && skill != Skill.PRAYER) {
								if(PrayerHandler.isActivated(this, PrayerHandler.RAPID_RESTORE)) {
									restoreRate = 2;
								}
							} else if(skill == Skill.HITPOINTS) {
								if(PrayerHandler.isActivated(this, PrayerHandler.RAPID_HEAL)) {
									restoreRate = 2;
								}
							}

							getSkillManager().increaseCurrentLevel(skill, restoreRate, max);
						}
					} else if(current > max) {

						//Should boosted stats be decreased?
						if(decreaseStats.secondsElapsed() >= (PrayerHandler.isActivated(this, PrayerHandler.PRESERVE) ? 72 : 60)) {

							//Never decrease Hitpoints / Prayer
							if(skill != Skill.HITPOINTS && skill != Skill.PRAYER) {
								getSkillManager().decreaseCurrentLevel(skill, 1, 1);
							}

						}
					}
				}

				//Reset timers
				if(increaseStats.finished()) {
					increaseStats.start(60);
				}
				if(decreaseStats.secondsElapsed() >= (PrayerHandler.isActivated(this, PrayerHandler.PRESERVE) ? 72 : 60)) {
					decreaseStats.start((PrayerHandler.isActivated(this, PrayerHandler.PRESERVE) ? 72 : 60));
				}			
			}
		}
	}

	public void save() {
		if (session.getState() == SessionState.LOGGED_IN || session.getState() == SessionState.LOGGING_OUT) {
			PlayerSaving.save(this);
		}
	}

	/**
	 * Can the player logout?
	 * @return	Yes if they can logout, false otherwise.
	 */
	public boolean canLogout() {
		if (CombatFactory.isBeingAttacked(this)) {
			getPacketSender().sendMessage("You must wait a few seconds after being out of combat before doing this.");
			return false;
		}
		if(busy()) {
			getPacketSender().sendMessage("You cannot log out at the moment.");
			return false;
		}
		return true;
	}

	/**
	 * Sends the logout packet to the client.
	 * This results in the ChannelEventHandler
	 * adding the player to the logout queue.
	 */
	public void logout() {
		getSession().setState(SessionState.REQUESTED_LOG_OUT);
		getPacketSender().sendLogout();
	}

	/**
	 * Called by the world's logout queue!
	 */
	public void onLogout() {

		//Notify us
		System.out.println("[World] Deregistering player - [username, host] : [" + getUsername() + ", " + getHostAddress() + "]");

		//Update session state
		getSession().setState(SessionState.LOGGING_OUT);

		//If we're in a duel, make sure to give us a loss for logging out.
		if(getDueling().inDuel()) {
			getDueling().duelLost();
		}
		
		//Do stuff...
		BossPets.pickup(this, getCurrentPet());
		getRelations().updateLists(false);
		BountyHunter.unassign(this);
		getPacketSender().sendLogout();
		getPacketSender().sendInterfaceRemoval();
		ClanChatManager.leave(this, false);
		Locations.logout(this);
		TaskManager.cancelTasks(this);
		save();

		//Send and queue the logout. Also close channel!
		getPacketSender().sendLogout();
		session.setState(SessionState.LOGGED_OUT);
		if(getSession().getChannel().isOpen()) {
			getSession().getChannel().close();
		}
	}

	/**
	 * Called by the world's login queue!
	 */
	public void onLogin() {
		//Attempt to register the player..
		System.out.println("[World] Registering player - [username, host] : [" + getUsername() + ", " + getHostAddress() + "]");

		//Check if the player is already logged in.. If so, disconnect!!
		Player copy_ = World.getPlayerByName(getUsername());
		if(copy_ != null && copy_.getSession().getState() == SessionState.LOGGED_IN) {
			copy_.logout();
		}

		//Update session state
		getSession().setState(SessionState.LOGGED_IN);
		
		//Packets
		getPacketSender().sendMapRegion().sendDetails(); //Map region, player index and player rights
		getPacketSender().sendTabs(); //Client sideicons
		getPacketSender().sendMessage("Welcome to MistPS! We hope you enjoy your stay.");
		getPacketSender().sendMessage("Check out the new inferno deal in the website shop!");

		//Send levels and total exp
		long totalExp = 0;
		for (Skill skill : Skill.values()) {
			getSkillManager().updateSkill(skill);
			totalExp += getSkillManager().getExperience(skill);
		}
		getPacketSender().sendTotalExp(totalExp);

		//Send friends and ignored players lists...
		getRelations().setPrivateMessageId(1).onLogin(this).updateLists(true);

		//Reset prayer configs...
		PrayerHandler.resetAll(this);
		getPacketSender().sendConfig(709, PrayerHandler.canUse(this, PrayerData.PRESERVE, false) ? 1 : 0);
		getPacketSender().sendConfig(711, PrayerHandler.canUse(this, PrayerData.RIGOUR, false) ? 1 : 0);
		getPacketSender().sendConfig(713, PrayerHandler.canUse(this, PrayerData.AUGURY, false) ? 1 : 0);

		//Refresh item containers..
		getInventory().refreshItems();
		getEquipment().refreshItems();

		//Interaction options on right click...
		getPacketSender().sendInteractionOption("Follow", 3, false);
		getPacketSender().sendInteractionOption("Trade With", 4, false);

		//Sending run energy attributes...
		getPacketSender().sendRunStatus();
		getPacketSender().sendRunEnergy(getRunEnergy());

		//Sending player's rights..
		getPacketSender().sendRights();

		//Close all interfaces, just in case...
		getPacketSender().sendInterfaceRemoval();

		//Update weapon data and interfaces..
		WeaponInterfaces.assign(this);

		//Update weapon interface configs
		getPacketSender().sendConfig(getCombat().getFightType().getParentId(), getCombat().getFightType().getChildId())
		.sendConfig(172, getCombat().autoRetaliate() ? 1 : 0).updateSpecialAttackOrb();

		//Reset autocasting
		Autocasting.setAutocast(this, null);

		//Update locations..
		Locations.login(this);

		//Update feed
		Feed.sendEntries(this);

		//Update toplist
		Toplist.sendToplist(this);

		//Send pvp stats..
		getPacketSender().
		sendString(52029, "@or1@Killstreak: "+getKillstreak()).
		sendString(52030, "@or1@Kills: "+getTotalKills()).
		sendString(52031, "@or1@Deaths: "+getDeaths()).
		sendString(52033, "@or1@K/D Ratio: "+getKillDeathRatio()).
		sendString(52034, "@or1@Donated: "+getAmountDonated());

		//Join clanchat
		ClanChatManager.onLogin(this);

		//Handle timers and run tasks
		if(isPoisoned()) {
			TaskManager.submit(new CombatPoisonEffect(this));
		}
		if(getSpecialPercentage() < 100) {
			TaskManager.submit(new RestoreSpecialAttackTask(this));
		}

		if(!getCombat().getFreezeTimer().finished()) {
			getPacketSender().sendEffectTimer(getCombat().getFreezeTimer().secondsRemaining(), 
					EffectTimer.FREEZE);
		}
		if(!getVengeanceTimer().finished()) {
			getPacketSender().sendEffectTimer(getVengeanceTimer().secondsRemaining(), 
					EffectTimer.VENGEANCE);
		}
		if(!getCombat().getFireImmunityTimer().finished()) {
			getPacketSender().sendEffectTimer(getCombat().getFireImmunityTimer().secondsRemaining(), 
					EffectTimer.ANTIFIRE);
		}
		if(!getCombat().getTeleBlockTimer().finished()) {
			getPacketSender().sendEffectTimer(getCombat().getTeleBlockTimer().secondsRemaining(), 
					EffectTimer.TELE_BLOCK);
		}

		decreaseStats.start(60);
		increaseStats.start(60);

		getUpdateFlag().flag(Flag.PLAYER_APPEARANCE);

		if(isNewPlayer()) {
			getPacketSender().sendMessage("").sendMessage("To get started, simply select a quick loadout or create your own.");//.sendMessage("We've added a bunch of items in your bank.");
			Presetables.open(this, Presetables.GLOBAL_PRESETS[0]);
		}
	}

	public void restart(boolean full) {
		performAnimation(new Animation(65535));

		if(full) {
			setSpecialPercentage(100);
			setSkullTimer(0);
			setSkullType(SkullType.WHITE_SKULL);
		} else {
			if(getSpecialPercentage() < 100) {

				if(getSpecialAttackRestore().finished()) {
					setSpecialPercentage(100);
					getSpecialAttackRestore().start(120);
				} else {
					getPacketSender().sendMessage("@red@You must wait another "+getSpecialAttackRestore().secondsRemaining()+" seconds to restore special attack energy.").
					sendMessage("@red@Don't feel like waiting? Pick up Bluryberry's special from the Emblem trader's shop!");
				}
			}
		}

		setSpecialActivated(false);
		CombatSpecial.updateBar(this);
		setHasVengeance(false);
		getCombat().getFireImmunityTimer().stop();
		getCombat().getPoisonImmunityTimer().stop();
		getCombat().getTeleBlockTimer().stop();
		getCombat().getFreezeTimer().stop();
		getCombat().getPrayerBlockTimer().stop();
		setPoisonDamage(0);
		setWildernessLevel(0);
		setRecoilDamage(0);
		WeaponInterfaces.assign(this);
		BonusManager.update(this);
		PrayerHandler.deactivatePrayers(this);
		getEquipment().refreshItems();
		getInventory().refreshItems();
		for (Skill skill : Skill.values())
			getSkillManager().setCurrentLevel(skill, getSkillManager().getMaxLevel(skill));
		setRunEnergy(100);
		getMovementQueue().setMovementStatus(MovementStatus.NONE).reset();
		getUpdateFlag().flag(Flag.PLAYER_APPEARANCE);
		isDying = false;
		getPacketSender().
		sendEffectTimer(0, EffectTimer.ANTIFIRE).
		sendEffectTimer(0, EffectTimer.FREEZE).
		sendEffectTimer(0, EffectTimer.VENGEANCE).
		sendEffectTimer(0, EffectTimer.TELE_BLOCK);
	}

	public boolean busy() {
		return interfaceId > 0 || isDying || getHitpoints() <= 0 || isNeedsPlacement() || getStatus() != PlayerStatus.NONE;
	}

	/*
	 * Fields
	 */

	private DialogueOptions dialogueOptions;
	private String username;
	private String password;
	private String hostAddress;
	private Long longUsername;
	private final List<Player> localPlayers = new LinkedList<Player>();
	private final List<NPC> localNpcs = new LinkedList<NPC>();
	private final PacketSender packetSender = new PacketSender(this);
	private final Appearance appearance = new Appearance(this);
	private final SkillManager skillManager = new SkillManager(this);
	private final PlayerRelations relations = new PlayerRelations(this);
	private final ChatMessage chatMessages = new ChatMessage();
	private final FrameUpdater frameUpdater = new FrameUpdater();
	private final BonusManager bonusManager = new BonusManager();
	private final QuickPrayers quickPrayers = new QuickPrayers(this);
	private PlayerSession session;
	private PlayerInteractingOption playerInteractingOption = PlayerInteractingOption.NONE;
	private PlayerRights rights = PlayerRights.PLAYER;
	private PlayerStatus status = PlayerStatus.NONE;
	private ClanChat currentClanChat;
	private String clanChatName = "Mist";
	private Dialogue dialogue;
	private Shop shop;
	private int interfaceId = -1, walkableInterfaceId = -1, multiIcon;
	private boolean isRunning = true;
	private int runEnergy = 100;
	private boolean isDying;
	private boolean regionChange, allowRegionChangePacket;
	private boolean experienceLocked;
	private final Inventory inventory = new Inventory(this);
	private final Equipment equipment = new Equipment(this);
	private final PriceChecker priceChecker = new PriceChecker(this);
	private ForceMovement forceMovement;
	private NPC currentPet;
	private int skillAnimation;
	private boolean drainingPrayer;
	private double prayerPointDrain;
	private final Stopwatch clickDelay = new Stopwatch();
	private final Stopwatch lastItemPickup = new Stopwatch();
	private WalkToTask walkToTask;
	private EnterSyntax enterSyntax;
	private MagicSpellbook spellbook = MagicSpellbook.NORMAL;
	private final Stopwatch karambwanTimer = new Stopwatch();
	private final Stopwatch foodTimer = new Stopwatch();
	private final Stopwatch potionTimer = new Stopwatch();

	private final SecondsTimer yellDelay = new SecondsTimer();
	public final SecondsTimer increaseStats = new SecondsTimer();
	public final SecondsTimer decreaseStats = new SecondsTimer();

	private int destroyItem = -1;	
	private boolean updateInventory; //Updates inventory on next tick
	private boolean queuedAppearanceUpdate; //Updates appearance on next tick
	private boolean newPlayer;

	//Combat
	private SkullType skullType = SkullType.WHITE_SKULL;
	private final SecondsTimer aggressionTolerance = new SecondsTimer();
	private CombatSpecial combatSpecial;
	private int recoilDamage;
	private SecondsTimer vengeanceTimer = new SecondsTimer();
	private int wildernessLevel;
	private int skullTimer;
	private int points;
	private int amountDonated;
	
	//Blowpipe
	private int blowpipeScales;

	//Delay for restoring special attack
	private final SecondsTimer specialAttackRestore = new SecondsTimer();

	//Bounty hunter
	private int targetKills;
	private int normalKills;
	private int totalKills;
	private int killstreak;
	private int highestKillstreak;
	private int deaths;
	private int safeTimer = 180;
	private final SecondsTimer targetSearchTimer = new SecondsTimer();
	private final List<String> recentKills = new ArrayList<String>(); //Contains ip addresses of recent kills

	//Logout
	private final SecondsTimer forcedLogoutTimer = new SecondsTimer();

	private boolean preserveUnlocked;
	private boolean rigourUnlocked;
	private boolean auguryUnlocked;
	private boolean targetTeleportUnlocked;

	//Banking
	private int currentBankTab;
	private Bank[] banks = new Bank[Bank.TOTAL_BANK_TABS]; // last index is for bank searches
	private boolean noteWithdrawal, insertMode, searchingBank;
	private String searchSyntax = "";

	//Trading
	private final Trading trading = new Trading(this);
	private final Dueling dueling = new Dueling(this);

	//Presets
	private Presetable currentPreset;
	private Presetable[] presets = new Presetable[Presetables.MAX_PRESETS];
	private boolean openPresetsOnDeath = true;

	/*
	 * Getters/Setters
	 */

	public PlayerSession getSession() {
		return session;
	}

	public String getUsername() {
		return username;
	}

	public Player setUsername(String username) {
		this.username = username;
		return this;
	}

	public Long getLongUsername() {
		return longUsername;
	}

	public Player setLongUsername(Long longUsername) {
		this.longUsername = longUsername;
		return this;
	}

	public String getPassword() {
		return password;
	}

	public Player setPassword(String password) {
		this.password = password;
		return this;
	}


	public String getHostAddress() {
		return hostAddress;
	}

	public Player setHostAddress(String hostAddress) {
		this.hostAddress = hostAddress;
		return this;
	}

	public PlayerRights getRights() {
		return rights;
	}

	public Player setRights(PlayerRights rights) {
		this.rights = rights;
		return this;
	}

	public PacketSender getPacketSender() {
		return packetSender;
	}

	public SkillManager getSkillManager() {
		return skillManager;
	}

	public Appearance getAppearance() {
		return appearance;
	}

	public SecondsTimer getForcedLogoutTimer() {
		return forcedLogoutTimer;
	}

	public boolean isDying() {
		return isDying;
	}

	public List<Player> getLocalPlayers() {
		return localPlayers;
	}

	public List<NPC> getLocalNpcs() {
		return localNpcs;
	}

	public Player setInterfaceId(int interfaceId) {
		this.interfaceId = interfaceId;
		return this;
	}

	public int getInterfaceId() {
		return interfaceId;
	}

	public boolean experienceLocked() {
		return experienceLocked;
	}

	public void setExperienceLocked(boolean experienceLocked) {
		this.experienceLocked = experienceLocked;
	}

	public PlayerRelations getRelations() {
		return relations;
	}

	public ChatMessage getChatMessages() {
		return chatMessages;
	}

	public Dialogue getDialogue() {
		return this.dialogue;
	}

	public void setDialogue(Dialogue dialogue) {
		this.dialogue = dialogue;
	}

	public DialogueOptions getDialogueOptions() {
		return dialogueOptions;
	}

	public void setDialogueOptions(DialogueOptions dialogueOptions) {
		this.dialogueOptions = dialogueOptions;
	}

	public Player setRegionChange(boolean regionChange) {
		this.regionChange = regionChange;
		return this;
	}

	public boolean isChangingRegion() {
		return this.regionChange;
	}

	public void setAllowRegionChangePacket(boolean allowRegionChangePacket) {
		this.allowRegionChangePacket = allowRegionChangePacket;
	}

	public boolean isAllowRegionChangePacket() {
		return allowRegionChangePacket;
	}

	public int getWalkableInterfaceId() {
		return walkableInterfaceId;
	}

	public void setWalkableInterfaceId(int interfaceId2) {
		this.walkableInterfaceId = interfaceId2;		
	}

	public Player setRunning(boolean isRunning) {
		this.isRunning = isRunning;
		return this;
	}

	public boolean isRunning() {
		return isRunning;
	}

	public PlayerInteractingOption getPlayerInteractingOption() {
		return playerInteractingOption;
	}

	public Player setPlayerInteractingOption(PlayerInteractingOption playerInteractingOption) {
		this.playerInteractingOption = playerInteractingOption;
		return this;
	}

	public FrameUpdater getFrameUpdater() {
		return frameUpdater;
	}

	public BonusManager getBonusManager() {
		return bonusManager;
	}

	public int getMultiIcon() {
		return multiIcon;
	}

	public Player setMultiIcon(int multiIcon) {
		this.multiIcon = multiIcon;
		return this;
	}

	public Inventory getInventory() {
		return inventory;
	}

	public Equipment getEquipment() {
		return equipment;
	}

	public ForceMovement getForceMovement() {
		return forceMovement;
	}

	public Player setForceMovement(ForceMovement forceMovement) {
		this.forceMovement = forceMovement;
		
		if(forceMovement != null) {
			getUpdateFlag().flag(Flag.FORCED_MOVEMENT);
		}
		
		return this;
	}

	public int getSkillAnimation() {
		return skillAnimation;
	}

	public Player setSkillAnimation(int animation) {
		this.skillAnimation = animation;
		return this;
	}

	public int getRunEnergy() {
		return runEnergy;
	}

	public void setRunEnergy(int runEnergy) {
		this.runEnergy = runEnergy;
	}

	public boolean isDrainingPrayer() {
		return drainingPrayer;
	}

	public void setDrainingPrayer(boolean drainingPrayer) {
		this.drainingPrayer = drainingPrayer;
	}

	public double getPrayerPointDrain() {
		return prayerPointDrain;
	}

	public void setPrayerPointDrain(double prayerPointDrain) {
		this.prayerPointDrain = prayerPointDrain;
	}

	public Stopwatch getLastItemPickup() {
		return lastItemPickup;
	}

	public WalkToTask getWalkToTask() {
		return walkToTask;
	}

	public void setWalkToTask(WalkToTask walkToTask) {
		this.walkToTask = walkToTask;
	}

	public CombatSpecial getCombatSpecial() {
		return combatSpecial;
	}

	public void setCombatSpecial(CombatSpecial combatSpecial) {
		this.combatSpecial = combatSpecial;
	}

	public int getRecoilDamage() {
		return recoilDamage;
	}

	public void setRecoilDamage(int recoilDamage) {
		this.recoilDamage = recoilDamage;
	}

	public MagicSpellbook getSpellbook() {
		return spellbook;
	}

	public void setSpellbook(MagicSpellbook spellbook) {
		this.spellbook = spellbook;
	}

	public SecondsTimer getVengeanceTimer() {
		return vengeanceTimer;
	}

	public Stopwatch getFoodTimer() {
		return foodTimer;
	}

	public Stopwatch getKarambwanTimer() {
		return karambwanTimer;
	}

	public Stopwatch getPotionTimer() {
		return potionTimer;
	}

	public int getWildernessLevel() {
		return wildernessLevel;
	}


	public void setWildernessLevel(int wildernessLevel) {
		this.wildernessLevel = wildernessLevel;
	}

	public void setDestroyItem(int destroyItem) {
		this.destroyItem = destroyItem;
	}

	public int getDestroyItem() {
		return destroyItem;
	}

	public boolean isSkulled() {
		return skullTimer > 0;
	}

	public void setSkullTimer(int skullTimer) {
		this.skullTimer = skullTimer;
	}

	public int getAndDecrementSkullTimer() {
		return this.skullTimer--;
	}

	public int getSkullTimer() {
		return this.skullTimer;
	}

	public int getPoints() {
		return points;
	}

	public void setPoints(int points) {
		this.points = points;
	}

	public void incrementPoints(int points) {
		this.points += points;
	}


	public boolean isUpdateInventory() {
		return updateInventory;
	}


	public void setUpdateInventory(boolean updateInventory) {
		this.updateInventory = updateInventory;
	}

	public Stopwatch getClickDelay() {
		return clickDelay;
	}


	public Shop getShop() {
		return shop;
	}


	public Player setShop(Shop shop) {
		this.shop = shop;
		return this;
	}


	public PlayerStatus getStatus() {
		return status;
	}

	public Player setStatus(PlayerStatus status) {
		this.status = status;
		return this;
	}


	public EnterSyntax getEnterSyntax() {
		return enterSyntax;
	}


	public void setEnterSyntax(EnterSyntax enterSyntax) {
		this.enterSyntax = enterSyntax;
	}


	public int getCurrentBankTab() {
		return currentBankTab;
	}

	public Player setCurrentBankTab(int tab) {
		this.currentBankTab = tab;
		return this;
	}

	public void setNoteWithdrawal(boolean noteWithdrawal) {
		this.noteWithdrawal = noteWithdrawal;
	}

	public boolean withdrawAsNote() {
		return noteWithdrawal;
	}

	public void setInsertMode(boolean insertMode) {
		this.insertMode = insertMode;
	}

	public boolean insertMode() {
		return insertMode;
	}

	public Bank[] getBanks() {
		return banks;
	}

	public Bank getBank(int index) {
		if(banks[index] == null) {
			banks[index] = new Bank(this);
		}
		return banks[index];
	}

	public Player setBank(int index, Bank bank) {
		this.banks[index] = bank;
		return this;
	}


	public boolean isNewPlayer() {
		return newPlayer;
	}


	public void setNewPlayer(boolean newPlayer) {
		this.newPlayer = newPlayer;
	}


	public boolean isSearchingBank() {
		return searchingBank;
	}


	public void setSearchingBank(boolean searchingBank) {
		this.searchingBank = searchingBank;
	}


	public String getSearchSyntax() {
		return searchSyntax;
	}


	public void setSearchSyntax(String searchSyntax) {
		this.searchSyntax = searchSyntax;
	}


	public boolean isPreserveUnlocked() {
		return preserveUnlocked;
	}


	public void setPreserveUnlocked(boolean preserveUnlocked) {
		this.preserveUnlocked = preserveUnlocked;
	}


	public boolean isRigourUnlocked() {
		return rigourUnlocked;
	}


	public void setRigourUnlocked(boolean rigourUnlocked) {
		this.rigourUnlocked = rigourUnlocked;
	}


	public boolean isAuguryUnlocked() {
		return auguryUnlocked;
	}


	public void setAuguryUnlocked(boolean auguryUnlocked) {
		this.auguryUnlocked = auguryUnlocked;
	}

	public PriceChecker getPriceChecker() {
		return priceChecker;
	}


	public ClanChat getCurrentClanChat() {
		return currentClanChat;
	}


	public void setCurrentClanChat(ClanChat currentClanChat) {
		this.currentClanChat = currentClanChat;
	}


	public String getClanChatName() {
		return clanChatName;
	}


	public void setClanChatName(String clanChatName) {
		this.clanChatName = clanChatName;
	}


	public Trading getTrading() {
		return trading;
	}


	public Presetable[] getPresets() {
		return presets;
	}

	public void setPresets(Presetable[] sets) {
		this.presets = sets;
	}


	public boolean isOpenPresetsOnDeath() {
		return openPresetsOnDeath;
	}

	public void setOpenPresetsOnDeath(boolean openPresetsOnDeath) {
		this.openPresetsOnDeath = openPresetsOnDeath;
	}


	public Presetable getCurrentPreset() {
		return currentPreset;
	}


	public void setCurrentPreset(Presetable currentPreset) {
		this.currentPreset = currentPreset;
	}


	public QuickPrayers getQuickPrayers() {
		return quickPrayers;
	}


	public boolean isTargetTeleportUnlocked() {
		return targetTeleportUnlocked;
	}


	public void setTargetTeleportUnlocked(boolean targetTeleportUnlocked) {
		this.targetTeleportUnlocked = targetTeleportUnlocked;
	}

	public SecondsTimer getYellDelay() {
		return yellDelay;
	}

	public int getAmountDonated() {
		return amountDonated;
	}

	public void setAmountDonated(int amountDonated) {
		this.amountDonated = amountDonated;
	}

	public void incrementAmountDonated(int amountDonated) {
		this.amountDonated += amountDonated;
	}

	public void setTargetKills(int targetKills) {
		this.targetKills = targetKills;
	}

	public void incrementTargetKills() {
		targetKills++;
	}

	public int getTargetKills() {
		return targetKills;
	}

	public void setNormalKills(int normalKills) {
		this.normalKills = normalKills;
	}

	public void incrementKills() {
		normalKills++;
	}

	public int getNormalKills() {
		return normalKills;
	}

	public int getTotalKills() {
		return totalKills;
	}

	public void setTotalKills(int totalKills) {
		this.totalKills = totalKills;
	}

	public void incrementTotalKills() {
		this.totalKills++;
	}

	public void setDeaths(int deaths) {
		this.deaths = deaths;
	}

	public void incrementDeaths() {
		deaths++;
	}

	public int getDeaths() {
		return deaths;
	}

	public void resetSafingTimer() {
		this.setSafeTimer(180);
	}

	public int getHighestKillstreak() {
		return highestKillstreak;
	}

	public void setHighestKillstreak(int highestKillstreak) {
		this.highestKillstreak = highestKillstreak;
	}

	public int getKillstreak() {
		return killstreak;
	}

	public void setKillstreak(int killstreak) {
		this.killstreak = killstreak;
	}

	public void incrementKillstreak() {
		this.killstreak++;
	}

	public String getKillDeathRatio() {
		double kc = 0;
		if(deaths == 0) {
			kc = totalKills / 1;
		} else {
			kc = ((double)totalKills / deaths);
		}
		return Misc.FORMATTER.format(kc);
	}

	public List<String> getRecentKills() {
		return recentKills;
	}

	public int getSafeTimer() {
		return safeTimer;
	}

	public void setSafeTimer(int safeTimer) {
		this.safeTimer = safeTimer;
	}

	public int decrementAndGetSafeTimer() {
		return this.safeTimer--;
	}

	public SecondsTimer getTargetSearchTimer() {
		return targetSearchTimer;
	}

	public SecondsTimer getSpecialAttackRestore() {
		return specialAttackRestore;
	}

	public SkullType getSkullType() {
		return skullType;
	}

	public void setSkullType(SkullType skullType) {
		this.skullType = skullType;
	}

	public boolean queuedAppearanceUpdate() {
		return queuedAppearanceUpdate;
	}

	public void setQueuedAppearanceUpdate(boolean updateAppearance) {
		this.queuedAppearanceUpdate = updateAppearance;
	}

	public Dueling getDueling() {
		return dueling;
	}

	public int getBlowpipeScales() {
		return blowpipeScales;
	}

	public void incrementBlowpipeScales(int blowpipeScales) {
		this.blowpipeScales += blowpipeScales;
	}
	
	public int decrementAndGetBlowpipeScales() {
		return this.blowpipeScales--;
	}
	
	public void setBlowpipeScales(int blowpipeScales) {
		this.blowpipeScales = blowpipeScales;
	}

	public NPC getCurrentPet() {
		return currentPet;
	}

	public void setCurrentPet(NPC currentPet) {
		this.currentPet = currentPet;
	}

	public SecondsTimer getAggressionTolerance() {
		return aggressionTolerance;
	}
}
