package com.medical.main;

import com.medical.dao.PatientDAO;
import com.medical.model.Patient;
import com.medical.model.User;
import com.medical.service.*;
import com.medical.util.DBConnection;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.Scanner;

public class Main {
    private static final Scanner IN = new Scanner(System.in);
    private static final AuthService AUTH = new AuthService();
    private static final DoctorService DOCTORS = new DoctorService();
    private static final AppointmentService APPOINTMENTS = new AppointmentService();
    private static final AdminService ADMIN = new AdminService();

    public static void main(String[] args) {
        System.out.println("=== Medical Appointment Scheduling System ===");
        if(!DBConnection.testConnection()) {
            System.out.println("Fix database configuration, then restart the application.");
            return;
        }
        System.out.println("Database connection successful.");
        while(true) {
            System.out.println("\n1. Register patient\n2. Login\n3. Create first administrator\n4. Exit");
            String choice=prompt("Choose: ");
            try {
                switch(choice) {
                    case "1" -> register();
                    case "2" -> login();
                    case "3" -> createFirstAdmin();
                    case "4" -> { System.out.println("Thank You!, Goodbye."); return; }
                    default -> System.out.println("Invalid choice.");
                }
            } catch(Exception e) { showError(e); }
        }
    }

    private static void register() throws SQLException {
        String username=prompt("Username: ");
        char[] password=password("Password (minimum 8 characters): ");
        String name=prompt("Full name: "), phone=prompt("Phone: "), email=prompt("Email: ");
        AUTH.registerPatient(username,password,name,phone,email);
        System.out.println("Registration successful. You can now log in.");
    }

    private static void login() throws SQLException {
        String username=prompt("Username: ");
        User user=AUTH.login(username,password("Password: "));
        if(user==null) { System.out.println("Invalid credentials or account inactive."); return; }
        System.out.println("Welcome, "+user.getUsername()+" ("+user.getRole()+")");
        if("PATIENT".equals(user.getRole())) patientMenu(user);
        else if("ADMIN".equals(user.getRole())) adminMenu(user);
        else System.out.println("Doctor login is not enabled in this starter project; administrators can manage doctor records.");
    }

    private static void patientMenu(User user) {
        while(true) {
            System.out.println("\n--- Patient Menu ---\n1. View profile\n2. List doctors\n3. View schedules by date\n4. Book appointment\n5. My appointments\n6. Cancel my appointment\n0. Logout");
            try {
                switch(prompt("Choose: ")) {
                    case "1" -> {
                        Patient p=new PatientDAO().findByUserId(user.getUserId());
                        if(p==null) System.out.println("Profile not found.");
                        else System.out.printf("Patient ID: %d%nName: %s%nPhone: %s%nEmail: %s%n",p.getPatientId(),p.getFullName(),p.getPhone(),p.getEmail());
                    }
                    case "2" -> printRows(DOCTORS.listDoctors(prompt("Specialization filter (blank for all): ")));
                    case "3" -> printRows(APPOINTMENTS.schedules(datePrompt()));
                    case "4" -> {
                        LocalDate date=datePrompt(); printRows(APPOINTMENTS.schedules(date));
                        int scheduleId=intPrompt("Schedule ID to book: ");
                        String reason=prompt("Reason for appointment: ");
                        System.out.println("Appointment booked. ID: "+APPOINTMENTS.book(user,scheduleId,reason));
                    }
                    case "5" -> printRows(APPOINTMENTS.myAppointments(user));
                    case "6" -> System.out.println(APPOINTMENTS.cancelMine(user,intPrompt("Appointment ID to cancel: ")) ? "Appointment cancelled." : "Appointment not found, not yours, or not BOOKED.");
                    case "0" -> { return; }
                    default -> System.out.println("Invalid choice.");
                }
            } catch(Exception e) { showError(e); }
        }
    }

    private static void adminMenu(User user) {
        while(true) {
            System.out.println("\n--- Administrator Menu ---\n1. List users\n2. Activate/deactivate user\n3. List patients\n4. Add doctor\n5. List doctors\n6. Activate/deactivate doctor\n7. Add doctor schedule\n8. View all appointments\n9. Cancel appointment\n10. Mark appointment completed\n0. Logout");
            try {
                switch(prompt("Choose: ")) {
                    case "1" -> printRows(ADMIN.listUsers(user));
                    case "2" -> {
                        int id=intPrompt("User ID: "); boolean active=booleanPrompt("Activate account? (y/n): ");
                        System.out.println(ADMIN.setUserActive(user,id,active)?"Updated.":"User not found.");
                    }
                    case "3" -> printRows(ADMIN.listPatients(user));
                    case "4" -> {
                        int id=DOCTORS.addDoctor(prompt("Full name: "),prompt("Specialization: "),prompt("Phone: "),prompt("Email: "));
                        System.out.println("Doctor created. ID: "+id);
                    }
                    case "5" -> printRows(DOCTORS.listDoctors(prompt("Specialization filter (blank for all): ")));
                    case "6" -> {
                        int id=intPrompt("Doctor ID: "); boolean active=booleanPrompt("Activate doctor? (y/n): ");
                        System.out.println(DOCTORS.setDoctorActive(id,active)?"Updated.":"Doctor not found.");
                    }
                    case "7" -> {
                        int doctor=intPrompt("Doctor ID: "); LocalDate date=datePrompt();
                        LocalTime start=timePrompt("Start time (HH:mm): "), end=timePrompt("End time (HH:mm): ");
                        DOCTORS.addSchedule(doctor,date,start,end); System.out.println("Schedule added.");
                    }
                    case "8" -> printRows(APPOINTMENTS.allAppointments(user));
                    case "9" -> System.out.println(APPOINTMENTS.cancelAsAdmin(user,intPrompt("Appointment ID: "))?"Cancelled.":"Appointment not found or not BOOKED.");
                    case "10" -> System.out.println(APPOINTMENTS.completeAsAdmin(user,intPrompt("Appointment ID: "))?"Marked completed.":"Appointment not found or not BOOKED.");
                    case "0" -> { return; }
                    default -> System.out.println("Invalid choice.");
                }
            } catch(Exception e) { showError(e); }
        }
    }

    private static void createFirstAdmin() throws SQLException {
        String username=prompt("New administrator username: ");
        boolean created=ADMIN.createFirstAdmin(username,password("Administrator password (minimum 8 characters): "));
        System.out.println(created?"Administrator created.":"An administrator already exists; use an existing administrator account.");
    }
    private static String prompt(String label) { System.out.print(label); return IN.nextLine().trim(); }
    private static char[] password(String label) { System.out.print(label); return IN.nextLine().toCharArray(); }
    private static int intPrompt(String label) {
        while(true) try { return Integer.parseInt(prompt(label)); }
        catch(NumberFormatException e) { System.out.println("Enter a valid whole number."); }
    }
    private static LocalDate datePrompt() {
        while(true) try {
            LocalDate d=LocalDate.parse(prompt("Date (YYYY-MM-DD): "));
            if(d.isBefore(LocalDate.now())) { System.out.println("Date cannot be in the past."); continue; }
            return d;
        } catch(DateTimeParseException e) { System.out.println("Use YYYY-MM-DD."); }
    }
    private static LocalTime timePrompt(String label) {
        while(true) try { return LocalTime.parse(prompt(label)); }
        catch(DateTimeParseException e) { System.out.println("Use HH:mm, for example 14:30."); }
    }
    private static boolean booleanPrompt(String label) {
        while(true) { String s=prompt(label).toLowerCase();
            if(s.equals("y")) return true; if(s.equals("n")) return false;
            System.out.println("Enter y or n.");
        }
    }
    private static void printRows(List<String> rows) {
        if(rows.isEmpty()) System.out.println("No records found.");
        else rows.forEach(System.out::println);
    }
    private static void showError(Exception e) {
        if(e instanceof SecurityException) System.out.println("Access denied: "+e.getMessage());
        else System.out.println("Operation failed: "+e.getMessage());
    }
}
