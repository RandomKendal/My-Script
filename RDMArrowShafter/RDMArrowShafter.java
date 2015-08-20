package RDMArrowShafter;

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
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import javax.swing.border.EmptyBorder;

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
import org.rev317.min.api.methods.Game;
import org.rev317.min.api.methods.Inventory;
import org.rev317.min.api.methods.Menu;
import org.rev317.min.api.methods.Npcs;
import org.rev317.min.api.methods.Players;
import org.rev317.min.api.methods.SceneObjects;
import org.rev317.min.api.wrappers.Item;
import org.rev317.min.api.wrappers.Npc;
import org.rev317.min.api.wrappers.SceneObject;

@ScriptManifest(
		author = "Random (Kendal)", 
		category = Category.FLETCHING, 
		description = "Fletches arrowshafts out of regular logs on PkHonor. Start with a knife in your inventory, and depending on the mode you use (bank or not), you should start near a bankbooth/butler- or start near trees with an axe equipped.", 
		name = "RDM ArrowShafter", 
		servers = { "PkHonor" }, 
		version = 0.11
		)

public class RDMArrowShafter extends Script implements Paintable {

	private final ArrayList<Strategy> Strategies = new ArrayList<Strategy>();
	private Timer scriptTimer = new Timer();
	private ScriptManifest Manifest = (ScriptManifest) RDMArrowShafter.class.getAnnotation(ScriptManifest.class);

	DecimalFormat formatter = new DecimalFormat("#,###,###,###");
	
	private static Image backgroundIMG;

	private static int BANK_INTERFACE = 23350;
	private static int WITHDRAW_ALL = 53;

	private static int DIALOGUE_INTERFACE = 8899;
	private static int DIALOGUE_BUTTON = 8906;
	
	private static int LOGS = 1512;
	private static int KNIFE = 947;
	private static int ARROW_SHAFTS = 53;
	
	private static int[] TREE = { 1276, 1278, 1282, 1286 };
	
	boolean isBanking = false;
	boolean startScript = false;
	int startingShafts;
	
	@Override
	public boolean onExecute() {
		backgroundIMG = getImage("http://i.imgur.com/cciyTiy.png");
		
		GUI g = new GUI();
        while(g.isVisible()){
                Time.sleep(100);}
		
        if(startScript != true) {
        	return false;
        }
        
        if(Inventory.getCount(KNIFE) == 0) {
            System.out.println("Start with a knife in your inventory.");
        	return false;
        }
        
        try {
        	startingShafts = Inventory.getItem(ARROW_SHAFTS).getStackSize();
        } catch (Exception _e) {}

		Strategies.add(new ChopLogs());
		Strategies.add(new MakeShafts());
		Strategies.add(new KnifeOnLogs());
		Strategies.add(new OpenBank());
		Strategies.add(new UseBank());
		
		provide(Strategies);
		
		System.out.println("==== " + Manifest.name() + " v" + Manifest.version() + " ====");
		System.out.println("Banking: " + ((isBanking == true) ? "True." : "False."));
		System.out.println("==== Started Script! ====");
		System.out.println("");
		return true;
	}
	
	@Override
	public void onFinish() {
		System.out.println("==== " + Manifest.name() + " v" + Manifest.version() + " ==== ");
        System.out.println("Ran for: " + scriptTimer.toString());    
        try {
        	System.out.println("Shafts made: " + (Inventory.getItem(ARROW_SHAFTS).getStackSize() - startingShafts) + " (" + formatter.format(scriptTimer.getPerHour((Inventory.getItem(ARROW_SHAFTS).getStackSize() - startingShafts))) + " Shafts/HR)");
        } catch (Exception _e) {}
        
        System.out.println("=== Thanks for using! === ");
	}
	
	public class ChopLogs implements Strategy {
		SceneObject[] Trees;
		@Override
		public boolean activate() {
			if(!isBanking) {
				if(Inventory.getCount() < 28 && (Players.getMyPlayer().getAnimation() == -1 || Players.getMyPlayer().getAnimation() == 1353)) {
					Trees = SceneObjects.getNearest(TREE);
					if(Trees.length > 0 && Trees != null)
						return true;
				}
			}
			return false;
		}

		@Override
		public void execute() {
			Trees[0].interact(0);
			Time.sleep(Trees[0].distanceTo() * 400 + 600);
		}
	}
	
	public class MakeShafts implements Strategy {
		@Override
		public boolean activate() {
			if(Game.getOpenBackDialogId() == DIALOGUE_INTERFACE && Inventory.getCount(LOGS) > 0)
				return true;
			return false;
		}

		@Override
		public void execute() {
			Menu.clickButton(DIALOGUE_BUTTON);
			Time.sleep(1000);
			Keyboard.getInstance().sendKeys(Random.between(28, 99) + "");
			Time.sleep(new SleepCondition() {
                @Override
                public boolean isValid() {
                      return (Players.getMyPlayer().getAnimation() != -1 && Players.getMyPlayer().getAnimation() != 1353);
                }
            }, 2000);
		}
	}
	
	public class KnifeOnLogs implements Strategy {
		@Override
		public boolean activate() {
			if(Inventory.getCount(LOGS) > 0 && (Players.getMyPlayer().getAnimation() == -1 || Players.getMyPlayer().getAnimation() == 1353))
					return true;
			return false;
		}

		@Override
		public void execute() {
			Item[] knife = Inventory.getItems(KNIFE);
			Item[] logs = Inventory.getItems(LOGS);
			if(knife != null && logs != null) {
				try {
					knife[knife.length - 1].interact(0);
					Time.sleep(50);
					Menu.sendAction(870, logs[logs.length - 1].getId() - 1, logs[logs.length - 1].getSlot(), 3214);
					Time.sleep(new SleepCondition() {
		                 @Override
		                 public boolean isValid() {
		                       return (Game.getOpenBackDialogId() == DIALOGUE_INTERFACE);
		                 }
		            }, 1000);
				} catch (Exception _e) {
					System.out.println("Invalid Item.");
				}
			}
		}
	}
	
	public class OpenBank implements Strategy {
		@Override
		public boolean activate() {
			if(isBanking) {
				if(Inventory.getCount(LOGS) == 0 && Game.getOpenInterfaceId() != BANK_INTERFACE)
					return true;
			}
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
			if(isBanking) {
				if(Inventory.getCount(LOGS) == 0 && Game.getOpenInterfaceId() == BANK_INTERFACE)
					return true;
			}
			return false;
		}

		@Override
		public void execute() {
			int[] bankIds = Loader.getClient().getInterfaceCache()[5382].getItems();
			for (int i = 0; i < bankIds.length; i++) {
				if(bankIds[i] == LOGS) {
					Menu.sendAction(WITHDRAW_ALL, bankIds[i] - 1, i, 5382);
					Time.sleep(new SleepCondition() {
		                @Override
		                public boolean isValid() {
		                      return (Inventory.getCount(LOGS) != 0);
		                }
		            }, 1000);
					return;
				}
			}
		}
	}
	
	@Override
	public void paint(Graphics Graphs) {
		Graphs.drawImage(backgroundIMG, 400, 5, null);
		
		Graphics2D g = (Graphics2D) Graphs;
		g.setColor(Color.WHITE);
		g.drawString(Manifest.name(), 407, 20);
		g.drawString("Runtime: " + scriptTimer.toString(), 404, 36);
		g.drawString("Shafts:", 404, 52);
		g.drawString("P/H:", 404, 64);

		
		try {
			g.drawString(formatter.format((Inventory.getItem(ARROW_SHAFTS).getStackSize() - startingShafts)), 448, 52);
			g.drawString(formatter.format(scriptTimer.getPerHour((Inventory.getItem(ARROW_SHAFTS).getStackSize() - startingShafts))), 448, 64);
		} catch (Exception _e) {}
	}
	
	public static Image getImage(String url) {
		try {
			return ImageIO.read(new URL(url));
		} catch (IOException e) {
			return null;
		}
	}
	
	public class GUI extends JFrame implements ActionListener {
		private static final long serialVersionUID = 7519153641069525353L;

		private JPanel contentPane;
		
		JCheckBox chckbxUseBank;
		JButton btnStart;
		
		public GUI() {
			setResizable(false);
			setTitle("RDM ArrowShafter");
			setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
			setBounds(100, 100, 216, 194);
			contentPane = new JPanel();
			contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
			setContentPane(contentPane);
			contentPane.setLayout(null);
			
			chckbxUseBank = new JCheckBox("Use Bank");
			chckbxUseBank.setHorizontalAlignment(SwingConstants.CENTER);
			chckbxUseBank.setBounds(59, 61, 84, 23);
			contentPane.add(chckbxUseBank);
			
			btnStart = new JButton("Start!");
			btnStart.setBounds(53, 108, 104, 48);
			contentPane.add(btnStart);
	        btnStart.addActionListener(this);
			
			JLabel lblRdmArro = new JLabel("RDM ArrowShafter");
			lblRdmArro.setHorizontalAlignment(SwingConstants.CENTER);
			lblRdmArro.setFont(new Font("Verdana", Font.PLAIN, 17));
			lblRdmArro.setBounds(22, 7, 172, 26);
			contentPane.add(lblRdmArro);
			
			Dimension dim = Toolkit.getDefaultToolkit().getScreenSize();
			this.setLocation(dim.width/2-this.getSize().width/2, dim.height/2-this.getSize().height/2);
            setVisible(true);
		}
		
		@Override
		public void actionPerformed(ActionEvent e) {
			if(e.getSource().equals(btnStart)){
				isBanking = chckbxUseBank.isSelected();
				startScript = true;
	            setVisible(false);
			}
		}
	}
}