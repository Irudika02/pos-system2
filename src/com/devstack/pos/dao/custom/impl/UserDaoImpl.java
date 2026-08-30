package com.devstack.pos.dao.custom.impl;

import com.devstack.pos.dao.CrudUtil;
import com.devstack.pos.dao.custom.UserDao;
import com.devstack.pos.model.User;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

public class UserDaoImpl implements UserDao {
    @Override
    public boolean save(User user) throws SQLException, ClassNotFoundException {
        return CrudUtil.execute("Insert Into user VALUES(?,?,?,?,?)",
                user.getUser_id(),
                user.getEmail(),
                user.getDisplay_name(),
                user.getContact_number(),
                user.getPassword());
    }

    @Override
    public boolean update(User user) {
        return false;
    }

    @Override
    public boolean delete(String s) {
        return false;
    }

    @Override
    public User findById(String s) {
        return null;
    }

    @Override
    public List<User> findAll() {
        return null;
    }

    @Override
    public List<User> searchByname(String name) {
        return List.of();
    }

    @Override
    public User findByUserEmail(String email) throws SQLException, ClassNotFoundException {
        ResultSet resultSet = CrudUtil.execute("SELECT * FROM user WHERE email=?",email);
        if(resultSet.next()){
            String userId=resultSet.getString("user_id");
            String userEmail=resultSet.getString("email");
            String hashedPassword=resultSet.getString("password");
            String displayName=resultSet.getString("display_name");
            String contactNumber=resultSet.getString("contact_number");
            return new User(userId,userEmail,displayName,contactNumber,hashedPassword);
        }else {
        return null;
        }

    }
}
