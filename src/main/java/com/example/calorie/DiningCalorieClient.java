package com.example.calorie;

import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import io.grpc.stub.StreamObserver;
import java.util.Scanner;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;
import java.util.ArrayList;
import java.util.List;
import com.example.calorie.DiningCalorieServiceGrpc;
import com.example.calorie.FoodCalorieInfo;
import com.example.calorie.FoodItem;

/**
 * Dining Calorie Calculator Client
 * Calculates calories in real-time for user input foods and shows the total sum
 */
public class DiningCalorieClient {
    private static final Logger logger = Logger.getLogger(DiningCalorieClient.class.getName());
    private final ManagedChannel channel;
    private final DiningCalorieServiceGrpc.DiningCalorieServiceStub asyncStub;
    private List<FoodCalorieInfo> foodList = new ArrayList<>();

    public DiningCalorieClient(String host, int port) {
        channel = ManagedChannelBuilder.forAddress(host, port)
                .usePlaintext()
                .build();
        asyncStub = DiningCalorieServiceGrpc.newStub(channel);
    }

    /**
     * Get the async stub for gRPC communication
     * @return The async stub
     */
    public DiningCalorieServiceGrpc.DiningCalorieServiceStub getAsyncStub() {
        return asyncStub;
    }

    public void shutdown() throws InterruptedException {
        channel.shutdown().awaitTermination(5, TimeUnit.SECONDS);
    }

    /**
     * Execute the calorie calculation service
     * Takes food name and quantity as input, shows real-time calorie information
     * and calculates the total sum at the end
     */
    public void calculateCalories() {
        final CountDownLatch finishLatch = new CountDownLatch(1);
        Scanner scanner = new Scanner(System.in);
        
        // Set up StreamObserver for handling stream responses
        StreamObserver<FoodCalorieInfo> responseObserver = new StreamObserver<FoodCalorieInfo>() {
            @Override
            public void onNext(FoodCalorieInfo response) {
                System.out.println("\nFood Information:");
                System.out.println("Name: " + response.getName());
                System.out.println("Calories: " + response.getCalories());
                System.out.println("Message: " + response.getMessage());  // Message from server containing cumulative total calories
                System.out.println("------------------------");
                foodList.add(response);
            }

            @Override
            public void onError(Throwable t) {
                System.err.println("Error occurred: " + t.getMessage());
                finishLatch.countDown();
            }

            @Override
            public void onCompleted() {
                finishLatch.countDown();
            }
        };

        // Create StreamObserver for sending food information to server
        StreamObserver<FoodItem> requestObserver = asyncStub.streamFoodCalories(responseObserver);

        try {
            // Display available food menu
            System.out.println("\n=== Calorie Calculator Service ===");
            System.out.println("Please select from the following foods:");
            System.out.println("- hamburger (550 calories)");
            System.out.println("- pizza (285 calories)");
            System.out.println("- salad (100 calories)");
            System.out.println("- french fries (365 calories)");
            System.out.println("- coke (140 calories)");
            System.out.println("- beer (150 calories)");
            System.out.println("- rice (130 calories)");
            System.out.println("- kimchi (15 calories)");
            System.out.println("- ramen (450 calories)");
            System.out.println("- bibimbap (550 calories)");
            System.out.println("\nHow to use:");
            System.out.println("1. Enter your food name (as shown above)");
            System.out.println("2. Enter the quantity");
            System.out.println("3. Repeat until enter all the foods");
            System.out.println("4. Enter 'quit' to finish and see total calories");
            System.out.println("----------------------------------------");

            while (true) {
                System.out.print("\nEnter food name (or 'quit' to finish): ");
                String foodName = scanner.nextLine().trim().toLowerCase();
                
                if (foodName.equals("quit")) {
                    break;
                }

                System.out.print("Enter quantity (default: 1): ");
                String quantityStr = scanner.nextLine().trim();
                int quantity = quantityStr.isEmpty() ? 1 : Integer.parseInt(quantityStr);

                // Send food information
                FoodItem foodItem = FoodItem.newBuilder()
                    .setName(foodName)
                    .setQuantity(quantity)
                    .build();
                requestObserver.onNext(foodItem);

                System.out.println("\nWhat would you like to do next?");
                System.out.println("- Enter another food name to add more items");
                System.out.println("- Type 'quit' to finish and see total calories");
            }

            // End stream
            requestObserver.onCompleted();

            // Wait for final result
            finishLatch.await(1, TimeUnit.MINUTES);

            // Calculate and display total calories
            System.out.println("\n=== Order Summary and Total Calories ===");
            for (FoodCalorieInfo food : foodList) {
                System.out.printf("%s: %d calories\n", food.getName(), food.getCalories());
            }
            System.out.println("----------------------------------------");
            System.out.printf("Total Calories: %d calories\n", foodList.stream().mapToInt(FoodCalorieInfo::getCalories).sum());

        } catch (NumberFormatException e) {
            System.err.println("Invalid quantity. Please enter numbers only.");
            requestObserver.onError(e);
        } catch (Exception e) {
            System.err.println("Error occurred: " + e.getMessage());
            requestObserver.onError(e);
        }
    }

    public static void main(String[] args) throws Exception {
        DiningCalorieClient client = new DiningCalorieClient("localhost", 50051);
        try {
            client.calculateCalories();
        } finally {
            client.shutdown();
        }
    }
} 