package com.revature.controllers;
import com.revature.exceptions.InvalidInputException;
import com.revature.models.User;
import com.revature.services.UserService;
import io.javalin.http.Handler;
import io.javalin.http.HttpStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AuthController {
    private static final Logger logger = LoggerFactory.getLogger(AuthController.class);
    UserService userService = new UserService();

    public Handler loginHandler = (ctx) -> {
        try {
            // Read the incoming JSON body and convert it into a User object
            User loginRequest = ctx.bodyAsClass(User.class);
            logger.info("Login attempt for username: {}", loginRequest.getUsername());

            // service validates input, checks the password, and requires the manager role;
            // it returns null for every authentication failure (no user enumeration)
            User foundUser = userService.login(loginRequest.getUsername(), loginRequest.getPassword());

            if (foundUser != null) {
                ctx.json(foundUser);
                ctx.status(HttpStatus.OK);
            } else {
                // deliberately no body detail: same response for bad username and bad password
                ctx.status(HttpStatus.UNAUTHORIZED);
            }

        } catch (InvalidInputException e) {
            ctx.status(HttpStatus.BAD_REQUEST);
            ctx.result(e.getMessage());
        } catch (Exception e) {
            logger.error("Unexpected error during login: {}", e.getMessage());
            ctx.status(HttpStatus.INTERNAL_SERVER_ERROR);
            ctx.result("An unexpected error occurred.");
        }
    };
}
