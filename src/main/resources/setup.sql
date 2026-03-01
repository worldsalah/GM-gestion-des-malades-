-- SQL Script to Create Database and Users Table
-- Ensure MySQL is running on port 3307 and execute this script

CREATE DATABASE IF NOT EXISTS appdb;

USE appdb;

CREATE TABLE IF NOT EXISTS users (
    id INT AUTO_INCREMENT PRIMARY KEY,
    username VARCHAR(50) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    first_name VARCHAR(100),
    last_name VARCHAR(100),
    email VARCHAR(100),
    phone VARCHAR(20),
    gender VARCHAR(10),
    role VARCHAR(20)
);

-- Insert the default credential hanen / SALAH
INSERT INTO users (username, password) VALUES ('hanen', 'SALAH')
    ON DUPLICATE KEY UPDATE password='SALAH';
