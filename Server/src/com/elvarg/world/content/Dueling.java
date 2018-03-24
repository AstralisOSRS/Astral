package com.elvarg.world.content;

import java.util.ArrayList;
import java.util.List;

import com.elvarg.GameConstants;
import com.elvarg.definitions.ItemDefinition;
import com.elvarg.engine.task.Task;
import com.elvarg.engine.task.TaskManager;
import com.elvarg.util.Misc;
import com.elvarg.world.entity.impl.player.Player;
import com.elvarg.world.model.Item;
import com.elvarg.world.model.PlayerInteractingOption;
import com.elvarg.world.model.PlayerStatus;
import com.elvarg.world.model.Position;
import com.elvarg.world.model.SecondsTimer;
import com.elvarg.world.model.container.ItemContainer;
import com.elvarg.world.model.container.StackType;
import com.elvarg.world.model.container.impl.Equipment;
import com.elvarg.world.model.container.impl.Inventory;
import com.elvarg.world.model.movement.MovementStatus;

/**
 * Handles the dueling system.
 * @author Professor Oak
 */
public class Dueling {

	private final Player player;
	private final ItemContainer container;
	private Player interact;
	private int configValue;
	private DuelState state = DuelState.NONE;
	
	//Delays!!
	private SecondsTimer button_delay = new SecondsTimer();
	private SecondsTimer request_delay = new SecondsTimer();

	//Rules
	private final boolean[] rules = new boolean[DuelRule.values().length];

	private static final int DUELING_WITH_FRAME = 6671;
	private static final int INTERFACE_ID = 6575;
	private static final int CONFIRM_INTERFACE_ID = 6412;
	private static final int SCOREBOARD_INTERFACE_ID = 6733;
	private static final int SCOREBOARD_CONTAINER = 6822;
	private static final int SCOREBOARD_USERNAME_FRAME = 6840;
	private static final int SCOREBOARD_COMBAT_LEVEL_FRAME = 6839;
	public static final int MAIN_INTERFACE_CONTAINER = 6669;
	private static final int SECOND_INTERFACE_CONTAINER = 6670;
	private static final int STATUS_FRAME_1 = 6684;
	private static final int STATUS_FRAME_2 = 6571;
	private static final int ITEM_LIST_1_FRAME = 6516;
	private static final int ITEM_LIST_2_FRAME = 6517;
	private static final int RULES_FRAME_START = 8242;
	private static final int RULES_CONFIG_ID = 286;
	private static final int TOTAL_WORTH_FRAME = 24234;
	
	public Dueling(Player player) {
		this.player = player;
		//The container which will hold all our offered items.
		this.container = new ItemContainer(player) {
			@Override
			public StackType stackType() {
				return StackType.DEFAULT;
			}

			@Override
			public ItemContainer refreshItems() {
				player.getPacketSender().sendInterfaceSet(INTERFACE_ID, Trading.CONTAINER_INVENTORY_INTERFACE);
				player.getPacketSender().sendItemContainer(player.getInventory(), Trading.INVENTORY_CONTAINER_INTERFACE);
				player.getPacketSender().sendInterfaceItems(MAIN_INTERFACE_CONTAINER, player.getDueling().getContainer().getValidItems());
				player.getPacketSender().sendInterfaceItems(SECOND_INTERFACE_CONTAINER, interact.getDueling().getContainer().getValidItems());
				interact.getPacketSender().sendInterfaceItems(MAIN_INTERFACE_CONTAINER, interact.getDueling().getContainer().getValidItems());
				interact.getPacketSender().sendInterfaceItems(SECOND_INTERFACE_CONTAINER, player.getDueling().getContainer().getValidItems());
				return this;
			}

			@Override
			public ItemContainer full() {
				getPlayer().getPacketSender().sendMessage("You cannot stake more items.");
				return this;
			}

			@Override
			public int capacity() {
				return 28;
			}
		};
	}

	public enum DuelState {
		NONE,
		REQUESTED_DUEL,
		DUEL_SCREEN,
		ACCEPTED_DUEL_SCREEN,
		CONFIRM_SCREEN,
		ACCEPTED_CONFIRM_SCREEN,
		STARTING_DUEL,
		IN_DUEL;
	}

	public enum DuelRule {
		NO_RANGED(16, 6725, -1, -1),
		NO_MELEE(32, 6726, -1, -1),
		NO_MAGIC(64, 6727, -1, -1),
		NO_SPECIAL_ATTACKS(8192, 7816, -1, -1),
		LOCK_WEAPON(4096, 670, -1, -1),
		NO_FORFEIT(1, 6721, -1, -1),
		NO_POTIONS(128, 6728, -1, -1),
		NO_FOOD(256, 6729, -1, -1),
		NO_PRAYER(512, 6730, -1, -1),
		NO_MOVEMENT(2, 6722, -1, -1),
		OBSTACLES(1024, 6732, -1, -1),

		NO_HELM(16384, 13813, 1, Equipment.HEAD_SLOT),
		NO_CAPE(32768, 13814, 1, Equipment.CAPE_SLOT),
		NO_AMULET(65536, 13815, 1, Equipment.AMULET_SLOT),
		NO_AMMUNITION(134217728, 13816, 1, Equipment.AMMUNITION_SLOT),
		NO_WEAPON(131072, 13817, 1, Equipment.WEAPON_SLOT),
		NO_BODY(262144, 13818, 1, Equipment.BODY_SLOT),
		NO_SHIELD(524288, 13819, 1, Equipment.SHIELD_SLOT),
		NO_LEGS(2097152, 13820, 1, Equipment.LEG_SLOT),
		NO_RING(67108864, 13821, 1, Equipment.RING_SLOT),
		NO_BOOTS(16777216, 13822, 1, Equipment.FEET_SLOT),
		NO_GLOVES(8388608, 13823, 1, Equipment.HANDS_SLOT);

		DuelRule(int configId, int buttonId, int inventorySpaceReq, int equipmentSlot) {
			this.configId = configId;
			this.buttonId = buttonId;
			this.inventorySpaceReq = inventorySpaceReq;
			this.equipmentSlot = equipmentSlot;
		}

		private int configId;
		private int buttonId;
		private int inventorySpaceReq;
		private int equipmentSlot;

		public int getConfigId() {
			return configId;
		}

		public int getButtonId() {
			return this.buttonId;
		}

		public int getInventorySpaceReq() {
			return this.inventorySpaceReq;
		}

		public int getEquipmentSlot() {
			return this.equipmentSlot;
		}

		public static DuelRule forId(int i) {
			for(DuelRule r : DuelRule.values()) {
				if(r.ordinal() == i)
					return r;
			}
			return null;
		}

		static DuelRule forButtonId(int buttonId) {
			for(DuelRule r : DuelRule.values()) {
				if(r.getButtonId() == buttonId)
					return r;
			}
			return null;
		}

		@Override
		public String toString() {
			return Misc.formatText(this.name().toLowerCase());
		}
	}

	public void process() {
		if(state == DuelState.NONE || state == DuelState.REQUESTED_DUEL) {
			//Show challenge option
			if(player.getPlayerInteractingOption() != PlayerInteractingOption.CHALLENGE) {
				player.getPacketSender().sendInteractionOption("Challenge", 1, false);
				player.getPacketSender().sendInteractionOption("null", 2, true); //Remove attack option
			}
		} else if(state == DuelState.STARTING_DUEL || state == DuelState.IN_DUEL) {
			//Show attack option
			if(player.getPlayerInteractingOption() != PlayerInteractingOption.ATTACK) {
				player.getPacketSender().sendInteractionOption("Attack", 2, true);
				player.getPacketSender().sendInteractionOption("null", 1, false); //Remove challenge option
			}
		} else {
			//Hide both options if player isn't in one of those states
			if(player.getPlayerInteractingOption() != PlayerInteractingOption.NONE) {
				player.getPacketSender().sendInteractionOption("null", 2, true);
				player.getPacketSender().sendInteractionOption("null", 1, false);
			}
		}
	}

	public void requestDuel(Player t_) {
		if(state == DuelState.NONE || state == DuelState.REQUESTED_DUEL) {
			
			//Make sure to not allow flooding!
			if(!request_delay.finished()) {
				int seconds = request_delay.secondsRemaining();
				player.getPacketSender().sendMessage("You must wait another "+(seconds == 1 ? "second" : ""+seconds+" seconds")+" before sending more duel challenges.");
				return;
			}
			
			//The other players' current duel state.
			final DuelState t_state = t_.getDueling().getState();

			//Should we initiate the duel or simply send a request?
			boolean initiateDuel = false;

			//Update this instance...
			this.setInteract(t_);
			this.setState(DuelState.REQUESTED_DUEL);

			//Check if target requested a duel with us...
			if(t_state == DuelState.REQUESTED_DUEL) {
				if(t_.getDueling().getInteract() != null && 
						t_.getDueling().getInteract() == player) {
					initiateDuel = true;
				}
			}

			//Initiate duel for both players with eachother?
			if(initiateDuel) {
				player.getDueling().initiateDuel();
				t_.getDueling().initiateDuel();
			} else {
				player.getPacketSender().sendMessage("You've sent a duel challenge to "+t_.getUsername()+"...");
				t_.getPacketSender().sendMessage(player.getUsername() + ":duelreq:");
			}
			
			//Set the request delay to 2 seconds at least.
			request_delay.start(2);
		} else {
			player.getPacketSender().sendMessage("You cannot do that right now.");
		}
	}

	public void initiateDuel() {
		//Set our duel state
		setState(DuelState.DUEL_SCREEN);

		//Set our player status
		player.setStatus(PlayerStatus.DUELING);

		//Reset rule toggle configs
		player.getPacketSender().sendConfig(RULES_CONFIG_ID, 0);

		//Update strings on interface 
		player.getPacketSender().
		sendString(DUELING_WITH_FRAME, "@or1@Dueling with: @whi@"+interact.getUsername()+"@or1@          Combat level: @whi@"+interact.getSkillManager().getCombatLevel()).
		sendString(STATUS_FRAME_1, "").sendString(669, "Lock Weapon").sendString(8278, "Neither player is allowed to change weapon.");

		//Send equipment on the interface..
		int equipSlot = 0;
		for(Item item : player.getEquipment().getItems()) {
			player.getPacketSender().sendItemOnInterface(13824, item.getId(), equipSlot, item.getAmount());
			equipSlot++;
		}

		//Reset container
		container.resetItems();

		//Refresh and send container...
		container.refreshItems();
	}

	public void closeDuel() {
		if(state != DuelState.NONE) {

			//Cache the current interact
			final Player interact_ = interact;

			//Return all items...
			for(Item t : container.getValidItems()) {
				container.switchItem(player.getInventory(), t.copy(), false, false);
			}

			//Refresh inventory
			player.getInventory().refreshItems();

			//Reset all attributes...
			resetAttributes();

			//Send decline message
			player.getPacketSender().sendMessage("Duel declined.");
			player.getPacketSender().sendInterfaceRemoval();

			//Reset/close duel for other player aswell (the cached interact)
			if(interact_ != null) {
				if(interact_.getStatus() == PlayerStatus.DUELING) {
					if(interact_.getDueling().getInteract() != null 
							&& interact_.getDueling().getInteract() == player) {
						interact_.getPacketSender().sendInterfaceRemoval();
					}
				}
			}
		}
	}

	public void resetAttributes() {

		//Reset duel attributes
		setInteract(null);
		setState(DuelState.NONE);

		//Reset player status if it's dueling.
		if(player.getStatus() == PlayerStatus.DUELING) {
			player.setStatus(PlayerStatus.NONE);
		}

		//Reset container..
		container.resetItems();

		//Reset rules
		for(int i = 0; i < rules.length; i++) {
			rules[i] = false;
		}

		//Clear toggles
		configValue = 0;
		player.getPacketSender().sendConfig(RULES_CONFIG_ID, 0);

		//Clear head hint
		player.getPacketSender().sendEntityHintRemoval(true);

		//Clear items on interface
		player.getPacketSender().
		clearItemOnInterface(MAIN_INTERFACE_CONTAINER).
		clearItemOnInterface(SECOND_INTERFACE_CONTAINER);
	}

	//Deposit or withdraw an item....
	public void handleItem(int id, int amount, int slot, ItemContainer from, ItemContainer to) {
		if(player.getInterfaceId() == INTERFACE_ID) {

			//Validate this stake action..
			if(!validate(player, interact, PlayerStatus.DUELING, new DuelState[]{DuelState.DUEL_SCREEN, DuelState.ACCEPTED_DUEL_SCREEN})) {
				return;
			}

			if(ItemDefinition.forId(id).getValue() == 0) {
				player.getPacketSender().sendMessage("There's no point in staking that. It's spawnable!");
				return;
			}

			//Check if the duel was previously accepted (and now modified)...
			if(state == DuelState.ACCEPTED_DUEL_SCREEN) {
				state = DuelState.DUEL_SCREEN;
			}
			if(interact.getDueling().getState() == DuelState.ACCEPTED_DUEL_SCREEN) {
				interact.getDueling().setState(DuelState.DUEL_SCREEN);
			}
			player.getPacketSender().sendString(STATUS_FRAME_1, "@red@DUEL MODIFIED!");
			interact.getPacketSender().sendString(STATUS_FRAME_1, "@red@DUEL MODIFIED!");

			//Handle the item switch..
			if(state == DuelState.DUEL_SCREEN 
					&& interact.getDueling().getState() == DuelState.DUEL_SCREEN) {

				//Check if the item is in the right place
				if(from.getItems()[slot].getId() == id) {

					//Make sure we can fit that amount in the duel
					if(from instanceof Inventory) {
						if(!ItemDefinition.forId(id).isStackable()) {
							if(amount > container.getFreeSlots()) {
								amount = container.getFreeSlots();
							}
						}
					}

					if(amount <= 0) {
						return;
					}

					final Item item = new Item(id, amount);

					//Only sort items if we're withdrawing items from the duel.
					final boolean sort = (from == (player.getDueling().getContainer()));

					//Do the switch!					
					if(item.getAmount() == 1) {
						from.switchItem(to, item, slot, sort, true);
					} else {
						from.switchItem(to, item, sort, true);
					}
				}
			} else {
				player.getPacketSender().sendInterfaceRemoval();
			}
		}
	}

	public void acceptDuel() {

		//Validate this stake action..
		if(!validate(player, interact, PlayerStatus.DUELING, new DuelState[]{DuelState.DUEL_SCREEN, DuelState.ACCEPTED_DUEL_SCREEN, DuelState.CONFIRM_SCREEN, DuelState.ACCEPTED_CONFIRM_SCREEN})) {
			return;
		}

		//Check button delay...
		if(!button_delay.finished()) {
			return;
		}
		
		//Check button delay...
		//if(!button_delay.finished()) {
		//	return;
		//}

		//Cache the interact...
		final Player interact_ = interact;

		//Interact's current trade state.
		final DuelState t_state = interact_.getDueling().getState();

		//Check which action to take..
		if(state == DuelState.DUEL_SCREEN) {

			//Verify that the interact can receive all items first..
			int slotsRequired = getFreeSlotsRequired(player);
			if(player.getInventory().getFreeSlots() < slotsRequired) {
				player.getPacketSender().sendMessage("You need at least "+slotsRequired+" free inventory slots for this duel.");
				return;
			}
			
			if(rules[DuelRule.NO_MELEE.ordinal()] && rules[DuelRule.NO_RANGED.ordinal()] && rules[DuelRule.NO_MAGIC.ordinal()]) {
				player.getPacketSender().sendMessage("You must enable at least one of the three combat styles.");
				return;
			}

			//Both are in the same state. Do the first-stage accept.
			setState(DuelState.ACCEPTED_DUEL_SCREEN);

			//Update status...
			player.getPacketSender().sendString(STATUS_FRAME_1, "Waiting for other player..");
			interact_.getPacketSender().sendString(STATUS_FRAME_1, ""+player.getUsername()+" has accepted.");

			//Check if both have accepted..
			if(state == DuelState.ACCEPTED_DUEL_SCREEN &&
					t_state == DuelState.ACCEPTED_DUEL_SCREEN) {

				//Technically here, both have accepted.	
				//Go into confirm screen!
				player.getDueling().confirmScreen();
				interact_.getDueling().confirmScreen();
			}		
		} else if(state == DuelState.CONFIRM_SCREEN) {
			//Both are in the same state. Do the second-stage accept.
			setState(DuelState.ACCEPTED_CONFIRM_SCREEN);

			//Update status...
			player.getPacketSender().sendString(STATUS_FRAME_2, "Waiting for "+interact_.getUsername()+"'s confirmation..");
			interact_.getPacketSender().sendString(STATUS_FRAME_2, ""+player.getUsername()+" has accepted. Do you wish to do the same?");

			//Check if both have accepted..
			if(state == DuelState.ACCEPTED_CONFIRM_SCREEN &&
					t_state == DuelState.ACCEPTED_CONFIRM_SCREEN) {

				//Both accepted, start duel

				//Decide where they will spawn in the arena..
				final boolean obstacle = rules[DuelRule.OBSTACLES.ordinal()];
				final boolean movementDisabled = rules[DuelRule.NO_MOVEMENT.ordinal()];

				Position pos1 = getRandomSpawn(obstacle);
				Position pos2 = getRandomSpawn(obstacle);

				//Make them spaw next to eachother
				if(movementDisabled) {
					pos2 = pos1.copy().add(-1, 0);
				}

				player.getDueling().startDuel(pos1);
				interact_.getDueling().startDuel(pos2);
			}
		}
		
		button_delay.start(1);
	}

	public Position getRandomSpawn(boolean obstacle) {
		if(obstacle) {
			return new Position(3366 + Misc.getRandom(11), 3246 + Misc.getRandom(6));
		}
		return new Position(3335 + Misc.getRandom(11), 3246 + Misc.getRandom(6));
	}

	private void confirmScreen() {
		//Update state
		player.getDueling().setState(DuelState.CONFIRM_SCREEN);

		//Send new interface frames
		String this_items = Trading.listItems(container);
		String interact_item = Trading.listItems(interact.getDueling().getContainer());		
		player.getPacketSender().sendString(ITEM_LIST_1_FRAME, this_items);
		player.getPacketSender().sendString(ITEM_LIST_2_FRAME, interact_item);

		//Reset all previous strings related to rules
		for (int i = 8238; i <= 8253; i++) {
			player.getPacketSender().sendString(i, "");
		}

		//Send new ones
		player.getPacketSender().sendString(8250, "Hitpoints will be restored.");
		player.getPacketSender().sendString(8238, "Boosted stats will be restored.");
		if (rules[DuelRule.OBSTACLES.ordinal()]) {
			player.getPacketSender().sendString(8239, "@red@There will be obstacles in the arena.");
		}
		player.getPacketSender().sendString(8240, "");
		player.getPacketSender().sendString(8241, "");

		int ruleFrameIndex = RULES_FRAME_START;
		for (int i = 0; i < DuelRule.values().length; i++) {
			if(i == DuelRule.OBSTACLES.ordinal())
				continue;
			if (rules[i]) {
				player.getPacketSender().sendString(ruleFrameIndex, "" + DuelRule.forId(i).toString());
				ruleFrameIndex++;
			}
		}

		player.getPacketSender().sendString(STATUS_FRAME_2, "");

		//Send new interface..
		player.getPacketSender().sendInterfaceSet(CONFIRM_INTERFACE_ID, Inventory.INTERFACE_ID);
		player.getPacketSender().sendItemContainer(player.getInventory(), Trading.INVENTORY_CONTAINER_INTERFACE);
	}
	public boolean isInteract(Player p2) {
		return (interact != null 
				&& interact.equals(p2));
	}
	public boolean checkRule(int button) {
		DuelRule rule = DuelRule.forButtonId(button);
		if(rule != null) {
			checkRule(rule);
			return true;
		}
		return false;
	}

	private void checkRule(DuelRule rule) {

		//Check if we're actually dueling..
		if(player.getStatus() != PlayerStatus.DUELING) {
			return;
		}

		//Verify stake...
		if(!validate(player, interact, PlayerStatus.DUELING, new DuelState[]{DuelState.DUEL_SCREEN, DuelState.ACCEPTED_DUEL_SCREEN})) {
			return;
		}

		//Verify our current state..
		if(state == DuelState.DUEL_SCREEN || state == DuelState.ACCEPTED_DUEL_SCREEN) {		

			//Toggle the rule..
			if(!rules[rule.ordinal()]) {
				rules[rule.ordinal()] = true;
				configValue += rule.getConfigId();
			} else {
				rules[rule.ordinal()] = false;
				configValue -= rule.getConfigId();
			}

			//Update interact's rules to match ours.
			interact.getDueling().setConfigValue(configValue);
			interact.getDueling().getRules()[rule.ordinal()] = rules[rule.ordinal()];

			//Send toggles for both players.
			player.getPacketSender().sendToggle(RULES_CONFIG_ID, configValue);
			interact.getPacketSender().sendToggle(RULES_CONFIG_ID, configValue);

			//Send modify status
			if(state == DuelState.ACCEPTED_DUEL_SCREEN) {
				state = DuelState.DUEL_SCREEN;
			}
			if(interact.getDueling().getState() == DuelState.ACCEPTED_DUEL_SCREEN) {
				interact.getDueling().setState(DuelState.DUEL_SCREEN);
			}
			player.getPacketSender().sendString(STATUS_FRAME_1, "@red@DUEL MODIFIED!");
			interact.getPacketSender().sendString(STATUS_FRAME_1, "@red@DUEL MODIFIED!");

			//Inform them about this "custom" rule.
			if(rule == DuelRule.LOCK_WEAPON && rules[rule.ordinal()]) {
				player.getPacketSender().sendMessage("@red@Warning! The rule 'Lock Weapon' has been enabled. You will not be able to change").sendMessage("@red@weapon during the duel!");
				interact.getPacketSender().sendMessage("@red@Warning! The rule 'Lock Weapon' has been enabled. You will not be able to change").sendMessage("@red@weapon during the duel!");
			}
		}
	}

	private void startDuel(Position telePos) {
		//Let's start the duel!

		//Set current duel state
		setState(DuelState.STARTING_DUEL);

		//Close open interfaces
		player.getPacketSender().sendInterfaceRemoval();

		//Unequip items based on the rules set for this duel
		for(int i = 11; i < rules.length; i++) {
			DuelRule rule = DuelRule.forId(i);
			if(rules[i]) {
				if(rule.getEquipmentSlot() < 0)
					continue;
				if(player.getEquipment().getItems()[rule.getEquipmentSlot()].getId() > 0) {
					Item item = new Item(player.getEquipment().getItems()[rule.getEquipmentSlot()].getId(), player.getEquipment().getItems()[rule.getEquipmentSlot()].getAmount());
					player.getEquipment().delete(item);
					player.getInventory().add(item);
				}
			}
		}
		if(rules[DuelRule.NO_WEAPON.ordinal()] || rules[DuelRule.NO_SHIELD.ordinal()]) {
			if(player.getEquipment().getItems()[Equipment.WEAPON_SLOT].getId() > 0) {
				if(ItemDefinition.forId(player.getEquipment().getItems()[Equipment.WEAPON_SLOT].getId()).isDoubleHanded()) {
					Item item = new Item(player.getEquipment().getItems()[Equipment.WEAPON_SLOT].getId(), player.getEquipment().getItems()[Equipment.WEAPON_SLOT].getAmount());
					player.getEquipment().delete(item);
					player.getInventory().add(item);
				}
			}
		}

		//Clear items on interface
		player.getPacketSender().
		clearItemOnInterface(MAIN_INTERFACE_CONTAINER).
		clearItemOnInterface(SECOND_INTERFACE_CONTAINER);

		//Restart the player
		player.restart(true);

		//Freeze the player
		if(rules[DuelRule.NO_MOVEMENT.ordinal()]) {
			player.getMovementQueue().reset().setMovementStatus(MovementStatus.DISABLED);
		}

		//Send interact hints
		player.getPacketSender().sendPositionalHint(interact.getPosition().copy(), 10);
		player.getPacketSender().sendEntityHint(interact);

		//Teleport the player
		player.moveTo(telePos);

		//Make them interact with eachother
		player.setEntityInteraction(interact);

		//Send countdown as a task
		TaskManager.submit(new Task(2, player, false) {
			int timer = 3;
			@Override
			public void execute() {
				if(player.getDueling().getState() != DuelState.STARTING_DUEL) {
					stop();
					return;
				}
				if(timer == 3 || timer == 2 || timer == 1) {
					player.forceChat(""+timer+"..");
				} else {
					player.getDueling().setState(DuelState.IN_DUEL);
					player.forceChat("FIGHT!!");
					stop();
				}
				timer--;
			}
		});
	}

	public void duelLost() {

		//Make sure both players are in a duel..
		if(validate(player, interact, null, new DuelState[]{DuelState.STARTING_DUEL, DuelState.IN_DUEL})) {

			//Add won items to a list..
			int totalValue = 0;
			List<Item> winnings = new ArrayList<Item>();
			for(Item item : interact.getDueling().getContainer().getValidItems()) {
				interact.getInventory().add(item);
				winnings.add(item);
				totalValue += item.getDefinition().getValue();
			}
			for(Item item : player.getDueling().getContainer().getValidItems()) {
				interact.getInventory().add(item);
				winnings.add(item);
				totalValue += item.getDefinition().getValue();
			}

			//Send interface data..
			interact.getPacketSender().
			sendString(SCOREBOARD_USERNAME_FRAME, player.getUsername()).
			sendString(SCOREBOARD_COMBAT_LEVEL_FRAME, ""+player.getSkillManager().getCombatLevel()).
			sendString(TOTAL_WORTH_FRAME, "@yel@Total: @or1@"+Misc.insertCommasToNumber(""+totalValue+"") + " value!");

			//Send winnings onto interface
			interact.getPacketSender().sendInterfaceItems(SCOREBOARD_CONTAINER, winnings);

			//Send the scoreboard interface
			interact.getPacketSender().sendInterface(SCOREBOARD_INTERFACE_ID);

			//Restart the winner's stats
			interact.restart(true);

			//Move players home			
			interact.moveTo(GameConstants.DEFAULT_POSITION.copy().add(Misc.getRandom(2), Misc.getRandom(2)));
			player.moveTo(GameConstants.DEFAULT_POSITION.copy().add(Misc.getRandom(2), Misc.getRandom(2)));

			//Send messages
			interact.getPacketSender().sendMessage("You won the duel!");
			player.getPacketSender().sendMessage("You lost the duel!");

			//Reset attributes for both
			interact.getDueling().resetAttributes();
			player.getDueling().resetAttributes();
		} else {

			player.getDueling().resetAttributes();
			player.getPacketSender().sendInterfaceRemoval();

			if(interact != null) {
				interact.getDueling().resetAttributes();
				interact.getPacketSender().sendInterfaceRemoval();
			}
		}
	}

	public boolean inDuel() {
		return state == DuelState.STARTING_DUEL || state == DuelState.IN_DUEL;
	}

	/**
	 * Validates a player. Basically checks that all specified params add up.
	 * @param player
	 * @param interact
	 * @param playerStatus
	 * @param duelStates
	 * @return
	 */
	private static boolean validate(Player player, Player interact, PlayerStatus playerStatus, DuelState... duelStates) {
		//Verify player...
		if(player == null || interact == null) {
			return false;
		}

		//Make sure we have proper status
		if(playerStatus != null) {
			if(player.getStatus() != playerStatus) {
				return false;
			}

			//Make sure we're interacting with eachother
			if(interact.getStatus() != playerStatus) {
				return false;
			}
		}

		if(player.getDueling().getInteract() == null
				|| player.getDueling().getInteract() != interact) {
			return false;
		}
		if(interact.getDueling().getInteract() == null
				|| interact.getDueling().getInteract() != player) {
			return false;
		}

		//Make sure we have proper duel state.
		boolean found = false;
		for(DuelState duelState : duelStates) {
			if(player.getDueling().getState() == duelState) {
				found = true;
				break;
			}
		}
		if(!found) {
			return false;
		}

		//Do the same for our interact
		found = false;
		for(DuelState duelState : duelStates) {
			if(interact.getDueling().getState() == duelState) {
				found = true;
				break;
			}
		}
		if(!found) {
			return false;
		}
		return true;
	}

	private int getFreeSlotsRequired(Player player) {
		int slots = 0;

		//Count equipment that needs to be taken off
		for(int i = 11; i < player.getDueling().getRules().length; i++) {
			DuelRule rule = DuelRule.values()[i];
			if(player.getDueling().getRules()[rule.ordinal()]) {
				Item item = player.getEquipment().getItems()[rule.getEquipmentSlot()];
				if(!item.isValid()) {
					continue;
				}
				if(!(item.getDefinition().isStackable() && player.getInventory().contains(item.getId()))) {
					slots += rule.getInventorySpaceReq();
				}
				if(rule == DuelRule.NO_WEAPON || rule == DuelRule.NO_SHIELD) {

				}
			}
		}

		//Count inventory slots from interact's container aswell as ours
		for(Item item : container.getItems()) {
			if(item == null || !item.isValid())
				continue;
			if(!(item.getDefinition().isStackable() && player.getInventory().contains(item.getId()))) {
				slots++;
			}
		}

		for(Item item : interact.getDueling().getContainer().getItems()) {
			if(item == null || !item.isValid())
				continue;
			if(!(item.getDefinition().isStackable() && player.getInventory().contains(item.getId()))) {
				slots++;
			}
		}

		return slots;
	}
	
	public SecondsTimer getButtonDelay() {
		return button_delay;
	}

	public DuelState getState() {
		return state;
	}

	public void setState(DuelState state) {
		this.state = state;
	}

	public ItemContainer getContainer() {
		return container;
	}

	public Player getInteract() {
		return interact;
	}

	public void setInteract(Player interact) {
		this.interact = interact;
	}

	public boolean[] getRules() {
		return rules;
	}

	public int getConfigValue() {
		return configValue;
	}

	public void setConfigValue(int configValue) {
		this.configValue = configValue;
	}

	public void incrementConfigValue(int configValue) {
		this.configValue += configValue;
	}
}
