package com.hms.doctor.entity;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.DayOfWeek;
import java.time.LocalTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ScheduleBuilderTest {

    private final Doctor doctor = new Doctor(1L, "Anita", "Rao", "REG-1", "MBBS",
            "Cardiology", "Cardiology", BigDecimal.valueOf(500));

    @Test
    void buildsWithMandatoryFields_defaultsDurationTo15() {
        Schedule schedule = Schedule.builder()
                .doctor(doctor)
                .dayOfWeek(DayOfWeek.MONDAY)
                .startTime(LocalTime.of(9, 0))
                .endTime(LocalTime.of(13, 0))
                .build();

        assertThat(schedule.getSlotDurationMin()).isEqualTo(15);
        assertThat(schedule.getDayOfWeek()).isEqualTo(DayOfWeek.MONDAY);
    }

    @Test
    void missingDoctor_throwsNullPointerException() {
        assertThatThrownBy(() -> Schedule.builder()
                .dayOfWeek(DayOfWeek.MONDAY)
                .startTime(LocalTime.of(9, 0))
                .endTime(LocalTime.of(13, 0))
                .build())
                .isInstanceOf(NullPointerException.class)
                .hasMessageContaining("doctor is mandatory");
    }

    @Test
    void endTimeBeforeStartTime_throwsIllegalArgumentException() {
        assertThatThrownBy(() -> Schedule.builder()
                .doctor(doctor)
                .dayOfWeek(DayOfWeek.MONDAY)
                .startTime(LocalTime.of(13, 0))
                .endTime(LocalTime.of(9, 0))
                .build())
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("endTime must be after startTime");
    }

    @Test
    void customSlotDuration_isRespected() {
        Schedule schedule = Schedule.builder()
                .doctor(doctor)
                .dayOfWeek(DayOfWeek.TUESDAY)
                .startTime(LocalTime.of(9, 0))
                .endTime(LocalTime.of(10, 0))
                .slotDurationMin(30)
                .build();

        assertThat(schedule.getSlotDurationMin()).isEqualTo(30);
    }
}
