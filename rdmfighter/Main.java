package rdmfighter;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.lang.reflect.Field;
import java.util.ArrayList;

import org.parabot.core.Context;
import org.parabot.environment.api.interfaces.Paintable;
import org.parabot.environment.api.utils.Time;
import org.parabot.environment.api.utils.Timer;
import org.parabot.environment.scripts.Category;
import org.parabot.environment.scripts.Script;
import org.parabot.environment.scripts.ScriptManifest;
import org.parabot.environment.scripts.framework.Strategy;
import org.rev317.min.Loader;
import org.rev317.min.api.methods.Npcs;
import org.rev317.min.api.methods.Players;
import org.rev317.min.api.methods.Skill;
import org.rev317.min.api.wrappers.Npc;

import rdmfighter.data.EnemyNpc;
import rdmfighter.data.Food;
import rdmfighter.strategies.Attack;
import rdmfighter.strategies.Eat;
import rdmfighter.strategies.Loot;

@ScriptManifest(
		author = "Random (Kendal)", 
		category = Category.COMBAT, 
		description = "The script can be used to fight several monsters. Also supports food eating at a specified percentage. (Made for my SW application)", 
		name = "RDM Fighter", 
		servers = { "PkHonor" }, 
		version = 0.1
		)

public class Main extends Script implements Paintable {

	private final ArrayList<Strategy> Strategies = new ArrayList<Strategy>();
	public Timer scriptTimer = new Timer();
	public ScriptManifest Manifest = (ScriptManifest) Main.class.getAnnotation(ScriptManifest.class);
	
	public int food_index = 0;
	public int npc_index = 3;
	public int eatAtPercent = 50;
	public int[] LootableItems;
	public Food FOOD;
	public EnemyNpc ENEMY_NPC;
	
	boolean startScript = false;

	//Paint:
	String enemyInformation;
	String foodInformation;
	int biggestWidth = -1;
	
	
	@Override
	public boolean onExecute() {
		GUI g = new GUI(this);
        while(g.isVisible()){
                Time.sleep(100);}
		
		if(!startScript)
			return false;
        
		FOOD = Food.values()[food_index];
		ENEMY_NPC = EnemyNpc.values()[npc_index];
		
		Context.getInstance().getRandomHandler().clearActiveRandoms();
		
		Strategies.add(new Eat(this));
		Strategies.add(new Loot(this));
		Strategies.add(new Attack(this));

        System.out.println("");
        System.out.println("=== Starting Script! ===");
        System.out.println("Attacking: " + ENEMY_NPC.name);
        System.out.println("Eating: " + FOOD.name + " at " + eatAtPercent + "% health");
        
        if(LootableItems != null) {
	        String Lootables = "";
	        for(int Lootable : LootableItems) {
	        	Lootables += Lootable + ", ";
	        }
	        System.out.println("Looting: " + Lootables);
		} else {
	        System.out.println("Not looting anything.");
		}
        System.out.println("========================");
        System.out.println("");
        
        enemyInformation = ENEMY_NPC.name + " (#" + ENEMY_NPC.npcId[0] + ")";
		foodInformation = FOOD.name + " at " + eatAtPercent + "%";
        
		provide(Strategies);
		return true;
	}
	
	@Override
	public void onFinish() {
        System.out.println("Ran " + Manifest.name() + " v" + Manifest.version() + " for: " + scriptTimer.toString());
        System.out.println("Thank you for using my script!");
	}
	
	public Npc getHittingNPC() {
		for(Npc checker : Npcs.getNearest()) {
			try {
				if(checker.getInteractingCharacter().getIndex() == Players.getMyPlayer().getIndex() 
						&& Players.getMyPlayer().isInCombat()) {
					return checker;
				}
			} catch (Exception _e) {}
		}
		return null;
	}
	
	public Npc getAttackingNPC() {
		for(Npc checker : Npcs.getNearest()) {
			try {
				if(checker.getInteractingCharacter().getIndex() == Players.getMyPlayer().getIndex() 
						&& Players.getMyPlayer().getInteractingCharacter().getIndex() == checker.getIndex()) {
					return checker;
				}
			} catch (Exception _e) {}
		}
		return null;
	}
	
	public String getText(int id) {
        try {
            Field text = Loader.getClient().getInterfaceCache()[id].getClass().getDeclaredField("R");
            text.setAccessible(true);

            return (String) text.get(Loader.getClient().getInterfaceCache()[id]);

        } catch (NoSuchFieldException | IllegalAccessException e) {
            return null;
        }
    }
	
	public Integer getHPLevel() {
		return ((Skill.HITPOINTS.getRealLevel() > 99) ? 99 : Skill.HITPOINTS.getRealLevel());
	}
	
	@Override
	public void paint(Graphics Graphs) {
		if(startScript) {
			try {
				Graphics2D g = (Graphics2D) Graphs;
				Color orange = new Color(255, 152, 31);
				
				if(biggestWidth == -1) {
					int enemyStringWidth = (int) g.getFontMetrics().getStringBounds(enemyInformation, g).getWidth();
					int foodStringWidth = (int) g.getFontMetrics().getStringBounds(foodInformation, g).getWidth();
					biggestWidth = ((enemyStringWidth > foodStringWidth) ? enemyStringWidth : foodStringWidth);
				}
				
				g.setColor(new Color(0, 0, 0, 160));
				g.fillRect(0, 0, 112, 34);
				g.fillRect(0, 34, 47 + biggestWidth, 31);
				
				int Y1 = 16;
				int Y2 = 48;
				
				drawDString(Manifest.name() + " v" + Manifest.version(), 6, Y1, g, orange);
				drawDString("Runtime: " + scriptTimer.toString(), 6, Y1 + 13, g, orange);
	
				drawDString("Killing:", 6, Y2, g, orange);
				drawDString("Food:", 6, Y2 + 13, g, orange);
				drawDString(enemyInformation, 45, Y2, g, Color.white);
				drawDString(foodInformation, 45, Y2 + 13, g, Color.white);
				
				Npc fightingNPC = getAttackingNPC();
				if(fightingNPC != null && fightingNPC.getMaxHealth() > 0) {
					double percentHealth = ((double) fightingNPC.getHealth()) / fightingNPC.getMaxHealth();
					g.setColor(new Color(0, 0, 0, 160));
					g.fillRect(0, 70, 160, 25);
					g.setColor(Color.red);
					g.fillRect(6, 76, 148, 13);
					g.setColor(new Color(0, 153, 0));
					g.fillRect(6, 76, (int) (((double) 148) * percentHealth), 13);
					if(fightingNPC.getHealth() != 0)
						drawDString(fightingNPC.getHealth() + " / " + fightingNPC.getMaxHealth() + " HP", 10, 87, g, Color.white);
					else
						drawDString("(DEAD)", 10, 87, g, Color.white);
				}
			} catch (Exception _e) {}
		}
	}
	
	public void drawDString(String text, int x, int y, Graphics2D g, Color col) {
		g.setColor(Color.black);
		g.drawString(text, x + 1, y + 1);
		g.setColor(new Color(255, 152, 31));
		g.setColor(col);
		g.drawString(text, x, y);
	}
}