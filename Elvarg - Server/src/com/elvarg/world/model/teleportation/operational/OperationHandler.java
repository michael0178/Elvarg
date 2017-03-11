package com.elvarg.world.model.teleportation.operational;

import java.util.Arrays;
import java.util.Optional;

import com.elvarg.world.entity.impl.player.Player;
import com.google.common.collect.ImmutableSet;

/**
 * Handles functionality for operable items. Such items include Amulet of Glory,
 * Dragonfire shield, and ring of recoil. Most of these items have special
 * events upon operation, within this class will be systems that will handle
 * such events, for example the dragon fire shield operation attack.
 * 
 * @author Andys1814.
 */
public class OperationHandler {

	private final Player player;

	public OperationHandler(final Player player) {
		this.player = player;
	}

	public enum OperableItem implements Operable {
		AMULET_OF_GLORY(1712, (player) -> player.getPacketSender().sendMessage("Testing item operation")),
		DRAGONFIRE_SHIELD(11283, (player) -> player.getPacketSender().sendMessage("Testing draongfire shield operation"));

		private final int id;
		private final Operable execution;

		OperableItem(final int id, final Operable execution) {
			this.id = id;
			this.execution = execution;
		}

		@Override
		public void operate(Player player) {
			execution.operate(player);
		}

	}

	/**
	 * An {@link ImmutableSet} containing a list of items that are considered to
	 * be operable.
	 */
	public static final ImmutableSet<Integer> OPERABLE_ITEMS = ImmutableSet.of(1712);

	/**
	 * Handles the execution of an operable item with the provided item id. This
	 * method will automatically search through the {@link OperableItem} enum
	 * with the provided item id and find the appropriate enum constant to
	 * operate with. It will then call that enum constant's implementation of
	 * {@link Operable#operate}.
	 * 
	 * @param id
	 *            The item id which will be used.
	 */
	public void execute(int id) {
		Optional<OperableItem> item = Arrays.stream(OperableItem.values()).filter(field -> field.id == id).findAny();
		item.ifPresent(i -> i.operate(player));
	}
}
