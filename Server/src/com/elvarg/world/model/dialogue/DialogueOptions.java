package com.elvarg.world.model.dialogue;

import com.elvarg.world.entity.impl.player.Player;

/**
 * An abstract class for handling dialogue options.
 * 
 * @author Professor Oak
 */
public abstract class DialogueOptions {

	public abstract void handleOption(Player player, int option);
}
