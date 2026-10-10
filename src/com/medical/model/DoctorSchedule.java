package com.medical.model;

import java.time.LocalDate;
import java.time.LocalTime;

public class DoctorSchedule {
    private final int scheduleId;
    private final int doctorId;
    private final LocalDate availableDate;
    private final LocalTime startTime;
    private final LocalTime endTime;
    private final boolean available;

    public DoctorSchedule(int scheduleId, int doctorId, LocalDate availableDate,
                          LocalTime startTime, LocalTime endTime, boolean available) {
        this.scheduleId = scheduleId; this.doctorId = doctorId; this.availableDate = availableDate;
        this.startTime = startTime; this.endTime = endTime; this.available = available;
    }
    public int getScheduleId() { return scheduleId; }
    public int getDoctorId() { return doctorId; }
    public LocalDate getAvailableDate() { return availableDate; }
    public LocalTime getStartTime() { return startTime; }
    public LocalTime getEndTime() { return endTime; }
    public boolean isAvailable() { return available; }
}
