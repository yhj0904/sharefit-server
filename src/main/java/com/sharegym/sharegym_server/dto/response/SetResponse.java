package com.sharegym.sharegym_server.dto.response;

import com.sharegym.sharegym_server.entity.WorkoutSet;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 세트 정보 응답 DTO
 */
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SetResponse {

    private Long id;
    private Integer setNumber;
    private Integer reps;
    private Double weight;
    private Double distance;
    private Integer durationSeconds;
    private Integer level;
    private String setType;
    private Boolean isCompleted;
    private Integer restSeconds;
    private Double volume;
    private Double estimated1RM;
    private String note;

    /**
     * Entity를 DTO로 변환
     */
    public static SetResponse from(WorkoutSet set) {
        return SetResponse.builder()
            .id(set.getId())
            .setNumber(set.getSetNumber())
            .reps(set.getReps())
            .weight(set.getWeight())
            .distance(set.getDistance())
            .durationSeconds(set.getDurationSeconds())
            .level(set.getLevel())
            .setType(set.getSetType().name())
            .isCompleted(set.getIsCompleted())
            .restSeconds(set.getRestSeconds())
            .volume(set.getVolume())
            .estimated1RM(set.getEstimated1RM())
            .note(set.getNote())
            .build();
    }
}