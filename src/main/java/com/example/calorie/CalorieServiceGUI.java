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
 * 칼로리 서비스 GUI 클래스
 * 서버 실행, 클라이언트 실행, 입력/출력, 로그 표시 기능을 제공합니다.
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
        // 로그 캡처를 위한 설정
        logOutputStream = new ByteArrayOutputStream();
        originalOut = System.out;
        System.setOut(new PrintStream(logOutputStream));

        // GUI 초기화
        setTitle("Calorie Service GUI");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(600, 500);
        setLayout(new BorderLayout(5, 5));

        // 상단 패널 (서버/클라이언트 컨트롤)
        JPanel controlPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 5));
        serverButton = new JButton("Start Server");
        clientButton = new JButton("Start Client");
        backToMainButton = new JButton("Back to Main");
        controlPanel.add(serverButton);
        controlPanel.add(clientButton);
        controlPanel.add(backToMainButton);
        add(controlPanel, BorderLayout.NORTH);

        // 중앙 패널 (입력/출력)
        JPanel centerPanel = new JPanel(new BorderLayout(5, 5));
        centerPanel.setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 10));
        
        // 입력 영역 (왼쪽)
        JPanel inputPanel = new JPanel(new GridBagLayout());
        inputPanel.setBorder(BorderFactory.createTitledBorder("Input"));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(5, 5, 5, 5);
        
        // 라벨과 필드를 세로로 정렬
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
        
        // 출력 영역 (오른쪽)
        outputArea = new JTextArea();
        outputArea.setEditable(false);
        outputArea.setLineWrap(true);
        outputArea.setWrapStyleWord(true);
        JScrollPane outputScroll = new JScrollPane(outputArea);
        outputScroll.setBorder(BorderFactory.createTitledBorder("Results"));
        outputScroll.setPreferredSize(new Dimension(300, 200));

        // 입력과 출력 패널을 수평으로 배치
        JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, inputPanel, outputScroll);
        splitPane.setResizeWeight(0.5);
        centerPanel.add(splitPane, BorderLayout.CENTER);
        
        add(centerPanel, BorderLayout.CENTER);

        // 하단 패널 (로그)
        logArea = new JTextArea();
        logArea.setEditable(false);
        logArea.setRows(6);
        JScrollPane logScroll = new JScrollPane(logArea);
        logScroll.setBorder(BorderFactory.createTitledBorder("Logs"));
        add(logScroll, BorderLayout.SOUTH);

        // 이벤트 리스너 설정
        setupEventListeners();

        // 로그 업데이트 타이머 시작
        startLogUpdateTimer();
        
        // 창을 화면 중앙에 배치
        setLocationRelativeTo(null);
    }

    /**
     * 이벤트 리스너 설정
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
     * 서버 시작
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
     * 서버 중지
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
     * 클라이언트 시작
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
     * 요청 제출
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

            // 입력값 검증
            if (age <= 0 || weight <= 0 || height <= 0) {
                appendLog("Please enter valid positive numbers for age, weight, and height");
                return;
            }

            if (!gender.equals("MALE") && !gender.equals("FEMALE")) {
                appendLog("Gender must be either MALE or FEMALE");
                return;
            }

            // 클라이언트 요청 실행
            String result = client.calculateDailyCalories(age, gender, weight, height, activityLevel);
            outputArea.append(result);
            
            // 계산 결과를 로그에 기록
            String logMessage = String.format("Calorie calculation - Age: %d, Gender: %s, Weight: %.1f, Height: %.1f, Activity: %s, Result: %s",
                age, gender, weight, height, activityLevel, result);
            appendLog(logMessage);
            
            // 입력 필드 초기화
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
     * 로그 메시지를 추가하는 메서드
     */
    private void appendLog(String message) {
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
        String logMessage = String.format("[%s] %s", timestamp, message);
        logArea.append(logMessage + "\n");
        logArea.setCaretPosition(logArea.getDocument().getLength());
        
        // SimpleLogger를 사용하여 analytics.log에 기록
        SimpleLogger.log(message);
    }

    /**
     * 로그 업데이트 타이머 시작
     */
    private void startLogUpdateTimer() {
        Timer timer = new Timer(1000, e -> {
            // 로그 영역 스크롤을 항상 최하단으로 유지
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