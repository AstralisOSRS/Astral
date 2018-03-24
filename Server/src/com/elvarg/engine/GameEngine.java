package com.elvarg.engine;

import com.elvarg.engine.task.TaskManager;
import com.elvarg.world.World;
import com.elvarg.world.content.clan.ClanChatManager;

/**
 * The engine which processes the game.
 * Also contains a logic service which can 
 * be used for asynchronous tasks such as file writing.
 * 
 * @author lare96
 * @author Professor Oak
 */
public final class GameEngine implements Runnable {
	
	@Override
	public void run() {
		try {
			
			TaskManager.sequence();
			World.sequence();
					
		} catch (Throwable e) {
			e.printStackTrace();
			World.savePlayers();
			ClanChatManager.save();
		}
	}
}