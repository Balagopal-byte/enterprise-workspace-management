-- ============================================================
-- Enterprise Workspace Management System
-- Database Schema for MySQL 8+
-- ============================================================

-- Create database
CREATE DATABASE IF NOT EXISTS enterprise_workspace
    CHARACTER SET utf8mb4
    COLLATE utf8mb4_unicode_ci;

USE enterprise_workspace;

-- ============================================================
-- ROLES TABLE
-- ============================================================
CREATE TABLE roles (
    id INT AUTO_INCREMENT PRIMARY KEY,
    role_name VARCHAR(50) NOT NULL UNIQUE,
    description VARCHAR(255),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) ENGINE=InnoDB;

-- ============================================================
-- USERS TABLE
-- ============================================================
CREATE TABLE users (
    id INT AUTO_INCREMENT PRIMARY KEY,
    first_name VARCHAR(100) NOT NULL,
    last_name VARCHAR(100) NOT NULL,
    email VARCHAR(255) NOT NULL UNIQUE,
    password_hash VARCHAR(255) NOT NULL,
    role_id INT NOT NULL,
    department VARCHAR(100),
    phone VARCHAR(20),
    status ENUM('ACTIVE', 'INACTIVE', 'SUSPENDED') NOT NULL DEFAULT 'ACTIVE',
    last_login TIMESTAMP NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (role_id) REFERENCES roles(id) ON UPDATE CASCADE,
    INDEX idx_users_email (email),
    INDEX idx_users_role (role_id),
    INDEX idx_users_status (status)
) ENGINE=InnoDB;

-- ============================================================
-- DESKS TABLE
-- ============================================================
CREATE TABLE desks (
    id INT AUTO_INCREMENT PRIMARY KEY,
    desk_code VARCHAR(20) NOT NULL UNIQUE,
    floor INT NOT NULL DEFAULT 1,
    zone VARCHAR(50) NOT NULL,
    position_x INT NOT NULL DEFAULT 0,
    position_y INT NOT NULL DEFAULT 0,
    desk_type ENUM('STANDARD', 'STANDING', 'CORNER', 'EXECUTIVE') NOT NULL DEFAULT 'STANDARD',
    status ENUM('AVAILABLE', 'RESERVED', 'OCCUPIED', 'MAINTENANCE', 'INACTIVE') NOT NULL DEFAULT 'AVAILABLE',
    has_monitor BOOLEAN DEFAULT TRUE,
    has_phone BOOLEAN DEFAULT FALSE,
    has_power_outlet BOOLEAN DEFAULT TRUE,
    description VARCHAR(255),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_desks_floor (floor),
    INDEX idx_desks_zone (zone),
    INDEX idx_desks_status (status),
    INDEX idx_desks_code (desk_code)
) ENGINE=InnoDB;

-- ============================================================
-- BOOKINGS TABLE (Desk Bookings)
-- ============================================================
CREATE TABLE bookings (
    id INT AUTO_INCREMENT PRIMARY KEY,
    user_id INT NOT NULL,
    desk_id INT NOT NULL,
    booking_date DATE NOT NULL,
    start_time TIME NOT NULL,
    end_time TIME NOT NULL,
    status ENUM('PENDING', 'CONFIRMED', 'CHECKED_IN', 'COMPLETED', 'CANCELLED', 'AUTO_RELEASED') NOT NULL DEFAULT 'CONFIRMED',
    notes VARCHAR(500),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(id) ON UPDATE CASCADE,
    FOREIGN KEY (desk_id) REFERENCES desks(id) ON UPDATE CASCADE,
    INDEX idx_bookings_user (user_id),
    INDEX idx_bookings_desk (desk_id),
    INDEX idx_bookings_date (booking_date),
    INDEX idx_bookings_status (status),
    INDEX idx_bookings_desk_date_time (desk_id, booking_date, start_time, end_time),
    CHECK (end_time > start_time)
) ENGINE=InnoDB;

-- ============================================================
-- MEETING ROOMS TABLE
-- ============================================================
CREATE TABLE meeting_rooms (
    id INT AUTO_INCREMENT PRIMARY KEY,
    room_name VARCHAR(100) NOT NULL UNIQUE,
    floor INT NOT NULL DEFAULT 1,
    capacity INT NOT NULL DEFAULT 4,
    location VARCHAR(100),
    status ENUM('AVAILABLE', 'BOOKED', 'MAINTENANCE', 'INACTIVE') NOT NULL DEFAULT 'AVAILABLE',
    description VARCHAR(255),
    position_x INT NOT NULL DEFAULT 0,
    position_y INT NOT NULL DEFAULT 0,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_rooms_floor (floor),
    INDEX idx_rooms_status (status)
) ENGINE=InnoDB;

-- ============================================================
-- EQUIPMENT TABLE
-- ============================================================
CREATE TABLE equipment (
    id INT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(100) NOT NULL UNIQUE,
    category VARCHAR(50),
    description VARCHAR(255),
    status ENUM('ACTIVE', 'INACTIVE', 'MAINTENANCE') NOT NULL DEFAULT 'ACTIVE',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) ENGINE=InnoDB;

-- ============================================================
-- ROOM EQUIPMENT (Junction Table)
-- ============================================================
CREATE TABLE room_equipment (
    id INT AUTO_INCREMENT PRIMARY KEY,
    room_id INT NOT NULL,
    equipment_id INT NOT NULL,
    quantity INT NOT NULL DEFAULT 1,
    FOREIGN KEY (room_id) REFERENCES meeting_rooms(id) ON DELETE CASCADE ON UPDATE CASCADE,
    FOREIGN KEY (equipment_id) REFERENCES equipment(id) ON DELETE CASCADE ON UPDATE CASCADE,
    UNIQUE KEY uk_room_equipment (room_id, equipment_id)
) ENGINE=InnoDB;

-- ============================================================
-- MEETING ROOM BOOKINGS TABLE
-- ============================================================
CREATE TABLE meeting_room_bookings (
    id INT AUTO_INCREMENT PRIMARY KEY,
    user_id INT NOT NULL,
    room_id INT NOT NULL,
    booking_date DATE NOT NULL,
    start_time TIME NOT NULL,
    end_time TIME NOT NULL,
    title VARCHAR(200) NOT NULL,
    description VARCHAR(500),
    attendee_count INT DEFAULT 1,
    status ENUM('CONFIRMED', 'COMPLETED', 'CANCELLED') NOT NULL DEFAULT 'CONFIRMED',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(id) ON UPDATE CASCADE,
    FOREIGN KEY (room_id) REFERENCES meeting_rooms(id) ON UPDATE CASCADE,
    INDEX idx_room_bookings_user (user_id),
    INDEX idx_room_bookings_room (room_id),
    INDEX idx_room_bookings_date (booking_date),
    INDEX idx_room_bookings_status (status),
    INDEX idx_room_bookings_room_date_time (room_id, booking_date, start_time, end_time),
    CHECK (end_time > start_time)
) ENGINE=InnoDB;

-- ============================================================
-- CHECK-INS TABLE
-- ============================================================
CREATE TABLE check_ins (
    id INT AUTO_INCREMENT PRIMARY KEY,
    booking_id INT NOT NULL,
    user_id INT NOT NULL,
    check_in_time TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    check_out_time TIMESTAMP NULL,
    FOREIGN KEY (booking_id) REFERENCES bookings(id) ON UPDATE CASCADE,
    FOREIGN KEY (user_id) REFERENCES users(id) ON UPDATE CASCADE,
    INDEX idx_checkins_booking (booking_id),
    INDEX idx_checkins_user (user_id)
) ENGINE=InnoDB;

-- ============================================================
-- OCCUPANCY RECORDS TABLE
-- ============================================================
CREATE TABLE occupancy_records (
    id INT AUTO_INCREMENT PRIMARY KEY,
    record_date DATE NOT NULL,
    record_hour INT NOT NULL,
    total_desks INT NOT NULL DEFAULT 0,
    occupied_desks INT NOT NULL DEFAULT 0,
    total_rooms INT NOT NULL DEFAULT 0,
    occupied_rooms INT NOT NULL DEFAULT 0,
    occupancy_percentage DECIMAL(5,2) DEFAULT 0.00,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_occupancy_date (record_date),
    INDEX idx_occupancy_date_hour (record_date, record_hour)
) ENGINE=InnoDB;

-- ============================================================
-- AUDIT LOGS TABLE
-- ============================================================
CREATE TABLE audit_logs (
    id INT AUTO_INCREMENT PRIMARY KEY,
    user_id INT,
    action VARCHAR(100) NOT NULL,
    entity_type VARCHAR(50),
    entity_id INT,
    description VARCHAR(500),
    ip_address VARCHAR(45),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE SET NULL ON UPDATE CASCADE,
    INDEX idx_audit_user (user_id),
    INDEX idx_audit_action (action),
    INDEX idx_audit_date (created_at),
    INDEX idx_audit_entity (entity_type, entity_id)
) ENGINE=InnoDB;

-- ============================================================
-- APPLICATION SETTINGS TABLE
-- ============================================================
CREATE TABLE app_settings (
    setting_key VARCHAR(100) PRIMARY KEY,
    setting_value VARCHAR(500) NOT NULL,
    description VARCHAR(255),
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) ENGINE=InnoDB;
