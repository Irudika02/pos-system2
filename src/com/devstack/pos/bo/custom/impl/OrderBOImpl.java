package com.devstack.pos.bo.custom.impl;

import com.devstack.pos.bo.custom.OrderBO;
import com.devstack.pos.dao.DaoFactory;
import com.devstack.pos.dao.custom.OrderDao;
import com.devstack.pos.dao.custom.OrderDetailDao;
import com.devstack.pos.dao.custom.ProductDao;
import com.devstack.pos.dao.custom.impl.OrderDetailDaoImpl;
import com.devstack.pos.dto.CartItemDTO;
import com.devstack.pos.dto.OrderDTO;
import com.devstack.pos.entity.Order;
import com.devstack.pos.entity.OrderDetail;
import com.devstack.pos.util.DaoType;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class OrderBOImpl implements OrderBO {
    private OrderDao orderDao = DaoFactory.getInstance().getDao(DaoType.ORDER);
    private ProductDao productDao = DaoFactory.getInstance().getDao(DaoType.PRODUCT);
    private OrderDetailDao orderDetailDao = new OrderDetailDaoImpl();

    @Override
    public boolean placeOrder(OrderDTO dto) throws SQLException, ClassNotFoundException {
        // Save Order header
        Order order = new Order(dto.getOrderId(), dto.getDate(), dto.getTotalCost(), dto.getCustomerId(), dto.getUserEmail());
        boolean isOrderSaved = orderDao.save(order);
        if (!isOrderSaved) {
            return false;
        }

        // Save Order Details and update stock
        if (dto.getItems() != null) {
            for (CartItemDTO item : dto.getItems()) {
                OrderDetail detail = new OrderDetail(dto.getOrderId(), item.getCode(), item.getUnitPrice(), item.getQty(), 0.0);
                boolean isDetailSaved = orderDetailDao.save(detail);
                if (!isDetailSaved) {
                    return false;
                }
                productDao.updateQty(item.getCode(), item.getQty());
            }
        }
        return true;
    }

    @Override
    public String generateNextOrderId() throws SQLException, ClassNotFoundException {
        return orderDao.generateNextOrderId();
    }

    @Override
    public List<OrderDTO> getAllOrders() throws SQLException, ClassNotFoundException {
        List<Order> list = orderDao.findAll();
        List<OrderDTO> dtoList = new ArrayList<>();
        for (Order o : list) {
            dtoList.add(new OrderDTO(o.getOrderId(), o.getDate(), o.getTotalCost(), o.getCustomerId(), o.getUserEmail(), null));
        }
        return dtoList;
    }
}
