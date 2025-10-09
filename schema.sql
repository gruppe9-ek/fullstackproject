-- =====================================================================
-- KinoXP - agreed schema (syntax-fixed, MySQL 8.0+ / InnoDB)
-- =====================================================================

-- (Valgfrit) Opret database og brug den
CREATE DATABASE IF NOT EXISTS kinoxp
  CHARACTER SET utf8mb4
  COLLATE utf8mb4_0900_ai_ci;
USE kinoxp;

-- Idempotent cleanup (drop i FK-rigtig rækkefølge)
SET FOREIGN_KEY_CHECKS = 0;
DROP TABLE IF EXISTS product_sales;
DROP TABLE IF EXISTS products;
DROP TABLE IF EXISTS work_schedule;
DROP TABLE IF EXISTS booking_seats;
DROP TABLE IF EXISTS bookings;
DROP TABLE IF EXISTS shows;
DROP TABLE IF EXISTS employees;
DROP TABLE IF EXISTS seats;
DROP TABLE IF EXISTS theaters;
DROP TABLE IF EXISTS movies;
SET FOREIGN_KEY_CHECKS = 1;

-- ============================================
-- 1. MOVIES
-- ============================================
CREATE TABLE movies (
  movie_id INT PRIMARY KEY AUTO_INCREMENT,
  title VARCHAR(255) NOT NULL,
  poster_url VARCHAR(500) NULL,
  category VARCHAR(50) NOT NULL,
  age_limit INT NOT NULL,
  duration_minutes INT NOT NULL,
  actors TEXT,
  description TEXT,
  CHECK (age_limit >= 0),
  CHECK (duration_minutes > 0)
) ENGINE=InnoDB;

-- ============================================
-- 2. THEATERS
-- ============================================
CREATE TABLE theaters (
  theater_id INT PRIMARY KEY AUTO_INCREMENT,
  theater_name VARCHAR(50) NOT NULL UNIQUE,
  total_rows INT NOT NULL,
  seats_per_row INT NOT NULL,
  CHECK (total_rows > 0),
  CHECK (seats_per_row > 0)
) ENGINE=InnoDB;

-- ============================================
-- 3. SEATS
-- ============================================
CREATE TABLE seats (
  seat_id INT PRIMARY KEY AUTO_INCREMENT,
  theater_id INT NOT NULL,
  `row_number` INT NOT NULL,
  `seat_number` INT NOT NULL,
  CONSTRAINT fk_seats_theater
    FOREIGN KEY (theater_id) REFERENCES theaters(theater_id)
    ON DELETE CASCADE ON UPDATE CASCADE,
  CONSTRAINT unique_seat UNIQUE (theater_id, `row_number`, `seat_number`),
  CHECK (`row_number` > 0),
  CHECK (`seat_number` > 0)
) ENGINE=InnoDB;

CREATE INDEX idx_theater_seat ON seats(theater_id, `row_number`, `seat_number`);

-- ============================================
-- 4. EMPLOYEES
-- ============================================
CREATE TABLE employees (
  employee_id INT PRIMARY KEY AUTO_INCREMENT,
  name VARCHAR(255) NOT NULL,
  username VARCHAR(50) NOT NULL UNIQUE,
  password_hash VARCHAR(255) NOT NULL,
  role ENUM('ticket_sales', 'operator', 'inspector_cleaner', 'admin') NOT NULL
) ENGINE=InnoDB;

CREATE INDEX idx_username ON employees(username);

-- ============================================
-- 5. SHOWS
-- ============================================
CREATE TABLE shows (
  show_id INT PRIMARY KEY AUTO_INCREMENT,
  movie_id INT NOT NULL,
  theater_id INT NOT NULL,
  status VARCHAR(20) NOT NULL DEFAULT 'scheduled',
  show_datetime DATETIME NOT NULL,
  price DECIMAL(10,2) NOT NULL,
  operator_id INT NULL,
  inspector_id INT NULL,
  CONSTRAINT fk_shows_movie
    FOREIGN KEY (movie_id) REFERENCES movies(movie_id)
    ON DELETE RESTRICT ON UPDATE CASCADE,
  CONSTRAINT fk_shows_theater
    FOREIGN KEY (theater_id) REFERENCES theaters(theater_id)
    ON DELETE RESTRICT ON UPDATE CASCADE,
  CONSTRAINT fk_shows_operator
    FOREIGN KEY (operator_id) REFERENCES employees(employee_id)
    ON DELETE SET NULL ON UPDATE CASCADE,
  CONSTRAINT fk_shows_inspector
    FOREIGN KEY (inspector_id) REFERENCES employees(employee_id)
    ON DELETE SET NULL ON UPDATE CASCADE,
  CONSTRAINT unique_theater_time UNIQUE (theater_id, show_datetime),
  CHECK (price >= 0)
) ENGINE=InnoDB;

CREATE INDEX idx_show_datetime ON shows(show_datetime);
CREATE INDEX idx_movie_shows   ON shows(movie_id);

-- ============================================
-- 6. BOOKINGS
-- ============================================
CREATE TABLE bookings (
  booking_id INT PRIMARY KEY AUTO_INCREMENT,
  show_id INT NOT NULL,
  customer_name VARCHAR(255) NOT NULL,
  customer_phone VARCHAR(20) NOT NULL,
  total_amount DECIMAL(10,2) NOT NULL,
  sold_by_id INT NOT NULL,
  CONSTRAINT fk_bookings_show
    FOREIGN KEY (show_id) REFERENCES shows(show_id)
    ON DELETE RESTRICT ON UPDATE CASCADE,
  CONSTRAINT fk_bookings_seller
    FOREIGN KEY (sold_by_id) REFERENCES employees(employee_id)
    ON DELETE RESTRICT ON UPDATE CASCADE,
  CHECK (total_amount >= 0),
  CHECK (customer_name <> ''),
  CHECK (customer_phone <> '')
) ENGINE=InnoDB;

CREATE INDEX idx_show_bookings  ON bookings(show_id);
CREATE INDEX idx_customer_phone ON bookings(customer_phone);

-- ============================================
-- 7. BOOKING_SEATS
-- ============================================
CREATE TABLE booking_seats (
  booking_seat_id INT PRIMARY KEY AUTO_INCREMENT,
  booking_id INT NOT NULL,
  seat_id INT NOT NULL,
  CONSTRAINT fk_bs_booking
    FOREIGN KEY (booking_id) REFERENCES bookings(booking_id)
    ON DELETE CASCADE ON UPDATE CASCADE,
  CONSTRAINT fk_bs_seat
    FOREIGN KEY (seat_id) REFERENCES seats(seat_id)
    ON DELETE RESTRICT ON UPDATE CASCADE,
  CONSTRAINT unique_booking_seat UNIQUE (booking_id, seat_id)
) ENGINE=InnoDB;

CREATE INDEX idx_seat_bookings ON booking_seats(seat_id);

-- ============================================
-- 8. WORK_SCHEDULE
-- ============================================
CREATE TABLE work_schedule (
  schedule_id INT PRIMARY KEY AUTO_INCREMENT,
  employee_id INT NOT NULL,
  work_date DATE NOT NULL,
  start_time TIME NOT NULL,
  end_time TIME NOT NULL,
  CONSTRAINT fk_ws_employee
    FOREIGN KEY (employee_id) REFERENCES employees(employee_id)
    ON DELETE CASCADE ON UPDATE CASCADE,
  CONSTRAINT unique_employee_date UNIQUE (employee_id, work_date),
  CHECK (end_time > start_time)
) ENGINE=InnoDB;

CREATE INDEX idx_work_date ON work_schedule(work_date);

-- ============================================
-- 9. PRODUCTS
-- ============================================
CREATE TABLE products (
  product_id INT PRIMARY KEY AUTO_INCREMENT,
  product_name VARCHAR(255) NOT NULL,
  category ENUM('candy', 'soda', 'popcorn', 'other') NOT NULL,
  price DECIMAL(10,2) NOT NULL,
  CHECK (price >= 0),
  CHECK (product_name <> '')
) ENGINE=InnoDB;

-- ============================================
-- 10. PRODUCT_SALES
-- ============================================
CREATE TABLE product_sales (
  sale_id INT PRIMARY KEY AUTO_INCREMENT,
  product_id INT NOT NULL,
  quantity INT NOT NULL,
  total_price DECIMAL(10,2) NOT NULL,
  sold_by_id INT NOT NULL,
  booking_id INT NULL,
  CONSTRAINT fk_ps_product
    FOREIGN KEY (product_id) REFERENCES products(product_id)
    ON DELETE RESTRICT ON UPDATE CASCADE,
  CONSTRAINT fk_ps_seller
    FOREIGN KEY (sold_by_id) REFERENCES employees(employee_id)
    ON DELETE RESTRICT ON UPDATE CASCADE,
  CONSTRAINT fk_ps_booking
    FOREIGN KEY (booking_id) REFERENCES bookings(booking_id)
    ON DELETE SET NULL ON UPDATE CASCADE,
  CHECK (quantity > 0),
  CHECK (total_price >= 0)
) ENGINE=InnoDB;

CREATE INDEX idx_booking_sales ON product_sales(booking_id);
CREATE INDEX idx_product_sales ON product_sales(product_id);
