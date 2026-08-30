package com.devstack.pos.bo.custom.impl;

import com.devstack.pos.bo.custom.ProductBO;
import com.devstack.pos.dao.DaoFactory;
import com.devstack.pos.dao.custom.ProductDao;
import com.devstack.pos.dto.ProductDTO;
import com.devstack.pos.entity.Product;
import com.devstack.pos.util.DaoType;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class ProductBOImpl implements ProductBO {
    private ProductDao productDao = DaoFactory.getInstance().getDao(DaoType.PRODUCT);

    @Override
    public boolean saveProduct(ProductDTO p) throws SQLException, ClassNotFoundException {
        return productDao.save(new Product(p.getCode(), p.getDescription(), p.getUnitPrice(), p.getQtyOnHand(), p.getQrCode()));
    }

    @Override
    public boolean updateProduct(ProductDTO p) throws SQLException, ClassNotFoundException {
        return productDao.update(new Product(p.getCode(), p.getDescription(), p.getUnitPrice(), p.getQtyOnHand(), p.getQrCode()));
    }

    @Override
    public boolean deleteProduct(String code) throws SQLException, ClassNotFoundException {
        return productDao.delete(code);
    }

    @Override
    public ProductDTO searchProduct(String code) throws SQLException, ClassNotFoundException {
        Product p = productDao.findById(code);
        if (p != null) {
            return new ProductDTO(p.getCode(), p.getDescription(), p.getUnitPrice(), p.getQtyOnHand(), p.getQrCode());
        }
        return null;
    }

    @Override
    public List<ProductDTO> getAllProducts() throws SQLException, ClassNotFoundException {
        List<Product> list = productDao.findAll();
        List<ProductDTO> dtoList = new ArrayList<>();
        for (Product p : list) {
            dtoList.add(new ProductDTO(p.getCode(), p.getDescription(), p.getUnitPrice(), p.getQtyOnHand(), p.getQrCode()));
        }
        return dtoList;
    }

    @Override
    public List<ProductDTO> searchByDescription(String search) throws SQLException, ClassNotFoundException {
        List<Product> list = productDao.searchByDescription(search);
        List<ProductDTO> dtoList = new ArrayList<>();
        for (Product p : list) {
            dtoList.add(new ProductDTO(p.getCode(), p.getDescription(), p.getUnitPrice(), p.getQtyOnHand(), p.getQrCode()));
        }
        return dtoList;
    }
}
