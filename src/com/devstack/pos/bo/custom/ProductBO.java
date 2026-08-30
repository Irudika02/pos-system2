package com.devstack.pos.bo.custom;

import com.devstack.pos.bo.SuperBO;
import com.devstack.pos.dto.ProductDTO;

import java.sql.SQLException;
import java.util.List;

public interface ProductBO extends SuperBO {
    public boolean saveProduct(ProductDTO product) throws SQLException, ClassNotFoundException;
    public boolean updateProduct(ProductDTO product) throws SQLException, ClassNotFoundException;
    public boolean deleteProduct(String code) throws SQLException, ClassNotFoundException;
    public ProductDTO searchProduct(String code) throws SQLException, ClassNotFoundException;
    public List<ProductDTO> getAllProducts() throws SQLException, ClassNotFoundException;
    public List<ProductDTO> searchByDescription(String search) throws SQLException, ClassNotFoundException;
}
