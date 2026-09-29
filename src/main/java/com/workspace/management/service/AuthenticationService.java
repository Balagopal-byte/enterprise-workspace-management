package com.workspace.management.service;

import com.workspace.management.dao.AnalyticsDAO;
import com.workspace.management.dao.UserDAO;
import com.workspace.management.dao.impl.AnalyticsDAOImpl;
import com.workspace.management.dao.impl.UserDAOImpl;
import com.workspace.management.exception.AuthenticationException;
import com.workspace.management.model.User;
import com.workspace.management.util.PasswordUtil;

import java.util.Optional;
import java.util.logging.Logger;

/**
 * Service for user authentication and session management.
 * Demonstrates the Service Layer pattern separating business logic from DAO.
 */
public class AuthenticationService {
    private static final Logger LOGGER = Logger.getLogger(AuthenticationService.class.getName());
    private final UserDAO userDAO;
    private final AnalyticsDAO analyticsDAO;
    private static User currentUser;

    public AuthenticationService() {
        this.userDAO = new UserDAOImpl();
        this.analyticsDAO = new AnalyticsDAOImpl();
    }

    /**
     * Authenticate a user with email and password.
     * @throws AuthenticationException if credentials are invalid or account is disabled
     */
    public User authenticate(String email, String password) throws AuthenticationException {
        if (email == null || email.trim().isEmpty()) {
            throw new AuthenticationException("Email is required.");
        }
        if (password == null || password.isEmpty()) {
            throw new AuthenticationException("Password is required.");
        }

        Optional<User> userOpt = userDAO.findByEmail(email.trim().toLowerCase());
        if (userOpt.isEmpty()) {
            throw new AuthenticationException("Invalid email or password.");
        }

        User user = userOpt.get();

        if (!"ACTIVE".equals(user.getStatus())) {
            throw new AuthenticationException("Your account has been disabled. Please contact an administrator.");
        }

        if (!PasswordUtil.verifyPassword(password, user.getPasswordHash())) {
            throw new AuthenticationException("Invalid email or password.");
        }

        // Update last login
        userDAO.updateLastLogin(user.getId());

        // Log audit
        analyticsDAO.logAudit(user.getId(), "LOGIN", "USER", user.getId(),
                "User " + user.getFullName() + " logged in");

        currentUser = user;
        LOGGER.info("User authenticated: " + user.getEmail());
        return user;
    }

    /**
     * Get the currently logged-in user.
     */
    public static User getCurrentUser() {
        return currentUser;
    }

    /**
     * Set the current user (used after authentication).
     */
    public static void setCurrentUser(User user) {
        currentUser = user;
    }

    /**
     * Log out the current user.
     */
    public void logout() {
        if (currentUser != null) {
            analyticsDAO.logAudit(currentUser.getId(), "LOGOUT", "USER", currentUser.getId(),
                    "User " + currentUser.getFullName() + " logged out");
            LOGGER.info("User logged out: " + currentUser.getEmail());
            currentUser = null;
        }
    }

    /**
     * Check if a user is currently authenticated.
     */
    public static boolean isAuthenticated() {
        return currentUser != null;
    }
}
