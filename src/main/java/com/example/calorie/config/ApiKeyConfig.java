package com.example.calorie.config;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;
import java.util.HashSet;
import java.util.Set;

/**
 * Class that manages API key configuration.
 * Manages API keys for authentication between server and client.
 */
public class ApiKeyConfig {
    // Set to store API key values
    private static final Set<String> API_KEYS = new HashSet<>();
    
    // Load configuration file in static initializer block
    static {
        // Add default API key
        API_KEYS.add("calorie-service-key-2024");
        
        try (InputStream input = ApiKeyConfig.class.getClassLoader().getResourceAsStream("application.properties")) {
            if (input != null) {
                Properties properties = new Properties();
                properties.load(input);
                
                // Load all API keys from the configuration file
                for (String key : properties.stringPropertyNames()) {
                    if (key.startsWith("api.key")) {
                        String apiKey = properties.getProperty(key);
                        if (apiKey != null && !apiKey.isEmpty()) {
                            API_KEYS.add(apiKey);
                        }
                    }
                }
            }
        } catch (IOException e) {
            System.err.println("Error occurred while loading the API key configuration file: " + e.getMessage());
        }
        
        // Print loaded API keys (for debugging)
        System.out.println("Loaded API keys:");
        for (String key : API_KEYS) {
            System.out.println("- " + key);
        }
    }
    
    /**
     * Method to validate an API key
     * @param key API key to validate
     * @return true if the API key is valid, false otherwise
     */
    public static boolean isValidApiKey(String key) {
        return API_KEYS.contains(key);
    }
    
    /**
     * Method to return the first API key
     * @return the first API key value
     */
    public static String getApiKey() {
        return API_KEYS.iterator().next();
    }
    
    /**
     * Method to return all API key values
     * @return Set containing all API key values
     */
    public static Set<String> getAllApiKeys() {
        return new HashSet<>(API_KEYS);
    }
}
