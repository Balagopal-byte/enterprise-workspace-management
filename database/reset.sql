-- ============================================================
-- Enterprise Workspace Management System
-- Reset Database Script
-- WARNING: This will DROP and recreate the entire database!
-- ============================================================

DROP DATABASE IF EXISTS enterprise_workspace;

-- Re-run schema and seed
SOURCE database/schema.sql;
SOURCE database/seed.sql;
