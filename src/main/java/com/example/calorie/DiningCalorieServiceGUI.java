package com.example.calorie;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import io.grpc.stub.StreamObserver;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Dining Calorie Service GUI Class
 * Provides server execution, client execution, food selection, calorie calculation, and log display functions.
 */
public class DiningCalorieServiceGUI extends JFrame {
    private JTextArea outputArea;
    private JTextArea logArea;
    private JButton serverButton;
    private JButton clientButton;
    private JButton submitButton;
    private DiningCalorieServer server;
    private DiningCalorieClient client;
    private boolean isServerRunning = false;
    private final ByteArrayOutputStream logOutputStream;
    private final PrintStream originalOut;
    private static final int PORT = 50053;
    private static final String HOST = "localhost";
    
    // Food menu data
    private static final String[][] FOOD_MENU = {
        {"Western", "hamburger", "pizza", "salad", "french fries"},
        {"Beverages", "coke", "beer"},
        {"Asian", "rice", "kimchi", "ramen", "bibimbap"}
    };
    
    // Food calorie data (based on 100g)
    private static final int[][] FOOD_CALORIES = {
        {550, 285, 100, 365},  // Western
        {140, 150},            // Beverages
        {130, 15, 450, 550}    // Asian
    };
    
    private List<JCheckBox> foodCheckboxes;
    private List<JSpinner> foodSpinners;

    public DiningCalorieServiceGUI() {
        // Log capture setup
        logOutputStream = new ByteArrayOutputStream();
        originalOut = System.out;
        System.setOut(new PrintStream(logOutputStream));

        // GUI initialization
        setTitle("Dining Calorie Service GUI");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(800, 600);
        setLayout(new BorderLayout(5, 5));

        // Top panel (Server/Client control)
        JPanel controlPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 5));
        serverButton = new JButton("Start Server");
        clientButton = new JButton("Start Client");
        controlPanel.add(serverButton);
        controlPanel.add(clientButton);
        add(controlPanel, BorderLayout.NORTH);

        // Center panel (Input/Output)
        JPanel centerPanel = new JPanel(new BorderLayout(5, 5));
        centerPanel.setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 10));
        
        // Input area (Left)
        JPanel inputPanel = new JPanel(new BorderLayout());
        inputPanel.setBorder(BorderFactory.createTitledBorder("Food Selection"));
        
        // Food selection area
        JPanel foodSelectionPanel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(5, 5, 5, 5);
        
        foodCheckboxes = new ArrayList<>();
        foodSpinners = new ArrayList<>();
        
        int row = 0;
        for (int i = 0; i < FOOD_MENU.length; i++) {
            // Category label
            gbc.gridx = 0;
            gbc.gridy = row;
            gbc.gridwidth = 2;
            foodSelectionPanel.add(new JLabel(FOOD_MENU[i][0] + ":"), gbc);
            row++;
            
            // Foods in each category
            for (int j = 1; j < FOOD_MENU[i].length; j++) {
                gbc.gridx = 0;
                gbc.gridy = row;
                gbc.gridwidth = 1;
                
                JCheckBox checkBox = new JCheckBox(FOOD_MENU[i][j]);
                foodCheckboxes.add(checkBox);
                foodSelectionPanel.add(checkBox, gbc);
                
                gbc.gridx = 1;
                SpinnerNumberModel spinnerModel = new SpinnerNumberModel(1, 1, 10, 1);
                JSpinner spinner = new JSpinner(spinnerModel);
                spinner.setEnabled(false);
                foodSpinners.add(spinner);
                foodSelectionPanel.add(spinner, gbc);
                
                // Enable/disable spinner based on checkbox selection
                checkBox.addActionListener(e -> {
                    spinner.setEnabled(checkBox.isSelected());
                });
                
                row++;
            }
        }
        
        // Add food selection panel to scroll pane
        JScrollPane foodScrollPane = new JScrollPane(foodSelectionPanel);
        foodScrollPane.setPreferredSize(new Dimension(300, 400));
        inputPanel.add(foodScrollPane, BorderLayout.CENTER);
        
        // Calculate button
        submitButton = new JButton("Calculate Total Calories");
        inputPanel.add(submitButton, BorderLayout.SOUTH);
        
        // Output area (Right)
        outputArea = new JTextArea();
        outputArea.setEditable(false);
        outputArea.setLineWrap(true);
        outputArea.setWrapStyleWord(true);
        JScrollPane outputScroll = new JScrollPane(outputArea);
        outputScroll.setBorder(BorderFactory.createTitledBorder("Results"));
        outputScroll.setPreferredSize(new Dimension(300, 400));

        // Arrange input and output panels horizontally
        JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, inputPanel, outputScroll);
        splitPane.setResizeWeight(0.5);
        centerPanel.add(splitPane, BorderLayout.CENTER);
        
        add(centerPanel, BorderLayout.CENTER);

        // Bottom panel (Log)
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
        
        // Center the window on screen
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
    }

    /**
     * Start server
     */
    private void startServer() {
        try {
            server = new DiningCalorieServer(PORT);
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
            client = new DiningCalorieClient(HOST, PORT);
            appendLog("Client initialized successfully");
        } catch (Exception e) {
            appendLog("Error initializing client: " + e.getMessage());
        }
    }

    /**
     * Submit request and calculate calories
     */
    private void submitRequest() {
        if (client == null) {
            appendLog("Please start the client first");
            return;
        }

        try {
            StringBuilder resultBuilder = new StringBuilder();
            final AtomicInteger totalCalories = new AtomicInteger(0);
            boolean hasSelection = false;

            // Create StreamObserver for handling server responses
            final CountDownLatch finishLatch = new CountDownLatch(1);
            StreamObserver<FoodCalorieInfo> responseObserver = new StreamObserver<FoodCalorieInfo>() {
                @Override
                public void onNext(FoodCalorieInfo response) {
                    resultBuilder.append(response.getName())
                               .append(": ")
                               .append(response.getCalories())
                               .append(" kcal\n")
                               .append(response.getMessage())
                               .append("\n");
                    totalCalories.addAndGet(response.getCalories());
                }

                @Override
                public void onError(Throwable t) {
                    appendLog("Error from server: " + t.getMessage());
                    finishLatch.countDown();
                }

                @Override
                public void onCompleted() {
                    resultBuilder.append("\nTotal Calories: ")
                               .append(totalCalories.get())
                               .append(" kcal");
                    outputArea.setText(resultBuilder.toString());
                    finishLatch.countDown();
                }
            };

            // Get StreamObserver for sending requests
            StreamObserver<FoodItem> requestObserver = client.getAsyncStub().streamFoodCalories(responseObserver);

            // Send food items to server
            for (int i = 0; i < foodCheckboxes.size(); i++) {
                JCheckBox checkBox = foodCheckboxes.get(i);
                if (checkBox.isSelected()) {
                    hasSelection = true;
                    String foodName = checkBox.getText();
                    int quantity = (Integer) foodSpinners.get(i).getValue();
                    
                    // Send food information to server
                    FoodItem foodItem = FoodItem.newBuilder()
                        .setName(foodName.toLowerCase())
                        .setQuantity(quantity)
                        .build();
                    requestObserver.onNext(foodItem);
                }
            }

            if (!hasSelection) {
                appendLog("Please select at least one food item");
                return;
            }

            // Complete the request
            requestObserver.onCompleted();

            // Wait for all responses
            finishLatch.await(1, TimeUnit.MINUTES);
            appendLog("Calorie calculation completed successfully");
            
        } catch (Exception e) {
            appendLog("Error calculating calories: " + e.getMessage());
        }
    }

    /**
     * Append log message
     */
    private void appendLog(String message) {
        logArea.append("[" + java.time.LocalDateTime.now().format(java.time.format.DateTimeFormatter.ofPattern("HH:mm:ss")) + "] " + message + "\n");
        logArea.setCaretPosition(logArea.getDocument().getLength());
    }

    /**
     * Start log update timer
     */
    private void startLogUpdateTimer() {
        Timer timer = new Timer(1000, e -> {
            String newLog = logOutputStream.toString();
            if (!newLog.isEmpty()) {
                appendLog(newLog);
                logOutputStream.reset();
            }
        });
        timer.start();
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            new DiningCalorieServiceGUI().setVisible(true);
        });
    }
} 