package com.example.calorie.util;

import java.io.FileWriter;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Simple logging utility class.
 * All logs are saved to the 'analytics.log' file in the 'logs' folder.
 */
public class SimpleLogger {
    // Set log file path
    private static final String LOG_FILE = "logs/analytics.log";
    
    /**
     * Method to write a log message to the file
     * @param message the log message to record
     */
    public static void log(String message) {
        try {
            // Create 'logs' directory if it doesn't exist
            java.io.File logDir = new java.io.File("logs");
            if (!logDir.exists()) {
                logDir.mkdir();
            }
            
            // Write to the log file (append mode)
            try (FileWriter fw = new FileWriter(LOG_FILE, true)) {
                String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
                String logEntry = String.format("[%s] %s%n", timestamp, message);
                fw.write(logEntry);
                fw.flush(); // Immediately write to file
            }
        } catch (IOException e) {
            System.err.println("Error occurred while writing log: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
