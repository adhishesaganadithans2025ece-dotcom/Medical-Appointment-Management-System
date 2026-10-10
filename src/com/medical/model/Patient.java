package com.medical.model;

public class Patient {
    private final int patientId;
    private final int userId;
    private final String fullName;
    private final String phone;
    private final String email;

    public Patient(int patientId, int userId, String fullName, String phone, String email) {
        this.patientId = patientId; this.userId = userId;
        this.fullName = fullName; this.phone = phone; this.email = email;
    }
    public int getPatientId() { return patientId; }
    public int getUserId() { return userId; }
    public String getFullName() { return fullName; }
    public String getPhone() { return phone; }
    public String getEmail() { return email; }
}
