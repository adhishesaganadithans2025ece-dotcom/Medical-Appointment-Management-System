package com.medical.model;

import java.time.LocalDate;
import java.time.LocalTime;

public class Appointment {
    private final int appointmentId, patientId, doctorId, scheduleId;
    private final LocalDate appointmentDate;
    private final LocalTime startTime, endTime;
    private final String reason, status;

    public Appointment(int appointmentId, int patientId, int doctorId, int scheduleId,
                       LocalDate appointmentDate, LocalTime startTime, LocalTime endTime,
                       String reason, String status) {
        this.appointmentId = appointmentId; this.patientId = patientId; this.doctorId = doctorId;
        this.scheduleId = scheduleId; this.appointmentDate = appointmentDate;
        this.startTime = startTime; this.endTime = endTime; this.reason = reason; this.status = status;
    }
    public int getAppointmentId() { return appointmentId; }
    public int getPatientId() { return patientId; }
    public int getDoctorId() { return doctorId; }
    public int getScheduleId() { return scheduleId; }
    public LocalDate getAppointmentDate() { return appointmentDate; }
    public LocalTime getStartTime() { return startTime; }
    public LocalTime getEndTime() { return endTime; }
    public String getReason() { return reason; }
    public String getStatus() { return status; }
}
