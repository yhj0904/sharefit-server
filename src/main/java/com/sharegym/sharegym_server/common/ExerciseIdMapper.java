package com.sharegym.sharegym_server.common;

import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

/**
 * 클라이언트 문자열 ID와 서버 숫자 ID 매핑
 */
@Component
public class ExerciseIdMapper {

    private static final Map<String, Integer> CLIENT_TO_SERVER = new HashMap<>();
    private static final Map<Integer, String> SERVER_TO_CLIENT = new HashMap<>();

    static {
        initializeMapping();
    }

    /**
     * 운동 ID 매핑 초기화
     */
    private static void initializeMapping() {
        // 가슴 운동 (1000번대)
        addMapping("bench-press", 1001);
        addMapping("incline-bench-press", 1002);
        addMapping("decline-bench-press", 1003);
        addMapping("dumbbell-press", 1004);
        addMapping("dumbbell-fly", 1005);
        addMapping("cable-fly", 1006);
        addMapping("push-up", 1007);
        addMapping("dips", 1008);
        addMapping("pec-deck", 1009);

        // 등 운동 (2000번대)
        addMapping("pull-up", 2001);
        addMapping("lat-pulldown", 2002);
        addMapping("bent-over-row", 2003);
        addMapping("t-bar-row", 2004);
        addMapping("seated-row", 2005);
        addMapping("cable-row", 2006);
        addMapping("deadlift", 2007);
        addMapping("rack-pull", 2008);
        addMapping("shrug", 2009);

        // 어깨 운동 (3000번대)
        addMapping("overhead-press", 3001);
        addMapping("dumbbell-shoulder-press", 3002);
        addMapping("arnold-press", 3003);
        addMapping("lateral-raise", 3004);
        addMapping("front-raise", 3005);
        addMapping("rear-delt-fly", 3006);
        addMapping("upright-row", 3007);
        addMapping("face-pull", 3008);

        // 하체 운동 (4000번대)
        addMapping("squat", 4001);
        addMapping("front-squat", 4002);
        addMapping("leg-press", 4003);
        addMapping("leg-extension", 4004);
        addMapping("leg-curl", 4005);
        addMapping("romanian-deadlift", 4006);
        addMapping("walking-lunge", 4007);
        addMapping("bulgarian-split-squat", 4008);
        addMapping("calf-raise", 4009);
        addMapping("seated-calf-raise", 4010);

        // 팔 운동 (5000번대)
        addMapping("barbell-curl", 5001);
        addMapping("dumbbell-curl", 5002);
        addMapping("hammer-curl", 5003);
        addMapping("preacher-curl", 5004);
        addMapping("cable-curl", 5005);
        addMapping("tricep-extension", 5006);
        addMapping("overhead-tricep-extension", 5007);
        addMapping("tricep-pushdown", 5008);
        addMapping("close-grip-bench-press", 5009);
        addMapping("diamond-push-up", 5010);

        // 복근 운동 (6000번대)
        addMapping("crunch", 6001);
        addMapping("sit-up", 6002);
        addMapping("plank", 6003);
        addMapping("side-plank", 6004);
        addMapping("russian-twist", 6005);
        addMapping("leg-raise", 6006);
        addMapping("hanging-leg-raise", 6007);
        addMapping("ab-wheel", 6008);
        addMapping("mountain-climber", 6009);
        addMapping("bicycle-crunch", 6010);

        // 유산소 운동 (7000번대)
        addMapping("treadmill", 7001);
        addMapping("cycling", 7002);
        addMapping("rowing-machine", 7003);
        addMapping("elliptical", 7004);
        addMapping("stairmaster", 7005);
        addMapping("stair-climber", 7006);
        addMapping("jump-rope", 7007);
        addMapping("swimming", 7008);

        // 맨몸 운동 (8000번대)
        addMapping("burpees", 8001);
        addMapping("jumping-jacks", 8002);
        addMapping("box-jump", 8003);
        addMapping("wall-sit", 8004);
        addMapping("handstand-push-up", 8005);
        addMapping("muscle-up", 8006);
    }

    /**
     * 매핑 추가 헬퍼 메서드
     */
    private static void addMapping(String clientId, Integer serverId) {
        CLIENT_TO_SERVER.put(clientId, serverId);
        SERVER_TO_CLIENT.put(serverId, clientId);
    }

    /**
     * 클라이언트 ID를 서버 ID로 변환
     */
    public static Integer toServerId(String clientId) {
        Integer serverId = CLIENT_TO_SERVER.get(clientId);
        if (serverId == null) {
            throw new IllegalArgumentException("Unknown exercise client ID: " + clientId);
        }
        return serverId;
    }

    /**
     * 서버 ID를 클라이언트 ID로 변환
     */
    public static String toClientId(Integer serverId) {
        String clientId = SERVER_TO_CLIENT.get(serverId);
        if (clientId == null) {
            throw new IllegalArgumentException("Unknown exercise server ID: " + serverId);
        }
        return clientId;
    }

    /**
     * 클라이언트 ID 존재 여부 확인
     */
    public static boolean hasClientId(String clientId) {
        return CLIENT_TO_SERVER.containsKey(clientId);
    }

    /**
     * 서버 ID 존재 여부 확인
     */
    public static boolean hasServerId(Integer serverId) {
        return SERVER_TO_CLIENT.containsKey(serverId);
    }

    /**
     * 전체 매핑 크기
     */
    public static int getMappingSize() {
        return CLIENT_TO_SERVER.size();
    }
}