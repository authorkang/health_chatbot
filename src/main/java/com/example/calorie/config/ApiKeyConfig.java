package com.example.calorie.config;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;
import java.util.HashSet;
import java.util.Set;

/**
 * API 키 설정을 관리하는 클래스
 * 서버와 클라이언트 간의 인증을 위한 API 키를 관리합니다.
 */
public class ApiKeyConfig {
    // API 키 값들을 저장하는 Set
    private static final Set<String> API_KEYS = new HashSet<>();
    
    // 정적 초기화 블록에서 설정 파일 로드
    static {
        // 기본 API 키 추가
        API_KEYS.add("calorie-service-key-2024");
        
        try (InputStream input = ApiKeyConfig.class.getClassLoader().getResourceAsStream("application.properties")) {
            if (input != null) {
                Properties properties = new Properties();
                properties.load(input);
                
                // 설정 파일에서 모든 API 키 로드
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
            System.err.println("API 키 설정 파일을 로드하는 중 오류 발생: " + e.getMessage());
        }
        
        // 로드된 API 키 출력 (디버깅용)
        System.out.println("로드된 API 키 목록:");
        for (String key : API_KEYS) {
            System.out.println("- " + key);
        }
    }
    
    /**
     * API 키의 유효성을 검사하는 메서드
     * @param key 검사할 API 키
     * @return API 키가 유효하면 true, 아니면 false
     */
    public static boolean isValidApiKey(String key) {
        return API_KEYS.contains(key);
    }
    
    /**
     * 첫 번째 API 키 값을 반환하는 메서드
     * @return 첫 번째 API 키 값
     */
    public static String getApiKey() {
        return API_KEYS.iterator().next();
    }
    
    /**
     * 모든 API 키 값을 반환하는 메서드
     * @return 모든 API 키 값이 포함된 Set
     */
    public static Set<String> getAllApiKeys() {
        return new HashSet<>(API_KEYS);
    }
} 