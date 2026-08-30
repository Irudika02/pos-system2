package com.devstack.pos.model;

public class User {
    private String user_id;
    private String email;
    private String display_name;
    private String contact_number;
    private String password;

    public User() {
    }

    public User(String user_id, String email, String display_name, String contact_number, String password) {
        this.user_id = user_id;
        this.email = email;
        this.display_name = display_name;
        this.contact_number = contact_number;
        this.password = password;
    }

    public String getUser_id() {
        return user_id;
    }

    public void setUser_id(String user_id) {
        this.user_id = user_id;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getDisplay_name() {
        return display_name;
    }

    public void setDisplay_name(String display_name) {
        this.display_name = display_name;
    }

    public String getContact_number() {
        return contact_number;
    }

    public void setContact_number(String contact_number) {
        this.contact_number = contact_number;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }
}
