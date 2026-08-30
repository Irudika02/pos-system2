package com.devstack.pos.dto.request;

public class RequestUserDTO {
    private String email;
    private String displayName;

    public RequestUserDTO() {
    }

    public RequestUserDTO(String email, String displayName, String contactNumber, String password) {
        this.email = email;
        this.displayName = displayName;
        this.contactNumber = contactNumber;
        this.password = password;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    public void setContactNumber(String contactNumber) {
        this.contactNumber = contactNumber;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    private String contactNumber;

    public String getEmail() {
        return email;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getContactNumber() {
        return contactNumber;
    }

    public String getPassword() {
        return password;
    }

    private String password;
}
