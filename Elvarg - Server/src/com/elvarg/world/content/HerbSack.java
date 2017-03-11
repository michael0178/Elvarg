package com.elvarg.world.content;

import java.util.Arrays;
import java.util.Iterator;
import java.util.Objects;
import java.util.function.Predicate;

import com.elvarg.cache.impl.definitions.ItemDefinition;
import com.elvarg.world.entity.impl.player.Player;
import com.elvarg.world.model.Item;
import com.google.common.collect.HashMultiset;
import com.google.common.collect.Multiset;
import com.google.common.collect.Multisets;

/**
 * Handles all functionality for the Herb Sack item from OSRS.
 * 
 * @author Andys1814.
 */
public final class HerbSack {

	private Player player;

	public HerbSack(Player player) {
		this.player = player;
	}

	/**
	 * This collection handles the constants of the Herb Sack.
	 */
	private final Multiset<Integer> herbSack = HashMultiset.create();

	/**
	 * Handles the action of filling the Herb Sack.
	 */
	public void fillSack() {
		player.getPacketSender().sendMessage("You search your inventory for herbs appropriate to put in the sack...");
		if (Arrays.stream(player.getInventory().getItems()).noneMatch(isGrimyHerb())) {
			player.getPacketSender().sendMessage("There are no herbs in your inventory that can be added to the sack.");
			return;
		}
		Arrays.stream(player.getInventory().getItems()).filter(Objects::nonNull).filter(isGrimyHerb()).forEach(herb -> {
			if (herbSack.count(herb.getId()) < 30) {
				herbSack.add(herb.getId());
				player.getInventory().delete(herb);
			}
		});
		player.getPacketSender().sendMessage("You add the herb(s) to your sack.");
	}

	/**
	 * Handles the action of emptying the Herb Sack.
	 */
	public void emptySack() {
		if (herbSack.isEmpty()) {
			player.getPacketSender().sendMessage("The herb sack is already empty.");
			return;
		}
		if (player.getInventory().getFreeSlots() <= 0) {
			player.getPacketSender()
					.sendMessage("You don't have enough inventory space to empty the contents of this sack.");
			return;
		}
		for (Iterator<Integer> it = herbSack.iterator(); it.hasNext();) {
			if (player.getInventory().getFreeSlots() <= 0) {
				return;
			}
			int herb = it.next();
			player.getInventory().add(new Item(herb));
			it.remove();
		}
	}

	/**
	 * Handles the action of checking the contents of the Herb Sack. It will
	 * automatically outprint the herbs which have the highest amount of herbs,
	 * thanks to {@link Multisets#copyHighestCountFirst}.
	 */
	public void checkSack() {
		player.getPacketSender().sendMessage("You look in your herb sack and see:");
		if (herbSack.isEmpty()) {
			player.getPacketSender().sendMessage("The herb sack is empty.");
			return;
		}
		for (int herbId : Multisets.copyHighestCountFirst(herbSack).elementSet()) {
			player.getPacketSender()
					.sendMessage(herbSack.count(herbId) + " x " + ItemDefinition.getDefinitions()[herbId].getName());
		}
	}

	/**
	 * This predicate method is used while filling the sack to filter out items
	 * that are not allowed to be stored in the sack. Basically, anything with
	 * "Grimy" in the beginning of the name can be stored in the sack so we
	 * generalize it as such.
	 * 
	 * @return The result of the predicate function.
	 */
	private final Predicate<Item> isGrimyHerb() {
		return herb -> herb.getDefinition().getName().startsWith("Grimy");
	}

}
