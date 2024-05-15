INSERT INTO roles(id, name) VALUES (1, 'CUSTOMER');
INSERT INTO roles(id, name) VALUES (2, 'MANAGER');

INSERT INTO users(id, email, first_name, last_name, password, is_deleted)
VALUES (1, 'john@manager.com', 'John', 'Jackson', '$2a$10$2UWH5EMjHJGwl1JbzyXd1uG1OS7W1pmOhWQXcF9nFByYM7aGUhlS6', 0);
INSERT INTO users(id, email, first_name, last_name, password, is_deleted)
VALUES (2, 'paul@customer.com', 'Paul', 'Walker', '$2a$10$2UWH5EMjHJGwl1JbzyXd1uG1OS7W1pmOhWQXcF9nFByYM7aGUhlS6', 0);

INSERT INTO users_roles(user_id, role_id) VALUES (1, 2);
INSERT INTO users_roles(user_id, role_id) VALUES (2, 1);

INSERT INTO cars(id, model, brand, type, inventory, daily_fee, is_deleted)
VALUES (10, 'Astra', 'Opel', 'HATCHBACK', 3, 40.99, 0);

INSERT INTO rentals(id, car_id, user_id, rental_date, return_date, actual_return_date)
VALUES (1, 10, 2, '2021-01-11', '2021-02-08', null);
INSERT INTO rentals(id, car_id, user_id, rental_date, return_date, actual_return_date)
VALUES (2, 10, 1, '2024-01-06', '2024-01-09', null);

INSERT INTO payments(id, status, type, rental_id, amount_to_pay, session_id, session_url)
VALUES (1, 'PENDING', 'PAYMENT', 1, 100, 'sessionId', 'sessionUrl');

INSERT INTO payments(id, status, type, rental_id, amount_to_pay, session_id, session_url)
VALUES (2, 'PENDING', 'PAYMENT', 2, 100, 'sessionId2', 'sessionUrl2');
