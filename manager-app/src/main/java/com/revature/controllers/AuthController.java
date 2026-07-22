package com.revature.controllers;
import com.revature.models.User;
import com.revature.services.AuthService;
import io.javalin.http.Handler;
import io.javalin.http.HttpStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.revature.exceptions.ResourceNotFoundException;

public class AuthController {
    private static final Logger logger = LoggerFactory.getLogger(AuthController.class);
    AuthService authService = new AuthService();

    public Handler loginHandler = (ctx) -> {
        try {
            // Read the incoming JSON body and convert it into a User object
            User loginRequest = ctx.bodyAsClass(User.class);
            logger.info("Login attempt for username: {}", loginRequest.getUsername());

            // service checks the user exists, the password matches, and the role is manager
            User foundUser = authService.login(loginRequest.getUsername(), loginRequest.getPassword());

            if (foundUser != null) {
                ctx.json(foundUser);
                ctx.status(HttpStatus.OK);
            } else {
                ctx.status(HttpStatus.UNAUTHORIZED);
            }

        } catch (ResourceNotFoundException e) {
            logger.warn("User not found during login: {}", e.getMessage());
            ctx.status(HttpStatus.NOT_FOUND);
            ctx.result(e.getMessage());
        } catch (Exception e) {
            logger.error("Unexpected error during login: {}", e.getMessage());
            ctx.status(HttpStatus.INTERNAL_SERVER_ERROR);
            ctx.result("An unexpected error occurred.");
        }
    };
}
