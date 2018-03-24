package com.elvarg.net.packet.impl;

import com.elvarg.definitions.WeaponInterfaces;
import com.elvarg.net.packet.Packet;
import com.elvarg.net.packet.PacketConstants;
import com.elvarg.net.packet.PacketListener;
import com.elvarg.world.content.Dueling;
import com.elvarg.world.content.Trading;
import com.elvarg.world.entity.combat.CombatSpecial;
import com.elvarg.world.entity.combat.magic.Autocasting;
import com.elvarg.world.entity.impl.player.Player;
import com.elvarg.world.model.Flag;
import com.elvarg.world.model.Item;
import com.elvarg.world.model.PlayerStatus;
import com.elvarg.world.model.container.impl.Bank;
import com.elvarg.world.model.container.impl.Equipment;
import com.elvarg.world.model.container.impl.PriceChecker;
import com.elvarg.world.model.container.impl.Shop;
import com.elvarg.world.model.equipment.BonusManager;
import com.elvarg.world.model.syntax.impl.BankX;
import com.elvarg.world.model.syntax.impl.BuyX;
import com.elvarg.world.model.syntax.impl.PriceCheckX;
import com.elvarg.world.model.syntax.impl.SellX;
import com.elvarg.world.model.syntax.impl.StakeX;
import com.elvarg.world.model.syntax.impl.TradeX;
import com.elvarg.world.model.syntax.impl.WithdrawBankX;

public class ItemContainerActionPacketListener implements PacketListener {

	@Override
	public void handleMessage(Player player, Packet packet) {
		
		if(player == null || player.getHitpoints() <= 0) {
			return;
		}
		
		switch (packet.getOpcode()) {
		case PacketConstants.FIRST_ITEM_CONTAINER_ACTION_OPCODE:
			firstAction(player, packet);
			break;
		case PacketConstants.SECOND_ITEM_CONTAINER_ACTION_OPCODE:
			secondAction(player, packet);
			break;
		case PacketConstants.THIRD_ITEM_CONTAINER_ACTION_OPCODE:
			thirdAction(player, packet);
			break;
		case PacketConstants.FOURTH_ITEM_CONTAINER_ACTION_OPCODE:
			fourthAction(player, packet);
			break;
		case PacketConstants.FIFTH_ITEM_CONTAINER_ACTION_OPCODE:
			fifthAction(player, packet);
			break;
		case PacketConstants.SIXTH_ITEM_CONTAINER_ACTION_OPCODE:
			sixthAction(player, packet);
			break;
		}
	}

	private static void firstAction(Player player, Packet packet) {
		int interfaceId = packet.readInt();
		int slot = packet.readShortA();
		int id = packet.readShortA();

		//Bank withdrawal..
		if(interfaceId >= Bank.CONTAINER_START && interfaceId < Bank.CONTAINER_START + Bank.TOTAL_BANK_TABS) {
			Bank.withdraw(player, id, slot, 1, interfaceId - Bank.CONTAINER_START);
			return;
		}

		switch(interfaceId) {
		
		//Withdrawing items from duel
		case Dueling.MAIN_INTERFACE_CONTAINER:
			if(player.getStatus() == PlayerStatus.DUELING) {
				player.getDueling().handleItem(id, 1, slot, 
						player.getDueling().getContainer(), player.getInventory());
			}
			break;

		case Trading.INVENTORY_CONTAINER_INTERFACE: //Duel/Trade inventory
			if(player.getStatus() == PlayerStatus.PRICE_CHECKING) {
				player.getPriceChecker().deposit(id, 1, slot);	
			} else if(player.getStatus() == PlayerStatus.TRADING) {
				player.getTrading().handleItem(id, 1, slot, player.getInventory(), 
						player.getTrading().getContainer());
			} else if(player.getStatus() == PlayerStatus.DUELING) {
				player.getDueling().handleItem(id, 1, slot, player.getInventory(), 
						player.getDueling().getContainer());
			}
			break;
		case Trading.CONTAINER_INTERFACE_ID:
			if(player.getStatus() == PlayerStatus.TRADING) {
				player.getTrading().handleItem(id, 1, slot, player.getTrading().getContainer(), 
						player.getInventory());
			}
			break;
		case PriceChecker.CONTAINER_ID:
			player.getPriceChecker().withdraw(id, 1, slot);
			break;

		case Bank.INVENTORY_INTERFACE_ID:
			Bank.deposit(player, id, slot, 1);
			break;

		case Shop.ITEM_CHILD_ID:
		case Shop.INVENTORY_INTERFACE_ID:
			Shop.checkValue(player, interfaceId, slot, id, interfaceId == Shop.INVENTORY_INTERFACE_ID);
			break;

		case Equipment.INVENTORY_INTERFACE_ID: //Unequip
			Item item = player.getEquipment().getItems()[slot];
			if(item == null || item.getId() != id)
				return;
			/*	if(player.getLocation() == Location.DUEL_ARENA) {
				if(player.getDueling().selectedDuelRules[DuelRule.LOCK_WEAPON.ordinal()]) {
					if(item.getDefinition().getEquipmentSlot() == Equipment.WEAPON_SLOT || item.getDefinition().isTwoHanded()) {
						player.getPacketSender().sendMessage("Weapons have been locked during this duel!");
						return;
					}
				}
			}*/
			boolean stackItem = item.getDefinition().isStackable() && player.getInventory().getAmount(item.getId()) > 0;
			int inventorySlot = player.getInventory().getEmptySlot();
			if (inventorySlot != -1) {

				player.getEquipment().setItem(slot, new Item(-1, 0));

				if(stackItem) {
					player.getInventory().add(item.getId(), item.getAmount());
				} else {
					player.getInventory().setItem(inventorySlot, item);
				}

				BonusManager.update(player);
				if (item.getDefinition().getEquipmentSlot() == Equipment.WEAPON_SLOT) {
					WeaponInterfaces.assign(player);
					player.setSpecialActivated(false);
					CombatSpecial.updateBar(player);
					if(player.getCombat().getAutocastSpell() != null) {
						Autocasting.setAutocast(player, null);
						player.getPacketSender().sendMessage("Autocast spell cleared.");
					}
				}
				player.getEquipment().refreshItems();
				player.getInventory().refreshItems();
				player.getUpdateFlag().flag(Flag.PLAYER_APPEARANCE);
			} else {
				player.getInventory().full();
			}
			break;
		}
	}

	private static void secondAction(Player player, Packet packet) {
		int interfaceId = packet.readInt();
		int id = packet.readLEShortA();
		int slot = packet.readLEShort();

		//Bank withdrawal..
		if(interfaceId >= Bank.CONTAINER_START && interfaceId < Bank.CONTAINER_START + Bank.TOTAL_BANK_TABS) {
			Bank.withdraw(player, id, slot, 5, interfaceId - Bank.CONTAINER_START);
			return;
		}

		switch(interfaceId) {
		case Shop.ITEM_CHILD_ID:
			Shop.buyItem(player, interfaceId, id, slot, 1);
			break;
		case Shop.INVENTORY_INTERFACE_ID:
			Shop.sellItem(player, interfaceId, id, slot, 1);
			break;
		case Bank.INVENTORY_INTERFACE_ID:
			Bank.deposit(player, id, slot, 5);
			break;
		case Dueling.MAIN_INTERFACE_CONTAINER:
			if(player.getStatus() == PlayerStatus.DUELING) {
				player.getDueling().handleItem(id, 5, slot, 
						player.getDueling().getContainer(), player.getInventory());
			}
			break;
		case Trading.INVENTORY_CONTAINER_INTERFACE: //Duel/Trade inventory
			if(player.getStatus() == PlayerStatus.PRICE_CHECKING) {
				player.getPriceChecker().deposit(id, 5, slot);
			} else if(player.getStatus() == PlayerStatus.TRADING) {
				player.getTrading().handleItem(id, 5, slot, player.getInventory(), 
						player.getTrading().getContainer());
			} else if(player.getStatus() == PlayerStatus.DUELING) {
				player.getDueling().handleItem(id, 5, slot, player.getInventory(), 
						player.getDueling().getContainer());
			}
			break;
		case Trading.CONTAINER_INTERFACE_ID:
			if(player.getStatus() == PlayerStatus.TRADING) {
				player.getTrading().handleItem(id, 5, slot, player.getTrading().getContainer(), 
						player.getInventory());
			}
			break;
		case PriceChecker.CONTAINER_ID:
			player.getPriceChecker().withdraw(id, 5, slot);
			break;
		}
	}

	private static void thirdAction(Player player, Packet packet) {
		int interfaceId = packet.readInt();
		int id = packet.readShortA();
		int slot = packet.readShortA();

		//Bank withdrawal..
		if(interfaceId >= Bank.CONTAINER_START && interfaceId < Bank.CONTAINER_START + Bank.TOTAL_BANK_TABS) {
			Bank.withdraw(player, id, slot, 10, interfaceId - Bank.CONTAINER_START);
			return;
		}

		switch(interfaceId) {
		case Shop.ITEM_CHILD_ID:
			Shop.buyItem(player, interfaceId, id, slot, 5);
			break;
		case Shop.INVENTORY_INTERFACE_ID:
			Shop.sellItem(player, interfaceId, id, slot, 5);
			break;
		case Bank.INVENTORY_INTERFACE_ID:
			Bank.deposit(player, id, slot, 10);
			break;
			//Withdrawing items from duel
		case Dueling.MAIN_INTERFACE_CONTAINER:
			if(player.getStatus() == PlayerStatus.DUELING) {
				player.getDueling().handleItem(id, 10, slot, 
						player.getDueling().getContainer(), player.getInventory());
			}
			break;
		case Trading.INVENTORY_CONTAINER_INTERFACE: //Duel/Trade inventory
			if(player.getStatus() == PlayerStatus.PRICE_CHECKING) {
				player.getPriceChecker().deposit(id, 10, slot);
			} else if(player.getStatus() == PlayerStatus.TRADING) {
				player.getTrading().handleItem(id, 10, slot, player.getInventory(), 
						player.getTrading().getContainer());
			} else if(player.getStatus() == PlayerStatus.DUELING) {
				player.getDueling().handleItem(id, 10, slot, player.getInventory(), 
						player.getDueling().getContainer());
			}
			break;
		case Trading.CONTAINER_INTERFACE_ID:
			if(player.getStatus() == PlayerStatus.TRADING) {
				player.getTrading().handleItem(id, 10, slot, player.getTrading().getContainer(), 
						player.getInventory());
			}
			break;
		case PriceChecker.CONTAINER_ID:
			player.getPriceChecker().withdraw(id, 10, slot);
			break;
		}
	}

	private static void fourthAction(Player player, Packet packet) {
		int slot = packet.readShortA();
		int interfaceId = packet.readInt();
		int id = packet.readShortA();

		//Bank withdrawal..
		if(interfaceId >= Bank.CONTAINER_START && interfaceId < Bank.CONTAINER_START + Bank.TOTAL_BANK_TABS) {
			Bank.withdraw(player, id, slot, -1, interfaceId - Bank.CONTAINER_START);
			return;
		}

		switch(interfaceId) {
		case Shop.ITEM_CHILD_ID:
			Shop.buyItem(player, interfaceId, id, slot, 10);
			break;
		case Shop.INVENTORY_INTERFACE_ID:
			Shop.sellItem(player, interfaceId, id, slot, 10);
			break;
		case Bank.INVENTORY_INTERFACE_ID:
			Bank.deposit(player, id, slot, -1);
			break;
			//Withdrawing items from duel
		case Dueling.MAIN_INTERFACE_CONTAINER:
			if(player.getStatus() == PlayerStatus.DUELING) {
				player.getDueling().handleItem(id, player.getDueling().getContainer().getAmount(id), slot, 
						player.getDueling().getContainer(), player.getInventory());
			}
			break;
		case Trading.INVENTORY_CONTAINER_INTERFACE: //Duel/Trade inventory
			if(player.getStatus() == PlayerStatus.PRICE_CHECKING) {
				player.getPriceChecker().deposit(id, player.getInventory().getAmount(id), slot);
			} else if(player.getStatus() == PlayerStatus.TRADING) {
				player.getTrading().handleItem(id, player.getInventory().getAmount(id), 
						slot, player.getInventory(), 
						player.getTrading().getContainer());
			} else if(player.getStatus() == PlayerStatus.DUELING) {
				player.getDueling().handleItem(id, player.getInventory().getAmount(id), 
						slot, player.getInventory(), 
						player.getDueling().getContainer());
			}
			break;
		case Trading.CONTAINER_INTERFACE_ID:
			if(player.getStatus() == PlayerStatus.TRADING) {
				player.getTrading().handleItem(id, player.getTrading().getContainer().getAmount(id), 
						slot, player.getTrading().getContainer(), 
						player.getInventory());
			}
			break;
		case PriceChecker.CONTAINER_ID:
			player.getPriceChecker().withdraw(id, player.getPriceChecker().getAmount(id), slot);
			break;
		}
	}

	private static void fifthAction(Player player, Packet packet) {
		int interfaceId = packet.readInt();
		int slot = packet.readLEShort();
		int id = packet.readLEShort();
		
		//Bank withdrawal..
		if(interfaceId >= Bank.CONTAINER_START && interfaceId < Bank.CONTAINER_START + Bank.TOTAL_BANK_TABS) {
			player.setEnterSyntax(new WithdrawBankX(id, slot, interfaceId - Bank.CONTAINER_START));
			player.getPacketSender().sendEnterAmountPrompt("How many would you like to withdraw?");
			return;
		}

		switch (interfaceId) {
		case Shop.INVENTORY_INTERFACE_ID:
			player.setEnterSyntax(new SellX(id, interfaceId, slot));
			player.getPacketSender().sendEnterAmountPrompt("How many would you like to sell?");
			break;
		case Shop.ITEM_CHILD_ID:
			player.setEnterSyntax(new BuyX(id, interfaceId, slot));
			player.getPacketSender().sendEnterAmountPrompt("How many would you like to buy?");
			break;

		case Bank.INVENTORY_INTERFACE_ID:
			player.setEnterSyntax(new BankX(id, slot));
			player.getPacketSender().sendEnterAmountPrompt("How many would you like to bank?");
			break;
		case Trading.INVENTORY_CONTAINER_INTERFACE: //Duel/Trade inventory
			if(player.getStatus() == PlayerStatus.PRICE_CHECKING) {
				player.setEnterSyntax(new PriceCheckX(id, slot, true));
				player.getPacketSender().sendEnterAmountPrompt("How many would you like to deposit?");
			} else if(player.getStatus() == PlayerStatus.TRADING) {
				player.setEnterSyntax(new TradeX(id, slot, true));
				player.getPacketSender().sendEnterAmountPrompt("How many would you like to offer?");
			} else if(player.getStatus() == PlayerStatus.DUELING) {
				player.setEnterSyntax(new StakeX(id, slot, true));
				player.getPacketSender().sendEnterAmountPrompt("How many would you like to offer?");
			}
			break;
		case Trading.CONTAINER_INTERFACE_ID:
			if(player.getStatus() == PlayerStatus.TRADING) {
				player.setEnterSyntax(new TradeX(id, slot, false));
				player.getPacketSender().sendEnterAmountPrompt("How many would you like to remove?");
			}
			break;
		case Dueling.MAIN_INTERFACE_CONTAINER:
			if(player.getStatus() == PlayerStatus.DUELING) {
				player.setEnterSyntax(new StakeX(id, slot, false));
				player.getPacketSender().sendEnterAmountPrompt("How many would you like to remove?");
			}
			break;
		case PriceChecker.CONTAINER_ID:
			player.setEnterSyntax(new PriceCheckX(id, slot, false));
			player.getPacketSender().sendEnterAmountPrompt("How many would you like to withdraw?");
			break;
		}
	}

	private static void sixthAction(Player player, Packet packet) {
	}
}
