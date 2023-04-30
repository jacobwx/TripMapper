import javax.swing.*;
import java.awt.*;
import java.io.IOException;

public class MapGUI extends JFrame {

    private JComboBox<String> animationTimeComboBox;
    private JCheckBox includeStopsCheckBox;
    private JButton playButton;
    private MapPanel mapPanel;

    public MapGUI(String filename) {
    	// Set up the JFrame
        super("Project 5 - Jacob Widanski");
        setSize(800, 600);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        // Set up the layout manager
        setLayout(new BorderLayout());

        // Set up the JComboBox for animation time
        String[] animationTimes = {"Animation Time", "15", "30", "60", "90"};
        animationTimeComboBox = new JComboBox<String>(animationTimes);
        
        // Create centered top panel
        JPanel topPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        topPanel.add(animationTimeComboBox);

        // Set up the JCheckBox for including stops
        includeStopsCheckBox = new JCheckBox("Include Stops");
        includeStopsCheckBox.setSelected(false);
        topPanel.add(includeStopsCheckBox);

        // Set up the JButton for playing/resetting the animation
        playButton = new JButton("Play");
        playButton.addActionListener(e -> {
        	// Get desired length of animation
            int animationTime = Integer.parseInt((String) animationTimeComboBox.getSelectedItem());
            // Determine whether or not stops should be included
            boolean includeStops = includeStopsCheckBox.isSelected();
            
            try {
				mapPanel.playAnimation(animationTime, includeStops, filename);
			} catch (IOException e1) {
				e1.printStackTrace();
			}
        });
        topPanel.add(playButton);

        add(topPanel, BorderLayout.NORTH);

        // Set up the MapPanel for displaying the map
        mapPanel = new MapPanel();
        add(mapPanel, BorderLayout.CENTER);

        // Display the JFrame
        setVisible(true);
    }
}

