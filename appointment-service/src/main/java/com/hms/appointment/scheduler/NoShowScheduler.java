package com.hms.appointment.scheduler;

import com.hms.appointment.service.AppointmentService;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/** FR-AP-09: mark no-shows automatically 30 minutes after the scheduled slot. */
@Component
public class NoShowScheduler {

    private final AppointmentService appointmentService;

    public NoShowScheduler(AppointmentService appointmentService) {
        this.appointmentService = appointmentService;
    }

    @Scheduled(fixedDelay = 300_000)
    public void markNoShows() {
        appointmentService.markNoShows();
    }
}
