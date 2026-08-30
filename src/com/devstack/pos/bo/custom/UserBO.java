package com.devstack.pos.bo.custom;

import com.devstack.pos.dto.request.RequestUserDTO;
import com.devstack.pos.dto.response.ResponseUserDTO;
import com.devstack.pos.model.User;

import java.sql.SQLException;

public interface UserBO {
    public boolean registerUser(RequestUserDTO userDTO) throws SQLException, ClassNotFoundException;
    public ResponseUserDTO loginUser(String email, String password) throws SQLException, ClassNotFoundException;

}
