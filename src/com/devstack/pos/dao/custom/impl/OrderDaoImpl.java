package com.devstack.pos.dao.custom.impl;

import com.devstack.pos.dao.CrudUtil;
import com.devstack.pos.dao.custom.OrderDao;
import com.devstack.pos.entity.Order;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class OrderDaoImpl implements OrderDao {
    @Override
    public boolean save(Order order) throws SQLException, ClassNotFoundException {
        return CrudUtil.execute("INSERT INTO orders VALUES(?,?,?,?,?)",
                order.getOrderId(),
                order.getDate(),
                order.getTotalCost(),
                order.getCustomerId(),
                order.getUserEmail());
    }

    @Override
    public boolean update(Order order) {
        return false;
    }

    @Override
    public boolean delete(String s) {
        try {
            return CrudUtil.execute("DELETE FROM orders WHERE order_id=?", s);
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    @Override
    public Order findById(String s) {
        try {
            ResultSet rst = CrudUtil.execute("SELECT * FROM orders WHERE order_id=?", s);
            if (rst.next()) {
                return new Order(rst.getString(1), rst.getString(2), rst.getDouble(3), rst.getString(4), rst.getString(5));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public List<Order> findAll() {
        List<Order> list = new ArrayList<>();
        try {
            ResultSet rst = CrudUtil.execute("SELECT * FROM orders ORDER BY order_id DESC");
            while (rst.next()) {
                list.add(new Order(rst.getString(1), rst.getString(2), rst.getDouble(3), rst.getString(4), rst.getString(5)));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return list;
    }

    @Override
    public String generateNextOrderId() throws SQLException, ClassNotFoundException {
        ResultSet rst = CrudUtil.execute("SELECT order_id FROM orders ORDER BY order_id DESC LIMIT 1");
        if (rst.next()) {
            String lastId = rst.getString(1);
            try {
                int idNum = Integer.parseInt(lastId.replaceAll("[^0-9]", ""));
                return String.format("ORD-%03d", idNum + 1);
            } catch (Exception e) {
                return "ORD-" + System.currentTimeMillis();
            }
        }
        return "ORD-001";
    }
}
