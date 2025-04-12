package com.example.calorie;

import io.grpc.Server;
import io.grpc.ServerBuilder;
import io.grpc.stub.StreamObserver;
import io.grpc.Status;
import com.example.calorie.CalorieServiceGrpc.CalorieServiceImplBase;
import com.example.calorie.UserInfo;
import com.example.calorie.CalorieResult;
import com.example.calorie.config.ApiKeyConfig;
import io.grpc.Context;
import io.grpc.Metadata;

import java.io.IOException;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;

/**
 * Basic Calorie Calculation Server
 * Provides daily calorie needs calculation based on user information
 */
public class CalorieServer {
    private static final Logger logger = Logger.getLogger(CalorieServer.class.getName());
    private final int port;
    private final Server server;

    public CalorieServer(int port) {
        this.port = port;
        this.server = ServerBuilder.forPort(port)
                .addService(new CalorieServiceImpl())
                .build();
    }

    public void start() throws IOException {
        server.start();
        logger.info("Calorie Server started, listening on port " + port);
        logger.info("Server configuration: " + server.toString());
    }

    public void stop() throws InterruptedException {
        if (server != null) {
            logger.info("Stopping server...");
            server.shutdown().awaitTermination(30, TimeUnit.SECONDS);
            logger.info("Server stopped");
        }
    }

    private static class CalorieServiceImpl extends CalorieServiceImplBase {
        @Override
        public void calculateDailyCalories(UserInfo request, StreamObserver<CalorieResult> responseObserver) {
            try {
                // Input validation
                if (request.getAge() <= 0) {
                    responseObserver.onError(Status.INVALID_ARGUMENT
                            .withDescription("Age must be greater than 0")
                            .asException());
                    return;
                }
                if (request.getWeight() <= 0) {
                    responseObserver.onError(Status.INVALID_ARGUMENT
                            .withDescription("Weight must be greater than 0")
                            .asException());
                    return;
                }
                if (request.getHeight() <= 0) {
                    responseObserver.onError(Status.INVALID_ARGUMENT
                            .withDescription("Height must be greater than 0")
                            .asException());
                    return;
                }
                if (!request.getGender().equalsIgnoreCase("MALE") && !request.getGender().equalsIgnoreCase("FEMALE")) {
                    responseObserver.onError(Status.INVALID_ARGUMENT
                            .withDescription("Gender must be 'MALE' or 'FEMALE'")
                            .asException());
                    return;
                }

                // BMR calculation
                double bmr = calculateBMR(request);
                
                // Set activity factor
                double activityFactor = getActivityMultiplier(request.getActivityLevel());
                
                // TDEE calculation
                double tdee = bmr * activityFactor;
                
                // Calculate calories for weight loss/gain
                double weightLossCalories = tdee - 500; // Reduce by 500 calories
                double weightGainCalories = tdee + 500; // Increase by 500 calories

                // Build response
                CalorieResult result = CalorieResult.newBuilder()
                    .setBmr(bmr)
                    .setTdee(tdee)
                    .setWeightLossCalories(weightLossCalories)
                    .setWeightGainCalories(weightGainCalories)
                    .build();

                responseObserver.onNext(result);
                responseObserver.onCompleted();

            } catch (Exception e) {
                logger.severe("Error calculating calories: " + e.getMessage());
                responseObserver.onError(Status.INTERNAL
                        .withDescription("Error calculating calories: " + e.getMessage())
                        .asException());
            }
        }

        private double calculateBMR(UserInfo request) {
            if (request.getGender().equalsIgnoreCase("MALE")) {
                // BMR formula for males
                return (10 * request.getWeight()) + (6.25 * request.getHeight()) - (5 * request.getAge()) + 5;
            } else {
                // BMR formula for females
                return (10 * request.getWeight()) + (6.25 * request.getHeight()) - (5 * request.getAge()) - 161;
            }
        }

        private double getActivityMultiplier(String activityLevel) {
            switch (activityLevel.toUpperCase()) {
                case "SEDENTARY":
                    return 1.2;      // Little or no exercise
                case "LIGHT":
                    return 1.375;    // Light exercise (1-3 days/week)
                case "MODERATE":
                    return 1.55;     // Moderate exercise (3-5 days/week)
                case "VERY_ACTIVE":
                    return 1.725;    // Heavy exercise (6-7 days/week)
                case "EXTRA_ACTIVE":
                    return 1.9;      // Extra active (very heavy exercise/physical job + exercise)
                default:
                    throw new IllegalArgumentException("Invalid activity level. Must be one of: SEDENTARY, LIGHT, MODERATE, VERY_ACTIVE, EXTRA_ACTIVE");
            }
        }
    }

    public static void main(String[] args) throws Exception {
        int port = 50052;  // Change the port to 50052
        if (args.length > 0) {
            port = Integer.parseInt(args[0]);
        }
        
        final CalorieServer server = new CalorieServer(port);
        server.start();
        
        // Keep the main thread alive to prevent server shutdown
        try {
            // Run server in infinite loop
            while (true) {
                Thread.sleep(1000);
            }
        } catch (InterruptedException e) {
            logger.severe("Server interrupted: " + e.getMessage());
            server.stop();
            throw e;
        }
    }

    private void blockUntilShutdown() throws InterruptedException {
        if (server != null) {
            // Wait until server shuts down
            server.awaitTermination();
        }
    }
} 