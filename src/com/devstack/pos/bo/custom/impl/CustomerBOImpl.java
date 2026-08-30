package com.devstack.pos.bo.custom.impl;

import com.devstack.pos.bo.custom.CustomerBO;
import com.devstack.pos.dao.DaoFactory;
import com.devstack.pos.dao.custom.CustomerDao;
import com.devstack.pos.dto.CustomerDTO;
import com.devstack.pos.entity.Customer;
import com.devstack.pos.util.DaoType;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class CustomerBOImpl implements CustomerBO {
    private CustomerDao customerDao = DaoFactory.getInstance().getDao(DaoType.CUSTOMER);

    @Override
    public boolean saveCustomer(CustomerDTO c) throws SQLException, ClassNotFoundException {
        return customerDao.save(new Customer(c.getCustomerId(), c.getName(), c.getAddress(), c.getSalary()));
    }

    @Override
    public boolean updateCustomer(CustomerDTO c) throws SQLException, ClassNotFoundException {
        return customerDao.update(new Customer(c.getCustomerId(), c.getName(), c.getAddress(), c.getSalary()));
    }

    @Override
    public boolean deleteCustomer(String id) throws SQLException, ClassNotFoundException {
        return customerDao.delete(id);
    }

    @Override
    public CustomerDTO searchCustomer(String id) throws SQLException, ClassNotFoundException {
        Customer c = customerDao.findById(id);
        if (c != null) {
            return new CustomerDTO(c.getCustomerId(), c.getName(), c.getAddress(), c.getSalary());
        }
        return null;
    }

    @Override
    public List<CustomerDTO> getAllCustomers() throws SQLException, ClassNotFoundException {
        List<Customer> list = customerDao.findAll();
        List<CustomerDTO> dtoList = new ArrayList<>();
        for (Customer c : list) {
            dtoList.add(new CustomerDTO(c.getCustomerId(), c.getName(), c.getAddress(), c.getSalary()));
        }
        return dtoList;
    }

    @Override
    public List<CustomerDTO> searchByName(String name) throws SQLException, ClassNotFoundException {
        List<Customer> list = customerDao.searchByName(name);
        List<CustomerDTO> dtoList = new ArrayList<>();
        for (Customer c : list) {
            dtoList.add(new CustomerDTO(c.getCustomerId(), c.getName(), c.getAddress(), c.getSalary()));
        }
        return dtoList;
    }
}
