package com.example.calorie.util;

import java.io.FileWriter;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * 간단한 로깅 유틸리티 클래스
 * 모든 로그는 'logs' 폴더의 'analytics.log' 파일에 저장됩니다.
 */
public class SimpleLogger {
    // 로그 파일 경로 설정
    private static final String LOG_FILE = "logs/analytics.log";
    
    /**
     * 로그를 파일에 기록하는 메서드
     * @param message 기록할 로그 메시지
     */
    public static void log(String message) {
        try {
            // logs 디렉토리가 없으면 생성
            java.io.File logDir = new java.io.File("logs");
            if (!logDir.exists()) {
                logDir.mkdir();
            }
            
            // 파일에 로그 작성 (append 모드)
            try (FileWriter fw = new FileWriter(LOG_FILE, true)) {
                String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
                String logEntry = String.format("[%s] %s%n", timestamp, message);
                fw.write(logEntry);
                fw.flush(); // 즉시 파일에 기록
            }
        } catch (IOException e) {
            System.err.println("로그 작성 중 오류 발생: " + e.getMessage());
            e.printStackTrace();
        }
    }
} 