package com.devstack.pos.dto;

import java.util.List;

public class OrderDTO {
    private String orderId;
    private String date;
    private double totalCost;
    private String customerId;
    private String userEmail;
    private List<CartItemDTO> items;

    public OrderDTO() {
    }

    public OrderDTO(String orderId, String date, double totalCost, String customerId, String userEmail, List<CartItemDTO> items) {
        this.orderId = orderId;
        this.date = date;
        this.totalCost = totalCost;
        this.customerId = customerId;
        this.userEmail = userEmail;
        this.items = items;
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

    public List<CartItemDTO> getItems() {
        return items;
    }

    public void setItems(List<CartItemDTO> items) {
        this.items = items;
    }
}
