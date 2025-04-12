package com.example.calorie;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import com.example.calorie.util.SimpleLogger;

/**
 * Workout Recommendation Service GUI
 * Provides interface for server/client control, workout area/level selection, and result display
 */
public class WorkoutRecommendationGUI extends JFrame {
    private JTextArea outputArea;
    private JTextArea logArea;
    private JComboBox<String> targetAreaCombo;
    private JComboBox<String> fitnessLevelCombo;
    private JButton serverButton;
    private JButton clientButton;
    private JButton submitButton;
    private JButton backToMainButton;
    private WorkoutRecommendationServer server;
    private WorkoutRecommendationClient client;
    private boolean isServerRunning = false;
    private final ByteArrayOutputStream logOutputStream;
    private final PrintStream originalOut;
    private static final int PORT = 50053;
    private static final String HOST = "localhost";

    public WorkoutRecommendationGUI() {
        // Setup for capturing logs
        logOutputStream = new ByteArrayOutputStream();
        originalOut = System.out;
        System.setOut(new PrintStream(logOutputStream));

        // Initialize GUI
        setTitle("Workout Recommendation Service GUI");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(600, 500);
        setLayout(new BorderLayout(5, 5));

        // Top panel (server/client control)
        JPanel controlPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 5));
        serverButton = new JButton("Start Server");
        clientButton = new JButton("Start Client");
        backToMainButton = new JButton("Back to Main");
        controlPanel.add(serverButton);
        controlPanel.add(clientButton);
        controlPanel.add(backToMainButton);
        add(controlPanel, BorderLayout.NORTH);

        // Center panel (input/output)
        JPanel centerPanel = new JPanel(new BorderLayout(5, 5));
        centerPanel.setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 10));
        
        // Input area (left)
        JPanel inputPanel = new JPanel(new GridBagLayout());
        inputPanel.setBorder(BorderFactory.createTitledBorder("Input"));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(5, 5, 5, 5);
        
        // Select target workout area
        gbc.gridx = 0;
        gbc.gridy = 0;
        inputPanel.add(new JLabel("Target Area:"), gbc);
        
        gbc.gridx = 1;
        targetAreaCombo = new JComboBox<>(new String[]{
            "UPPER_BODY", "LOWER_BODY", "CORE"
        });
        inputPanel.add(targetAreaCombo, gbc);
        
        // Select fitness level
        gbc.gridx = 0;
        gbc.gridy = 1;
        inputPanel.add(new JLabel("Fitness Level:"), gbc);
        
        gbc.gridx = 1;
        fitnessLevelCombo = new JComboBox<>(new String[]{
            "BEGINNER", "INTERMEDIATE", "ADVANCED"
        });
        inputPanel.add(fitnessLevelCombo, gbc);
        
        // Submit button
        gbc.gridx = 0;
        gbc.gridy = 2;
        gbc.gridwidth = 2;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        submitButton = new JButton("Get Workout Recommendations");
        inputPanel.add(submitButton, gbc);
        
        // Output area (right)
        outputArea = new JTextArea();
        outputArea.setEditable(false);
        outputArea.setLineWrap(true);
        outputArea.setWrapStyleWord(true);
        JScrollPane outputScroll = new JScrollPane(outputArea);
        outputScroll.setBorder(BorderFactory.createTitledBorder("Results"));
        outputScroll.setPreferredSize(new Dimension(300, 200));

        // Arrange input and output panels side by side
        JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, inputPanel, outputScroll);
        splitPane.setResizeWeight(0.5);
        centerPanel.add(splitPane, BorderLayout.CENTER);
        
        add(centerPanel, BorderLayout.CENTER);

        // Bottom panel (logs)
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
        
        // Center the window on the screen
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
            server = new WorkoutRecommendationServer(PORT);
            new Thread(() -> {
                try {
                    server.start();
                    isServerRunning = true;
                    serverButton.setText("Stop Server");
                    appendLog("Server started successfully on port " + PORT);
                    
                    // Let the server run in background
                    // blockUntilShutdown() not called here
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
            client = new WorkoutRecommendationClient(HOST, PORT);
            appendLog("Client initialized successfully");
        } catch (Exception e) {
            appendLog("Error initializing client: " + e.getMessage());
        }
    }

    /**
     * Submit workout recommendation request
     */
    private void submitRequest() {
        if (client == null) {
            appendLog("Please start the client first");
            return;
        }

        try {
            String targetArea = (String) targetAreaCombo.getSelectedItem();
            String fitnessLevel = (String) fitnessLevelCombo.getSelectedItem();

            // Clear the output area
            outputArea.setText("");
            
            // Request workout recommendations
            StringBuilder resultBuilder = new StringBuilder();
            AtomicBoolean isFirstRecommendation = new AtomicBoolean(true);
            final AtomicInteger recommendationCount = new AtomicInteger(0);
            
            client.getWorkoutRecommendationsAsync(targetArea, fitnessLevel, recommendation -> {
                if (isFirstRecommendation.get()) {
                    resultBuilder.append("Workout Recommendations for ").append(targetArea)
                            .append(" at ").append(fitnessLevel).append(" level:\n\n");
                    isFirstRecommendation.set(false);
                }
                
                resultBuilder.append("Exercise: ").append(recommendation.getExerciseName()).append("\n");
                resultBuilder.append("Sets: ").append(recommendation.getSets()).append("\n");
                resultBuilder.append("Reps: ").append(recommendation.getReps()).append("\n");
                resultBuilder.append("Equipment: ").append(recommendation.getEquipment()).append("\n");
                resultBuilder.append("Description: ").append(recommendation.getDescription()).append("\n");
                resultBuilder.append("Tips: ").append(recommendation.getTips()).append("\n");
                resultBuilder.append("-------------------\n");
                
                // Log each workout recommendation
                String exerciseLogMessage = String.format("Workout recommendation - Exercise: %s, Sets: %d, Reps: %d, Equipment: %s",
                    recommendation.getExerciseName(), recommendation.getSets(), recommendation.getReps(), recommendation.getEquipment());
                appendLog(exerciseLogMessage);
                
                // Increase recommendation count
                recommendationCount.incrementAndGet();
                
                // GUI update on EDT
                SwingUtilities.invokeLater(() -> {
                    outputArea.setText(resultBuilder.toString());
                    outputArea.setCaretPosition(0);
                    
                    // Log total number of recommendations when done
                    if (recommendationCount.get() > 0) {
                        appendLog(String.format("Total workout recommendations: %d for %s at %s level",
                            recommendationCount.get(), targetArea, fitnessLevel));
                    }
                });
            });
            
            appendLog("Workout recommendations requested for " + targetArea + " at " + fitnessLevel + " level");
        } catch (Exception e) {
            appendLog("Error getting workout recommendations: " + e.getMessage());
        }
    }

    /**
     * Append log message
     */
    private void appendLog(String message) {
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
        String logMessage = String.format("[%s] %s", timestamp, message);
        logArea.append(logMessage + "\n");
        logArea.setCaretPosition(logArea.getDocument().getLength());
        
        // Record to analytics.log using SimpleLogger
        SimpleLogger.log(message);
    }

    /**
     * Start log update timer
     */
    private void startLogUpdateTimer() {
        Timer timer = new Timer(1000, e -> {
            // Keep the log area scrolled to the bottom
            logArea.setCaretPosition(logArea.getDocument().getLength());
        });
        timer.start();
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            new WorkoutRecommendationGUI().setVisible(true);
        });
    }
} 