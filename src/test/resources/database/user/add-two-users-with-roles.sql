INSERT INTO roles(id, name) VALUES (1, 'CUSTOMER');
INSERT INTO roles(id, name) VALUES (2, 'MANAGER');

INSERT INTO users(id, email, first_name, last_name, password, is_deleted)
VALUES (1, 'john@manager.com', 'John', 'Jackson', '$2a$10$2UWH5EMjHJGwl1JbzyXd1uG1OS7W1pmOhWQXcF9nFByYM7aGUhlS6', 0);
INSERT INTO users(id, email, first_name, last_name, password, is_deleted)
VALUES (2, 'paul@customer.com', 'Paul', 'Walker', '$2a$10$2UWH5EMjHJGwl1JbzyXd1uG1OS7W1pmOhWQXcF9nFByYM7aGUhlS6', 0);

INSERT INTO users_roles(user_id, role_id) VALUES (1, 2);
INSERT INTO users_roles(user_id, role_id) VALUES (2, 1);
