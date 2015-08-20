package RDMFarmer;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.net.URL;
import java.text.DecimalFormat;
import java.util.ArrayList;

import javax.imageio.ImageIO;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import javax.swing.border.EmptyBorder;

import org.parabot.environment.api.interfaces.Paintable;
import org.parabot.environment.api.utils.Time;
import org.parabot.environment.api.utils.Timer;
import org.parabot.environment.input.Mouse;
import org.parabot.environment.scripts.Category;
import org.parabot.environment.scripts.Script;
import org.parabot.environment.scripts.ScriptManifest;
import org.parabot.environment.scripts.framework.Strategy;
import org.rev317.min.api.methods.Game;
import org.rev317.min.api.methods.Inventory;
import org.rev317.min.api.methods.Menu;
import org.rev317.min.api.methods.Npcs;
import org.rev317.min.api.methods.Players;
import org.rev317.min.api.methods.SceneObjects;
import org.rev317.min.api.methods.Skill;
import org.rev317.min.api.wrappers.Area;
import org.rev317.min.api.wrappers.Item;
import org.rev317.min.api.wrappers.Npc;
import org.rev317.min.api.wrappers.SceneObject;
import org.rev317.min.api.wrappers.Tile;

@ScriptManifest(
		author = "Random (Kendal)", 
		category = Category.FARMING, 
		description = "Farms ANY Herb in PkHonor. Start with a Seed Dipper, Spade, Rake and the usuable seeds in your inventory. If you don't have a Woad plant, you might want to bring some cash for Plant Cures aswell. Also supports Super Compost!", 
		name = "RDM Farmer", 
		servers = { "PkHonor" }, 
		version = 0.11
		)

public class RDMFarmer extends Script implements Paintable {

	private final ArrayList<Strategy> Strategies = new ArrayList<Strategy>();
	private Timer scriptTimer;
	private ScriptManifest Manifest = (ScriptManifest) RDMFarmer.class.getAnnotation(ScriptManifest.class);

	DecimalFormat formatter = new DecimalFormat("#,###,###,###");
	
	int startExperience = Skill.FARMING.getExperience();
	int CollectedHerbs = 0;
	
	private static Image backgroundIMG;
	
	private static String[] HERB_NAMES = {"Guam", "Marrentill", "Tarromin", "Harralander", "Ranarr", "Toadflax", "Irit", 
											"Avantoe", "Kwuarm", "Snapdragon", "Cadantine", "Lantadyme", "Dwarf Weed", "Torstol"};
	private static int[] HERB_IDS = {250, 252, 254, 256, 258, 2999, 260, 262, 264, 266, 3001, 2482, 268, 270};
	
	private static int RAKE = 5342;
	private static int SEED_DIPPER = 5344;
	private static int SPADE = 953;
	private static int PLANT_CURE = 6037;
	private static int EMPTY_VIAL = 230;
	private static int EMPTY_BUCKET = 1926;
	private static int MAGIC_SECATEURS = 7409;
	private static int SUPERCOMPOST = 6035;
	private static int COINS = 996;
	
	private static int[] DISEASED_PLANTS = {8144, 8145, 8146};
	private static int[] GRASSY_PATCHES = {8150, 8151, 8152, 8153};
	private static int EMPTY_PATCH = 8132;
	private static int GROWN_HERB = 8143;
	private static int DEPOSIT_BOX = 9398;
	
	private static int[] FARMING_SHOPS = {2323, 2324, 2325, 2326};
	
	private static int SHOP_INTERFACE = 3824;
	private static int BANK_INTERFACE = 23350;
	private static int DEPOSIT_ALL = 432;
	
	private Area FALADOR = new Area(new Tile(3047, 3299), new Tile(3062, 3315));
	private Area PORT_PHASMATYS = new Area(new Tile(3594, 3518), new Tile(3609, 3533));
	private Area CATHERBY = new Area(new Tile(2801, 3456), new Tile(2817, 3471));
	private Area ARDOUGNE = new Area(new Tile(2659, 3367), new Tile(2675, 3381));
	
	int herbIndex = -1;
	boolean useCompost;
	
	boolean notCompostedYet = true;
	
	@Override
	public boolean onExecute() {
		backgroundIMG = getImage("http://i.imgur.com/cciyTiy.png");
		
		GUI g = new GUI();
        while(g.isVisible()){
                Time.sleep(100);}
		
        if(herbIndex == -1) {
            System.out.println("Please select a Herb Type.");
        	return false;
        }
        
        if(Inventory.getCount(SEED_DIPPER) == 0 || Inventory.getCount(SPADE) == 0 || Inventory.getCount(RAKE) == 0) {
            System.out.println("Make sure you have a Seed Dipper, Spade and Rake in your inventory.");
        	return false;
        }
        
        if(Inventory.getCount(COINS) == 0 && useCompost) {
            System.out.println("Make sure you have some cash to buy Super Compost.");
        	return false;
        }
        
		scriptTimer = new Timer();

		Strategies.add(new RakePatch());
		Strategies.add(new CompostPatch());
		Strategies.add(new PlantSeed());
		Strategies.add(new BankHerbs());
		Strategies.add(new PickHerbs());
		Strategies.add(new CurePlants());
		Strategies.add(new BuyCompost());
		Strategies.add(new TeleportToPatch());
		
		provide(Strategies);

        System.out.println("==== " + Manifest.name() + " v" + Manifest.version() + " ====");
		System.out.println("Farming: " + HERB_NAMES[herbIndex]);
		System.out.println((useCompost == true) ? "Using compost." : "Not using compost.");
		System.out.println("==== Started Script! ====");
		System.out.println("");
		return true;
	}
	
	@Override
	public void onFinish() {
		int endExperience = Skill.FARMING.getExperience();
        System.out.println("==== " + Manifest.name() + " v" + Manifest.version() + " ==== ");
        
        System.out.println("Ran for: " + scriptTimer.toString());        
        System.out.println("Experience gained: " + formatter.format((endExperience - startExperience)) + " (" + formatter.format(scriptTimer.getPerHour((endExperience - startExperience))) + " XP/HR)");
        System.out.println("Herbs gained: " + CollectedHerbs + " (" + formatter.format(scriptTimer.getPerHour(CollectedHerbs)) + " Herbs/HR)");
        System.out.println("=== Thanks for using! === ");
	}
	
	public class CurePlants implements Strategy {
		@Override
		public boolean activate() {
			for(int diseasedID : DISEASED_PLANTS) {
				SceneObject[] DiseasedPatch = SceneObjects.getNearest(diseasedID);
				if(DiseasedPatch.length > 0) {
					return true;
				}
			}
			return false;
		}

		@Override
		public void execute() {
	        if(Inventory.getCount(PLANT_CURE) > 0) {
	        	Item[] PlantCure = Inventory.getItems(PLANT_CURE);

				Time.sleep(300);
				Menu.sendAction(447, PLANT_CURE - 1, PlantCure[0].getSlot(), 3214);
				Time.sleep(500);
		        
		        for(int diseasedID : DISEASED_PLANTS) {
					SceneObject[] DiseasedPatch = SceneObjects.getNearest(diseasedID);
					
					if(DiseasedPatch.length > 0) {
						SceneObject Patch = DiseasedPatch[0];
						
						if (Patch != null) {
							Menu.sendAction(62, Patch.getHash(), Patch.getLocalRegionX(), Patch.getLocalRegionY(), diseasedID, 1);
							Time.sleep(Patch.getLocation().distanceTo() * 400);
						}
					}
				}
	        } else {
	        	if(Game.getOpenInterfaceId() != SHOP_INTERFACE) {
		        	for(int NPC_IDs : FARMING_SHOPS) {
		        		Npc[] shopNPC = Npcs.getNearest(NPC_IDs);
	
						if (shopNPC.length > 0) {
							try {
								shopNPC[0].interact(2);
								Time.sleep(shopNPC[0].getLocation().distanceTo() * 400);
							} catch(Exception _e) {
								System.out.println("Prevented an error! - Nulled NPC");
							}
						}
					}
	        	} else {
	        		Menu.sendAction(867, PLANT_CURE - 1, 8, 3900);
	        		Time.sleep(500);
	        	}
	        }
		}
	}
	
	public class CompostPatch implements Strategy {
		@Override
		public boolean activate() {
			if(useCompost && notCompostedYet) {
				SceneObject[] OpenHerbPatch = SceneObjects.getNearest(EMPTY_PATCH);
				if(OpenHerbPatch.length > 0) {
					return true;
				}
			}
			return false;
		}

		@Override
		public void execute() {
	        if(Inventory.getCount(SUPERCOMPOST) > 0) {
	        	Item[] SuperCompost = Inventory.getItems(SUPERCOMPOST);

				Time.sleep(300);
				Menu.sendAction(447, SUPERCOMPOST - 1, SuperCompost[0].getSlot(), 3214);
				Time.sleep(500);
		        SceneObject[] OpenHerbPatch = SceneObjects.getNearest(EMPTY_PATCH);
				if(OpenHerbPatch.length > 0) {
					SceneObject Patch = OpenHerbPatch[0];
					if (Patch != null) {
						Menu.sendAction(62, Patch.getHash(), Patch.getLocalRegionX(), Patch.getLocalRegionY(), EMPTY_PATCH, 1);
						Time.sleep(Patch.getLocation().distanceTo() * 400 + 500);
				        notCompostedYet = false;
					}
				} else {
					System.out.println("Cannot find patch.");
					notCompostedYet = false;
				}
	        } else {
	        	if(Inventory.getCount() == 28) {
					System.out.println("Don't have enough inventory space to buy Super Compost, skipping it.");
	        		notCompostedYet = false;
	        	} else {
		        	if(Game.getOpenInterfaceId() != SHOP_INTERFACE) {
			        	for(int NPC_IDs : FARMING_SHOPS) {
			        		Npc[] shopNPC = Npcs.getNearest(NPC_IDs);
		
							if (shopNPC.length > 0) {
								try {
									shopNPC[0].interact(2);
									Time.sleep(shopNPC[0].getLocation().distanceTo() * 400);
								} catch(Exception _e) {
									System.out.println("Prevented an error! - Nulled NPC");
								}
							}
						}
		        	} else {
		        		Menu.sendAction(867, SUPERCOMPOST - 1, 7, 3900);
		        		Time.sleep(500);
		        	}
	        	}
	        }
		}
	}
	
	public class BankHerbs implements Strategy {
		@Override
		public boolean activate() {
			for(int HERB : HERB_IDS) {
				if(Inventory.getCount(HERB) > 0) {
					SceneObject[] DepositBox = SceneObjects.getNearest(DEPOSIT_BOX);
					if(DepositBox.length > 0) {
						return true;
					}
				}
			}
			return false;
		}

		@Override
		public void execute() {
			if(Game.getOpenInterfaceId() != BANK_INTERFACE) {
				SceneObject[] DepositBox = SceneObjects.getNearest(DEPOSIT_BOX);
				
				if(DepositBox.length > 0) {
					SceneObject Bank = DepositBox[0];
					
					if (Bank != null) {
						Bank.interact(0);
						Time.sleep(Bank.getLocation().distanceTo() * 400 + 400);
					}
				}
			} else {
				for(int HERB : HERB_IDS) {
					if(Inventory.getCount(HERB) > 0) {
						Item[] DepositableHerb = Inventory.getItems(HERB);
						CollectedHerbs += DepositableHerb.length;
						Menu.sendAction(DEPOSIT_ALL, HERB - 1, DepositableHerb[0].getSlot(), 5064);
						sleep(200);
					}
				}
				if(Inventory.getCount(EMPTY_VIAL) > 0) {
					Item[] EmptyVials = Inventory.getItems(EMPTY_VIAL);
					for(Item Vial : EmptyVials) {
						Vial.drop();
						sleep(100);
					}
					sleep(200);
				}
				if(Inventory.getCount(EMPTY_BUCKET) > 0) {
					Item[] EmptyBucket = Inventory.getItems(EMPTY_BUCKET);
					for(Item Bucket : EmptyBucket) {
						Bucket.drop();
						sleep(100);
					}
					sleep(200);
				}
				sleep(500);
				Mouse.getInstance().click(486, 27, true);
				sleep(200);
			}
		}
	}
	
	public class PlantSeed implements Strategy {
		@Override
		public boolean activate() {
			SceneObject[] OpenHerbPatch = SceneObjects.getNearest(EMPTY_PATCH);
			if(OpenHerbPatch.length > 0) {
				return true;
			}
			return false;
		}

		@Override
		public void execute() {
			Item[] Seed = Inventory.getItems(5292 + herbIndex);
			if(Seed.length > 0) {
				Time.sleep(300);
				Menu.sendAction(447, 5291 + herbIndex, Seed[0].getSlot(), 3214);
				Time.sleep(500);
				SceneObject[] OpenHerbPatch = SceneObjects.getNearest(EMPTY_PATCH);
				if(OpenHerbPatch.length > 0) {
					SceneObject Patch = OpenHerbPatch[0];
					if (Patch != null) {
						Menu.sendAction(62, Patch.getHash(), Patch.getLocalRegionX(), Patch.getLocalRegionY(), EMPTY_PATCH, 1);
						Time.sleep(1500);
				        notCompostedYet = true;
					}
				}
			}
	        
		}
	}
	
	public class RakePatch implements Strategy {
		@Override
		public boolean activate() {
			for(int grassy_id : GRASSY_PATCHES) {
				SceneObject[] GrassyPatch = SceneObjects.getNearest(grassy_id);
				if(GrassyPatch.length > 0) {
					return true;
				}
			}
			return false;
		}

		@Override
		public void execute() {
			Item[] Rake = Inventory.getItems(RAKE);
			if(Rake.length > 0) {
				Time.sleep(300);
				Menu.sendAction(447, RAKE - 1, Rake[0].getSlot(), 3214);
				Time.sleep(500);
				for(int grassy_id : GRASSY_PATCHES) {
					SceneObject[] GrassyPatch = SceneObjects.getNearest(grassy_id);
					if(GrassyPatch.length > 0) {
						SceneObject Patch = GrassyPatch[0];
						if (Patch != null) {
							Menu.sendAction(62, Patch.getHash(), Patch.getLocalRegionX(), Patch.getLocalRegionY(), grassy_id, 1);
							Time.sleep(Patch.getLocation().distanceTo() * 400 + 1000);
						}
					}
				}
			}
	        
		}
	}
	
	public class PickHerbs implements Strategy {
		@Override
		public boolean activate() {
			SceneObject[] PickableHerbs = SceneObjects.getNearest(GROWN_HERB);
			if(PickableHerbs.length > 0) {
				return true;
			}
			return false;
		}

		@Override
		public void execute() {
			SceneObject[] PickableHerbs = SceneObjects.getNearest(GROWN_HERB);
			if(PickableHerbs.length > 0) {
				PickableHerbs[0].interact(0);
				Time.sleep(PickableHerbs[0].getLocation().distanceTo() * 400 + 1000);
			}
		}
	}
	
	public class TeleportToPatch implements Strategy {
		@Override
		public boolean activate() {
			SceneObject[] AlmostDoneHerb = SceneObjects.getNearest(8142);
			if(AlmostDoneHerb.length == 0) {
				if(Players.getMyPlayer().getAnimation() == -1) {
					return true;
				}
			}
			return false;
		}

		@Override
		public void execute() {
			Area[] HERB_LOCATIONS = {FALADOR, PORT_PHASMATYS, CATHERBY, ARDOUGNE};
			Tile MyPos = Players.getMyPlayer().getLocation();
			int currentLocation = 0;
			for(int location = 0; location < HERB_LOCATIONS.length; location++) {
				if(inArea(HERB_LOCATIONS[location], MyPos)) {
					currentLocation = location;
				}
			}
			sleep(200);
			showTeleports();
			sleep(200);
			Menu.clickButton(2494);
			sleep(200);
			Menu.clickButton(2494 + (currentLocation + 1) % 4);
			sleep(2500);
		}
	}
	
	public class BuyCompost implements Strategy {
		@Override
		public boolean activate() {
			if(useCompost && Inventory.getCount() != 28 && Inventory.getCount(SUPERCOMPOST) == 0) {
				for(int NPC_IDs : FARMING_SHOPS) {
	        		Npc[] shopNPC = Npcs.getNearest(NPC_IDs);
					if (shopNPC.length > 0) {
						return true;
					}
				}
			}
			return false;
		}

		@Override
		public void execute() {
			if(Game.getOpenInterfaceId() != SHOP_INTERFACE) {
				for(int NPC_IDs : FARMING_SHOPS) {
			        Npc[] shopNPC = Npcs.getNearest(NPC_IDs);
					if (shopNPC.length > 0) {
						try {
							shopNPC[0].interact(2);
							Time.sleep(shopNPC[0].getLocation().distanceTo() * 400);
						} catch(Exception _e) {
							System.out.println("Prevented an error! - Nulled NPC");
						}
					}
				}
		    } else {
		        Menu.sendAction(867, SUPERCOMPOST - 1, 7, 3900);
		        Time.sleep(500);
		    }
		}
	}
	
	public void showTeleports() {
		Menu.sendAction(867, MAGIC_SECATEURS, 3, 1688);
	}
	
	public boolean inArea(Area givenArea, Tile givenTile) {
		Tile leftUpper = givenArea.getPoints()[0];
		Tile rightBottom = givenArea.getPoints()[1];
		if(givenTile.getX() > leftUpper.getX() && givenTile.getX() < rightBottom.getX()
				&& givenTile.getY() > leftUpper.getY() && givenTile.getY() < rightBottom.getY())
			return true;
		return false;
	}
	
	public class GUI extends JFrame implements ActionListener {
		
		private static final long serialVersionUID = 7519153641069525353L;

		private JPanel contentPane;
		
		JCheckBox usingCompost;
		JButton btnStart;
		JComboBox<String> herbType;
		
		public GUI() {
			
			setResizable(false);
			setTitle("RDM Farmer");
			setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
			setBounds(100, 100, 242, 206);
			contentPane = new JPanel();
			contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
			setContentPane(contentPane);
			contentPane.setLayout(null);
			
			JLabel lblRdmFarmer = new JLabel("RDM Farmer");
			lblRdmFarmer.setHorizontalAlignment(SwingConstants.CENTER);
			lblRdmFarmer.setFont(new Font("Verdana", Font.PLAIN, 17));
			lblRdmFarmer.setBounds(45, 0, 143, 34);
			contentPane.add(lblRdmFarmer);
			
			herbType = new JComboBox<>(HERB_NAMES);
			herbType.setBounds(45, 59, 143, 20);
			contentPane.add(herbType);
			
			JLabel lblChooseHerbtype = new JLabel("Choose Herb-Type:");
			lblChooseHerbtype.setHorizontalAlignment(SwingConstants.CENTER);
			lblChooseHerbtype.setBounds(65, 45, 101, 14);
			contentPane.add(lblChooseHerbtype);
			
			usingCompost = new JCheckBox("Use Supercompost");
			usingCompost.setHorizontalAlignment(SwingConstants.CENTER);
			usingCompost.setBounds(45, 86, 133, 23);
			contentPane.add(usingCompost);
			
			btnStart = new JButton("Start!");
			btnStart.setBounds(59, 124, 110, 34);
			contentPane.add(btnStart);
	        btnStart.addActionListener(this);

			Dimension dim = Toolkit.getDefaultToolkit().getScreenSize();
			this.setLocation(dim.width/2-this.getSize().width/2, dim.height/2-this.getSize().height/2);
            setVisible(true);
		}
		
		@Override
		public void actionPerformed(ActionEvent e) {
			if(e.getSource().equals(btnStart)){
				herbIndex = herbType.getSelectedIndex();
				useCompost = usingCompost.isSelected();
	            setVisible(false);
			}
		}
	}


	
	@Override
	public void paint(Graphics Graphs) {
		Graphs.drawImage(backgroundIMG, 400, 5, null);
		
		Graphics2D g = (Graphics2D) Graphs;
		g.setColor(Color.WHITE);
		g.drawString(Manifest.name(), 420, 20);
		try {
			g.drawString("Runtime: " + scriptTimer.toString(), 404, 36);
			g.drawString("XP:", 405, 52);
			g.drawString("Herbs:", 404, 64);
	
			g.drawString(formatter.format((Skill.FARMING.getExperience() - startExperience) / 1000) + "K (" + formatter.format(scriptTimer.getPerHour((Skill.FARMING.getExperience() - startExperience))).substring(0, 4) + "M)", 425, 52);
			g.drawString(CollectedHerbs + " (" + scriptTimer.getPerHour(CollectedHerbs) + ")", 445, 64);
		} catch (Exception _e) {}
	}
	
	public static Image getImage(String url) {
		try {
			return ImageIO.read(new URL(url));
		} catch (IOException e) {
			return null;
		}
	}

}