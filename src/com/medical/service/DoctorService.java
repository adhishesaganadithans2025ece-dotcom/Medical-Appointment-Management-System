package com.medical.service;

import com.medical.dao.DoctorDAO;
import com.medical.util.DBConnection;
import java.sql.*;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

public class DoctorService {
    private final DoctorDAO doctors = new DoctorDAO();

    public int addDoctor(String name,String specialization,String phone,String email) throws SQLException {
        if(name.isBlank() || specialization.isBlank() || phone.isBlank() || email.isBlank())
            throw new IllegalArgumentException("All doctor fields are required.");
        return doctors.add(null,name,specialization,phone,email);
    }
    public List<String> listDoctors(String specialization) throws SQLException { return doctors.list(specialization); }
    public boolean setDoctorActive(int id, boolean active) throws SQLException { return doctors.setActive(id,active); }

    public void addSchedule(int doctorId, LocalDate date, LocalTime start, LocalTime end) throws SQLException {
        if(date.isBefore(LocalDate.now())) throw new IllegalArgumentException("Schedule date cannot be in the past.");
        if(!end.isAfter(start)) throw new IllegalArgumentException("End time must be later than start time.");
        if(!doctors.isActive(doctorId)) throw new IllegalArgumentException("Doctor ID is invalid or inactive.");
        try(Connection c=DBConnection.getConnection()) {
            c.setAutoCommit(false);
            try {
                String overlap="SELECT COUNT(*) FROM doctor_schedules WHERE doctor_id=? AND available_date=? AND start_time < ? AND end_time > ?";
                try(PreparedStatement ps=c.prepareStatement(overlap)) {
                    ps.setInt(1,doctorId); ps.setDate(2,Date.valueOf(date)); ps.setTime(3,Time.valueOf(end)); ps.setTime(4,Time.valueOf(start));
                    try(ResultSet rs=ps.executeQuery()){ rs.next(); if(rs.getInt(1)>0) throw new SQLException("Schedule overlaps an existing schedule."); }
                }
                try(PreparedStatement ps=c.prepareStatement(
                        "INSERT INTO doctor_schedules(doctor_id,available_date,start_time,end_time,is_available) VALUES(?,?,?,?,TRUE)")) {
                    ps.setInt(1,doctorId); ps.setDate(2,Date.valueOf(date)); ps.setTime(3,Time.valueOf(start)); ps.setTime(4,Time.valueOf(end)); ps.executeUpdate();
                }
                c.commit();
            } catch(SQLException | RuntimeException ex) { c.rollback(); throw ex; }
            finally { c.setAutoCommit(true); }
        }
    }
}
