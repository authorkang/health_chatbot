package com.example.calorie;

import io.grpc.Server;
import io.grpc.ServerBuilder;
import io.grpc.stub.StreamObserver;
import java.io.IOException;
import java.util.concurrent.TimeUnit;
import java.util.HashMap;
import java.util.Map;
import java.util.List;
import java.util.ArrayList;
import com.example.calorie.util.SimpleLogger;

/**
 * Workout Recommendation Service Server
 * Provides personalized workout recommendations based on target area and fitness level
 */
public class WorkoutRecommendationServer {
    private final int port;
    private final Server server;

    // Workout database (In production, use a proper database)
    private static final Map<String, List<WorkoutRecommendation>> WORKOUT_DATABASE = new HashMap<>();

    static {
        SimpleLogger.log("WorkoutRecommendationServer: Initializing workout database...");
        // Initialize workout database with sample data
        initializeWorkoutDatabase();
        SimpleLogger.log("WorkoutRecommendationServer: Workout database initialized successfully");
    }

    private static void initializeWorkoutDatabase() {
        SimpleLogger.log("WorkoutRecommendationServer: Initializing workout database...");
        
        // 가슴 운동 데이터베이스 초기화
        List<WorkoutRecommendation> chestWorkouts = new ArrayList<>();
        // 초급 가슴 운동
        chestWorkouts.add(WorkoutRecommendation.newBuilder()
                .setExerciseName("Push-ups")
                .setSets(3)
                .setReps(10)
                .setEquipment("None")
                .setDescription("Basic bodyweight exercise for chest")
                .setTips("Keep your body straight and elbows close to body")
                .setFitnessLevel("BEGINNER")
                .build());
        // 중급 가슴 운동
        chestWorkouts.add(WorkoutRecommendation.newBuilder()
                .setExerciseName("Diamond Push-ups")
                .setSets(4)
                .setReps(12)
                .setEquipment("None")
                .setDescription("Advanced variation of push-ups targeting inner chest")
                .setTips("Form a diamond shape with your hands")
                .setFitnessLevel("INTERMEDIATE")
                .build());
        // 고급 가슴 운동
        chestWorkouts.add(WorkoutRecommendation.newBuilder()
                .setExerciseName("Plyometric Push-ups")
                .setSets(5)
                .setReps(15)
                .setEquipment("None")
                .setDescription("Explosive push-up variation for advanced users")
                .setTips("Push with enough force to lift hands off the ground")
                .setFitnessLevel("ADVANCED")
                .build());
        WORKOUT_DATABASE.put("chest", chestWorkouts);
        SimpleLogger.log("WorkoutRecommendationServer: Added chest workouts to database");

        // 다리 운동 데이터베이스 초기화
        List<WorkoutRecommendation> legWorkouts = new ArrayList<>();
        // 초급 다리 운동
        legWorkouts.add(WorkoutRecommendation.newBuilder()
                .setExerciseName("Bodyweight Squats")
                .setSets(3)
                .setReps(12)
                .setEquipment("None")
                .setDescription("Basic lower body exercise")
                .setTips("Keep your back straight and knees aligned with toes")
                .setFitnessLevel("BEGINNER")
                .build());
        // 중급 다리 운동
        legWorkouts.add(WorkoutRecommendation.newBuilder()
                .setExerciseName("Jump Squats")
                .setSets(4)
                .setReps(15)
                .setEquipment("None")
                .setDescription("Dynamic squat variation with jump")
                .setTips("Land softly and maintain proper form")
                .setFitnessLevel("INTERMEDIATE")
                .build());
        // 고급 다리 운동
        legWorkouts.add(WorkoutRecommendation.newBuilder()
                .setExerciseName("Pistol Squats")
                .setSets(5)
                .setReps(8)
                .setEquipment("None")
                .setDescription("Single-leg squat variation for advanced users")
                .setTips("Keep the non-working leg extended forward")
                .setFitnessLevel("ADVANCED")
                .build());
        WORKOUT_DATABASE.put("legs", legWorkouts);
        SimpleLogger.log("WorkoutRecommendationServer: Added leg workouts to database");

        // 등 운동 데이터베이스 초기화
        List<WorkoutRecommendation> backWorkouts = new ArrayList<>();
        // 초급 등 운동
        backWorkouts.add(WorkoutRecommendation.newBuilder()
                .setExerciseName("Assisted Pull-ups")
                .setSets(3)
                .setReps(8)
                .setEquipment("Pull-up bar, resistance band")
                .setDescription("Modified pull-up for beginners")
                .setTips("Use resistance band for assistance")
                .setFitnessLevel("BEGINNER")
                .build());
        // 중급 등 운동
        backWorkouts.add(WorkoutRecommendation.newBuilder()
                .setExerciseName("Pull-ups")
                .setSets(4)
                .setReps(10)
                .setEquipment("Pull-up bar")
                .setDescription("Standard pull-up exercise")
                .setTips("Keep your core tight and control the movement")
                .setFitnessLevel("INTERMEDIATE")
                .build());
        // 고급 등 운동
        backWorkouts.add(WorkoutRecommendation.newBuilder()
                .setExerciseName("Muscle-ups")
                .setSets(5)
                .setReps(6)
                .setEquipment("Pull-up bar")
                .setDescription("Advanced pull-up variation transitioning to dip")
                .setTips("Explosive pull-up with transition to dip position")
                .setFitnessLevel("ADVANCED")
                .build());
        WORKOUT_DATABASE.put("back", backWorkouts);
        SimpleLogger.log("WorkoutRecommendationServer: Added back workouts to database");

        // 코어 운동 데이터베이스 초기화
        List<WorkoutRecommendation> coreWorkouts = new ArrayList<>();
        // 초급 코어 운동
        coreWorkouts.add(WorkoutRecommendation.newBuilder()
                .setExerciseName("Plank")
                .setSets(3)
                .setReps(30)
                .setEquipment("None")
                .setDescription("Basic core stabilization exercise")
                .setTips("Hold the position with a straight body")
                .setFitnessLevel("BEGINNER")
                .build());
        // 중급 코어 운동
        coreWorkouts.add(WorkoutRecommendation.newBuilder()
                .setExerciseName("Russian Twists")
                .setSets(4)
                .setReps(20)
                .setEquipment("None")
                .setDescription("Rotational core exercise")
                .setTips("Keep your feet off the ground and rotate slowly")
                .setFitnessLevel("INTERMEDIATE")
                .build());
        // 고급 코어 운동
        coreWorkouts.add(WorkoutRecommendation.newBuilder()
                .setExerciseName("Dragon Flags")
                .setSets(5)
                .setReps(8)
                .setEquipment("Bench or bar")
                .setDescription("Advanced core strength exercise")
                .setTips("Control the movement and maintain straight body")
                .setFitnessLevel("ADVANCED")
                .build());
        WORKOUT_DATABASE.put("core", coreWorkouts);
        SimpleLogger.log("WorkoutRecommendationServer: Added core workouts to database");
    }

    public WorkoutRecommendationServer(int port) {
        this.port = port;
        SimpleLogger.log("WorkoutRecommendationServer: Creating server instance on port " + port);
        this.server = ServerBuilder.forPort(port)
                .addService(new WorkoutRecommendationServiceImpl())
                .build();
        SimpleLogger.log("WorkoutRecommendationServer: Server instance created successfully");
    }

    public void start() throws IOException {
        server.start();
        SimpleLogger.log("WorkoutRecommendationServer: Started on port " + port);
        SimpleLogger.log("WorkoutRecommendationServer: Server configuration: " + server.toString());
    }

    public void stop() throws InterruptedException {
        if (server != null) {
            SimpleLogger.log("WorkoutRecommendationServer: Initiating shutdown...");
            server.shutdown().awaitTermination(30, TimeUnit.SECONDS);
            SimpleLogger.log("WorkoutRecommendationServer: Shutdown completed");
        }
    }

    private void blockUntilShutdown() throws InterruptedException {
        if (server != null) {
            SimpleLogger.log("WorkoutRecommendationServer: Waiting for termination...");
            server.awaitTermination();
        }
    }

    /**
     * Main method to start the server
     */
    public static void main(String[] args) throws IOException, InterruptedException {
        SimpleLogger.log("WorkoutRecommendationServer: Starting server...");
        final WorkoutRecommendationServer server = new WorkoutRecommendationServer(50053);
        server.start();
        
        Runtime.getRuntime().addShutdownHook(new Thread() {
            @Override
            public void run() {
                try {
                    server.stop();
                } catch (InterruptedException e) {
                    SimpleLogger.log("WorkoutRecommendationServer: Error during shutdown - " + e.getMessage());
                }
            }
        });
        
        server.blockUntilShutdown();
    }

    /**
     * Service implementation for handling workout recommendations
     */
    static class WorkoutRecommendationServiceImpl extends WorkoutRecommendationServiceGrpc.WorkoutRecommendationServiceImplBase {
        @Override
        public void getWorkoutRecommendations(WorkoutRequest request, StreamObserver<WorkoutRecommendation> responseObserver) {
            try {
                String targetArea = request.getTargetArea().toLowerCase();
                String fitnessLevel = request.getFitnessLevel().toUpperCase();
                
                SimpleLogger.log(String.format("WorkoutRecommendationServer: Received request - Target Area: %s, Fitness Level: %s", 
                    targetArea, fitnessLevel));

                // 타겟 영역 이름 매핑
                String mappedTargetArea = targetArea;
                if (targetArea.equals("lower_body")) {
                    mappedTargetArea = "legs";
                } else if (targetArea.equals("upper_body")) {
                    mappedTargetArea = "chest";
                } else if (targetArea.equals("core")) {
                    mappedTargetArea = "core";
                }

                List<WorkoutRecommendation> workouts = WORKOUT_DATABASE.get(mappedTargetArea);
                if (workouts != null) {
                    boolean foundWorkout = false;
                    for (WorkoutRecommendation workout : workouts) {
                        if (workout.getFitnessLevel().equals(fitnessLevel)) {
                            SimpleLogger.log(String.format("WorkoutRecommendationServer: Recommending - Exercise: %s, Sets: %d, Reps: %d, Level: %s", 
                                workout.getExerciseName(), workout.getSets(), workout.getReps(), workout.getFitnessLevel()));
                            responseObserver.onNext(workout);
                            foundWorkout = true;
                        }
                    }
                    
                    if (!foundWorkout) {
                        String errorMsg = String.format("WorkoutRecommendationServer: No workouts found for target area: %s with fitness level: %s", 
                            targetArea, fitnessLevel);
                        SimpleLogger.log(errorMsg);
                        throw new IllegalArgumentException(errorMsg);
                    }
                    
                    SimpleLogger.log(String.format("WorkoutRecommendationServer: Successfully sent recommendations for target area: %s, level: %s", 
                        targetArea, fitnessLevel));
                } else {
                    String errorMsg = String.format("WorkoutRecommendationServer: No workouts found for target area: %s (mapped to: %s)", 
                        targetArea, mappedTargetArea);
                    SimpleLogger.log(errorMsg);
                    throw new IllegalArgumentException(errorMsg);
                }
                
                responseObserver.onCompleted();
                SimpleLogger.log("WorkoutRecommendationServer: Request completed successfully");
                
            } catch (Exception e) {
                String errorMsg = "WorkoutRecommendationServer: Error processing request - " + e.getMessage();
                SimpleLogger.log(errorMsg);
                responseObserver.onError(io.grpc.Status.INTERNAL
                        .withDescription(errorMsg)
                        .asException());
            }
        }
    }
} 