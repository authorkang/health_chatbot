package com.example.calorie;

import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;

/**
 * Basic Calorie Calculation Client
 * Provides interface for calculating daily calorie needs
 */
public class CalorieClient {
    private static final Logger logger = Logger.getLogger(CalorieClient.class.getName());
    private final ManagedChannel channel;
    private final CalorieServiceGrpc.CalorieServiceBlockingStub blockingStub;

    public CalorieClient(String host, int port) {
        this.channel = ManagedChannelBuilder.forAddress(host, port)
                .usePlaintext()
                .build();
        this.blockingStub = CalorieServiceGrpc.newBlockingStub(channel);
        logger.info("Client initialized with host: " + host + ", port: " + port);
    }

    public void shutdown() throws InterruptedException {
        channel.shutdown().awaitTermination(5, TimeUnit.SECONDS);
    }

    public void calculateDailyCalories(int age, String gender, double weight, double height, String activityLevel) {
        try {
            UserInfo request = UserInfo.newBuilder()
                .setAge(age)
                .setGender(gender)
                .setWeight(weight)
                .setHeight(height)
                .setActivityLevel(activityLevel)
                .build();

            CalorieResult response = blockingStub.calculateDailyCalories(request);

            System.out.println("\nCalculation Results:");
            System.out.println("Basal Metabolic Rate (BMR): " + response.getBmr() + " calories");
            System.out.println("Total Daily Energy Expenditure (TDEE): " + response.getTdee() + " calories");
            System.out.println("Calories for Weight Loss: " + response.getWeightLossCalories() + " calories");
            System.out.println("Calories for Weight Gain: " + response.getWeightGainCalories() + " calories");

        } catch (Exception e) {
            logger.severe("Error calculating calories: " + e.getMessage());
            System.err.println("Error occurred: " + e.getMessage());
        }
    }

    public static void main(String[] args) throws Exception {
        String host = "localhost";
        int port = 50052;
        
        final CalorieClient client = new CalorieClient(host, port);
        
        try {
            // 사용자 입력 받기
            java.util.Scanner scanner = new java.util.Scanner(System.in);
            
            System.out.println("=== Calorie Calculation Service ===");
            System.out.println("Calculating your daily calorie needs.");
            
            System.out.print("Enter your age: ");
            int age = Integer.parseInt(scanner.nextLine());
            
            System.out.print("Enter your gender (MALE/FEMALE): ");
            String gender = scanner.nextLine().toUpperCase();
            
            System.out.print("Enter your weight (kg): ");
            double weight = Double.parseDouble(scanner.nextLine());
            
            System.out.print("Enter your height (cm): ");
            double height = Double.parseDouble(scanner.nextLine());
            
            System.out.println("\nSelect your activity level:");
            System.out.println("1. SEDENTARY (Little to no exercise)");
            System.out.println("2. LIGHT (Light exercise 1-3 days/week)");
            System.out.println("3. MODERATE (Moderate exercise 3-5 days/week)");
            System.out.println("4. VERY_ACTIVE (Hard exercise 6-7 days/week)");
            System.out.println("5. EXTRA_ACTIVE (Very hard exercise + physical job)");
            System.out.print("Select (1-5): ");
            
            int activityChoice = Integer.parseInt(scanner.nextLine());
            String activityLevel;
            
            switch (activityChoice) {
                case 1: activityLevel = "SEDENTARY"; break;
                case 2: activityLevel = "LIGHT"; break;
                case 3: activityLevel = "MODERATE"; break;
                case 4: activityLevel = "VERY_ACTIVE"; break;
                case 5: activityLevel = "EXTRA_ACTIVE"; break;
                default: activityLevel = "MODERATE"; break;
            }
            
            client.calculateDailyCalories(age, gender, weight, height, activityLevel);
            
            scanner.close();
        } finally {
            client.shutdown();
        }
    }
} 