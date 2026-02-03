package com.sharegym.sharegym_server.config;

import com.sharegym.sharegym_server.entity.Exercise;
import com.sharegym.sharegym_server.repository.ExerciseRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

import java.util.Arrays;
import java.util.List;

/**
 * 운동 마스터 데이터 초기화
 */
@Slf4j
@Configuration
@RequiredArgsConstructor
public class DataInitializer {

    /**
     * 개발/테스트 환경에서 운동 데이터 초기화
     */
    @Bean
    @Profile({"dev", "test"})
    CommandLineRunner initExerciseData(ExerciseRepository exerciseRepository) {
        return args -> {
            if (exerciseRepository.count() == 0) {
                log.info("Initializing exercise master data...");

                List<Exercise> exercises = Arrays.asList(
                    // 가슴 운동 (1000번대)
                    Exercise.builder()
                        .id(1001).clientId("bench-press")
                        .exerciseName("Bench Press").exerciseNameKo("벤치프레스")
                        .category(Exercise.Category.CHEST)
                        .muscleGroups("가슴,삼두,전면삼각근")
                        .equipment("바벨,벤치").icon("barbell")
                        .unit(Exercise.Unit.KG).build(),

                    Exercise.builder()
                        .id(1002).clientId("incline-bench-press")
                        .exerciseName("Incline Bench Press").exerciseNameKo("인클라인 벤치프레스")
                        .category(Exercise.Category.CHEST)
                        .muscleGroups("상부가슴,삼두,전면삼각근")
                        .equipment("바벨,인클라인벤치").icon("barbell")
                        .unit(Exercise.Unit.KG).build(),

                    Exercise.builder()
                        .id(1004).clientId("dumbbell-press")
                        .exerciseName("Dumbbell Press").exerciseNameKo("덤벨프레스")
                        .category(Exercise.Category.CHEST)
                        .muscleGroups("가슴,삼두,전면삼각근")
                        .equipment("덤벨,벤치").icon("dumbbell")
                        .unit(Exercise.Unit.KG).build(),

                    // 등 운동 (2000번대)
                    Exercise.builder()
                        .id(2001).clientId("pull-up")
                        .exerciseName("Pull-up").exerciseNameKo("풀업")
                        .category(Exercise.Category.BACK)
                        .muscleGroups("광배근,이두,중부승모근")
                        .equipment("철봉").icon("pullup")
                        .unit(Exercise.Unit.REPS).build(),

                    Exercise.builder()
                        .id(2002).clientId("lat-pulldown")
                        .exerciseName("Lat Pulldown").exerciseNameKo("랫 풀다운")
                        .category(Exercise.Category.BACK)
                        .muscleGroups("광배근,이두,중부승모근")
                        .equipment("케이블머신").icon("cable")
                        .unit(Exercise.Unit.KG).build(),

                    Exercise.builder()
                        .id(2007).clientId("deadlift")
                        .exerciseName("Deadlift").exerciseNameKo("데드리프트")
                        .category(Exercise.Category.BACK)
                        .muscleGroups("전신,척추기립근,대둔근,햄스트링")
                        .equipment("바벨").icon("barbell")
                        .unit(Exercise.Unit.KG).build(),

                    // 어깨 운동 (3000번대)
                    Exercise.builder()
                        .id(3001).clientId("overhead-press")
                        .exerciseName("Overhead Press").exerciseNameKo("오버헤드 프레스")
                        .category(Exercise.Category.SHOULDERS)
                        .muscleGroups("전면삼각근,측면삼각근,삼두")
                        .equipment("바벨").icon("barbell")
                        .unit(Exercise.Unit.KG).build(),

                    Exercise.builder()
                        .id(3004).clientId("lateral-raise")
                        .exerciseName("Lateral Raise").exerciseNameKo("사이드 레터럴 레이즈")
                        .category(Exercise.Category.SHOULDERS)
                        .muscleGroups("측면삼각근")
                        .equipment("덤벨").icon("dumbbell")
                        .unit(Exercise.Unit.KG).build(),

                    // 하체 운동 (4000번대)
                    Exercise.builder()
                        .id(4001).clientId("squat")
                        .exerciseName("Squat").exerciseNameKo("스쿼트")
                        .category(Exercise.Category.LEGS)
                        .muscleGroups("대퇴사두근,대둔근,햄스트링")
                        .equipment("바벨,스쿼트랙").icon("barbell")
                        .unit(Exercise.Unit.KG).build(),

                    Exercise.builder()
                        .id(4003).clientId("leg-press")
                        .exerciseName("Leg Press").exerciseNameKo("레그프레스")
                        .category(Exercise.Category.LEGS)
                        .muscleGroups("대퇴사두근,대둔근")
                        .equipment("레그프레스머신").icon("machine")
                        .unit(Exercise.Unit.KG).build(),

                    Exercise.builder()
                        .id(4006).clientId("romanian-deadlift")
                        .exerciseName("Romanian Deadlift").exerciseNameKo("루마니안 데드리프트")
                        .category(Exercise.Category.LEGS)
                        .muscleGroups("햄스트링,대둔근,척추기립근")
                        .equipment("바벨").icon("barbell")
                        .unit(Exercise.Unit.KG).build(),

                    // 팔 운동 (5000번대)
                    Exercise.builder()
                        .id(5001).clientId("barbell-curl")
                        .exerciseName("Barbell Curl").exerciseNameKo("바벨 컬")
                        .category(Exercise.Category.ARMS)
                        .muscleGroups("이두근")
                        .equipment("바벨").icon("barbell")
                        .unit(Exercise.Unit.KG).build(),

                    Exercise.builder()
                        .id(5002).clientId("dumbbell-curl")
                        .exerciseName("Dumbbell Curl").exerciseNameKo("덤벨 컬")
                        .category(Exercise.Category.ARMS)
                        .muscleGroups("이두근")
                        .equipment("덤벨").icon("dumbbell")
                        .unit(Exercise.Unit.KG).build(),

                    Exercise.builder()
                        .id(5008).clientId("tricep-pushdown")
                        .exerciseName("Tricep Pushdown").exerciseNameKo("트라이셉 푸시다운")
                        .category(Exercise.Category.ARMS)
                        .muscleGroups("삼두근")
                        .equipment("케이블머신").icon("cable")
                        .unit(Exercise.Unit.KG).build(),

                    // 복근 운동 (6000번대)
                    Exercise.builder()
                        .id(6001).clientId("crunch")
                        .exerciseName("Crunch").exerciseNameKo("크런치")
                        .category(Exercise.Category.ABS)
                        .muscleGroups("복직근")
                        .equipment("매트").icon("bodyweight")
                        .unit(Exercise.Unit.REPS).build(),

                    Exercise.builder()
                        .id(6003).clientId("plank")
                        .exerciseName("Plank").exerciseNameKo("플랭크")
                        .category(Exercise.Category.ABS)
                        .muscleGroups("코어전체")
                        .equipment("매트").icon("bodyweight")
                        .unit(Exercise.Unit.REPS).build(),

                    // 유산소 운동 (7000번대)
                    Exercise.builder()
                        .id(7001).clientId("treadmill")
                        .exerciseName("Treadmill").exerciseNameKo("트레드밀")
                        .category(Exercise.Category.CARDIO)
                        .muscleGroups("전신,심폐")
                        .equipment("트레드밀").icon("cardio")
                        .unit(Exercise.Unit.KM).build(),

                    Exercise.builder()
                        .id(7002).clientId("cycling")
                        .exerciseName("Cycling").exerciseNameKo("사이클")
                        .category(Exercise.Category.CARDIO)
                        .muscleGroups("하체,심폐")
                        .equipment("자전거").icon("cardio")
                        .unit(Exercise.Unit.KM).build(),

                    Exercise.builder()
                        .id(7004).clientId("elliptical")
                        .exerciseName("Elliptical").exerciseNameKo("일립티컬")
                        .category(Exercise.Category.CARDIO)
                        .muscleGroups("전신,심폐")
                        .equipment("일립티컬머신").icon("cardio")
                        .unit(Exercise.Unit.LEVEL).build(),

                    // 맨몸 운동 (8000번대)
                    Exercise.builder()
                        .id(8001).clientId("burpees")
                        .exerciseName("Burpees").exerciseNameKo("버피")
                        .category(Exercise.Category.BODYWEIGHT)
                        .muscleGroups("전신")
                        .equipment("없음").icon("bodyweight")
                        .unit(Exercise.Unit.REPS).build(),

                    Exercise.builder()
                        .id(8002).clientId("jumping-jacks")
                        .exerciseName("Jumping Jacks").exerciseNameKo("점핑잭")
                        .category(Exercise.Category.BODYWEIGHT)
                        .muscleGroups("전신,심폐")
                        .equipment("없음").icon("bodyweight")
                        .unit(Exercise.Unit.REPS).build()
                );

                exerciseRepository.saveAll(exercises);
                log.info("Exercise master data initialized with {} exercises", exercises.size());
            } else {
                log.info("Exercise data already exists, skipping initialization");
            }
        };
    }
}