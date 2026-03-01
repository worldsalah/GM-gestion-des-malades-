package com.app.util;

public class UserSession {
    private static UserSession instance;

    private String username;
    private String role;
    private String firstName;
    private String lastName;
    private String email;
    private String phone;
    private String gender;
    private String profileImagePath;

    private UserSession(String username, String role, String firstName, String lastName, String email, String phone,
            String gender, String profileImagePath) {
        this.username = username;
        this.role = role;
        this.firstName = firstName;
        this.lastName = lastName;
        this.email = email;
        this.phone = phone;
        this.gender = gender;
        this.profileImagePath = profileImagePath;
    }

    public static void setInstance(String username, String role, String firstName, String lastName, String email,
            String phone, String gender, String profileImagePath) {
        instance = new UserSession(username, role, firstName, lastName, email, phone, gender, profileImagePath);
    }

    public static UserSession getInstance() {
        return instance;
    }

    public static void cleanUserSession() {
        instance = null;
    }

    // Getters
    public String getUsername() {
        return username;
    }

    public String getRole() {
        return role;
    }

    public String getFirstName() {
        return firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public String getEmail() {
        return email;
    }

    public String getPhone() {
        return phone;
    }

    public String getGender() {
        return gender;
    }

    public String getProfileImagePath() {
        return profileImagePath;
    }

    // Setters (for updates like photo upload and profile editing)
    public void setProfileImagePath(String path) {
        this.profileImagePath = path;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public void setGender(String gender) {
        this.gender = gender;
    }

    public void setRole(String role) {
        this.role = role;
    }
}
