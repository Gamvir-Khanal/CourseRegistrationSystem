-- ============================================================
--  Course Registration System — Full Schema
--  Run this ONCE to set up (or reset) the database.
--  Usage:  mysql -u root -p < database.sql
-- ============================================================

DROP DATABASE IF EXISTS course_db;
CREATE DATABASE course_db;
USE course_db;

-- Students
CREATE TABLE students (
    id          INT PRIMARY KEY AUTO_INCREMENT,
    roll_number VARCHAR(10)  UNIQUE NOT NULL,
    name        VARCHAR(100) NOT NULL,
    email       VARCHAR(100),
    password    VARCHAR(100)
);

-- Faculty
CREATE TABLE faculty (
    id       INT PRIMARY KEY AUTO_INCREMENT,
    name     VARCHAR(100) UNIQUE NOT NULL,
    password VARCHAR(100) NOT NULL
);

-- Courses
CREATE TABLE courses (
    id            INT PRIMARY KEY AUTO_INCREMENT,
    course_name   VARCHAR(100) NOT NULL,
    faculty_name  VARCHAR(100),
    capacity      INT DEFAULT 30,
    prerequisites VARCHAR(200) DEFAULT 'None',
    FOREIGN KEY (faculty_name) REFERENCES faculty(name) ON UPDATE CASCADE
);

-- Registrations
CREATE TABLE registrations (
    id         INT PRIMARY KEY AUTO_INCREMENT,
    student_id INT NOT NULL,
    course_id  INT NOT NULL,
    UNIQUE KEY uq_reg (student_id, course_id),
    FOREIGN KEY (student_id) REFERENCES students(id) ON DELETE CASCADE,
    FOREIGN KEY (course_id)  REFERENCES courses(id)  ON DELETE CASCADE
);
