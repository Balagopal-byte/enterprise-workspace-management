-- ============================================================
-- Enterprise Workspace Management System
-- Seed Data for MySQL 8+
-- ============================================================
-- Passwords are BCrypt hashed. The plain text passwords are:
--   admin@example.com    => Admin@123
--   manager@example.com  => Manager@123
--   john.smith, jane.doe, robert.wilson => Employee@123
-- BCrypt hashes below are pre-computed (cost=12, compatible with at.favre.lib bcrypt).
-- $2b$ and $2a$ prefixes are interchangeable in BCrypt verification.

USE enterprise_workspace;

-- ============================================================
-- ROLES
-- ============================================================
INSERT INTO roles (id, role_name, description) VALUES
(1, 'SYSTEM_ADMIN', 'Full system access including user management, configuration, and analytics'),
(2, 'FACILITY_MANAGER', 'Manage workspaces, equipment, view analytics, and monitor occupancy'),
(3, 'EMPLOYEE', 'Book desks, meeting rooms, check-in, and view personal bookings');

-- ============================================================
-- USERS (passwords all = respective role password, hashed with BCrypt)
-- We store a known BCrypt hash that the app can verify.
-- Hash generated for "Admin@123" / "Manager@123" / "Employee@123"
-- ============================================================
-- Using a placeholder hash. The application's PasswordUtil will handle verification.
-- The reset.sql can regenerate these if needed.

INSERT INTO users (id, first_name, last_name, email, password_hash, role_id, department, phone, status) VALUES
(1, 'System', 'Administrator', 'admin@example.com',
 '$2b$12$R5Hl5lWs5OstSIo6ACiQH.5pIF1p3NdU.2J1AifMFInZsDG6gLZzy',
 1, 'IT Administration', '+1-555-0101', 'ACTIVE'),

(2, 'Facility', 'Manager', 'manager@example.com',
 '$2b$12$xyCw3cAyLyXx7gpurqlLne78AL6E3NU9F5JHoXIQeEzpXUQMtZTyS',
 2, 'Facility Management', '+1-555-0102', 'ACTIVE'),

(3, 'John', 'Smith', 'john.smith@example.com',
 '$2b$12$o/SbLA4.OKSHDjU6SP5EOueNeQYVPjhMO1fL3w/LJpklTxCFAQxFC',
 3, 'Engineering', '+1-555-0201', 'ACTIVE'),

(4, 'Jane', 'Doe', 'jane.doe@example.com',
 '$2b$12$o/SbLA4.OKSHDjU6SP5EOueNeQYVPjhMO1fL3w/LJpklTxCFAQxFC',
 3, 'Marketing', '+1-555-0202', 'ACTIVE'),

(5, 'Robert', 'Wilson', 'robert.wilson@example.com',
 '$2b$12$o/SbLA4.OKSHDjU6SP5EOueNeQYVPjhMO1fL3w/LJpklTxCFAQxFC',
 3, 'Sales', '+1-555-0203', 'ACTIVE');

-- ============================================================
-- DESKS (20+ desks across different zones)
-- Positions are percentage-based (0-100) for the floor map renderer
-- ============================================================
INSERT INTO desks (id, desk_code, floor, zone, position_x, position_y, desk_type, status, has_monitor, has_phone, has_power_outlet, description) VALUES
-- Open Workspace Zone (left side, rows of 4)
(1,  'D01', 1, 'Open Workspace',   8,  12, 'STANDARD', 'AVAILABLE', TRUE, FALSE, TRUE, 'Open workspace desk near entrance'),
(2,  'D02', 1, 'Open Workspace',  18,  12, 'STANDARD', 'AVAILABLE', TRUE, FALSE, TRUE, 'Open workspace desk'),
(3,  'D03', 1, 'Open Workspace',  28,  12, 'STANDARD', 'AVAILABLE', TRUE, FALSE, TRUE, 'Open workspace desk'),
(4,  'D04', 1, 'Open Workspace',  38,  12, 'STANDARD', 'AVAILABLE', TRUE, FALSE, TRUE, 'Open workspace desk near window'),
(5,  'D05', 1, 'Open Workspace',   8,  28, 'STANDARD', 'AVAILABLE', TRUE, FALSE, TRUE, 'Open workspace desk second row'),
(6,  'D06', 1, 'Open Workspace',  18,  28, 'STANDING', 'AVAILABLE', TRUE, FALSE, TRUE, 'Standing desk with adjustable height'),
(7,  'D07', 1, 'Open Workspace',  28,  28, 'STANDARD', 'AVAILABLE', TRUE, FALSE, TRUE, 'Open workspace desk'),
(8,  'D08', 1, 'Open Workspace',  38,  28, 'STANDING', 'AVAILABLE', TRUE, FALSE, TRUE, 'Standing desk with adjustable height'),

-- Quiet Zone (bottom left)
(9,  'D09', 1, 'Quiet Zone',       8,  55, 'STANDARD', 'AVAILABLE', TRUE, FALSE, TRUE, 'Quiet zone desk - no phone calls'),
(10, 'D10', 1, 'Quiet Zone',      18,  55, 'STANDARD', 'AVAILABLE', TRUE, FALSE, TRUE, 'Quiet zone desk - focused work'),
(11, 'D11', 1, 'Quiet Zone',      28,  55, 'CORNER',   'AVAILABLE', TRUE, FALSE, TRUE, 'Quiet zone corner desk'),
(12, 'D12', 1, 'Quiet Zone',      38,  55, 'STANDARD', 'AVAILABLE', TRUE, FALSE, TRUE, 'Quiet zone desk'),

-- Collaborative Zone (bottom left, lower)
(13, 'D13', 1, 'Collaborative Zone', 8,  75, 'STANDARD', 'AVAILABLE', TRUE, TRUE, TRUE, 'Collaborative area desk with phone'),
(14, 'D14', 1, 'Collaborative Zone', 18, 75, 'STANDARD', 'AVAILABLE', TRUE, TRUE, TRUE, 'Collaborative area desk'),
(15, 'D15', 1, 'Collaborative Zone', 28, 75, 'STANDARD', 'AVAILABLE', TRUE, TRUE, TRUE, 'Collaborative area desk'),
(16, 'D16', 1, 'Collaborative Zone', 38, 75, 'STANDARD', 'AVAILABLE', TRUE, TRUE, TRUE, 'Collaborative area desk'),

-- Manager Zone (top right area)
(17, 'D17', 1, 'Manager Zone',    55,  12, 'EXECUTIVE', 'AVAILABLE', TRUE, TRUE, TRUE, 'Executive desk with dual monitors'),
(18, 'D18', 1, 'Manager Zone',    55,  28, 'EXECUTIVE', 'AVAILABLE', TRUE, TRUE, TRUE, 'Executive desk near meeting rooms'),

-- Extra desks
(19, 'D19', 1, 'Open Workspace',   8,  90, 'STANDARD', 'MAINTENANCE', TRUE, FALSE, TRUE, 'Desk under maintenance'),
(20, 'D20', 1, 'Open Workspace',  18,  90, 'STANDARD', 'AVAILABLE', TRUE, FALSE, TRUE, 'Open workspace desk near exit'),
(21, 'D21', 1, 'Quiet Zone',      28,  90, 'STANDARD', 'AVAILABLE', TRUE, FALSE, TRUE, 'Additional quiet zone desk'),
(22, 'D22', 1, 'Collaborative Zone', 38, 90, 'STANDING', 'AVAILABLE', TRUE, FALSE, TRUE, 'Standing collaborative desk');

-- ============================================================
-- MEETING ROOMS (5 rooms)
-- ============================================================
INSERT INTO meeting_rooms (id, room_name, floor, capacity, location, status, description, position_x, position_y) VALUES
(1, 'Innovation Hub',   1, 10, 'East Wing - 1st Floor', 'AVAILABLE', 'Large conference room with video conferencing', 70, 12),
(2, 'Strategy Room',    1,  6, 'East Wing - 1st Floor', 'AVAILABLE', 'Medium meeting room for team discussions', 85, 12),
(3, 'Focus Pod',        1,  4, 'East Wing - 1st Floor', 'AVAILABLE', 'Small meeting room for focused sessions', 70, 35),
(4, 'Boardroom',        1, 16, 'East Wing - 1st Floor', 'AVAILABLE', 'Executive boardroom with full AV setup', 85, 35),
(5, 'Brainstorm Lab',   1,  8, 'East Wing - 1st Floor', 'AVAILABLE', 'Creative space with whiteboards', 70, 58);

-- ============================================================
-- EQUIPMENT
-- ============================================================
INSERT INTO equipment (id, name, category, description, status) VALUES
(1, 'Projector',              'AV',          '4K Projector with HDMI input', 'ACTIVE'),
(2, 'Whiteboard',             'Writing',     'Large wall-mounted whiteboard', 'ACTIVE'),
(3, 'Video Conference Unit',  'AV',          'HD video conferencing system with camera and mic', 'ACTIVE'),
(4, '75" Display',            'AV',          '75-inch 4K display screen', 'ACTIVE'),
(5, 'Speaker System',         'AV',          'Ceiling-mounted speaker system', 'ACTIVE'),
(6, 'HDMI Cable',             'Connectivity', 'HDMI cable for laptop connection', 'ACTIVE'),
(7, 'Wireless Presenter',     'Presentation', 'Wireless presentation clicker', 'ACTIVE'),
(8, 'Webcam',                 'AV',          'HD webcam for video calls', 'ACTIVE');

-- ============================================================
-- ROOM EQUIPMENT (link equipment to rooms)
-- ============================================================
INSERT INTO room_equipment (room_id, equipment_id, quantity) VALUES
-- Innovation Hub: projector, whiteboard, video conf, speakers
(1, 1, 1), (1, 2, 2), (1, 3, 1), (1, 5, 1), (1, 6, 2),
-- Strategy Room: display, whiteboard, webcam
(2, 4, 1), (2, 2, 1), (2, 8, 1), (2, 6, 1),
-- Focus Pod: display, webcam
(3, 4, 1), (3, 8, 1), (3, 6, 1),
-- Boardroom: projector, video conf, display, speakers, whiteboard
(4, 1, 1), (4, 3, 1), (4, 4, 2), (4, 5, 1), (4, 2, 2), (4, 7, 2),
-- Brainstorm Lab: whiteboard, display
(5, 2, 4), (5, 4, 1), (5, 6, 2);

-- ============================================================
-- SAMPLE BOOKINGS
-- ============================================================
INSERT INTO bookings (user_id, desk_id, booking_date, start_time, end_time, status, notes) VALUES
(3, 1,  CURDATE(), '09:00', '17:00', 'CONFIRMED', 'Regular workday booking'),
(4, 5,  CURDATE(), '10:00', '16:00', 'CONFIRMED', 'Project sprint day'),
(5, 9,  CURDATE(), '08:00', '12:00', 'CONFIRMED', 'Morning focused work'),
(3, 3,  DATE_ADD(CURDATE(), INTERVAL 1 DAY), '09:00', '17:00', 'CONFIRMED', 'Tomorrow booking'),
(4, 7,  DATE_ADD(CURDATE(), INTERVAL 1 DAY), '09:00', '13:00', 'CONFIRMED', 'Half day booking'),
(5, 13, DATE_ADD(CURDATE(), INTERVAL 2 DAY), '10:00', '15:00', 'CONFIRMED', 'Collaborative work session');

-- ============================================================
-- SAMPLE MEETING ROOM BOOKINGS
-- ============================================================
INSERT INTO meeting_room_bookings (user_id, room_id, booking_date, start_time, end_time, title, description, attendee_count, status) VALUES
(3, 1, CURDATE(), '10:00', '11:00', 'Sprint Planning', 'Weekly sprint planning meeting', 8, 'CONFIRMED'),
(4, 2, CURDATE(), '14:00', '15:00', 'Design Review', 'UI/UX design review session', 5, 'CONFIRMED'),
(2, 4, CURDATE(), '09:00', '10:30', 'Management Sync', 'Monthly management alignment', 12, 'CONFIRMED'),
(3, 3, DATE_ADD(CURDATE(), INTERVAL 1 DAY), '11:00', '12:00', 'One-on-One', 'Manager one-on-one meeting', 2, 'CONFIRMED');

-- ============================================================
-- APPLICATION SETTINGS
-- ============================================================
INSERT INTO app_settings (setting_key, setting_value, description) VALUES
('checkin.grace_period_minutes', '15', 'Minutes after booking start time before auto-release'),
('booking.max_duration_hours', '10', 'Maximum booking duration in hours'),
('booking.min_duration_minutes', '30', 'Minimum booking duration in minutes'),
('auto_release.enabled', 'true', 'Enable automatic release of unchecked bookings'),
('auto_release.check_interval_seconds', '60', 'How often to check for auto-release in seconds'),
('refresh.interval_seconds', '30', 'Dashboard/floor map refresh interval in seconds'),
('app.name', 'Enterprise Workspace Management System', 'Application display name'),
('app.version', '1.0.0', 'Application version');

-- ============================================================
-- INITIAL AUDIT LOGS
-- ============================================================
INSERT INTO audit_logs (user_id, action, entity_type, entity_id, description) VALUES
(1, 'SYSTEM_INIT', 'SYSTEM', NULL, 'System initialized with seed data'),
(1, 'USER_CREATED', 'USER', 1, 'System Administrator account created'),
(1, 'USER_CREATED', 'USER', 2, 'Facility Manager account created'),
(1, 'USER_CREATED', 'USER', 3, 'Employee John Smith account created'),
(1, 'USER_CREATED', 'USER', 4, 'Employee Jane Doe account created'),
(1, 'USER_CREATED', 'USER', 5, 'Employee Robert Wilson account created');
