package com.revature.services;

import at.favre.lib.crypto.bcrypt.BCrypt;
import com.revature.DAOs.UserDAO;
import com.revature.DAOs.UserDAOInterface;
import com.revature.exceptions.InvalidInputException;
import com.revature.exceptions.ResourceNotFoundException;
import com.revature.models.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class UserService {

    private static final Logger logger = LoggerFactory.getLogger(UserService.class);

    private final UserDAOInterface userDAO;

    public UserService() {
        this(new UserDAO());
    }

    public UserService(UserDAOInterface userDAO) {
        this.userDAO = userDAO;
    }

    public User getUserByUsername(String username) {
        if (username == null || username.trim().isEmpty()) {
            logger.warn("username was null or empty");
            throw new InvalidInputException("Username cannot be empty.");
        }

        User user = userDAO.getUserByUsername(username);
        if (user == null) {
            throw new ResourceNotFoundException("No user found with username: " + username);
        }
        return user;
    }

    // Every authentication failure returns null, never a distinct exception.
    // This is deliberate: identical responses for "no such username" and
    // "wrong password" prevent user enumeration.
    public User login(String username, String password) {
        if (username == null || username.trim().isEmpty()) {
            logger.warn("Login rejected - username was null or blank");
            throw new InvalidInputException("Username cannot be empty.");
        }
        if (password == null || password.isEmpty()) {
            logger.warn("Login rejected - password was null or empty");
            throw new InvalidInputException("Password cannot be empty.");
        }

        // Call the DAO directly, not getUserByUsername(): that method throws
        // ResourceNotFoundException for a missing user, and login must never
        // surface that.
        User user = userDAO.getUserByUsername(username);
        if (user == null) {
            return null;
        }

        if (!"manager".equals(user.getRole())) {
            return null;
        }

        BCrypt.Result result = BCrypt.verifyer().verify(
                password.toCharArray(),
                user.getPassword()
        );
        if (!result.verified) {
            return null;
        }

        // Strip the password before the user object leaves the service
        user.setPassword(null);
        return user;
    }

    public User getUserById(int userId) {
        if (userId <= 0) {
            logger.warn("invalid user id: {}", userId);
            throw new InvalidInputException("User id must be a positive number.");
        }

        User user = userDAO.getUserById(userId);
        if (user == null) {
            throw new ResourceNotFoundException("No user found with id: " + userId);
        }
        return user;
    }
}