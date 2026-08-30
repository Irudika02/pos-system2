package com.devstack.pos.dao.custom.impl;

import com.devstack.pos.dao.CrudUtil;
import com.devstack.pos.dao.custom.OrderDetailDao;
import com.devstack.pos.entity.OrderDetail;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class OrderDetailDaoImpl implements OrderDetailDao {
    @Override
    public boolean save(OrderDetail detail) throws SQLException, ClassNotFoundException {
        return CrudUtil.execute("INSERT INTO order_detail VALUES(?,?,?,?,?)",
                detail.getOrderId(),
                detail.getProductCode(),
                detail.getUnitPrice(),
                detail.getQty(),
                detail.getDiscount());
    }

    @Override
    public boolean update(OrderDetail orderDetail) {
        return false;
    }

    @Override
    public boolean delete(String s) {
        return false;
    }

    @Override
    public OrderDetail findById(String s) {
        return null;
    }

    @Override
    public List<OrderDetail> findAll() {
        return List.of();
    }

    @Override
    public List<OrderDetail> findByOrderId(String orderId) throws SQLException, ClassNotFoundException {
        List<OrderDetail> list = new ArrayList<>();
        ResultSet rst = CrudUtil.execute("SELECT * FROM order_detail WHERE order_id=?", orderId);
        while (rst.next()) {
            list.add(new OrderDetail(rst.getString(1), rst.getString(2), rst.getDouble(3), rst.getInt(4), rst.getDouble(5)));
        }
        return list;
    }
}
