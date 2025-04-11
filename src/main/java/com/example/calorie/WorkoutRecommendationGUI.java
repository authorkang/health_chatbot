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
        // 로그 캡처를 위한 설정
        logOutputStream = new ByteArrayOutputStream();
        originalOut = System.out;
        System.setOut(new PrintStream(logOutputStream));

        // GUI 초기화
        setTitle("Workout Recommendation Service GUI");
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
        
        // 운동 부위 선택
        gbc.gridx = 0;
        gbc.gridy = 0;
        inputPanel.add(new JLabel("Target Area:"), gbc);
        
        gbc.gridx = 1;
        targetAreaCombo = new JComboBox<>(new String[]{
            "UPPER_BODY", "LOWER_BODY", "CORE"
        });
        inputPanel.add(targetAreaCombo, gbc);
        
        // 피트니스 레벨 선택
        gbc.gridx = 0;
        gbc.gridy = 1;
        inputPanel.add(new JLabel("Fitness Level:"), gbc);
        
        gbc.gridx = 1;
        fitnessLevelCombo = new JComboBox<>(new String[]{
            "BEGINNER", "INTERMEDIATE", "ADVANCED"
        });
        inputPanel.add(fitnessLevelCombo, gbc);
        
        // 제출 버튼
        gbc.gridx = 0;
        gbc.gridy = 2;
        gbc.gridwidth = 2;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        submitButton = new JButton("Get Workout Recommendations");
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
            server = new WorkoutRecommendationServer(PORT);
            new Thread(() -> {
                try {
                    server.start();
                    isServerRunning = true;
                    serverButton.setText("Stop Server");
                    appendLog("Server started successfully on port " + PORT);
                    
                    // 서버가 백그라운드에서 실행되도록 설정
                    // blockUntilShutdown()을 호출하지 않음
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
            client = new WorkoutRecommendationClient(HOST, PORT);
            appendLog("Client initialized successfully");
        } catch (Exception e) {
            appendLog("Error initializing client: " + e.getMessage());
        }
    }

    /**
     * 운동 추천 요청 제출
     */
    private void submitRequest() {
        if (client == null) {
            appendLog("Please start the client first");
            return;
        }

        try {
            String targetArea = (String) targetAreaCombo.getSelectedItem();
            String fitnessLevel = (String) fitnessLevelCombo.getSelectedItem();

            // 결과 영역 초기화
            outputArea.setText("");
            
            // 운동 추천 요청
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
                
                // 각 운동 추천을 로그에 기록
                String exerciseLogMessage = String.format("Workout recommendation - Exercise: %s, Sets: %d, Reps: %d, Equipment: %s",
                    recommendation.getExerciseName(), recommendation.getSets(), recommendation.getReps(), recommendation.getEquipment());
                appendLog(exerciseLogMessage);
                
                // 추천 카운트 증가
                recommendationCount.incrementAndGet();
                
                // GUI 업데이트는 EDT에서 수행
                SwingUtilities.invokeLater(() -> {
                    outputArea.setText(resultBuilder.toString());
                    outputArea.setCaretPosition(0);
                    
                    // 모든 추천이 로드되었을 때 총 개수를 로그에 기록
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
     * 로그 메시지 추가
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
            new WorkoutRecommendationGUI().setVisible(true);
        });
    }
} 