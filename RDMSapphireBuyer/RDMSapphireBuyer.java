package RDMSapphireBuyer;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;

import javax.imageio.ImageIO;

import org.parabot.environment.api.interfaces.Paintable;
import org.parabot.environment.api.utils.Time;
import org.parabot.environment.api.utils.Timer;
import org.parabot.environment.input.Keyboard;
import org.parabot.environment.scripts.Category;
import org.parabot.environment.scripts.Script;
import org.parabot.environment.scripts.ScriptManifest;
import org.parabot.environment.scripts.framework.Strategy;
import org.rev317.min.Loader;
import org.rev317.min.api.methods.Game;
import org.rev317.min.api.methods.Inventory;
import org.rev317.min.api.methods.Menu;
import org.rev317.min.api.methods.Npcs;
import org.rev317.min.api.methods.Players;
import org.rev317.min.api.methods.SceneObjects;
import org.rev317.min.api.wrappers.Item;
import org.rev317.min.api.wrappers.Npc;
import org.rev317.min.api.wrappers.SceneObject;
import org.rev317.min.api.wrappers.Tile;

@ScriptManifest(
		author = "Random (Kendal)", 
		category = Category.OTHER, 
		description = "Buys sapphires out of the crafting store at ::shops. Will bank the gems at ::private. Fairly quick and efficient! Written on Fixion's request.", 
		name = "RDM SapphireBuyer", 
		servers = { "PkHonor" }, 
		version = 0.1
		)

public class RDMSapphireBuyer extends Script implements Paintable  {

	private final ArrayList<Strategy> Strategies = new ArrayList<Strategy>();
	private Timer scriptTimer = new Timer();
	private ScriptManifest Manifest = (ScriptManifest) RDMSapphireBuyer.class.getAnnotation(ScriptManifest.class);
	
	private static Image backgroundIMG;
	
	private static int SHOP_INTERFACE = 3824;
	private static int SHOP_STOCK = 3900;
	private static int BANK_INTERFACE = 23350;
	
	private static int DEPOSIT_ALL = 432;
	
	private static int CRAFTING_STORE = 545;

	private static int SAPPHIRE = 1624;
	
	private static Tile ShopLocation = new Tile(3080, 3510);
	
	int boughtSapphires = 0;
	
	@Override
	public boolean onExecute() {
		backgroundIMG = getImage("http://i.imgur.com/cciyTiy.png");
		
		Strategies.add(new BuyItems());
		Strategies.add(new OpenShop());
		Strategies.add(new OpenBank());
		Strategies.add(new DepositItems());
		
		provide(Strategies);
		return true;
	}
	
	@Override
	public void onFinish() {
        System.out.println("Ran " + Manifest.name() + " v" + Manifest.version() + " for: " + scriptTimer.toString());
        System.out.println("Bought " + boughtSapphires + " sapphires (" + scriptTimer.getPerHour(boughtSapphires) + " Gems/PH).");
        System.out.println("Thank you for using my script!");
	}
	
	public class BuyItems implements Strategy {
		@Override
		public boolean activate() {
			if(Game.getOpenInterfaceId() == SHOP_INTERFACE && Inventory.getCount() < 28)
				return true;
			return false;
		}

		@Override
		public void execute() {
			int[] shopIDs = Loader.getClient().getInterfaceCache()[SHOP_STOCK].getItems();
			for(int index = 0; index < shopIDs.length; index++) {
				if(shopIDs[index] == SAPPHIRE) {
					Menu.sendAction(53, SAPPHIRE - 1, index, 3900);
					Time.sleep(() -> Inventory.getCount() == 28, 500);
					return;
				}
			}
		}
	}
	
	public class OpenShop implements Strategy {
		Npc[] Bartender;
		@Override
		public boolean activate() {
			if(Game.getOpenInterfaceId() != SHOP_INTERFACE && Inventory.getCount() < 28) {
				Bartender = Npcs.getNearest(CRAFTING_STORE);
				if(Bartender.length > 0 && Bartender != null)
					return true;
				else {
					Keyboard.getInstance().sendKeys("::shops");
					Time.sleep(() -> Players.getMyPlayer().getLocation() == ShopLocation, 3000);
				}
			}
			return false;
		}

		@Override
		public void execute() {
			try {
				Bartender[0].interact(0);
				Time.sleep(() -> (Game.getOpenInterfaceId() == SHOP_INTERFACE), 
						(Bartender[0].distanceTo() * 400 + 600));
			} catch (Exception _e) {}
		}
	}
	
	public class OpenBank implements Strategy {
		@Override
		public boolean activate() {
			if(Game.getOpenInterfaceId() != BANK_INTERFACE && Inventory.getCount() == 28)
				return true;
			return false;
		}

		@Override
		public void execute() {
			SceneObject[] BankBooths = SceneObjects.getNearest(2213);
			if(BankBooths.length > 0) {
				if(BankBooths[0].distanceTo() > 12) {
					Keyboard.getInstance().sendKeys("::private");
					Time.sleep(3000);
					return;
				}
				SceneObject BankBooth = BankBooths[0];
				BankBooth.interact(0);
				Time.sleep(BankBooth.getLocation().distanceTo() * 400 + 400);
				return;
			} else {
				Keyboard.getInstance().sendKeys("::private");
				Time.sleep(3000);
			}
			return;
		}
	}
	
	public class DepositItems implements Strategy {
		@Override
		public boolean activate() {
			if(Game.getOpenInterfaceId() == BANK_INTERFACE && Inventory.getCount() == 28)
				return true;
			return false;
		}

		@Override
		public void execute() {
			Item[] depositableItems = Inventory.getItems(SAPPHIRE);
			if(depositableItems.length > 0 && depositableItems != null) {
				boughtSapphires += Inventory.getCount(SAPPHIRE);
				Menu.sendAction(DEPOSIT_ALL, depositableItems[0].getId() - 1, depositableItems[0].getSlot(), 5064);
			}
			Time.sleep(() -> (Inventory.getCount() != 28), 1000);
		}
	}
	
	@Override
	public void paint(Graphics Graphs) {
		Graphs.drawImage(backgroundIMG, 400, 5, null);
		
		Graphics2D g = (Graphics2D) Graphs;
		g.setColor(Color.WHITE);
		g.drawString(Manifest.name(), 402, 20);
		g.drawString("Runtime: " + scriptTimer.toString(), 404, 36);
		g.drawString("Gems:", 405, 52);
		g.drawString("P/H:", 405, 64);

		g.drawString("" + boughtSapphires, 445, 52);
		g.drawString("" + scriptTimer.getPerHour(boughtSapphires), 445, 64);
	}
	
	public static Image getImage(String url) {
		try {
			return ImageIO.read(new URL(url));
		} catch (IOException e) {
			return null;
		}
	}
}