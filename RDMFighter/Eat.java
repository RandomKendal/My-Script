package RDMFighter;

import org.parabot.environment.api.utils.Time;
import org.parabot.environment.scripts.framework.Strategy;
import org.rev317.min.api.methods.Inventory;
import org.rev317.min.api.methods.Menu;
import org.rev317.min.api.methods.Skill;
import org.rev317.min.api.wrappers.Item;

public class Eat implements Strategy {
	/**
	 * 
	 */
	private final Main Core;

	/**
	 * @param main
	 */
	Eat(Main main) {
		Core = main;
	}

	@Override
	public boolean activate() {
		double percentHealth = ((double) Skill.HITPOINTS.getLevel()) / Core.getHPLevel() * 100;
		if(percentHealth <= Core.eatAtPercent)
			return true;
		return false;
	}

	@Override
	public void execute() {
		Item[] foodId = Inventory.getItems(Core.FOOD.itemID);
		if(foodId.length > 0 && foodId != null) {
			Menu.sendAction(74, foodId[0].getId() - 1, foodId[0].getSlot(), 3214);
			Time.sleep(1000);
		} else {
	        System.out.println("No more food left!");
	        Time.sleep(500);
		}
	}
}