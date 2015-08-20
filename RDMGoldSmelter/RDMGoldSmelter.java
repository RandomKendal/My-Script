package RDMGoldSmelter;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;

import javax.imageio.ImageIO;

import org.parabot.environment.api.interfaces.Paintable;
import org.parabot.environment.api.utils.Random;
import org.parabot.environment.api.utils.Time;
import org.parabot.environment.api.utils.Timer;
import org.parabot.environment.input.Keyboard;
import org.parabot.environment.scripts.Category;
import org.parabot.environment.scripts.Script;
import org.parabot.environment.scripts.ScriptManifest;
import org.parabot.environment.scripts.framework.SleepCondition;
import org.parabot.environment.scripts.framework.Strategy;
import org.rev317.min.Loader;
import org.rev317.min.api.events.MessageEvent;
import org.rev317.min.api.events.listeners.MessageListener;
import org.rev317.min.api.methods.Game;
import org.rev317.min.api.methods.Inventory;
import org.rev317.min.api.methods.Menu;
import org.rev317.min.api.methods.Npcs;
import org.rev317.min.api.methods.Players;
import org.rev317.min.api.methods.SceneObjects;
import org.rev317.min.api.wrappers.Npc;
import org.rev317.min.api.wrappers.SceneObject;

@ScriptManifest(
		author = "Random (Kendal)", 
		category = Category.SMITHING, 
		description = "Smelts gold near ANY furnace and bank. Start with an empty inventory! Written on Fixion's request.", 
		name = "RDM GoldSmelter", 
		servers = { "PkHonor" }, 
		version = 0.1
		)

public class RDMGoldSmelter extends Script implements MessageListener, Paintable {

	private final ArrayList<Strategy> Strategies = new ArrayList<Strategy>();
	private Timer scriptTimer = new Timer();
	private ScriptManifest Manifest = (ScriptManifest) RDMGoldSmelter.class.getAnnotation(ScriptManifest.class);
	
	private static Image backgroundIMG;
	
	private static int[] FURNACE = { 2643, 3994, 2781, 11666 };
	
	private static int SMELT_INTERFACE = 2400;
	private static int BANK_INTERFACE = 23350;

	private static int BAR_BUTTON = 4000;
	private static int DEPOSIT_ALL_BUTTON = 23412;
	private static int WITHDRAW_ALL = 53;
	
	private static int SMELT_ANIMATION = 899;
	
	private static int ORE_GOLD = 445;
	
	int moltenBars = 0;
	
	boolean isSmelting = false;
	int smeltCooldown = 10;
	
	@Override
	public boolean onExecute() {
		backgroundIMG = getImage("http://i.imgur.com/cciyTiy.png");
		
		Strategies.add(new CheckIfSmelting());
		Strategies.add(new SmeltBars());
		Strategies.add(new ClickFurnace());
		Strategies.add(new OpenBank());
		Strategies.add(new UseBank());
		
		provide(Strategies);
		return true;
	}
	
	@Override
	public void onFinish() {
        System.out.println("Ran " + Manifest.name() + " v" + Manifest.version() + " for: " + scriptTimer.toString());
        System.out.println("Smelted " + moltenBars + " bars (" + scriptTimer.getPerHour(moltenBars) + " Bars/PH).");
        System.out.println("Thank you for using my script!");
	}
	
	public class CheckIfSmelting implements Strategy {
		@Override
		public boolean activate() {
			if(isSmelting)
				return true;
			return false;
		}

		@Override
		public void execute() {
			if(Inventory.getCount(ORE_GOLD) == 0) {
				isSmelting = false;
				return;
			}
			if(Players.getMyPlayer().getAnimation() == SMELT_ANIMATION) {
				smeltCooldown = 4;
				Time.sleep(500);
			} else {
				smeltCooldown--;
				if(smeltCooldown <= 0) {
					isSmelting = false;
				}
				Time.sleep(500);
			}
		}
	}
	
	public class SmeltBars implements Strategy {
		@Override
		public boolean activate() {
			if(Game.getOpenBackDialogId() == SMELT_INTERFACE && !isSmelting && Inventory.getCount(ORE_GOLD) > 0)
				return true;
			return false;
		}

		@Override
		public void execute() {
			Menu.clickButton(BAR_BUTTON);
			Time.sleep(1000);
			Keyboard.getInstance().sendKeys(Random.between(28, 99) + "");
			Time.sleep(new SleepCondition() {
                @Override
                public boolean isValid() {
                      return (Players.getMyPlayer().getAnimation() == SMELT_ANIMATION);
                }
            }, 3000);
			
			isSmelting = true;
			smeltCooldown = 4;
		}
	}
	
	public class ClickFurnace implements Strategy {
		SceneObject[] Furnace;
		@Override
		public boolean activate() {
			if(Game.getOpenBackDialogId() != SMELT_INTERFACE && !isSmelting && Inventory.getCount(ORE_GOLD) > 0) {
				Furnace = SceneObjects.getNearest(FURNACE);
				if(Furnace.length > 0 && Furnace != null)
					return true;
			}
			return false;
		}

		@Override
		public void execute() {
			try {
				Furnace[0].interact(0);
				Time.sleep(() -> (Game.getOpenBackDialogId() == SMELT_INTERFACE), 
						(Furnace[0].distanceTo() * 400 + 600));
			} catch (Exception _e) {}
		}
	}
	
	public class OpenBank implements Strategy {
		@Override
		public boolean activate() {
			if(Game.getOpenInterfaceId() != BANK_INTERFACE && Inventory.getCount(ORE_GOLD) == 0)
				return true;
			return false;
		}

		@Override
		public void execute() {
			SceneObject[] BankBooths = SceneObjects.getNearest(2213);
			if(BankBooths.length > 0) {
				SceneObject BankBooth = BankBooths[0];
				BankBooth.interact(0);
				Time.sleep(BankBooth.getLocation().distanceTo() * 400 + 400);
				return;
			} else {
				Npc[] Butler = Npcs.getNearest(4241);
				if (Butler.length > 0) {
					try {
						Butler[0].interact(2);
						sleep(Butler[0].getLocation().distanceTo() * 400 + 400);
						return;
					} catch(Exception _e) {
						System.out.println("Prevented an error! - Nulled NPC");
					}
				} else {
					System.out.println("Unable to find a valid bank/butler.");
					sleep(1000);
				}
			}
			return;
		}
	}
	
	public class UseBank implements Strategy {
		@Override
		public boolean activate() {
			if(Game.getOpenInterfaceId() == BANK_INTERFACE && Inventory.getCount(ORE_GOLD) == 0)
				return true;
			return false;
		}

		@Override
		public void execute() {
			if(Inventory.getCount() > 0) {
				Menu.clickButton(DEPOSIT_ALL_BUTTON);
				Time.sleep(200);
			}
			int[] bankIDs = Loader.getClient().getInterfaceCache()[5382].getItems();
			for (int i = 0; i < bankIDs.length; i++) {
				if (bankIDs[i] == ORE_GOLD) {
					Menu.sendAction(WITHDRAW_ALL, bankIDs[i] - 1, i, 5382);
					Time.sleep(() -> Inventory.getCount(ORE_GOLD) > 0, 1000);
					return;
				}
			}
		}
	}
	
	@Override
	public void messageReceived(MessageEvent message) {
		if(message.getType() == 0) {
			String msg = message.getMessage().toLowerCase();
			if(msg.contains("you smelt the")) {
				moltenBars++;
				isSmelting = true;
				smeltCooldown = 4;
			}
			if(msg.contains("you do not have any ore of this type")) {
				isSmelting = false;
			}
		}
	}
	
	@Override
	public void paint(Graphics Graphs) {
		Graphs.drawImage(backgroundIMG, 400, 5, null);
		
		Graphics2D g = (Graphics2D) Graphs;
		g.setColor(Color.WHITE);
		g.drawString(Manifest.name(), 406, 20);
		g.drawString("Runtime: " + scriptTimer.toString(), 404, 36);
		g.drawString("Bars:", 405, 52);
		g.drawString("P/H:", 405, 64);

		g.drawString("" + moltenBars, 440, 52);
		g.drawString("" + scriptTimer.getPerHour(moltenBars), 440, 64);
	}
	
	public static Image getImage(String url) {
		try {
			return ImageIO.read(new URL(url));
		} catch (IOException e) {
			return null;
		}
	}
}