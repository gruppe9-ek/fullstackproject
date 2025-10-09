-- =====================================================================
-- KinoXP - Seed Data
-- =====================================================================

-- Insert theaters
INSERT INTO theaters (theater_name, total_rows, seats_per_row) VALUES
('Sal 1', 20, 12),
('Sal 2', 25, 16)
ON DUPLICATE KEY UPDATE
    theater_name = VALUES(theater_name),
    total_rows = VALUES(total_rows),
    seats_per_row = VALUES(seats_per_row);

-- Generate seats for Sal 1 (20 rows x 12 seats = 240 seats)
INSERT IGNORE INTO seats (theater_id, `row_number`, seat_number)
SELECT 1, r, s
FROM (
    SELECT 1 AS r UNION SELECT 2 UNION SELECT 3 UNION SELECT 4 UNION SELECT 5 UNION
    SELECT 6 UNION SELECT 7 UNION SELECT 8 UNION SELECT 9 UNION SELECT 10 UNION
    SELECT 11 UNION SELECT 12 UNION SELECT 13 UNION SELECT 14 UNION SELECT 15 UNION
    SELECT 16 UNION SELECT 17 UNION SELECT 18 UNION SELECT 19 UNION SELECT 20
) AS row_nums
CROSS JOIN (
    SELECT 1 AS s UNION SELECT 2 UNION SELECT 3 UNION SELECT 4 UNION SELECT 5 UNION
    SELECT 6 UNION SELECT 7 UNION SELECT 8 UNION SELECT 9 UNION SELECT 10 UNION
    SELECT 11 UNION SELECT 12
) AS seat_nums;

-- Generate seats for Sal 2 (25 rows x 16 seats = 400 seats)
INSERT IGNORE INTO seats (theater_id, `row_number`, seat_number)
SELECT 2, r, s
FROM (
    SELECT 1 AS r UNION SELECT 2 UNION SELECT 3 UNION SELECT 4 UNION SELECT 5 UNION
    SELECT 6 UNION SELECT 7 UNION SELECT 8 UNION SELECT 9 UNION SELECT 10 UNION
    SELECT 11 UNION SELECT 12 UNION SELECT 13 UNION SELECT 14 UNION SELECT 15 UNION
    SELECT 16 UNION SELECT 17 UNION SELECT 18 UNION SELECT 19 UNION SELECT 20 UNION
    SELECT 21 UNION SELECT 22 UNION SELECT 23 UNION SELECT 24 UNION SELECT 25
) AS row_nums
CROSS JOIN (
    SELECT 1 AS s UNION SELECT 2 UNION SELECT 3 UNION SELECT 4 UNION SELECT 5 UNION
    SELECT 6 UNION SELECT 7 UNION SELECT 8 UNION SELECT 9 UNION SELECT 10 UNION
    SELECT 11 UNION SELECT 12 UNION SELECT 13 UNION SELECT 14 UNION SELECT 15 UNION
    SELECT 16
) AS seat_nums;

-- =====================================================================
-- Insert Movies
-- =====================================================================
INSERT INTO movies (title, poster_url, category, age_limit, duration_minutes, actors, description) VALUES
('The Shawshank Redemption', 'https://image.tmdb.org/t/p/w500/q6y0Go1tsGEsmtFryDOJo3dEmqu.jpg', 'Drama', 15, 142, 'Tim Robbins, Morgan Freeman', 'To fængslede mænd knytter bånd gennem flere år og finder trøst og eventuel forløsning gennem handling af almindelig anstændighed.'),
('The Godfather', 'https://image.tmdb.org/t/p/w500/3bhkrj58Vtu7enYsRolD1fZdja1.jpg', 'Crime', 15, 175, 'Marlon Brando, Al Pacino', 'Den aldrende patriark af en organiseret kriminel dynasti overfører kontrollen af sit hemmelige imperium til sin modvillige søn.'),
('The Dark Knight', 'https://image.tmdb.org/t/p/w500/qJ2tW6WMUDux911r6m7haRef0WH.jpg', 'Action', 11, 152, 'Christian Bale, Heath Ledger', 'Når truslen kendt som Jokeren skaber kaos blandt befolkningen i Gotham, må Batman acceptere en af de største psykologiske tests.'),
('Inception', 'https://image.tmdb.org/t/p/w500/9gk7adHYeDvHkCSEqAvQNLV5Uge.jpg', 'Sci-Fi', 11, 148, 'Leonardo DiCaprio, Joseph Gordon-Levitt', 'En tyv der stjæler virksomhedshemmeligheder gennem brug af drømme-delingsteknologi får den omvendte opgave at plante en idé.'),
('Pulp Fiction', 'https://image.tmdb.org/t/p/w500/d5iIlFn5s0ImszYzBPb8JPIfbXD.jpg', 'Crime', 15, 154, 'John Travolta, Uma Thurman, Samuel L. Jackson', 'Livene for to lejemordere, en bokser, en gangsters kone og et par middagsbanditter sammenvæves i fire fortællinger om vold og forløsning.'),
('Forrest Gump', 'https://image.tmdb.org/t/p/w500/arw2vcBveWOVZr6pxd9XTd1TdQa.jpg', 'Drama', 7, 142, 'Tom Hanks, Robin Wright', 'Forrest Gump går gennem tiår af amerikansk historie og er vidne til og påvirker nogle af de mest ikoniske begivenheder.'),
('The Matrix', 'https://image.tmdb.org/t/p/w500/f89U3ADr1oiB1s9GkdPOEpXUk5H.jpg', 'Sci-Fi', 11, 136, 'Keanu Reeves, Laurence Fishburne', 'En computerhacker lærer om den sande natur af hans virkelighed og hans rolle i krigen mod dets kontrollører.'),
('Toy Story', 'https://image.tmdb.org/t/p/w500/uXDfjJbdP4ijW5hWSBrPrlKpxab.jpg', 'Animation', 0, 81, 'Tom Hanks, Tim Allen', 'Legetøj i et barns værelse lever hemmeligt som mennesker. Cowboy-dukken Woody føler sig truet når en rumranger ankommer.'),
('Spider-Man: No Way Home', 'https://image.tmdb.org/t/p/w500/1g0dhYtq4irTY1GPXvft6k4YLjm.jpg', 'Action', 11, 148, 'Tom Holland, Zendaya', 'Peter Parker bliver afsløret og kan ikke længere adskille sit normale liv fra super-heltens store indsats.'),
('Frozen', 'https://image.tmdb.org/t/p/w500/kgwjIb2JDHRhNk13lmSxiClFjc4.jpg', 'Animation', 0, 102, 'Kristen Bell, Idina Menzel', 'Frygtløs optimist Anna sætter ud sammen med den barske bjergmand Kristoff for at finde sin søster Elsa.');

-- =====================================================================
-- Insert Employees (with bcrypt hashed passwords: "password123")
-- =====================================================================
INSERT INTO employees (name, username, password_hash, role) VALUES
('Admin User', 'admin', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', 'admin'),
('Mette Nielsen', 'mette', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', 'ticket_sales'),
('Peter Hansen', 'peter', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', 'operator'),
('Line Andersen', 'line', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', 'inspector_cleaner'),
('Kasper Jensen', 'kasper', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', 'ticket_sales');

-- =====================================================================
-- Insert Shows (scheduled for today and near future)
-- =====================================================================
INSERT INTO shows (movie_id, theater_id, status, show_datetime, price, operator_id, inspector_id) VALUES
-- Today's shows
(1, 1, 'scheduled', DATE_ADD(CURDATE(), INTERVAL 18 HOUR), 95.00, 3, 4),
(3, 2, 'scheduled', DATE_ADD(CURDATE(), INTERVAL 19 HOUR), 110.00, 3, 4),
(8, 1, 'scheduled', DATE_ADD(CURDATE(), INTERVAL 14 HOUR), 75.00, 3, 4),

-- Tomorrow's shows
(2, 1, 'scheduled', DATE_ADD(DATE_ADD(CURDATE(), INTERVAL 1 DAY), INTERVAL 18 HOUR), 95.00, NULL, NULL),
(4, 2, 'scheduled', DATE_ADD(DATE_ADD(CURDATE(), INTERVAL 1 DAY), INTERVAL 20 HOUR), 105.00, NULL, NULL),
(10, 1, 'scheduled', DATE_ADD(DATE_ADD(CURDATE(), INTERVAL 1 DAY), INTERVAL 15 HOUR), 75.00, NULL, NULL),

-- Day after tomorrow
(5, 2, 'scheduled', DATE_ADD(DATE_ADD(CURDATE(), INTERVAL 2 DAY), INTERVAL 19 HOUR), 95.00, NULL, NULL),
(7, 1, 'scheduled', DATE_ADD(DATE_ADD(CURDATE(), INTERVAL 2 DAY), INTERVAL 21 HOUR), 105.00, NULL, NULL),
(9, 2, 'scheduled', DATE_ADD(DATE_ADD(CURDATE(), INTERVAL 2 DAY), INTERVAL 17 HOUR), 110.00, NULL, NULL),

-- 3 days from now
(6, 1, 'scheduled', DATE_ADD(DATE_ADD(CURDATE(), INTERVAL 3 DAY), INTERVAL 16 HOUR), 90.00, NULL, NULL),
(3, 2, 'scheduled', DATE_ADD(DATE_ADD(CURDATE(), INTERVAL 3 DAY), INTERVAL 20 HOUR), 110.00, NULL, NULL);

-- =====================================================================
-- Insert Bookings with Seats
-- =====================================================================
-- Booking 1: Today's Shawshank (show_id=1), 2 seats
INSERT INTO bookings (show_id, customer_name, customer_phone, total_amount, sold_by_id)
VALUES (1, 'Anders Thomsen', '20304050', 190.00, 2);

INSERT INTO booking_seats (booking_id, seat_id)
SELECT 1, seat_id FROM seats WHERE theater_id = 1 AND `row_number` = 5 AND seat_number IN (5, 6);

-- Booking 2: Today's Dark Knight (show_id=2), 4 seats
INSERT INTO bookings (show_id, customer_name, customer_phone, total_amount, sold_by_id)
VALUES (2, 'Maria Petersen', '30405060', 440.00, 5);

INSERT INTO booking_seats (booking_id, seat_id)
SELECT 2, seat_id FROM seats WHERE theater_id = 2 AND `row_number` = 10 AND seat_number BETWEEN 7 AND 10;

-- Booking 3: Today's Toy Story (show_id=3), 3 seats
INSERT INTO bookings (show_id, customer_name, customer_phone, total_amount, sold_by_id)
VALUES (3, 'Lars Larsen', '40506070', 225.00, 2);

INSERT INTO booking_seats (booking_id, seat_id)
SELECT 3, seat_id FROM seats WHERE theater_id = 1 AND `row_number` = 8 AND seat_number IN (3, 4, 5);

-- Booking 4: Tomorrow's Godfather (show_id=4), 2 seats
INSERT INTO bookings (show_id, customer_name, customer_phone, total_amount, sold_by_id)
VALUES (4, 'Sofie Kristensen', '50607080', 190.00, 5);

INSERT INTO booking_seats (booking_id, seat_id)
SELECT 4, seat_id FROM seats WHERE theater_id = 1 AND `row_number` = 12 AND seat_number IN (6, 7);

-- =====================================================================
-- Insert Products
-- =====================================================================
INSERT INTO products (product_name, category, price) VALUES
('Stor Popcorn', 'popcorn', 45.00),
('Mellem Popcorn', 'popcorn', 35.00),
('Lille Popcorn', 'popcorn', 25.00),
('Coca Cola 0.5L', 'soda', 30.00),
('Pepsi 0.5L', 'soda', 30.00),
('Fanta 0.5L', 'soda', 30.00),
('Vand 0.5L', 'soda', 25.00),
('M&Ms', 'candy', 20.00),
('Snickers', 'candy', 20.00),
('Maltesers', 'candy', 25.00),
('Nachos med dip', 'other', 40.00),
('Hotdog', 'other', 35.00);

-- =====================================================================
-- Insert Product Sales (linked to bookings)
-- =====================================================================
-- Sales for booking 1
INSERT INTO product_sales (product_id, quantity, total_price, sold_by_id, booking_id) VALUES
(1, 1, 45.00, 2, 1),  -- Stor Popcorn
(4, 2, 60.00, 2, 1);  -- 2x Coca Cola

-- Sales for booking 2
INSERT INTO product_sales (product_id, quantity, total_price, sold_by_id, booking_id) VALUES
(2, 2, 70.00, 5, 2),  -- 2x Mellem Popcorn
(5, 4, 120.00, 5, 2), -- 4x Pepsi
(8, 2, 40.00, 5, 2);  -- 2x M&Ms

-- Sales for booking 3
INSERT INTO product_sales (product_id, quantity, total_price, sold_by_id, booking_id) VALUES
(3, 3, 75.00, 2, 3),  -- 3x Lille Popcorn
(6, 3, 90.00, 2, 3);  -- 3x Fanta

-- Standalone sale (no booking)
INSERT INTO product_sales (product_id, quantity, total_price, sold_by_id, booking_id) VALUES
(12, 2, 70.00, 5, NULL); -- 2x Hotdog
