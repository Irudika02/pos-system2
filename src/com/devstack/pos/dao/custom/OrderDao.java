package com.devstack.pos.dao.custom;

import com.devstack.pos.dao.CrudDao;
import com.devstack.pos.entity.Order;

import java.sql.SQLException;

public interface OrderDao extends CrudDao<Order, String> {
    public String generateNextOrderId() throws SQLException, ClassNotFoundException;
}
