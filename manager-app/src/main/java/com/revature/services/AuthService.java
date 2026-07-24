package com.revature.services;

import at.favre.lib.crypto.bcrypt.BCrypt;
import com.revature.DAOs.UserDAO;
import com.revature.DAOs.UserDAOInterface;
import com.revature.models.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/*
 *
 * Owns the login rules for the manager side:
 *  - does the user exist?
 *  - does the password match?
 *  - is the user actually a manager?
 *
 * Depends on the DAO *interface* so unit tests can pass in a Mockito mock
 * instead of hitting the real database.
 */
public class AuthService {

    private static final Logger logger = LoggerFactory.getLogger(AuthService.class);

    private final UserDAOInterface userDAO;

    // used by the real app
    public AuthService() {
        this(new UserDAO());
    }

    // used by unit tests to inject a mock DAO
    public AuthService(UserDAOInterface userDAO) {
        this.userDAO = userDAO;
    }

    // Returns the logged-in user (password stripped), or null if the
    // credentials are wrong or the user is not a manager.
    public User login(String username, String password) {
        if (username == null || password == null) {
            return null;
        }

        User foundUser = userDAO.getUserByUsername(username);
        if (foundUser == null) {
            logger.warn("Login failed - no user found with username: {}", username);
            return null;
        }

        BCrypt.Result result = BCrypt.verifyer().verify(
                password.toCharArray(),
                foundUser.getPassword()
        );

        if (!result.verified) {
            logger.warn("Login failed - incorrect password for user: {}", username);
            return null;
        }

        if (!"manager".equals(foundUser.getRole())) {
            logger.warn("Login failed - user {} is not a manager", username);
            return null;
        }

        // Strip the password before the user object leaves the service
        foundUser.setPassword(null);
        logger.info("Successful login for user: {}", username);
        return foundUser;
    }
}
