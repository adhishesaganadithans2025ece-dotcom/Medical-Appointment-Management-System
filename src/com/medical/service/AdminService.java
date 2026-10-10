package com.medical.service;

import com.medical.dao.PatientDAO;
import com.medical.dao.UserDAO;
import com.medical.model.User;
import com.medical.util.PasswordUtil;
import java.sql.SQLException;
import java.util.List;

public class AdminService {
    private final UserDAO users = new UserDAO();
    private final PatientDAO patients = new PatientDAO();

    public void requireAdmin(User user) {
        if(user==null || !user.isActive() || !"ADMIN".equals(user.getRole()))
            throw new SecurityException("Administrator access required.");
    }
    public List<String> listUsers(User user) throws SQLException { requireAdmin(user); return users.listUsers(); }
    public List<String> listPatients(User user) throws SQLException { requireAdmin(user); return patients.listAll(); }
    public boolean setUserActive(User user,int id,boolean active) throws SQLException {
        requireAdmin(user); return users.setActive(id,active);
    }
    public boolean createFirstAdmin(String username,char[] password) throws SQLException {
        try {
            if(username==null || username.isBlank() || password.length<8)
                throw new IllegalArgumentException("Username required and password must be at least 8 characters.");
            return users.createFirstAdmin(username,PasswordUtil.hashPassword(password));
        } finally { java.util.Arrays.fill(password,'\0'); }
    }
}
