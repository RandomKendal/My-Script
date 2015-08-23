package RDMFighter;

import java.awt.Dimension;
import java.awt.Font;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSlider;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.border.EmptyBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import RDMFighter.Main.EnemyNpc;
import RDMFighter.Main.Food;

public class GUI extends JFrame implements ActionListener, ChangeListener {
	/**
	 * 
	 */
	private final Main Core;

	private static final long serialVersionUID = 7519153641069525353L;

	private JPanel contentPane;
	
	JTextField textField;
	JButton btnStart;
	JComboBox<String> comboFoodToEat;
	JComboBox<String> comboNpcToKill;
	JLabel lblPercent;
	JSlider sliderEatAt;
	
	String[] FoodNames = new String[Food.values().length];
	String[] EnemyNpcNames = new String[EnemyNpc.values().length];
	
	public GUI(Main main) {
		Core = main;
		for(int id = 0; id < FoodNames.length; id++) {
			FoodNames[id] = Food.values()[id].name;
		}
		for(int id = 0; id < EnemyNpcNames.length; id++) {
			EnemyNpcNames[id] = EnemyNpc.values()[id].name;
		}
		
		setResizable(false);
		setTitle("Main");
		setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		setBounds(100, 100, 300, 354);
		contentPane = new JPanel();
		contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
		setContentPane(contentPane);
		contentPane.setLayout(null);
		
		sliderEatAt = new JSlider();
		sliderEatAt.setValue(60);
		sliderEatAt.setBounds(45, 141, 200, 26);
		sliderEatAt.addChangeListener(this);
		contentPane.add(sliderEatAt);
		
		comboFoodToEat = new JComboBox<>(FoodNames);
		comboFoodToEat.setBounds(85, 110, 116, 20);
		contentPane.add(comboFoodToEat);
		
		comboNpcToKill = new JComboBox<>(EnemyNpcNames);
		comboNpcToKill.setBounds(83, 62, 116, 20);
		contentPane.add(comboNpcToKill);
		
		lblPercent = new JLabel("60%");
		lblPercent.setHorizontalAlignment(SwingConstants.CENTER);
		lblPercent.setBounds(124, 173, 46, 14);
		contentPane.add(lblPercent);
		
		JLabel lblNewLabel = new JLabel("RDM Fighter v" + Core.Manifest.version());
		lblNewLabel.setHorizontalAlignment(SwingConstants.CENTER);
		lblNewLabel.setFont(new Font("Verdana", Font.PLAIN, 17));
		lblNewLabel.setBounds(51, -1, 178, 43);
		contentPane.add(lblNewLabel);
		
		JLabel lblSelectYourFood = new JLabel("Select your food:");
		lblSelectYourFood.setHorizontalAlignment(SwingConstants.CENTER);
		lblSelectYourFood.setBounds(88, 93, 106, 14);
		contentPane.add(lblSelectYourFood);
		
		JLabel label = new JLabel("0%");
		label.setEnabled(false);
		label.setHorizontalAlignment(SwingConstants.CENTER);
		label.setBounds(25, 146, 24, 14);
		contentPane.add(label);
		
		JLabel label_1 = new JLabel("100%");
		label_1.setEnabled(false);
		label_1.setHorizontalAlignment(SwingConstants.CENTER);
		label_1.setBounds(244, 146, 30, 14);
		contentPane.add(label_1);
		
		JLabel lblSelectTheNpc = new JLabel("Select the Npc:");
		lblSelectTheNpc.setHorizontalAlignment(SwingConstants.CENTER);
		lblSelectTheNpc.setBounds(86, 45, 106, 14);
		contentPane.add(lblSelectTheNpc);
		
		textField = new JTextField();
		textField.setBounds(36, 221, 219, 20);
		contentPane.add(textField);
		textField.setColumns(10);
		
		JLabel lblItemsToLoot = new JLabel("Items IDs to loot (split by a comma ):");
		lblItemsToLoot.setHorizontalAlignment(SwingConstants.CENTER);
		lblItemsToLoot.setBounds(51, 204, 178, 14);
		contentPane.add(lblItemsToLoot);
		
		btnStart = new JButton("Start!");
		btnStart.setBounds(84, 254, 122, 47);
		contentPane.add(btnStart);
        btnStart.addActionListener(this);
		
		Dimension dim = Toolkit.getDefaultToolkit().getScreenSize();
		this.setLocation(dim.width/2-this.getSize().width/2, dim.height/2-this.getSize().height/2);
        setVisible(true);
	}
	
	@Override
	public void actionPerformed(ActionEvent e) {
		if(e.getSource().equals(btnStart)){
			Core.food_index = comboFoodToEat.getSelectedIndex();
			Core.npc_index = comboNpcToKill.getSelectedIndex();
			Core.eatAtPercent = sliderEatAt.getValue();
			if(textField.getText() != "") {
				try {
					String[] strArray = textField.getText().replaceAll("\\s","").split(",");
					int[] intArray = new int[strArray.length];
					for(int i = 0; i < strArray.length; i++) {
					    intArray[i] = Integer.parseInt(strArray[i]);
					}
					Core.LootableItems = intArray;
				} catch (Exception _e) {
					System.out.println("Invalid input for loots! Continueing without looting.");
				}
			}
			Core.startScript = true;
            setVisible(false);
		}
	}

	@Override
	public void stateChanged(ChangeEvent e) {
		if(e.getSource().equals(sliderEatAt)) {
			lblPercent.setText(sliderEatAt.getValue() + "%");
		}
	}
}