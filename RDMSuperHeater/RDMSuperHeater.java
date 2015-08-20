package RRDMSuperHeater;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.io.IOException;
import java.net.URL;
import java.text.DecimalFormat;
import java.util.ArrayList;

import javax.imageio.ImageIO;

import org.parabot.environment.api.interfaces.Paintable;
import org.parabot.environment.api.utils.Time;
import org.parabot.environment.api.utils.Timer;
import org.parabot.environment.input.Mouse;
import org.parabot.environment.scripts.Category;
import org.parabot.environment.scripts.Script;
import org.parabot.environment.scripts.ScriptManifest;
import org.parabot.environment.scripts.framework.Strategy;
import org.rev317.min.Loader;
import org.rev317.min.api.methods.Game;
import org.rev317.min.api.methods.Inventory;
import org.rev317.min.api.methods.Menu;
import org.rev317.min.api.methods.Npcs;
import org.rev317.min.api.methods.SceneObjects;
import org.rev317.min.api.methods.Skill;
import org.rev317.min.api.wrappers.Item;
import org.rev317.min.api.wrappers.Npc;
import org.rev317.min.api.wrappers.SceneObject;

@ScriptManifest(
		author = "Random (Kendal)", 
		category = Category.SMITHING, 
		description = "Super heats Rune Ore & Coal into Rune Bars. Start near a butler/bank with a Staff of Fire/Fire Runes and Nature Runes. Standard Spellbook is NOT required (Damn Mike, you have some sloppy coding).", 
		name = "RDM SuperHeater", 
		servers = { "PkHonor" }, 
		version = 0.1
		)
public class RDMSuperHeater extends Script implements Paintable {

	private final ArrayList<Strategy> Strategies = new ArrayList<Strategy>();
	private Timer scriptTimer = new Timer();
	private ScriptManifest Manifest = (ScriptManifest) RDMSuperHeater.class.getAnnotation(ScriptManifest.class);

	DecimalFormat formatter = new DecimalFormat("#,###,###,###");
	
	int startExperience = Skill.SMITHING.getExperience();
	
	private static Image backgroundIMG;
	
	private static int COAL_ID = 454;
	private static int RUNE_ORE_ID = 452;
	private static int RUNE_BAR_ID = 2364;
	private static int FIRE_RUNES_ID = 555;
	private static int FIRE_STAFF_ID = 1388;
	private static int NATURE_RUNES_ID = 562;
	
	private static int BANK_INTERFACE = 23350;
	private static int WITHDRAW_ALL = 53;
	private static int WITHDRAW_5 = 78;
	private static int DEPOSIT_ALL = 432;
	
	
	@Override
	public boolean onExecute() {

		backgroundIMG = getImage("http://i.imgur.com/cciyTiy.png");
		
		Strategies.add(new OpenBank());
		Strategies.add(new BankOre());
		Strategies.add(new SuperHeat());
		
		provide(Strategies);
		
		int weaponID = Loader.getClient().getInterfaceCache()[1688].getItems()[3];
		if(Inventory.getCount(true, NATURE_RUNES_ID) >= 1 && (weaponID == FIRE_STAFF_ID || Inventory.getCount(true, FIRE_RUNES_ID) >= 5))
			return true;
		else {
			System.out.println("Make sure you got a Fire Staff or Fire Runes, and enough Natures!");
			return false;
		}
	}
	
	public static Image getImage(String url) {
		try {
			return ImageIO.read(new URL(url));
		} catch (IOException e) {
			return null;
		}
	}
	
	@Override
	public void onFinish() {
		int endExperience = Skill.SMITHING.getExperience();
        System.out.println("== " + Manifest.name() + " v" + Manifest.version() + " == ");
        
        System.out.println("Ran for: " + scriptTimer.toString());        
        System.out.println("Experience gained: " + formatter.format((endExperience - startExperience)));
        System.out.println("XP gained P/H: " + formatter.format(scriptTimer.getPerHour((endExperience - startExperience))));
        System.out.println("=== Thanks for using! === ");
	}
	
	@Override
	public void paint(Graphics Graphs) {
		Graphs.drawImage(backgroundIMG, 400, 5, null);
		
		Graphics2D g = (Graphics2D) Graphs;
		g.setColor(Color.WHITE);
		g.drawString(Manifest.name(), 407, 20);
		g.drawString("Runtime: " + scriptTimer.toString(), 404, 36);
		g.drawString("XP:", 405, 52);
		g.drawString("P/H:", 404, 64);

		g.drawString(formatter.format((Skill.SMITHING.getExperience() - startExperience)), 434, 52);
		g.drawString(formatter.format(scriptTimer.getPerHour((Skill.SMITHING.getExperience() - startExperience))), 434, 64);
	}
	
	public class OpenBank implements Strategy {
		@Override
		public boolean activate() {
			if((Inventory.getCount(COAL_ID) < 4 || Inventory.getCount(RUNE_ORE_ID) == 0) && Game.getOpenInterfaceId() != BANK_INTERFACE)
				return true;
			return false;
		}

		@Override
		public void execute() {
			SceneObject[] obj = SceneObjects.getNearest(2213);
			if(obj.length > 0) {
				SceneObject bank = obj[0];
				bank.interact(0);
				sleep(bank.getLocation().distanceTo() * 500);
				return;
			} else {
				Npc[] butler = Npcs.getNearest(4241);

				if (butler.length > 0) {
					try {
						butler[0].interact(2);
						sleep(butler[0].getLocation().distanceTo() * 800);
						return;
					} catch(Exception _e) {
						System.out.println("Prevented an error! - Nulled NPC");
					}
				} else {
					System.out.println("Unable to find a valid bank/butler.");
				}
			}
			return;
		}
	}
	
	public class BankOre implements Strategy {
		@Override
		public boolean activate() {
			if((Inventory.getCount(COAL_ID) < 4 || Inventory.getCount(RUNE_ORE_ID) == 0 || Inventory.getCount() != 28) && Game.getOpenInterfaceId() == BANK_INTERFACE)
				return true;
			return false;
		}

		@Override
		public void execute() {			
			if(Inventory.getCount(RUNE_BAR_ID) > 0) {
				Item[] RuneBar = Inventory.getItems(RUNE_BAR_ID);
				Menu.sendAction(DEPOSIT_ALL, RUNE_BAR_ID - 1, RuneBar[0].getSlot(), 5064);
				sleep(200);
			} else if(Inventory.getCount(RUNE_ORE_ID) == 0) {
				if(Inventory.getCount(COAL_ID) > 0) {
					Item[] CoalOre = Inventory.getItems(COAL_ID);
					Menu.sendAction(DEPOSIT_ALL, COAL_ID - 1, CoalOre[0].getSlot(), 5064);
					sleep(200);
				}
			}
			
			int[] bankIds = Loader.getClient().getInterfaceCache()[5382].getItems();
			if(Inventory.getCount(RUNE_ORE_ID) == 0) {
				for (int i = 0; i < bankIds.length; i++) {
					if (bankIds[i] == RUNE_ORE_ID) {
						Menu.sendAction(WITHDRAW_5, RUNE_ORE_ID - 1, i, 5382);
						sleep(500);
						return;
					}
				}
			} else {
				for (int i = 0; i < bankIds.length; i++) {
					if (bankIds[i] == COAL_ID) {
						Menu.sendAction(WITHDRAW_ALL, COAL_ID - 1, i, 5382);
						sleep(500);
						return;
					}
				}
			}
		}
	}
	
	public class SuperHeat implements Strategy {
		@Override
		public boolean activate() {
			if(Inventory.getCount(RUNE_ORE_ID) > 0 && Inventory.getCount(COAL_ID) >= 4 && Inventory.getCount(true, NATURE_RUNES_ID) >= 1)
				return true;
			return false;
		}

		@Override
		public void execute() {
			if (Game.getOpenInterfaceId() == 23350) {
				Mouse.getInstance().click(486, 27, true);
				sleep(200);
				return;
			}
			
			Item[] RuneOre = Inventory.getItems(RUNE_ORE_ID);
	        if(RuneOre != null && Inventory.getCount(RUNE_ORE_ID) > 0) {
	        	try {
					Time.sleep(400);
					Menu.sendAction(626, RUNE_ORE_ID - 1, RuneOre.hashCode(), 1173);
			        Time.sleep(400);
					Menu.sendAction(543, RUNE_ORE_ID - 1, RuneOre[RuneOre.length - 1].getSlot(), 3214);
					Time.sleep(400);
	        	} catch(Exception _e) {
					System.out.println("Prevented an error! - Nulled Items");
				}
	        }
		}
	}
}