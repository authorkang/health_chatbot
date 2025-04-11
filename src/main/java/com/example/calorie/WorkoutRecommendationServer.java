package com.example.calorie;

import io.grpc.Server;
import io.grpc.ServerBuilder;
import io.grpc.stub.StreamObserver;
import java.io.IOException;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;
import java.util.HashMap;
import java.util.Map;
import java.util.List;
import java.util.ArrayList;

/**
 * Workout Recommendation Service Server
 * Provides personalized workout recommendations based on target area and fitness level
 */
public class WorkoutRecommendationServer {
    private static final Logger logger = Logger.getLogger(WorkoutRecommendationServer.class.getName());
    private Server server;
    private final int port;

    // Workout database (In production, use a proper database)
    private static final Map<String, List<WorkoutData>> WORKOUT_DATABASE = new HashMap<>();

    static {
        // Upper body exercises
        List<WorkoutData> upperBody = new ArrayList<>();
        upperBody.add(new WorkoutData("Push-ups", 3, 10, "None", 
            "Basic chest and triceps exercise", "Keep your core tight and elbows close to your body"));
        upperBody.add(new WorkoutData("Dumbbell Rows", 3, 12, "Dumbbells", 
            "Back strengthening exercise", "Keep your back straight and squeeze your shoulder blades"));
        upperBody.add(new WorkoutData("Shoulder Press", 3, 12, "Dumbbells", 
            "Shoulder strengthening exercise", "Don't arch your back, keep core engaged"));
        WORKOUT_DATABASE.put("UPPER_BODY", upperBody);

        // Lower body exercises
        List<WorkoutData> lowerBody = new ArrayList<>();
        lowerBody.add(new WorkoutData("Squats", 3, 15, "None", 
            "Basic leg strengthening exercise", "Keep your chest up and don't let knees go past toes"));
        lowerBody.add(new WorkoutData("Lunges", 3, 12, "None", 
            "Leg and balance exercise", "Take big steps and keep your upper body straight"));
        lowerBody.add(new WorkoutData("Calf Raises", 3, 20, "None", 
            "Calf muscle exercise", "Rise up on your toes fully and lower slowly"));
        WORKOUT_DATABASE.put("LOWER_BODY", lowerBody);

        // Core exercises
        List<WorkoutData> core = new ArrayList<>();
        core.add(new WorkoutData("Plank", 3, 1, "Mat", 
            "Core stability exercise", "Hold for 30 seconds to 1 minute"));
        core.add(new WorkoutData("Crunches", 3, 15, "Mat", 
            "Basic abs exercise", "Don't pull on your neck, focus on your abs"));
        core.add(new WorkoutData("Russian Twists", 3, 12, "Mat, Dumbbell", 
            "Oblique exercise", "Keep your feet off the ground for more challenge"));
        WORKOUT_DATABASE.put("CORE", core);
    }

    /**
     * 생성자 - 포트 번호를 매개변수로 받음
     * @param port 서버가 사용할 포트 번호
     */
    public WorkoutRecommendationServer(int port) {
        this.port = port;
    }

    /**
     * 서버 시작
     * @throws IOException 서버 시작 중 오류 발생 시
     */
    public void start() throws IOException {
        server = ServerBuilder.forPort(port)
                .addService(new WorkoutRecommendationServiceImpl())
                .build()
                .start();
        logger.info("Workout Recommendation Server started, listening on " + port);

        Runtime.getRuntime().addShutdownHook(new Thread() {
            @Override
            public void run() {
                try {
                    WorkoutRecommendationServer.this.stop();
                } catch (InterruptedException e) {
                    logger.warning(e.getMessage());
                }
                logger.info("Server shut down");
            }
        });
    }

    /**
     * 서버 중지
     * @throws InterruptedException 서버 중지 중 인터럽트 발생 시
     */
    public void stop() throws InterruptedException {
        if (server != null) {
            server.shutdown().awaitTermination(30, TimeUnit.SECONDS);
        }
    }

    // Internal class for storing workout data
    private static class WorkoutData {
        String name;
        int defaultSets;
        int defaultReps;
        String equipment;
        String description;
        String tips;

        WorkoutData(String name, int sets, int reps, String equipment, String description, String tips) {
            this.name = name;
            this.defaultSets = sets;
            this.defaultReps = reps;
            this.equipment = equipment;
            this.description = description;
            this.tips = tips;
        }
    }

    private static class WorkoutRecommendationServiceImpl extends WorkoutRecommendationServiceGrpc.WorkoutRecommendationServiceImplBase {
        @Override
        public void getWorkoutRecommendations(WorkoutRequest request, 
                StreamObserver<WorkoutRecommendation> responseObserver) {
            try {
                // Input validation
                String targetArea = request.getTargetArea().toUpperCase();
                String fitnessLevel = request.getFitnessLevel().toUpperCase();
                
                if (!WORKOUT_DATABASE.containsKey(targetArea)) {
                    responseObserver.onError(io.grpc.Status.INVALID_ARGUMENT
                            .withDescription("Invalid target area. Must be one of: UPPER_BODY, LOWER_BODY, CORE")
                            .asException());
                    return;
                }

                if (!fitnessLevel.matches("BEGINNER|INTERMEDIATE|ADVANCED")) {
                    responseObserver.onError(io.grpc.Status.INVALID_ARGUMENT
                            .withDescription("Invalid fitness level. Must be one of: BEGINNER, INTERMEDIATE, ADVANCED")
                            .asException());
                    return;
                }

                // Adjust sets and reps based on fitness level
                int setMultiplier = switch (fitnessLevel) {
                    case "BEGINNER" -> 1;
                    case "INTERMEDIATE" -> 2;
                    case "ADVANCED" -> 3;
                    default -> 1;
                };

                // Get workout recommendations
                List<WorkoutData> workouts = WORKOUT_DATABASE.get(targetArea);
                for (WorkoutData workout : workouts) {
                    WorkoutRecommendation recommendation = WorkoutRecommendation.newBuilder()
                            .setExerciseName(workout.name)
                            .setSets(workout.defaultSets * setMultiplier)
                            .setReps(workout.defaultReps * setMultiplier)
                            .setEquipment(workout.equipment)
                            .setDescription(workout.description)
                            .setTips(workout.tips + " (Recommended for: " + fitnessLevel + " level)")
                            .build();
                    
                    responseObserver.onNext(recommendation);
                }

                responseObserver.onCompleted();

            } catch (Exception e) {
                responseObserver.onError(io.grpc.Status.INTERNAL
                        .withDescription("Error generating workout recommendations: " + e.getMessage())
                        .asException());
            }
        }
    }

    public static void main(String[] args) throws Exception {
        final WorkoutRecommendationServer server = new WorkoutRecommendationServer(50053);
        server.start();
        server.blockUntilShutdown();
    }

    private void blockUntilShutdown() throws InterruptedException {
        if (server != null) {
            server.awaitTermination();
        }
    }
} 