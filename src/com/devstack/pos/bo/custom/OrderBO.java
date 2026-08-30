package com.devstack.pos.bo.custom;

import com.devstack.pos.bo.SuperBO;
import com.devstack.pos.dto.OrderDTO;

import java.sql.SQLException;
import java.util.List;

public interface OrderBO extends SuperBO {
    public boolean placeOrder(OrderDTO dto) throws SQLException, ClassNotFoundException;
    public String generateNextOrderId() throws SQLException, ClassNotFoundException;
    public List<OrderDTO> getAllOrders() throws SQLException, ClassNotFoundException;
}
