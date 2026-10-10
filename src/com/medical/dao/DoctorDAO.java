package com.medical.dao;

import com.medical.util.DBConnection;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class DoctorDAO {
    public int add(Integer userId, String name, String specialization, String phone, String email) throws SQLException {
        String sql="INSERT INTO doctors(user_id,full_name,specialization,phone,email,active) VALUES(?,?,?,?,?,TRUE)";
        try(Connection c=DBConnection.getConnection(); PreparedStatement ps=c.prepareStatement(sql,Statement.RETURN_GENERATED_KEYS)) {
            if(userId==null) ps.setNull(1,Types.INTEGER); else ps.setInt(1,userId);
            ps.setString(2,name); ps.setString(3,specialization); ps.setString(4,phone); ps.setString(5,email);
            ps.executeUpdate();
            try(ResultSet rs=ps.getGeneratedKeys()){ if(rs.next()) return rs.getInt(1); }
        }
        throw new SQLException("Could not add doctor.");
    }
    public boolean isActive(int doctorId) throws SQLException {
        try(Connection c=DBConnection.getConnection(); PreparedStatement ps=c.prepareStatement(
                "SELECT active FROM doctors WHERE doctor_id=?")) {
            ps.setInt(1,doctorId); try(ResultSet rs=ps.executeQuery()){ return rs.next() && rs.getBoolean(1); }
        }
    }
    public List<String> list(String specialization) throws SQLException {
        List<String> rows=new ArrayList<>();
        String sql="SELECT doctor_id,full_name,specialization,phone,email,active FROM doctors " +
            (specialization==null || specialization.isBlank() ? "" : "WHERE specialization LIKE ? ") +
            "ORDER BY full_name";
        try(Connection c=DBConnection.getConnection(); PreparedStatement ps=c.prepareStatement(sql)) {
            if(specialization!=null && !specialization.isBlank()) ps.setString(1,"%"+specialization+"%");
            try(ResultSet rs=ps.executeQuery()){ while(rs.next()) rows.add(String.format(
                "Doctor %d | Dr. %s | %s | %s | %s | active=%s",rs.getInt("doctor_id"),
                rs.getString("full_name"),rs.getString("specialization"),rs.getString("phone"),
                rs.getString("email"),rs.getBoolean("active"))); }
        }
        return rows;
    }
    public boolean setActive(int id, boolean active) throws SQLException {
        try(Connection c=DBConnection.getConnection(); PreparedStatement ps=c.prepareStatement(
                "UPDATE doctors SET active=? WHERE doctor_id=?")) {
            ps.setBoolean(1,active); ps.setInt(2,id); return ps.executeUpdate()==1;
        }
    }
}
