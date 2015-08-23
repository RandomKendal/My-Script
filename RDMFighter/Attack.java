package RDMFighter;

import org.parabot.environment.api.utils.Time;
import org.parabot.environment.scripts.framework.Strategy;
import org.rev317.min.api.methods.Inventory;
import org.rev317.min.api.methods.Npcs;
import org.rev317.min.api.wrappers.Item;
import org.rev317.min.api.wrappers.Npc;

public class Attack implements Strategy {
	/**
	 * 
	 */
	private final Main Core;
	/**
	 * @param main
	 */
	Attack(Main main) {
		Core = main;
	}

	Npc[] enemyNPCs;
	@Override
	public boolean activate() {
		Item[] foodId = Inventory.getItems(Core.FOOD.itemID);
		if(foodId.length > 0) {
			if(Core.getAttackingNPC() == null) {
				try {
					enemyNPCs = Npcs.getNearest(Core.ENEMY_NPC.npcId);
					if(enemyNPCs.length > 0) {
						return true;
					}
				} catch (Exception _e) {}
			}
		} else {
			System.out.println("Not going to attack anything; you have no valid food left!");
			Time.sleep(5000);
		}
		return false;
	}

	@Override
	public void execute() {
		if(Core.getHittingNPC() != null) {
			Npc enemyNPC = Core.getHittingNPC();
			if(enemyNPC.getHealth() != 0 && enemyNPC.getMaxHealth() >= 0 
					|| enemyNPC.getHealth() == 0 && enemyNPC.getMaxHealth() == 0) {
				enemyNPC.interact(1);
				Time.sleep(enemyNPC.distanceTo() * 400 + 400);
				return;
			}
		}
		for(Npc enemyNPC : enemyNPCs) {
			if(!enemyNPC.isInCombat()) {
				enemyNPC.interact(1);
				Time.sleep(enemyNPC.distanceTo() * 400 + 400);
				return;
			}
		}
	}
}