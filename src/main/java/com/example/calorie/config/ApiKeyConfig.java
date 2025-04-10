package com.example.calorie.config;

/**
 * API 키 설정을 관리하는 클래스
 * 서버와 클라이언트 간의 인증을 위한 API 키를 관리합니다.
 */
public class ApiKeyConfig {
    // API 키 값 설정
    private static final String API_KEY = "calorie-service-key-2024";
    
    /**
     * API 키의 유효성을 검사하는 메서드
     * @param key 검사할 API 키
     * @return API 키가 유효하면 true, 아니면 false
     */
    public static boolean isValidApiKey(String key) {
        return API_KEY.equals(key);
    }
    
    /**
     * API 키 값을 반환하는 메서드
     * @return API 키 값
     */
    public static String getApiKey() {
        return API_KEY;
    }
} 