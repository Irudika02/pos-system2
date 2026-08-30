package com.devstack.pos.entity;

public class Order {
    private String orderId;
    private String date;
    private double totalCost;
    private String customerId;
    private String userEmail;

    public Order() {
    }

    public Order(String orderId, String date, double totalCost, String customerId, String userEmail) {
        this.orderId = orderId;
        this.date = date;
        this.totalCost = totalCost;
        this.customerId = customerId;
        this.userEmail = userEmail;
    }

    public String getOrderId() {
        return orderId;
    }

    public void setOrderId(String orderId) {
        this.orderId = orderId;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public double getTotalCost() {
        return totalCost;
    }

    public void setTotalCost(double totalCost) {
        this.totalCost = totalCost;
    }

    public String getCustomerId() {
        return customerId;
    }

    public void setCustomerId(String customerId) {
        this.customerId = customerId;
    }

    public String getUserEmail() {
        return userEmail;
    }

    public void setUserEmail(String userEmail) {
        this.userEmail = userEmail;
    }
}
