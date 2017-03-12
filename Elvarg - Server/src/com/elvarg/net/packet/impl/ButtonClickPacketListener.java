package com.elvarg.net.packet.impl;

import com.elvarg.GameConstants;
import com.elvarg.cache.impl.definitions.WeaponInterfaces;
import com.elvarg.net.packet.Packet;
import com.elvarg.net.packet.PacketListener;
import com.elvarg.util.Misc;
import com.elvarg.world.content.Emotes;
import com.elvarg.world.content.ItemsKeptOnDeath;
import com.elvarg.world.content.PrayerHandler;
import com.elvarg.world.content.TeleportsInterface;
import com.elvarg.world.content.clan.ClanChatManager;
import com.elvarg.world.entity.combat.magic.Autocasting;
import com.elvarg.world.entity.combat.magic.MagicClickSpells;
import com.elvarg.world.entity.impl.player.Player;
import com.elvarg.world.index.ButtonIndex;
import com.elvarg.world.model.container.impl.Bank;
import com.elvarg.world.model.equipment.BonusManager;
import com.elvarg.world.model.teleportation.TeleportHandler;
import com.elvarg.world.model.teleportation.TeleportType;

/**
 * This packet listener manages a button that the player has clicked upon.
 * 
 * @author Gabriel Hannason
 */

public class ButtonClickPacketListener implements PacketListener {

	@Override
	public void handleMessage(Player player, Packet packet) {
		int button = packet.readInt();
		if (player.getRights().isStaff()) {
			player.getPacketSender().sendConsoleMessage("Button: " + button);
		}
		if (handlers(player, button)) {
			return;
		}
		switch (button) {
		case ButtonIndex.LOGOUT:
			if (player.canLogout()) {
				player.logout();
			} else {
				player.getPacketSender().sendMessage("You cannot log out at the moment.");
			}
			break;

		case ButtonIndex.TOGGLE_RUN_ENERGY_ORB:
		case ButtonIndex.TOGGLE_RUN_ENERGY_SETTINGS:
			if (player.getRunEnergy() > 0 && !player.busy()) {
				player.setRunning(!player.isRunning());
				player.getPacketSender().sendRunStatus();
			} else {
				player.getPacketSender().sendMessage("You cannot do that right now.");
			}
			break;

		case ButtonIndex.OPEN_EQUIPMENT_SCREEN:
			if (!player.busy()) {
				BonusManager.open(player);
			} else {
				player.getPacketSender().sendMessage("You cannot do that right now.");
			}
			break;

		case ButtonIndex.OPEN_PRICE_CHECKER:
			if (!player.busy()) {
				player.getPriceChecker().open();
			} else {
				player.getPacketSender().sendMessage("You cannot do that right now.");
			}
			break;

		case ButtonIndex.ITEMS_KEPT_ON_DEATH_INTERFACE:
			player.getPacketSender().sendInterfaceRemoval();
			break;
		case ButtonIndex.PRICE_CHECKER_WITHDRAW_ALL:
			player.getPriceChecker().withdrawAll();
			break;

		case ButtonIndex.PRICE_CHECKER_DEPOSIT_ALL:
			player.getPriceChecker().depositAll();
			break;

		case ButtonIndex.OPEN_ITEMS_KEPT_ON_DEATH_SCREEN:
			if (!player.busy()) {
				ItemsKeptOnDeath.open(player);
			} else {
				player.getPacketSender().sendMessage("You cannot do that right now.");
			}
			break;

		case ButtonIndex.TRADE_ACCEPT_BUTTON_1:
		case ButtonIndex.TRADE_ACCEPT_BUTTON_2:
			player.getTrading().acceptTrade();
			break;

		case ButtonIndex.TOGGLE_AUTO_RETALIATE:
		case ButtonIndex.TOGGLE_AUTO_RETALIATE_2:
		case ButtonIndex.TOGGLE_AUTO_RETALIATE_3:
			player.getCombat().setAutoRetaliate(!player.getCombat().autoRetaliate());
			break;

		case ButtonIndex.DESTROY_ITEM:
			if (player.getDestroyItem() != -1 && player.getInterfaceId() == 55) {
				player.getInventory().delete(player.getDestroyItem(),
						player.getInventory().getAmount(player.getDestroyItem()));
			}
			player.getPacketSender().sendInterfaceRemoval();
			break;

		case ButtonIndex.CANCEL_DESTROY_ITEM:
			player.getPacketSender().sendInterfaceRemoval();
			break;

		case ButtonIndex.HOME_TELEPORT_BUTTON:
			TeleportHandler.teleport(player,
					GameConstants.DEFAULT_POSITION.copy().add(Misc.getRandom(4), Misc.getRandom(2)),
					TeleportType.NORMAL);
			break;

		case ButtonIndex.AUTOCAST_BUTTON_1:
		case ButtonIndex.AUTOCAST_BUTTON_2:
			player.getPacketSender()
					.sendMessage("A spell can be autocast by simply right-clicking on it in your Magic spellbook and ")
					.sendMessage("selecting the \"Autocast\" option.");
			break;

		case ButtonIndex.CLOSE_BUTTON_1:
			player.getPacketSender().sendInterfaceRemoval();
			break;

		case ButtonIndex.FIRST_DIALOGUE_OPTION_OF_FIVE:
		case ButtonIndex.FIRST_DIALOGUE_OPTION_OF_FOUR:
		case ButtonIndex.FIRST_DIALOGUE_OPTION_OF_THREE:
		case ButtonIndex.FIRST_DIALOGUE_OPTION_OF_TWO:
			if (player.getDialogueOptions() != null) {
				player.getDialogueOptions().handleOption1(player);
			}
			break;

		case ButtonIndex.SECOND_DIALOGUE_OPTION_OF_FIVE:
		case ButtonIndex.SECOND_DIALOGUE_OPTION_OF_FOUR:
		case ButtonIndex.SECOND_DIALOGUE_OPTION_OF_THREE:
		case ButtonIndex.SECOND_DIALOGUE_OPTION_OF_TWO:
			if (player.getDialogueOptions() != null) {
				player.getDialogueOptions().handleOption2(player);
			}
			break;

		case ButtonIndex.THIRD_DIALOGUE_OPTION_OF_FIVE:
		case ButtonIndex.THIRD_DIALOGUE_OPTION_OF_FOUR:
		case ButtonIndex.THIRD_DIALOGUE_OPTION_OF_THREE:
			if (player.getDialogueOptions() != null) {
				player.getDialogueOptions().handleOption3(player);
			}
			break;

		case ButtonIndex.FOURTH_DIALOGUE_OPTION_OF_FIVE:
		case ButtonIndex.FOURTH_DIALOGUE_OPTION_OF_FOUR:
			if (player.getDialogueOptions() != null) {
				player.getDialogueOptions().handleOption4(player);
			}
			break;

		case ButtonIndex.FIFTH_DIALOGUE_OPTION_OF_FIVE:
			if (player.getDialogueOptions() != null) {
				player.getDialogueOptions().handleOption5(player);
			}
			break;
		}
	}

	public static boolean handlers(Player player, int button) {
		if (PrayerHandler.togglePrayer(player, button)) {
			return true;
		}
		if (Autocasting.toggleAutocast(player, button)) {
			return true;
		}
		if (WeaponInterfaces.changeCombatSettings(player, button)) {
			BonusManager.update(player);
			return true;
		}
		if (MagicClickSpells.handleSpell(player, button)) {
			return true;
		}
		if (Bank.handleButton(player, button, 0)) {
			return true;
		}
		if (TeleportsInterface.handleButton(player, button)) {
			return true;
		}
		if (Emotes.doEmote(player, button)) {
			return true;
		}
		if (ClanChatManager.handleButton(player, button, 0)) {
			return true;
		}
		return false;
	}
}