package com.example.calorie;

import io.grpc.Server;
import io.grpc.ServerBuilder;
import io.grpc.stub.StreamObserver;
import java.io.IOException;
import java.util.concurrent.TimeUnit;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

/**
 * Dining Calorie Calculator Server
 * Provides real-time calorie information for various food items
 */
public class DiningCalorieServer {
    private static final Logger logger = Logger.getLogger(DiningCalorieServer.class.getName());

    private Server server;
    private static final Map<String, Integer> FOOD_CALORIES = new HashMap<>();

    static {
        // Initialize food calorie database
        FOOD_CALORIES.put("hamburger", 550);
        FOOD_CALORIES.put("pizza", 285);
        FOOD_CALORIES.put("salad", 100);
        FOOD_CALORIES.put("french fries", 365);
        FOOD_CALORIES.put("coke", 140);
        FOOD_CALORIES.put("beer", 150);
        FOOD_CALORIES.put("rice", 130);
        FOOD_CALORIES.put("kimchi", 15);
        FOOD_CALORIES.put("ramen", 450);
        FOOD_CALORIES.put("bibimbap", 550);
    }

    private void start() throws IOException {
        int port = 50051;
        server = ServerBuilder.forPort(port)
                .addService(new DiningCalorieServiceImpl())
                .build()
                .start();
        logger.info("Server started, listening on " + port);

        Runtime.getRuntime().addShutdownHook(new Thread() {
            @Override
            public void run() {
                try {
                    DiningCalorieServer.this.stop();
                } catch (InterruptedException e) {
                    logger.warning(e.getMessage());
                }
                logger.info("Server shut down");
            }
        });
    }

    private void stop() throws InterruptedException {
        if (server != null) {
            server.shutdown().awaitTermination(30, TimeUnit.SECONDS);
        }
    }

    private void blockUntilShutdown() throws InterruptedException {
        if (server != null) {
            server.awaitTermination();
        }
    }

    /**
     * Main method to start the server
     */
    public static void main(String[] args) throws IOException, InterruptedException {
        final DiningCalorieServer server = new DiningCalorieServer();
        server.start();
        server.blockUntilShutdown();
    }

    /**
     * Service implementation for handling food calorie calculations
     */
    static class DiningCalorieServiceImpl extends DiningCalorieServiceGrpc.DiningCalorieServiceImplBase {
        @Override
        public StreamObserver<FoodItem> streamFoodCalories(final StreamObserver<FoodCalorieInfo> responseObserver) {
            return new StreamObserver<FoodItem>() {
                private int runningTotal = 0;  // Variable to store cumulative total calories

                @Override
                public void onNext(FoodItem foodItem) {
                    String foodName = foodItem.getName().toLowerCase();
                    int quantity = foodItem.getQuantity();
                    
                    if (FOOD_CALORIES.containsKey(foodName)) {
                        int caloriesPerServing = FOOD_CALORIES.get(foodName);
                        int itemCalories = caloriesPerServing * quantity;
                        runningTotal += itemCalories;  // Calculate cumulative total calories
                        
                        FoodCalorieInfo response = FoodCalorieInfo.newBuilder()
                            .setName(foodName)
                            .setCalories(itemCalories)
                            .setMessage(String.format("%s: %d calories per serving (Total so far: %d kcal)", 
                                foodName, caloriesPerServing, runningTotal))
                            .build();
                        responseObserver.onNext(response);
                    } else {
                        FoodCalorieInfo response = FoodCalorieInfo.newBuilder()
                            .setName(foodName)
                            .setCalories(0)
                            .setMessage("Food item not found in database. Please check the menu for available items.")
                            .build();
                        responseObserver.onNext(response);
                    }
                }

                @Override
                public void onError(Throwable t) {
                    logger.warning("Error in stream: " + t.getMessage());
                }

                @Override
                public void onCompleted() {
                    responseObserver.onCompleted();
                }
            };
        }

        @Override
        public StreamObserver<FoodItem> calculateTotalCalories(final StreamObserver<TotalCalorieResult> responseObserver) {
            return new StreamObserver<FoodItem>() {
                private int totalCalories = 0;
                private int itemCount = 0;

                @Override
                public void onNext(FoodItem foodItem) {
                    String foodName = foodItem.getName().toLowerCase();
                    int quantity = foodItem.getQuantity();
                    
                    if (FOOD_CALORIES.containsKey(foodName)) {
                        totalCalories += FOOD_CALORIES.get(foodName) * quantity;
                        itemCount++;
                    }
                }

                @Override
                public void onError(Throwable t) {
                    logger.warning("Error in stream: " + t.getMessage());
                }

                @Override
                public void onCompleted() {
                    TotalCalorieResult result = TotalCalorieResult.newBuilder()
                        .setTotalCalories(totalCalories)
                        .setItemCount(itemCount)
                        .setMessage(String.format("Total calories for %d items: %d", itemCount, totalCalories))
                        .build();
                    responseObserver.onNext(result);
                    responseObserver.onCompleted();
                }
            };
        }
    }
} 