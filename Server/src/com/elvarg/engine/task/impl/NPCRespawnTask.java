package com.elvarg.engine.task.impl;

import com.elvarg.engine.task.Task;
import com.elvarg.world.World;
import com.elvarg.world.entity.impl.npc.NPC;
import com.elvarg.world.entity.impl.npc.bots.NPCBotHandler;

/**
 * A {@link Task} implementation which handles the respawn
 * of an npc.
 * 
 * @author Professor Oak
 */
public class NPCRespawnTask extends Task {

	public NPCRespawnTask(NPC npc, int respawn) {
		super(respawn);
		this.npc = npc;
	}

	private final NPC npc;

	@Override
	public void execute() {
		
		//Create a new npc with fresh stats..
		NPC npc_ = new NPC(npc.getId(), npc.getSpawnPosition());
		
		//Register the npc
		World.getNpcAddQueue().add(npc_);
		
		//Stop the task
		stop();
	}

}
