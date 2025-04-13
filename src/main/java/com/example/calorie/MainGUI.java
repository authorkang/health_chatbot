package com.example.calorie;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

/**
 * Main Integrated GUI Class
 * Provides access to three services (Calorie Calculator, Dining Calorie, Workout Recommendation) from a single interface.
 */
public class MainGUI extends JFrame {
    private static final int WINDOW_WIDTH = 800;
    private static final int WINDOW_HEIGHT = 400;
    private static final int BUTTON_WIDTH = 200;
    private static final int BUTTON_HEIGHT = 150;

    public MainGUI() {
        // Basic window settings
        setTitle("Health chatbot");
        setSize(WINDOW_WIDTH, WINDOW_HEIGHT);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        // Main panel settings
        JPanel mainPanel = new JPanel();
        mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        // Title label
        JLabel titleLabel = new JLabel("Health chatbot", SwingConstants.CENTER);
        titleLabel.setFont(new Font("Arial", Font.BOLD, 24));
        titleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        mainPanel.add(titleLabel);
        mainPanel.add(Box.createVerticalStrut(30));

        // Button panel
        JPanel buttonPanel = new JPanel();
        buttonPanel.setLayout(new FlowLayout(FlowLayout.CENTER, 20, 20));

        // Calorie Calculator button
        JButton calorieButton = createServiceButton(
            "Calorie Calculator",
            "Basic Calorie Service",
            "Calculate daily calorie needs",
            e -> launchService(CalorieServiceGUI.class)
        );
        buttonPanel.add(calorieButton);

        // Dining Calorie button
        JButton diningButton = createServiceButton(
            "Dining Calorie",
            "Meal Calorie Service",
            "Calculate meal calories",
            e -> launchService(DiningCalorieServiceGUI.class)
        );
        buttonPanel.add(diningButton);

        // Workout Recommendation button
        JButton workoutButton = createServiceButton(
            "Workout Recommendation",
            "Exercise Service",
            "Get personalized workout plans",
            e -> launchService(WorkoutRecommendationGUI.class)
        );
        buttonPanel.add(workoutButton);

        mainPanel.add(buttonPanel);
        add(mainPanel);
    }

    /**
     * Creates a service button with title, subtitle, and description
     * @param title Button title
     * @param subtitle Button subtitle
     * @param description Button description
     * @param listener Button click event listener
     * @return Created button
     */
    private JButton createServiceButton(String title, String subtitle, String description, ActionListener listener) {
        JButton button = new JButton();
        button.setPreferredSize(new Dimension(BUTTON_WIDTH, BUTTON_HEIGHT));
        button.setLayout(new BoxLayout(button, BoxLayout.Y_AXIS));
        button.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // Button content settings
        JLabel titleLabel = new JLabel(title, SwingConstants.CENTER);
        titleLabel.setFont(new Font("Arial", Font.BOLD, 16));
        titleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel subtitleLabel = new JLabel(subtitle, SwingConstants.CENTER);
        subtitleLabel.setFont(new Font("Arial", Font.PLAIN, 12));
        subtitleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel descLabel = new JLabel(description, SwingConstants.CENTER);
        descLabel.setFont(new Font("Arial", Font.ITALIC, 10));
        descLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        button.add(titleLabel);
        button.add(Box.createVerticalStrut(5));
        button.add(subtitleLabel);
        button.add(Box.createVerticalStrut(5));
        button.add(descLabel);

        button.addActionListener(listener);
        return button;
    }

    /**
     * Launches the service GUI
     * @param serviceClass Service GUI class to launch
     */
    private void launchService(Class<?> serviceClass) {
        try {
            // Launch service GUI in a new thread
            new Thread(() -> {
                try {
                    serviceClass.getMethod("main", String[].class)
                        .invoke(null, (Object) new String[0]);
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(this,
                        "Error launching service: " + ex.getMessage(),
                        "Error",
                        JOptionPane.ERROR_MESSAGE);
                }
            }).start();
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this,
                "Error launching service: " + e.getMessage(),
                "Error",
                JOptionPane.ERROR_MESSAGE);
        }
    }

    public static void main(String[] args) {
        // Run GUI in EDT(Event Dispatch Thread)
        SwingUtilities.invokeLater(() -> {
            new MainGUI().setVisible(true);
        });
    }
} 