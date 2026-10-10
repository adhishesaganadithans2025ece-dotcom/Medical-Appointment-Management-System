package com.medical.dao;

import com.medical.model.User;
import com.medical.util.DBConnection;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class UserDAO {
    public int create(Connection conn, String username, String passwordHash, String role) throws SQLException {
        String sql = "INSERT INTO users(username,password_hash,role,active) VALUES(?,?,?,TRUE)";
        try (PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, username); ps.setString(2, passwordHash); ps.setString(3, role);
            ps.executeUpdate();
            try (ResultSet rs = ps.getGeneratedKeys()) { if (rs.next()) return rs.getInt(1); }
        }
        throw new SQLException("Could not create user.");
    }

    public User findByUsername(String username) throws SQLException {
        String sql = "SELECT user_id,username,password_hash,role,active FROM users WHERE username=?";
        try (Connection c = DBConnection.getConnection(); PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, username);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return new User(rs.getInt("user_id"), rs.getString("username"),
                    rs.getString("password_hash"), rs.getString("role"), rs.getBoolean("active"));
            }
        }
        return null;
    }

    public List<String> listUsers() throws SQLException {
        List<String> result = new ArrayList<>();
        String sql = "SELECT user_id,username,role,active FROM users ORDER BY user_id";
        try (Connection c = DBConnection.getConnection(); PreparedStatement ps = c.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) result.add(String.format("ID %d | %s | %s | active=%s",
                rs.getInt("user_id"), rs.getString("username"), rs.getString("role"), rs.getBoolean("active")));
        }
        return result;
    }

    public boolean setActive(int userId, boolean active) throws SQLException {
        try (Connection c = DBConnection.getConnection();
             PreparedStatement ps = c.prepareStatement("UPDATE users SET active=? WHERE user_id=?")) {
            ps.setBoolean(1, active); ps.setInt(2, userId); return ps.executeUpdate() == 1;
        }
    }

    public boolean createFirstAdmin(String username, String hash) throws SQLException {
        try (Connection c = DBConnection.getConnection();
             PreparedStatement count = c.prepareStatement("SELECT COUNT(*) FROM users WHERE role='ADMIN'");
             ResultSet rs = count.executeQuery()) {
            rs.next();
            if (rs.getInt(1) > 0) return false;
        }
        try (Connection c = DBConnection.getConnection();
             PreparedStatement ps = c.prepareStatement("INSERT INTO users(username,password_hash,role,active) VALUES(?,?,'ADMIN',TRUE)")) {
            ps.setString(1, username); ps.setString(2, hash); ps.executeUpdate(); return true;
        }
    }
}
