package com.example.calorie;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import com.example.calorie.util.SimpleLogger;

/**
 * GUI class for Calorie Service
 * Provides functionality to run server, run client, handle input/output, and display logs.
 */
public class CalorieServiceGUI extends JFrame {
    private JTextArea outputArea;
    private JTextArea logArea;
    private JTextField ageField;
    private JTextField genderField;
    private JTextField weightField;
    private JTextField heightField;
    private JComboBox<String> activityLevelCombo;
    private JButton serverButton;
    private JButton clientButton;
    private JButton submitButton;
    private JButton backToMainButton;
    private CalorieServer server;
    private CalorieClient client;
    private boolean isServerRunning = false;
    private final ByteArrayOutputStream logOutputStream;
    private final PrintStream originalOut;
    private static final int PORT = 50052;
    private static final String HOST = "localhost";
    private static final int FIELD_WIDTH = 15;

    public CalorieServiceGUI() {
        // Setup for log capturing
        logOutputStream = new ByteArrayOutputStream();
        originalOut = System.out;
        System.setOut(new PrintStream(logOutputStream));

        // Initialize GUI
        setTitle("Calorie Service GUI");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(600, 500);
        setLayout(new BorderLayout(5, 5));

        // Top panel (Server/Client controls)
        JPanel controlPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 5));
        serverButton = new JButton("Start Server");
        clientButton = new JButton("Start Client");
        backToMainButton = new JButton("Back to Main");
        controlPanel.add(serverButton);
        controlPanel.add(clientButton);
        controlPanel.add(backToMainButton);
        add(controlPanel, BorderLayout.NORTH);

        // Center panel (Input/Output)
        JPanel centerPanel = new JPanel(new BorderLayout(5, 5));
        centerPanel.setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 10));
        
        // Input area (Left side)
        JPanel inputPanel = new JPanel(new GridBagLayout());
        inputPanel.setBorder(BorderFactory.createTitledBorder("Input"));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(5, 5, 5, 5);
        
        // Arrange labels and fields vertically
        gbc.gridx = 0;
        gbc.gridy = 0;
        inputPanel.add(new JLabel("Age:"), gbc);
        
        gbc.gridx = 1;
        ageField = new JTextField(FIELD_WIDTH);
        inputPanel.add(ageField, gbc);
        
        gbc.gridx = 0;
        gbc.gridy = 1;
        inputPanel.add(new JLabel("Gender (MALE/FEMALE):"), gbc);
        
        gbc.gridx = 1;
        genderField = new JTextField(FIELD_WIDTH);
        inputPanel.add(genderField, gbc);
        
        gbc.gridx = 0;
        gbc.gridy = 2;
        inputPanel.add(new JLabel("Weight (kg):"), gbc);
        
        gbc.gridx = 1;
        weightField = new JTextField(FIELD_WIDTH);
        inputPanel.add(weightField, gbc);
        
        gbc.gridx = 0;
        gbc.gridy = 3;
        inputPanel.add(new JLabel("Height (cm):"), gbc);
        
        gbc.gridx = 1;
        heightField = new JTextField(FIELD_WIDTH);
        inputPanel.add(heightField, gbc);
        
        gbc.gridx = 0;
        gbc.gridy = 4;
        inputPanel.add(new JLabel("Activity Level:"), gbc);
        
        gbc.gridx = 1;
        activityLevelCombo = new JComboBox<>(new String[]{
            "SEDENTARY", "LIGHT", "MODERATE", "VERY_ACTIVE", "EXTRA_ACTIVE"
        });
        inputPanel.add(activityLevelCombo, gbc);
        
        gbc.gridx = 0;
        gbc.gridy = 5;
        gbc.gridwidth = 2;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        submitButton = new JButton("Calculate Calories");
        inputPanel.add(submitButton, gbc);
        
        // Output area (Right side)
        outputArea = new JTextArea();
        outputArea.setEditable(false);
        outputArea.setLineWrap(true);
        outputArea.setWrapStyleWord(true);
        JScrollPane outputScroll = new JScrollPane(outputArea);
        outputScroll.setBorder(BorderFactory.createTitledBorder("Results"));
        outputScroll.setPreferredSize(new Dimension(300, 200));

        // Place input and output panels side-by-side
        JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, inputPanel, outputScroll);
        splitPane.setResizeWeight(0.5);
        centerPanel.add(splitPane, BorderLayout.CENTER);
        
        add(centerPanel, BorderLayout.CENTER);

        // Bottom panel (Logs)
        logArea = new JTextArea();
        logArea.setEditable(false);
        logArea.setRows(6);
        JScrollPane logScroll = new JScrollPane(logArea);
        logScroll.setBorder(BorderFactory.createTitledBorder("Logs"));
        add(logScroll, BorderLayout.SOUTH);

        // Set up event listeners
        setupEventListeners();

        // Start log update timer
        startLogUpdateTimer();
        
        // Center window on screen
        setLocationRelativeTo(null);
    }

    /**
     * Set up event listeners
     */
    private void setupEventListeners() {
        serverButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (!isServerRunning) {
                    startServer();
                } else {
                    stopServer();
                }
            }
        });

        clientButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                startClient();
            }
        });

        submitButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                submitRequest();
            }
        });

        backToMainButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                dispose();
                SwingUtilities.invokeLater(() -> {
                    new MainGUI().setVisible(true);
                });
            }
        });
    }

    /**
     * Start server
     */
    private void startServer() {
        try {
            server = new CalorieServer(PORT);
            new Thread(() -> {
                try {
                    server.start();
                    isServerRunning = true;
                    serverButton.setText("Stop Server");
                    appendLog("Server started successfully on port " + PORT);
                } catch (Exception ex) {
                    appendLog("Error starting server: " + ex.getMessage());
                }
            }).start();
        } catch (Exception e) {
            appendLog("Error initializing server: " + e.getMessage());
        }
    }

    /**
     * Stop server
     */
    private void stopServer() {
        if (server != null) {
            try {
                server.stop();
                isServerRunning = false;
                serverButton.setText("Start Server");
                appendLog("Server stopped successfully");
            } catch (Exception e) {
                appendLog("Error stopping server: " + e.getMessage());
            }
        }
    }

    /**
     * Start client
     */
    private void startClient() {
        try {
            client = new CalorieClient(HOST, PORT);
            appendLog("Client initialized successfully");
        } catch (Exception e) {
            appendLog("Error initializing client: " + e.getMessage());
        }
    }

    /**
     * Submit request
     */
    private void submitRequest() {
        if (client == null) {
            appendLog("Please start the client first");
            return;
        }

        try {
            int age = Integer.parseInt(ageField.getText());
            String gender = genderField.getText().toUpperCase();
            double weight = Double.parseDouble(weightField.getText());
            double height = Double.parseDouble(heightField.getText());
            String activityLevel = (String) activityLevelCombo.getSelectedItem();

            // Validate input
            if (age <= 0 || weight <= 0 || height <= 0) {
                appendLog("Please enter valid positive numbers for age, weight, and height");
                return;
            }

            if (!gender.equals("MALE") && !gender.equals("FEMALE")) {
                appendLog("Gender must be either MALE or FEMALE");
                return;
            }

            // Execute client request
            String result = client.calculateDailyCalories(age, gender, weight, height, activityLevel);
            outputArea.append(result);
            
            // Log calculation result
            String logMessage = String.format("Calorie calculation - Age: %d, Gender: %s, Weight: %.1f, Height: %.1f, Activity: %s, Result: %s",
                age, gender, weight, height, activityLevel, result);
            appendLog(logMessage);
            
            // Clear input fields
            ageField.setText("");
            genderField.setText("");
            weightField.setText("");
            heightField.setText("");
            activityLevelCombo.setSelectedIndex(0);
            
        } catch (NumberFormatException e) {
            appendLog("Please enter valid numbers for age, weight, and height");
        } catch (Exception e) {
            appendLog("Error sending request: " + e.getMessage());
        }
    }

    /**
     * Method to append log messages
     */
    private void appendLog(String message) {
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
        String logMessage = String.format("[%s] %s", timestamp, message);
        logArea.append(logMessage + "\n");
        logArea.setCaretPosition(logArea.getDocument().getLength());
        
        // Use SimpleLogger to write to analytics.log
        SimpleLogger.log(message);
    }

    /**
     * Start log update timer
     */
    private void startLogUpdateTimer() {
        Timer timer = new Timer(1000, e -> {
            // Always keep log view scrolled to the bottom
            logArea.setCaretPosition(logArea.getDocument().getLength());
        });
        timer.start();
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            new CalorieServiceGUI().setVisible(true);
        });
    }
} 