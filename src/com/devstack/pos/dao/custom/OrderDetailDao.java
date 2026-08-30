package com.devstack.pos.dao.custom;

import com.devstack.pos.dao.CrudDao;
import com.devstack.pos.entity.OrderDetail;

import java.sql.SQLException;
import java.util.List;

public interface OrderDetailDao extends CrudDao<OrderDetail, String> {
    public List<OrderDetail> findByOrderId(String orderId) throws SQLException, ClassNotFoundException;
}
