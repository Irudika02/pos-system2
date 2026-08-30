package com.devstack.pos.bo.custom;

import com.devstack.pos.bo.SuperBO;
import com.devstack.pos.dto.CustomerDTO;

import java.sql.SQLException;
import java.util.List;

public interface CustomerBO extends SuperBO {
    public boolean saveCustomer(CustomerDTO customer) throws SQLException, ClassNotFoundException;
    public boolean updateCustomer(CustomerDTO customer) throws SQLException, ClassNotFoundException;
    public boolean deleteCustomer(String id) throws SQLException, ClassNotFoundException;
    public CustomerDTO searchCustomer(String id) throws SQLException, ClassNotFoundException;
    public List<CustomerDTO> getAllCustomers() throws SQLException, ClassNotFoundException;
    public List<CustomerDTO> searchByName(String name) throws SQLException, ClassNotFoundException;
}
