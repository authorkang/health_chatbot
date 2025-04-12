package com.example.calorie;

import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;
import java.util.Scanner;
import java.util.function.Consumer;

/**
 * Workout Recommendation Service Client
 * Receives target area and fitness level from user to get personalized workout recommendations
 */
public class WorkoutRecommendationClient {
    private static final Logger logger = Logger.getLogger(WorkoutRecommendationClient.class.getName());
    private final ManagedChannel channel;
    private final WorkoutRecommendationServiceGrpc.WorkoutRecommendationServiceBlockingStub blockingStub;

    public WorkoutRecommendationClient(String host, int port) {
        this.channel = ManagedChannelBuilder.forAddress(host, port)
                .usePlaintext()
                .build();
        this.blockingStub = WorkoutRecommendationServiceGrpc.newBlockingStub(channel);
    }

    public void shutdown() throws InterruptedException {
        channel.shutdown().awaitTermination(5, TimeUnit.SECONDS);
    }

    /**
     * Get workout recommendations
     * @param targetArea Target area (UPPER_BODY, LOWER_BODY, CORE)
     * @param fitnessLevel Fitness level (BEGINNER, INTERMEDIATE, ADVANCED)
     * @param callback Callback to handle each recommendation
     */
    public void getWorkoutRecommendationsAsync(String targetArea, String fitnessLevel, Consumer<WorkoutRecommendation> callback) {
        logger.info("Requesting workout recommendations for " + targetArea + " at " + fitnessLevel + " level");
        
        WorkoutRequest request = WorkoutRequest.newBuilder()
                .setTargetArea(targetArea)
                .setFitnessLevel(fitnessLevel)
                .build();

        try {
            blockingStub.getWorkoutRecommendations(request)
                    .forEachRemaining(recommendation -> {
                        logger.info("Received workout recommendation:");
                        logger.info("Exercise: " + recommendation.getExerciseName());
                        logger.info("Sets: " + recommendation.getSets());
                        logger.info("Reps: " + recommendation.getReps());
                        logger.info("Equipment: " + recommendation.getEquipment());
                        logger.info("Description: " + recommendation.getDescription());
                        logger.info("Tips: " + recommendation.getTips());
                        logger.info("-------------------");
                        
                        // Invoke callback
                        callback.accept(recommendation);
                    });
        } catch (Exception e) {
            logger.warning("Error getting workout recommendations: " + e.getMessage());
            throw e;
        }
    }

    public static void main(String[] args) throws Exception {
        // Create client
        WorkoutRecommendationClient client = new WorkoutRecommendationClient("localhost", 50053);
        Scanner scanner = new Scanner(System.in);

        try {
            System.out.println("Welcome to the Gym Workout Recommendation System!");
            
            while (true) {
                // Select target area
                System.out.println("1. Upper Body");
                System.out.println("2. Lower Body");
                System.out.println("3. Core");
                System.out.print("Enter your choice (1-3): ");
                
                int areaChoice = scanner.nextInt();
                String targetArea = switch (areaChoice) {
                    case 1 -> "UPPER_BODY";
                    case 2 -> "LOWER_BODY";
                    case 3 -> "CORE";
                    default -> throw new IllegalArgumentException("Invalid choice");
                };

                // Select fitness level
                System.out.println("\nPlease select your fitness level:");
                System.out.println("1. Beginner");
                System.out.println("2. Intermediate");
                System.out.println("3. Advanced");
                System.out.print("Enter your choice (1-3): ");
                
                int levelChoice = scanner.nextInt();
                String fitnessLevel = switch (levelChoice) {
                    case 1 -> "BEGINNER";
                    case 2 -> "INTERMEDIATE";
                    case 3 -> "ADVANCED";
                    default -> throw new IllegalArgumentException("Invalid choice");
                };

                System.out.println("\nConnecting to the workout recommendation service...");
                client.getWorkoutRecommendationsAsync(targetArea, fitnessLevel, recommendation -> {
                    // Print to console
                    System.out.println("Exercise: " + recommendation.getExerciseName());
                    System.out.println("Sets: " + recommendation.getSets());
                    System.out.println("Reps: " + recommendation.getReps());
                    System.out.println("Equipment: " + recommendation.getEquipment());
                    System.out.println("Description: " + recommendation.getDescription());
                    System.out.println("Tips: " + recommendation.getTips());
                    System.out.println("-------------------");
                });

                // Ask if user wants to continue
                System.out.print("\nWould you like to get recommendations for another area or fitness level? (y/n) ");
                scanner.nextLine(); // Clear buffer
                String answer = scanner.nextLine();
                if (!answer.toLowerCase().startsWith("y")) {
                    break;
                }
            }

            System.out.println("Thank you for using the Gym Workout Recommendation System!");

        } finally {
            scanner.close();
            client.shutdown();
        }
    }
} 