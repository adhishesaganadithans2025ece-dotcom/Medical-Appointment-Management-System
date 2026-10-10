package com.medical.dao;

import com.medical.model.Patient;
import com.medical.util.DBConnection;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class PatientDAO {
    public void create(Connection conn, int userId, String name, String phone, String email) throws SQLException {
        try (PreparedStatement ps = conn.prepareStatement(
                "INSERT INTO patients(user_id,full_name,phone,email) VALUES(?,?,?,?)")) {
            ps.setInt(1,userId); ps.setString(2,name); ps.setString(3,phone); ps.setString(4,email); ps.executeUpdate();
        }
    }
    public Patient findByUserId(int userId) throws SQLException {
        try (Connection c=DBConnection.getConnection(); PreparedStatement ps=c.prepareStatement(
                "SELECT patient_id,user_id,full_name,phone,email FROM patients WHERE user_id=?")) {
            ps.setInt(1,userId);
            try(ResultSet rs=ps.executeQuery()) {
                if(rs.next()) return new Patient(rs.getInt("patient_id"),rs.getInt("user_id"),
                    rs.getString("full_name"),rs.getString("phone"),rs.getString("email"));
            }
        }
        return null;
    }
    public List<String> listAll() throws SQLException {
        List<String> rows=new ArrayList<>();
        try(Connection c=DBConnection.getConnection(); PreparedStatement ps=c.prepareStatement(
                "SELECT patient_id,user_id,full_name,phone,email FROM patients ORDER BY patient_id");
            ResultSet rs=ps.executeQuery()) {
            while(rs.next()) rows.add(String.format("Patient %d | User %d | %s | %s | %s",
                rs.getInt("patient_id"),rs.getInt("user_id"),rs.getString("full_name"),
                rs.getString("phone"),rs.getString("email")));
        }
        return rows;
    }
}
