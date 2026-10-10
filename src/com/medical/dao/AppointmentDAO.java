package com.medical.dao;

import com.medical.util.DBConnection;
import java.sql.*;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

public class AppointmentDAO {
    public List<String> availableSchedules(LocalDate date) throws SQLException {
        List<String> rows=new ArrayList<>();
        String sql="SELECT s.schedule_id,s.doctor_id,d.full_name,d.specialization,s.start_time,s.end_time " +
            "FROM doctor_schedules s JOIN doctors d ON d.doctor_id=s.doctor_id " +
            "WHERE s.available_date=? AND s.is_available=TRUE AND d.active=TRUE ORDER BY s.start_time";
        try(Connection c=DBConnection.getConnection(); PreparedStatement ps=c.prepareStatement(sql)) {
            ps.setDate(1,Date.valueOf(date)); try(ResultSet rs=ps.executeQuery()) {
                while(rs.next()) rows.add(String.format("Schedule %d | Doctor %d Dr. %s (%s) | %s-%s",
                    rs.getInt("schedule_id"),rs.getInt("doctor_id"),rs.getString("full_name"),
                    rs.getString("specialization"),rs.getTime("start_time"),rs.getTime("end_time")));
            }
        }
        return rows;
    }

    public int book(int patientId, int scheduleId, String reason) throws SQLException {
        try(Connection c=DBConnection.getConnection()) {
            c.setAutoCommit(false);
            try {
                int doctorId; LocalDate date; LocalTime start; LocalTime end; boolean available; boolean active;
                String sql="SELECT s.doctor_id,s.available_date,s.start_time,s.end_time,s.is_available,d.active " +
                    "FROM doctor_schedules s JOIN doctors d ON d.doctor_id=s.doctor_id " +
                    "WHERE s.schedule_id=? FOR UPDATE";
                try(PreparedStatement ps=c.prepareStatement(sql)) {
                    ps.setInt(1,scheduleId);
                    try(ResultSet rs=ps.executeQuery()) {
                        if(!rs.next()) throw new SQLException("Schedule ID not found.");
                        doctorId=rs.getInt("doctor_id"); date=rs.getDate("available_date").toLocalDate();
                        start=rs.getTime("start_time").toLocalTime(); end=rs.getTime("end_time").toLocalTime();
                        available=rs.getBoolean("is_available"); active=rs.getBoolean("active");
                    }
                }
                if(!available || !active) throw new SQLException("That schedule/doctor is not available.");
                if(date.isBefore(LocalDate.now()) || (date.equals(LocalDate.now()) && !start.isAfter(LocalTime.now())))
                    throw new SQLException("Cannot book a past time.");
                try(PreparedStatement ps=c.prepareStatement("SELECT patient_id FROM patients WHERE patient_id=?")) {
                    ps.setInt(1,patientId); try(ResultSet rs=ps.executeQuery()){ if(!rs.next()) throw new SQLException("Patient ID not found."); }
                }
                String overlap="SELECT COUNT(*) FROM appointments WHERE doctor_id=? AND appointment_date=? " +
                    "AND status='BOOKED' AND start_time < ? AND end_time > ?";
                try(PreparedStatement ps=c.prepareStatement(overlap)) {
                    ps.setInt(1,doctorId); ps.setDate(2,Date.valueOf(date)); ps.setTime(3,Time.valueOf(end)); ps.setTime(4,Time.valueOf(start));
                    try(ResultSet rs=ps.executeQuery()){ rs.next(); if(rs.getInt(1)>0) throw new SQLException("This doctor already has an overlapping booking."); }
                }
                String insert="INSERT INTO appointments(patient_id,doctor_id,schedule_id,appointment_date,start_time,end_time,reason,status) VALUES(?,?,?,?,?,?,?,'BOOKED')";
                int id;
                try(PreparedStatement ps=c.prepareStatement(insert,Statement.RETURN_GENERATED_KEYS)) {
                    ps.setInt(1,patientId); ps.setInt(2,doctorId); ps.setInt(3,scheduleId);
                    ps.setDate(4,Date.valueOf(date)); ps.setTime(5,Time.valueOf(start)); ps.setTime(6,Time.valueOf(end));
                    ps.setString(7,reason); ps.executeUpdate();
                    try(ResultSet rs=ps.getGeneratedKeys()){ if(!rs.next()) throw new SQLException("Could not retrieve appointment ID."); id=rs.getInt(1); }
                }
                c.commit(); return id;
            } catch(SQLException | RuntimeException ex) {
                c.rollback(); throw ex;
            } finally { c.setAutoCommit(true); }
        }
    }

    public List<String> listForPatient(int patientId) throws SQLException {
        return list("WHERE a.patient_id=?", patientId);
    }
    public List<String> listAll() throws SQLException { return list("", null); }

    private List<String> list(String where, Integer patientId) throws SQLException {
        List<String> rows=new ArrayList<>();
        String sql="SELECT a.appointment_id,a.patient_id,a.doctor_id,a.appointment_date,a.start_time,a.end_time,a.reason,a.status,d.full_name " +
            "FROM appointments a JOIN doctors d ON d.doctor_id=a.doctor_id "+where+" ORDER BY a.appointment_date,a.start_time";
        try(Connection c=DBConnection.getConnection(); PreparedStatement ps=c.prepareStatement(sql)) {
            if(patientId!=null) ps.setInt(1,patientId);
            try(ResultSet rs=ps.executeQuery()) { while(rs.next()) rows.add(String.format(
                "Appointment %d | Patient %d | Dr. %s | %s %s-%s | %s | %s",
                rs.getInt("appointment_id"),rs.getInt("patient_id"),rs.getString("full_name"),
                rs.getDate("appointment_date"),rs.getTime("start_time"),rs.getTime("end_time"),
                rs.getString("status"),rs.getString("reason"))); }
        }
        return rows;
    }

    public boolean changeStatus(int appointmentId, Integer ownerPatientId, String status) throws SQLException {
        String sql="UPDATE appointments SET status=? WHERE appointment_id=? AND status='BOOKED'"+
            (ownerPatientId==null ? "" : " AND patient_id=?");
        try(Connection c=DBConnection.getConnection(); PreparedStatement ps=c.prepareStatement(sql)) {
            ps.setString(1,status); ps.setInt(2,appointmentId); if(ownerPatientId!=null) ps.setInt(3,ownerPatientId);
            return ps.executeUpdate()==1;
        }
    }
}
