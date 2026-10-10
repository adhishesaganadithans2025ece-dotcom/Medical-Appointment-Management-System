package com.medical.service;

import com.medical.dao.AppointmentDAO;
import com.medical.dao.PatientDAO;
import com.medical.model.Patient;
import com.medical.model.User;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;

public class AppointmentService {
    private final AppointmentDAO appointments = new AppointmentDAO();
    private final PatientDAO patients = new PatientDAO();

    public int book(User user, int scheduleId, String reason) throws SQLException {
        requireRole(user,"PATIENT");
        Patient patient=patients.findByUserId(user.getUserId());
        if(patient==null) throw new SQLException("Patient profile not found.");
        if(reason==null || reason.isBlank()) throw new IllegalArgumentException("Appointment reason is required.");
        return appointments.book(patient.getPatientId(),scheduleId,reason);
    }
    public List<String> myAppointments(User user) throws SQLException {
        requireRole(user,"PATIENT");
        Patient p=patients.findByUserId(user.getUserId());
        if(p==null) throw new SQLException("Patient profile not found.");
        return appointments.listForPatient(p.getPatientId());
    }
    public List<String> allAppointments(User user) throws SQLException {
        requireRole(user,"ADMIN"); return appointments.listAll();
    }
    public List<String> schedules(LocalDate date) throws SQLException {
        if(date.isBefore(LocalDate.now())) throw new IllegalArgumentException("Date cannot be in the past.");
        return appointments.availableSchedules(date);
    }
    public boolean cancelMine(User user,int appointmentId) throws SQLException {
        requireRole(user,"PATIENT");
        Patient p=patients.findByUserId(user.getUserId());
        if(p==null) return false;
        return appointments.changeStatus(appointmentId,p.getPatientId(),"CANCELLED");
    }
    public boolean cancelAsAdmin(User user,int appointmentId) throws SQLException {
        requireRole(user,"ADMIN"); return appointments.changeStatus(appointmentId,null,"CANCELLED");
    }
    public boolean completeAsAdmin(User user,int appointmentId) throws SQLException {
        requireRole(user,"ADMIN"); return appointments.changeStatus(appointmentId,null,"COMPLETED");
    }
    private void requireRole(User user,String role) {
        if(user==null || !user.isActive() || !role.equals(user.getRole()))
            throw new SecurityException("You are not authorized to perform this operation.");
    }
}
