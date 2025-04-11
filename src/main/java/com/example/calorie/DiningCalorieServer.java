package com.example.calorie;

import io.grpc.Server;
import io.grpc.ServerBuilder;
import io.grpc.stub.StreamObserver;
import java.io.IOException;
import java.util.concurrent.TimeUnit;
import java.util.HashMap;
import java.util.Map;
import com.example.calorie.util.SimpleLogger;

/**
 * Dining Calorie Calculator Server
 * Provides real-time calorie information for various food items
 */
public class DiningCalorieServer {
    private final int port;
    private final Server server;
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
        
        SimpleLogger.log("DiningCalorieServer: Food calorie database initialized with " + FOOD_CALORIES.size() + " items");
    }

    public DiningCalorieServer(int port) {
        this.port = port;
        this.server = ServerBuilder.forPort(port)
                .addService(new DiningCalorieServiceImpl())
                .build();
        SimpleLogger.log("DiningCalorieServer: Server instance created on port " + port);
    }

    public void start() throws IOException {
        server.start();
        SimpleLogger.log("DiningCalorieServer: Server started on port " + port);
        SimpleLogger.log("DiningCalorieServer: Server configuration - " + server.toString());
    }

    public void stop() throws InterruptedException {
        if (server != null) {
            SimpleLogger.log("DiningCalorieServer: Initiating server shutdown...");
            server.shutdown().awaitTermination(30, TimeUnit.SECONDS);
            SimpleLogger.log("DiningCalorieServer: Server stopped successfully");
        }
    }

    private void blockUntilShutdown() throws InterruptedException {
        if (server != null) {
            SimpleLogger.log("DiningCalorieServer: Waiting for server termination...");
            server.awaitTermination();
        }
    }

    /**
     * Main method to start the server
     */
    public static void main(String[] args) throws IOException, InterruptedException {
        SimpleLogger.log("DiningCalorieServer: Starting server initialization...");
        final DiningCalorieServer server = new DiningCalorieServer(50051);
        server.start();
        
        Runtime.getRuntime().addShutdownHook(new Thread() {
            @Override
            public void run() {
                try {
                    server.stop();
                } catch (InterruptedException e) {
                    SimpleLogger.log("DiningCalorieServer: Error during server shutdown - " + e.getMessage());
                }
            }
        });
        
        server.blockUntilShutdown();
    }

    /**
     * Service implementation for handling food calorie calculations
     */
    static class DiningCalorieServiceImpl extends DiningCalorieServiceGrpc.DiningCalorieServiceImplBase {
        @Override
        public StreamObserver<FoodItem> streamFoodCalories(final StreamObserver<FoodCalorieInfo> responseObserver) {
            SimpleLogger.log("DiningCalorieServer: New streaming food calories request received");
            return new StreamObserver<FoodItem>() {
                private int runningTotal = 0;

                @Override
                public void onNext(FoodItem foodItem) {
                    String foodName = foodItem.getName().toLowerCase();
                    int quantity = foodItem.getQuantity();
                    
                    SimpleLogger.log(String.format("DiningCalorieServer: Processing food item - Name: %s, Quantity: %d", foodName, quantity));
                    
                    if (FOOD_CALORIES.containsKey(foodName)) {
                        int caloriesPerServing = FOOD_CALORIES.get(foodName);
                        int itemCalories = caloriesPerServing * quantity;
                        runningTotal += itemCalories;
                        
                        SimpleLogger.log(String.format("DiningCalorieServer: Calculated calories - Food: %s, Calories: %d, Total: %d", 
                            foodName, itemCalories, runningTotal));
                        
                        FoodCalorieInfo response = FoodCalorieInfo.newBuilder()
                            .setName(foodName)
                            .setCalories(itemCalories)
                            .setMessage(String.format("%s: %d calories per serving (Total so far: %d kcal)", 
                                foodName, caloriesPerServing, runningTotal))
                            .build();
                        responseObserver.onNext(response);
                    } else {
                        SimpleLogger.log("DiningCalorieServer: Food item not found - " + foodName);
                        
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
                    SimpleLogger.log("DiningCalorieServer: Stream error - " + t.getMessage());
                }

                @Override
                public void onCompleted() {
                    SimpleLogger.log("DiningCalorieServer: Stream completed - Total calories: " + runningTotal);
                    responseObserver.onCompleted();
                }
            };
        }

        @Override
        public StreamObserver<FoodItem> calculateTotalCalories(final StreamObserver<TotalCalorieResult> responseObserver) {
            SimpleLogger.log("DiningCalorieServer: New total calories calculation request received");
            return new StreamObserver<FoodItem>() {
                private int totalCalories = 0;
                private int itemCount = 0;

                @Override
                public void onNext(FoodItem foodItem) {
                    String foodName = foodItem.getName().toLowerCase();
                    int quantity = foodItem.getQuantity();
                    
                    SimpleLogger.log(String.format("DiningCalorieServer: Processing total calculation - Food: %s, Quantity: %d", 
                        foodName, quantity));
                    
                    if (FOOD_CALORIES.containsKey(foodName)) {
                        int caloriesPerServing = FOOD_CALORIES.get(foodName);
                        int itemCalories = caloriesPerServing * quantity;
                        totalCalories += itemCalories;
                        itemCount++;
                        
                        SimpleLogger.log(String.format("DiningCalorieServer: Added to total - Food: %s, Calories: %d, Total: %d", 
                            foodName, itemCalories, totalCalories));
                    } else {
                        SimpleLogger.log("DiningCalorieServer: Food item not found - " + foodName);
                    }
                }

                @Override
                public void onError(Throwable t) {
                    SimpleLogger.log("DiningCalorieServer: Stream error - " + t.getMessage());
                }

                @Override
                public void onCompleted() {
                    String resultMsg = String.format("Total calories for %d items: %d", itemCount, totalCalories);
                    SimpleLogger.log("DiningCalorieServer: Calculation completed - " + resultMsg);
                    
                    TotalCalorieResult result = TotalCalorieResult.newBuilder()
                        .setTotalCalories(totalCalories)
                        .setItemCount(itemCount)
                        .setMessage(resultMsg)
                        .build();
                    responseObserver.onNext(result);
                    responseObserver.onCompleted();
                }
            };
        }
    }
} 