package com.revature.utils;

import com.revature.DAOs.UserDAO;
import com.revature.models.User;
import com.revature.utils.ConnectionUtil;
import java.sql.Connection;

public class TestUserDAO {
    public static void main(String[] args) {

        UserDAO dao = new UserDAO();

        User user = dao.getUserByUsername("vanessa");

        System.out.println(user.getUsername());
        System.out.println(user.getRole());
    }
}

