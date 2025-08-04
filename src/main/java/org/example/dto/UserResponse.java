package org.example.dto;

public class UserResponse {
    private Long id;
    private String fullName;
    private String email;
    private String ageGroup;

    public UserResponse() {
    }

    public UserResponse(Long id, String fullName, String email, String ageGroup) {
        this.id = id;
        this.fullName = fullName;
        this.email = email;
        this.ageGroup = ageGroup;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getAgeGroup() {
        return ageGroup;
    }

    public void setAgeGroup(String ageGroup) {
        this.ageGroup = ageGroup;
    }
}