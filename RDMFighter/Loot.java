package RDMFighter;

import org.parabot.environment.api.utils.Time;
import org.parabot.environment.scripts.framework.Strategy;
import org.rev317.min.api.methods.GroundItems;
import org.rev317.min.api.wrappers.GroundItem;

public class Loot implements Strategy {
	/**
	 * 
	 */
	private final Main Core;

	/**
	 * @param main
	 */
	Loot(Main main) {
		Core = main;
	}

	GroundItem[] foundItems;
	
	@Override
	public boolean activate() {
		if(Core.LootableItems != null) {
			foundItems = GroundItems.getNearest(Core.LootableItems);
			if(foundItems.length > 0) {
				return true;
			}
		}
		return false;
	}

	@Override
	public void execute() {
		for(GroundItem foundItem : foundItems) {
			foundItem.take();
			Time.sleep(foundItem.distanceTo() * 400 + 400);
		}
	}
}