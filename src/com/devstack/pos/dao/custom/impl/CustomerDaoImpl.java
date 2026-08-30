package com.devstack.pos.dao.custom.impl;

import com.devstack.pos.dao.CrudUtil;
import com.devstack.pos.dao.custom.CustomerDao;
import com.devstack.pos.entity.Customer;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class CustomerDaoImpl implements CustomerDao {
    @Override
    public boolean save(Customer customer) throws SQLException, ClassNotFoundException {
        return CrudUtil.execute("INSERT INTO customer VALUES(?,?,?,?)",
                customer.getCustomerId(),
                customer.getName(),
                customer.getAddress(),
                customer.getSalary());
    }

    @Override
    public boolean update(Customer customer) {
        try {
            return CrudUtil.execute("UPDATE customer SET name=?, address=?, salary=? WHERE customer_id=?",
                    customer.getName(), customer.getAddress(), customer.getSalary(), customer.getCustomerId());
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    @Override
    public boolean delete(String id) {
        try {
            return CrudUtil.execute("DELETE FROM customer WHERE customer_id=?", id);
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    @Override
    public Customer findById(String id) {
        try {
            ResultSet rst = CrudUtil.execute("SELECT * FROM customer WHERE customer_id=?", id);
            if (rst.next()) {
                return new Customer(rst.getString(1), rst.getString(2), rst.getString(3), rst.getDouble(4));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public List<Customer> findAll() {
        List<Customer> list = new ArrayList<>();
        try {
            ResultSet rst = CrudUtil.execute("SELECT * FROM customer");
            while (rst.next()) {
                list.add(new Customer(rst.getString(1), rst.getString(2), rst.getString(3), rst.getDouble(4)));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return list;
    }

    @Override
    public List<Customer> searchByName(String name) throws SQLException, ClassNotFoundException {
        List<Customer> list = new ArrayList<>();
        ResultSet rst = CrudUtil.execute("SELECT * FROM customer WHERE name LIKE ?", "%" + name + "%");
        while (rst.next()) {
            list.add(new Customer(rst.getString(1), rst.getString(2), rst.getString(3), rst.getDouble(4)));
        }
        return list;
    }
}
