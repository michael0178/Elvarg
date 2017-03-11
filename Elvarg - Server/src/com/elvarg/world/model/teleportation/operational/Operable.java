package com.elvarg.world.model.teleportation.operational;

import com.elvarg.world.entity.impl.player.Player;

/**
 * This functional interface serves that the skeleton for operable items.
 *
 * @author Andys1814
 */
@FunctionalInterface
public interface Operable {
	
	/*
	 * Handles the execution of the operable.
	 */
	void operate(Player player);

}
