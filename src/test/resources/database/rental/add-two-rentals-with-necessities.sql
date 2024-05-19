INSERT INTO cars(id, model, brand, type, inventory, daily_fee, is_deleted)
VALUES (1, 'Astra', 'Opel', 'HATCHBACK', 3, 40.99, 0);

INSERT INTO rentals(id, car_id, user_id, rental_date, return_date, actual_return_date)
VALUES (1, 1, 2, '2021-01-11', '2021-02-08', null);
INSERT INTO rentals(id, car_id, user_id, rental_date, return_date, actual_return_date)
VALUES (2, 1, 1, '2024-01-06', '2024-01-09', null);