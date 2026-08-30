package com.devstack.pos.dao.custom.impl;

import com.devstack.pos.dao.CrudUtil;
import com.devstack.pos.dao.custom.ProductDao;
import com.devstack.pos.entity.Product;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class ProductDaoImpl implements ProductDao {
    @Override
    public boolean save(Product product) throws SQLException, ClassNotFoundException {
        return CrudUtil.execute("INSERT INTO product VALUES(?,?,?,?,?)",
                product.getCode(),
                product.getDescription(),
                product.getUnitPrice(),
                product.getQtyOnHand(),
                product.getQrCode());
    }

    @Override
    public boolean update(Product product) {
        try {
            return CrudUtil.execute("UPDATE product SET description=?, unit_price=?, qty_on_hand=?, qr_code=? WHERE code=?",
                    product.getDescription(), product.getUnitPrice(), product.getQtyOnHand(), product.getQrCode(), product.getCode());
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    @Override
    public boolean delete(String code) {
        try {
            return CrudUtil.execute("DELETE FROM product WHERE code=?", code);
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    @Override
    public Product findById(String code) {
        try {
            ResultSet rst = CrudUtil.execute("SELECT * FROM product WHERE code=? OR qr_code=?", code, code);
            if (rst.next()) {
                return new Product(rst.getString(1), rst.getString(2), rst.getDouble(3), rst.getInt(4), rst.getString(5));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public List<Product> findAll() {
        List<Product> list = new ArrayList<>();
        try {
            ResultSet rst = CrudUtil.execute("SELECT * FROM product");
            while (rst.next()) {
                list.add(new Product(rst.getString(1), rst.getString(2), rst.getDouble(3), rst.getInt(4), rst.getString(5)));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return list;
    }

    @Override
    public List<Product> searchByDescription(String search) throws SQLException, ClassNotFoundException {
        List<Product> list = new ArrayList<>();
        ResultSet rst = CrudUtil.execute("SELECT * FROM product WHERE description LIKE ? OR code LIKE ?", "%" + search + "%", "%" + search + "%");
        while (rst.next()) {
            list.add(new Product(rst.getString(1), rst.getString(2), rst.getDouble(3), rst.getInt(4), rst.getString(5)));
        }
        return list;
    }

    @Override
    public boolean updateQty(String code, int qty) throws SQLException, ClassNotFoundException {
        return CrudUtil.execute("UPDATE product SET qty_on_hand = qty_on_hand - ? WHERE code=?", qty, code);
    }
}
