package com.medical.service;

import com.medical.dao.UserDAO;
import com.medical.dao.PatientDAO;
import com.medical.model.User;
import com.medical.util.DBConnection;
import com.medical.util.PasswordUtil;
import java.sql.Connection;
import java.sql.SQLException;

public class AuthService {
    private final UserDAO users = new UserDAO();
    private final PatientDAO patients = new PatientDAO();

    public void registerPatient(String username, char[] password, String name, String phone, String email) throws SQLException {
        if(username.isBlank() || password.length < 8 || name.isBlank() || phone.isBlank() || email.isBlank())
            throw new IllegalArgumentException("All fields are required; password must be at least 8 characters.");
        try(Connection c=DBConnection.getConnection()) {
            c.setAutoCommit(false);
            try {
                int userId=users.create(c,username,PasswordUtil.hashPassword(password),"PATIENT");
                patients.create(c,userId,name,phone,email);
                c.commit();
            } catch(SQLException | RuntimeException ex) { c.rollback(); throw ex; }
            finally { c.setAutoCommit(true); }
        } finally { java.util.Arrays.fill(password, '\0'); }
    }

    public User login(String username, char[] password) throws SQLException {
        try {
            User user=users.findByUsername(username);
            if(user==null || !user.isActive() || !PasswordUtil.verifyPassword(password,user.getPasswordHash()))
                return null;
            return user;
        } finally { java.util.Arrays.fill(password, '\0'); }
    }
}
