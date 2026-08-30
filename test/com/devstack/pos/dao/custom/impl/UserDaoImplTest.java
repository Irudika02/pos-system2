package com.devstack.pos.dao.custom.impl;


import com.devstack.pos.dao.custom.UserDao;
import com.devstack.pos.model.User;
import com.devstack.pos.util.PasswordHash;

import java.sql.SQLException;
import java.util.UUID;

class UserDaoImplTest {

    void save() throws SQLException, ClassNotFoundException {

        UserDao dao=new UserDaoImpl();
        User user=new User(
                UUID.randomUUID().toString(),
                "xyz@gmail.com",
                "Kamal",
                "038",
                PasswordHash.hashPassword("1234")
        );
        boolean isSaved = dao.save(user);
        System.out.println(isSaved);
    }

    void findByUserEmail() {


    }

    public static void main(String[] args) throws SQLException, ClassNotFoundException {

        UserDaoImplTest test= new UserDaoImplTest();
        test.save();

    }
}