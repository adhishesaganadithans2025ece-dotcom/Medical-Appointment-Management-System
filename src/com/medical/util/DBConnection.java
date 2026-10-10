package com.medical.util;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public final class DBConnection {
    // Update these values for your local MySQL installation.
    private static final String URL =
        "jdbc:mysql://localhost:3306/medical_appointment_db?useSSL=false&serverTimezone=UTC";
    private static final String USER = "root";
    private static final String PASSWORD = "adhishesagan@123";

    private DBConnection() {}

    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(URL, USER, PASSWORD);
    }

    public static boolean testConnection() {
        try (Connection connection = getConnection()) {
            return connection.isValid(3);
        } catch (SQLException e) {
            System.err.println("Database connection failed: " + e.getMessage());
            System.err.println("Check MySQL is running, schema.sql was executed, and DBConnection credentials are correct.");
            return false;
        }
    }
}
