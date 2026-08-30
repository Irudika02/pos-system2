package com.devstack.pos.dao.custom;

import com.devstack.pos.dao.CrudDao;
import com.devstack.pos.entity.Product;

import java.sql.SQLException;
import java.util.List;

public interface ProductDao extends CrudDao<Product, String> {
    public List<Product> searchByDescription(String search) throws SQLException, ClassNotFoundException;
    public boolean updateQty(String code, int qty) throws SQLException, ClassNotFoundException;
}
