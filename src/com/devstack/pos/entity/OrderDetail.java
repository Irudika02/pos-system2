package com.devstack.pos.entity;

public class OrderDetail {
    private String orderId;
    private String productCode;
    private double unitPrice;
    private int qty;
    private double discount;

    public OrderDetail() {
    }

    public OrderDetail(String orderId, String productCode, double unitPrice, int qty, double discount) {
        this.orderId = orderId;
        this.productCode = productCode;
        this.unitPrice = unitPrice;
        this.qty = qty;
        this.discount = discount;
    }

    public String getOrderId() {
        return orderId;
    }

    public void setOrderId(String orderId) {
        this.orderId = orderId;
    }

    public String getProductCode() {
        return productCode;
    }

    public void setProductCode(String productCode) {
        this.productCode = productCode;
    }

    public double getUnitPrice() {
        return unitPrice;
    }

    public void setUnitPrice(double unitPrice) {
        this.unitPrice = unitPrice;
    }

    public int getQty() {
        return qty;
    }

    public void setQty(int qty) {
        this.qty = qty;
    }

    public double getDiscount() {
        return discount;
    }

    public void setDiscount(double discount) {
        this.discount = discount;
    }
}
