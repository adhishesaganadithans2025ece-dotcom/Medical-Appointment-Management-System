package com.medical.model;

public class Doctor {
    private final int doctorId;
    private final Integer userId;
    private final String fullName;
    private final String specialization;
    private final String phone;
    private final String email;
    private final boolean active;

    public Doctor(int doctorId, Integer userId, String fullName, String specialization,
                  String phone, String email, boolean active) {
        this.doctorId = doctorId; this.userId = userId; this.fullName = fullName;
        this.specialization = specialization; this.phone = phone; this.email = email; this.active = active;
    }
    public int getDoctorId() { return doctorId; }
    public Integer getUserId() { return userId; }
    public String getFullName() { return fullName; }
    public String getSpecialization() { return specialization; }
    public String getPhone() { return phone; }
    public String getEmail() { return email; }
    public boolean isActive() { return active; }
}
