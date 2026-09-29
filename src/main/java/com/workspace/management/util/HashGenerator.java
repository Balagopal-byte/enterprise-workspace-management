package com.workspace.management.util;

/**
 * Utility to generate BCrypt hashes for seed data.
 * Run this class to generate password hashes for the database seed.
 *
 * Usage: java com.workspace.management.util.HashGenerator
 */
public class HashGenerator {
    public static void main(String[] args) {
        String[] passwords = {"Admin@123", "Manager@123", "Employee@123"};
        System.out.println("=== BCrypt Hash Generator ===");
        System.out.println("Use these hashes in seed.sql:");
        System.out.println();
        for (String pwd : passwords) {
            String hash = PasswordUtil.hashPassword(pwd);
            System.out.println("Password: " + pwd);
            System.out.println("Hash:     " + hash);
            System.out.println("Verify:   " + PasswordUtil.verifyPassword(pwd, hash));
            System.out.println();
        }
    }
}
