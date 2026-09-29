package com.workspace.management.dao;

import com.workspace.management.model.User;
import java.util.List;
import java.util.Optional;

/**
 * Data Access Object interface for User operations.
 * Demonstrates the DAO pattern with abstraction.
 */
public interface UserDAO {
    Optional<User> findById(int id);
    Optional<User> findByEmail(String email);
    List<User> findAll();
    List<User> findByRole(int roleId);
    int create(User user);
    boolean update(User user);
    boolean updateStatus(int userId, String status);
    boolean updatePassword(int userId, String passwordHash);
    boolean updateLastLogin(int userId);
    int countAll();
    int countByStatus(String status);
}
